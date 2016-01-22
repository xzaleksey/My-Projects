package com.valyakinaleksey.flipexample.animation;

import android.util.Log;

import com.valyakinaleksey.flipexample.Constants;
import com.valyakinaleksey.flipexample.interfaces.FlipInteractor;
import com.valyakinaleksey.flipexample.interfaces.IFlipView;
import com.valyakinaleksey.flipexample.interfaces.IGamePresenter;
import com.valyakinaleksey.flipexample.interfaces.IGameView;
import com.valyakinaleksey.flipexample.interfaces.OnFlipFinishedListener;

public class GamePresenter implements OnFlipFinishedListener, IGamePresenter {
    private IGameView gameView;
    private FlipInteractor flipInteractor;

    public GamePresenter(IGameView gameView) {
        this.gameView = gameView;
        flipInteractor = new FlipInteractorImp(this);
    }

    @Override
    public void checkFlip(IFlipView flipView) {
        flipInteractor.checkFlip(flipView);
    }

    @Override
    public void onSuccess() {
        gameView.showLuckyGuess();
    }

    @Override
    public void onMiss() {
        Log.d(Constants.TAG, "Missed");
    }
}
