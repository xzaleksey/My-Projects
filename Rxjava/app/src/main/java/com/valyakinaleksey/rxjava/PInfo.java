package com.valyakinaleksey.rxjava;

import android.graphics.drawable.Drawable;
import android.util.Log;

import static com.valyakinaleksey.rxjava.Constants.LOG_TAG;

public class PInfo {
    public String appname = "";
    public String pname = "";
    public String versionName = "";
    public int versionCode = 0;
    public Drawable icon;

    public void prettyPrint() {
        Log.d(LOG_TAG, appname + "\t" + pname + "\t" + versionName + "\t" + versionCode);
    }
}