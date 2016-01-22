package com.valyakinaleksey.flipexample.interfaces;

import android.view.animation.Animation;

public interface FlipInteractor extends Animation.AnimationListener {
    void checkFlip(IFlipView flipView);
}
