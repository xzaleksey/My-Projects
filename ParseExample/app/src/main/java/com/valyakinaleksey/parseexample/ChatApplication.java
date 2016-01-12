package com.valyakinaleksey.parseexample;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

public class ChatApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Register your parse models here
        ParseObject.registerSubclass(Message.class);
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "m7zsF1wMizh9J7JQLnMFFD2c0XkDlvmAatt1LHTG", "sVaKJrmtj3TazystZ9fXwZ5dBFkH7OzYf1CdqhUI");
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
