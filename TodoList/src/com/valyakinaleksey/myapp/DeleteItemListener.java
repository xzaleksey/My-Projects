package com.valyakinaleksey.myapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.widget.BaseAdapter;

import java.lang.ref.WeakReference;

public  class DeleteItemListener implements DialogInterface.OnClickListener {
    WeakReference<MyActivity> myActivityWeakReference;

    public DeleteItemListener(MyActivity myActivity) {
        myActivityWeakReference = new WeakReference<MyActivity>(myActivity);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                MyActivity myActivity = myActivityWeakReference.get();
                myActivity.getTasks().remove(myActivity.getCurrentPosition());
                ((BaseAdapter) myActivity.getLvMain().getAdapter()).notifyDataSetChanged();
                break;
            case Dialog.BUTTON_NEGATIVE:
                break;
        }
    }
}
