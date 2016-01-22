package com.valyakinaleksey.flipexample.custom_views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.valyakinaleksey.flipexample.R;
import com.valyakinaleksey.flipexample.interfaces.IFlipView;

public class FlipView extends ImageView implements IFlipView {
    private boolean forward = true;
    private int frontSrc;
    private int backSrc;

    public FlipView(Context context, int frontSrc, int backSrc) {
        super(context);
        this.frontSrc = frontSrc;
        this.backSrc = backSrc;
    }

    public FlipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.FlipView, 0, 0);
        frontSrc = a.getResourceId(R.styleable.FlipView_frontSrc,
                0);
        backSrc = a.getResourceId(R.styleable.FlipView_backSrc, 0);
        a.recycle();
        if (frontSrc == 0 || backSrc == 0) {
            throw new RuntimeException("FlipView: Please set attributes frontSrc and BackSrc in xml");
        }
        setImageResource(frontSrc);
    }


    @Override
    public void changeToFront() {
        setImageResource(frontSrc);
        Log.d("FlipView", this.toString() + "front " + frontSrc);
    }

    @Override
    public void changeToBack() {
        setImageResource(backSrc);
        Log.d("FlipView", this.toString() + "back " + backSrc);
    }

    @Override
    public boolean isForward() {
        return forward;
    }

    @Override
    public void setForward(boolean forward) {
        this.forward = forward;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlipView)) return false;

        FlipView view = (FlipView) o;

        if (frontSrc != view.frontSrc) return false;
        return backSrc == view.backSrc;
    }

    @Override
    public int hashCode() {
        int result = frontSrc;
        result = 31 * result + backSrc;
        return result;
    }
}
