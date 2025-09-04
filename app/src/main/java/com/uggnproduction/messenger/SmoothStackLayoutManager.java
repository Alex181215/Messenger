package com.uggnproduction.messenger;

import android.content.Context;
import android.util.DisplayMetrics;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

public class SmoothStackLayoutManager extends LinearLayoutManager {

    private static final float MILLISECONDS_PER_INCH = 50f; // регулировка скорости

    public SmoothStackLayoutManager(Context context) {
        super(context);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position){
        LinearSmoothScroller scroller = new LinearSmoothScroller(recyclerView.getContext()){
            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics){
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }
        };
        scroller.setTargetPosition(position);
        startSmoothScroll(scroller);
    }

}
