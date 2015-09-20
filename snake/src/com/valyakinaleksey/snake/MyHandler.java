package com.valyakinaleksey.snake;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.valyakinaleksey.snake.game.Direction;

import java.lang.ref.WeakReference;

class MyHandler extends Handler {
    private final WeakReference<MyActivity> mActivity;

    public MyHandler(MyActivity mActivity) {
        this.mActivity = new WeakReference<>(mActivity);
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Bundle bundle = msg.getData();
        Direction direction = (Direction) bundle.getSerializable("Direction");
        MyActivity myActivity = mActivity.get();
        if (MyActivity.game.move(direction)) {
            myActivity.currentDirection = direction;
            myActivity.updateField();
        } else {
            myActivity.btnPlay.setBackground(myActivity.getResources().getDrawable(R.drawable.ic_action_playback_play));
            MyActivity.game.setGameLost(true);
            myActivity.gameRestartDialogShow();
        }
    }
}