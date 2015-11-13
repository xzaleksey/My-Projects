package com.valyakinaleksey.snake;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.valyakinaleksey.snake.game.Direction;

import java.lang.ref.WeakReference;

class MyHandler extends Handler {
    private final WeakReference<MainActivity> mActivity;

    public MyHandler(MainActivity mActivity) {
        this.mActivity = new WeakReference<>(mActivity);
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Bundle bundle = msg.getData();
        Direction direction = (Direction) bundle.getSerializable("Direction");
        MainActivity mainActivity = mActivity.get();
        if (MainActivity.game.move(direction)) {
            mainActivity.currentDirection = direction;
            mainActivity.updateField();
        } else {
            MainActivity.game.setGameLost(true);
            mainActivity.gameRestartDialogShow();
        }
    }
}