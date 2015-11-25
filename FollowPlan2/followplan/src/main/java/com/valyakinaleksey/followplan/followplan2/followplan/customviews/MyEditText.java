package com.valyakinaleksey.followplan.followplan2.followplan.customviews;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import com.melnykov.fab.FloatingActionButton;

public class MyEditText extends AppCompatEditText {
    private View container;
    private FloatingActionButton fab;

    public MyEditText(Context context) {
        super(context);
    }

    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ENTER) {
            // User has pressed Back key. So hide the keyboard
            InputMethodManager mgr = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(this.getWindowToken(), 0);
            if (container != null) {
                container.setVisibility(GONE);
                fab.show();
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            // Eat the event
            return true;
        }
        return false;
    }

    public void setContainer(View container, FloatingActionButton fab) {
        this.container = container;
        this.fab = fab;
    }
}
