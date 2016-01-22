package com.valyakinaleksey.flipexample.animation;

import android.view.View;
import android.view.animation.Animation;

import com.valyakinaleksey.flipexample.interfaces.IFlipView;

public class FlipAnimationManager {

    public boolean startAnimation(final IFlipView flipView) {
        return startAnimation(flipView, null);
    }

    private void flip(IFlipView flipView, FlipAnimationUpdated flipAnimation) {
        flipAnimation.setFlipView(flipView);
        flipAnimation.reverse();
        ((View) flipView).startAnimation(flipAnimation);
    }

    public boolean startAnimation(final IFlipView flipView, Animation.AnimationListener animationListener) {
        FlipAnimationUpdated animationUpdated = new FlipAnimationUpdated();
        animationUpdated.setAnimationListener(animationListener);
        flip(flipView, animationUpdated);
        return true;
    }
}
