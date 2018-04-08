package com.mongoose.app.moodio.utils;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by vips on 7/17/15.
 */
public class ViewScrollingBehavior extends CoordinatorLayout.Behavior<View> {

    public ViewScrollingBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        android.support.design.widget.CoordinatorLayout.Behavior behavior = ((android.support.design.widget.CoordinatorLayout.LayoutParams)dependency.getLayoutParams()).getBehavior();
        if(behavior instanceof AppBarLayout.Behavior) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            int distanceToScroll = child.getHeight();
            float ratio = (float)dependency.getY()/48;
            child.setTranslationY(-distanceToScroll * ratio);
        }
        return super.onDependentViewChanged(parent, child, dependency);
    }
}
