package com.valyakinaleksey.flipexample.animation;

import android.view.animation.Animation;

import com.valyakinaleksey.flipexample.interfaces.FlipInteractor;
import com.valyakinaleksey.flipexample.interfaces.IFlipView;
import com.valyakinaleksey.flipexample.interfaces.OnFlipFinishedListener;

public class FlipInteractorImp implements FlipInteractor {
    private IFlipView flipViewOne;
    private IFlipView flipViewTwo;
    private boolean animationStarted;
    private FlipAnimationManager flipAnimationManager;
    private OnFlipFinishedListener onFlipFinishedListener;

    public FlipInteractorImp(OnFlipFinishedListener onFlipFinishedListener) {
        this.onFlipFinishedListener = onFlipFinishedListener;
        flipAnimationManager = new FlipAnimationManager();
    }

    private void clear() {
        flipViewOne = null;
        flipViewTwo = null;
    }

    @Override
    public void checkFlip(IFlipView flipView) {
        if (flipViewOne != null && flipViewTwo != null || !flipView.isForward()) return;
        if (flipViewOne == null) {
            flipViewOne = flipView;
            flipAnimationManager.startAnimation(flipView);
        } else {
            flipViewTwo = flipView;
            animationStarted = true;
            flipAnimationManager.startAnimation(flipView, this);
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (animationStarted) {
            animationStarted = false;
            if (flipViewOne.equals(flipViewTwo)) {
                //Одинаковые картинки
                clear();
                onFlipFinishedListener.onSuccess();
            } else {
                flipAnimationManager.startAnimation(flipViewOne);
                flipAnimationManager.startAnimation(flipViewTwo, this);
                onFlipFinishedListener.onMiss();
            }
        } else {
            //Обратный поворот завершен, очистка ссылок
            clear();
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
