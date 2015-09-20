package com.valyakinaleksey.snake.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import com.valyakinaleksey.snake.R;
import com.valyakinaleksey.snake.game.Game;

public class EndGameDialog extends DialogFragment {

    private OnClickListener onClickListener;

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                .setTitle(getActivity().getString(R.string.restart_question)).setPositiveButton(R.string.restart, onClickListener)
                .setNegativeButton(R.string.cancel, onClickListener)
                .setMessage(getString(R.string.your_count)+" " + Game.getResult());
        return adb.create();
    }
}