package org.smartregister.path.customshadow;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowLinearLayout;
import org.robolectric.shadows.ShadowViewGroup;

/**
 * Created by kaderchowdhury on 06/12/17.
 */
@Implements(LinearLayout.class)
public class ViewGroupShadow extends ShadowViewGroup {
    public void __constructor__(Context context) {
    }

    public void __constructor__(Context context, @Nullable AttributeSet attrs) {
    }

    public void __constructor__(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    }

    public void __constructor__(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    }

    public void addView(View v){

    }
    public void addView(ViewGroup v){

    }
}
