package com.lglab.ivan.lgxeducontroller.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;

import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Field;

import github.chenupt.springindicator.viewpager.FixedSpeedScroller;

public class CustomScrollerViewPager extends ViewPager {

    private static final String TAG = CustomScrollerViewPager.class.getSimpleName();

    private int duration = 1000;

    public CustomScrollerViewPager(Context context) {
        super(context);
    }

    public CustomScrollerViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void fixScrollSpeed(){
        fixScrollSpeed(duration);
    }

    public void fixScrollSpeed(int duration){
        this.duration = duration;
        setScrollSpeedUsingRefection(duration);
    }


    private void setScrollSpeedUsingRefection(int duration) {
        try {
            Field localField = ViewPager.class.getDeclaredField("mScroller");
            localField.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(getContext(), new DecelerateInterpolator(1.5F));
            scroller.setDuration(duration);
            localField.set(this, scroller);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException ignored) {
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;

        /*try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "onInterceptTouchEvent in IllegalArgumentException");
            return false;
        }*/
    }
}
