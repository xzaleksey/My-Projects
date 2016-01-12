package com.valyakinaleksey.parseexample;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Message")
public class Message extends ParseObject {
    public String getUserId() {
        return getString("userId");
    }

    public void setUserId(String userId) {
        put("userId", userId);
    }

    public String getBody() {
        return getString("body");
    }

    public void setBody(String body) {
        put("body", body);
    }
}
