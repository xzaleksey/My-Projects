package com.valyakinaleksey.flipexample.animation;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.valyakinaleksey.flipexample.interfaces.IFlipView;

public class FlipAnimationUpdated extends Animation {
    private Camera camera;
    private IFlipView flipView;

    private float centerX;
    private float centerY;

    public FlipAnimationUpdated() {
        setDuration(700);
        setFillAfter(false);
        setInterpolator(new AccelerateDecelerateInterpolator());
    }


    public void reverse() {
        flipView.setForward(!flipView.isForward());
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        centerX = width / 2;
        centerY = height / 2;
        camera = new Camera();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        // Angle around the y-axis of the rotation at the given time
        // calculated both in radians and degrees.
        final double radians = Math.PI * interpolatedTime;
        float degrees = (float) (180.0 * radians / Math.PI);

        // Once we reach the midpoint in the animation, we need to hide the
        // source view and show the destination view. We also need to change
        // the angle by 180 degrees so that the destination does not come in
        // flipped around
        if (interpolatedTime >= 0.5f) {
            degrees -= 180.f;
            if (flipView.isForward()) {
                flipView.changeToFront();
            } else {
                flipView.changeToBack();
            }
        }

        if (flipView.isForward()) {
            degrees = -degrees; //determines direction of rotation when flip begins
        }

        final Matrix matrix = t.getMatrix();
        camera.save();
        camera.rotateY(degrees);
        camera.getMatrix(matrix);
        camera.restore();
        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
    }

    public IFlipView getFlipView() {
        return flipView;
    }

    public void setFlipView(IFlipView flipView) {
        this.flipView = flipView;
    }
}
