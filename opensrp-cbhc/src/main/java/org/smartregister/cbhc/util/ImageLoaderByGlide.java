package org.smartregister.cbhc.util;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;

import org.smartregister.cbhc.application.AncApplication;

public class ImageLoaderByGlide {
    public static void setImageAsTarget(final String url, final ImageView iv, final int defaultImg) {
        ViewTarget viewTarget = new ViewTarget<ImageView, GlideDrawable>(iv) {
            @Override
            public void onLoadStarted(Drawable placeholder) {
                if(defaultImg!=0)iv.setImageResource(defaultImg);
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                if(defaultImg!=0) iv.setImageResource(defaultImg);
            }

            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                iv.setImageDrawable(resource.getCurrent());
            }
        };
        Glide.with(AncApplication.getInstance().getApplicationContext()).load(url).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(viewTarget);
    }
}
