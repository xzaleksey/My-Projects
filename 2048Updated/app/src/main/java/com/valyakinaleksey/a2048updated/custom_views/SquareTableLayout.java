package com.valyakinaleksey.a2048updated.custom_views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TableLayout;

public class SquareTableLayout extends TableLayout {


    public SquareTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, h, oldw, oldh);
    }
}
