package com.lglab.ivan.lgxeducontroller.games.trivia.adapters;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

//Replace RelativeLayout with any layout of your choice
public class DynamicSquareFrameLayout extends FrameLayout {

    public DynamicSquareFrameLayout(Context context) {
        super(context);
    }


    public DynamicSquareFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DynamicSquareFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


// here we are returning the width in place of height, so width = height
// you may modify further to create any proportion you like ie. height = 2*width etc

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(size, size);
    }


}
