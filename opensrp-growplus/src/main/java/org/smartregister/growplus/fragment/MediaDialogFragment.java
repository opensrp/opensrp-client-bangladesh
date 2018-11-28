package org.smartregister.growplus.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import org.smartregister.growplus.R;
import org.smartregister.growplus.activity.BaseRegisterActivity;
import org.smartregister.growplus.activity.ChildSmartRegisterActivity;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Jason Rogena - jrogena@ona.io on 14/03/2017.
 */

@SuppressLint("ValidFragment")
public class MediaDialogFragment extends DialogFragment implements View.OnClickListener {
    private final Activity parentActivity;
    String mediatype;
    String medialink;

    private MediaDialogFragment(Activity parentActivity, String mediatype, String medialink) {
        this.mediatype = mediatype;
        this.medialink = medialink;
        this.parentActivity = parentActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }

    public static MediaDialogFragment launchDialog(Activity activity,
                                                   String dialogTag, String mediatype, String medialink) {
        MediaDialogFragment dialogFragment = new MediaDialogFragment(activity,mediatype,medialink);
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        Fragment prev = activity.getFragmentManager().findFragmentByTag(dialogTag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        dialogFragment.show(ft, dialogTag);

        return dialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.media_dialog, container, false);
        if(mediatype.equalsIgnoreCase("image")){
            dialogView.findViewById(R.id.media_image).setVisibility(View.VISIBLE);
            dialogView.findViewById(R.id.media_video).setVisibility(View.GONE);
            ((ImageView)dialogView.findViewById(R.id.media_image)).setImageBitmap(getBitmapFromAssets("media/"+medialink));
        }else if (mediatype.equalsIgnoreCase("video")){
            dialogView.findViewById(R.id.media_video).setVisibility(View.VISIBLE);
            dialogView.findViewById(R.id.media_image).setVisibility(View.GONE);
            VideoView videoView = ((VideoView)dialogView.findViewById(R.id.media_video));
            playvideo(videoView,medialink);
        }
        return dialogView;
    }

    private void playvideo(VideoView videoView, String medialink) {
        MediaController mediaController = new MediaController(getActivity());
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        AssetFileDescriptor afd;
        try {

//            Uri uri = Uri.parse("file:///android_asset/media/"+medialink);
            videoView.setVideoPath("file:///android_asset/media/"+medialink);
            videoView.start();


        } catch (Exception e) { e.printStackTrace();}
    }

    public Bitmap getBitmapFromAssets(String fileName) {
        AssetManager assetManager = getActivity().getAssets();

        InputStream istr = null;
        try {
            istr = assetManager.open(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        try {
            istr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    @Override
    public void onClick(View v) {

    }
}
