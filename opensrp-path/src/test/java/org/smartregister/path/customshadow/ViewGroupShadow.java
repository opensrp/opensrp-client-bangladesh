package org.smartregister.path.customshadow;

import android.content.Context;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowLinearLayout;
import org.robolectric.shadows.ShadowViewGroup;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.immunization.view.VaccineGroup;

import static org.robolectric.shadow.api.Shadow.directlyOn;

/**
 * Created by kaderchowdhury on 06/12/17.
 */
@Implements(LinearLayout.class)
public class ViewGroupShadow extends ShadowLinearLayout {

//    public void __constructor__(Context context) {
//    }
//
//    public void __constructor__(Context context, @Nullable AttributeSet attrs) {
//    }
//
//    public void __constructor__(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
//    }
//
//    public void __constructor__(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//    }
//
//    @Implementation
//    public void addView(View v){
//
//    }
//
//    @Implementation
//    public void addView(ViewGroup v){
//
//    }

    @Override
    public void addView(final View child, final int index, final ViewGroup.LayoutParams params) {
        if(child instanceof VaccineGroup){

        }else{
            super.addView(child,index,params);
        }
    }

}
