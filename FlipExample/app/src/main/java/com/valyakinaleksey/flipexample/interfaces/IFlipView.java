package com.valyakinaleksey.flipexample.interfaces;

public interface IFlipView {
    void changeToFront();

    void changeToBack();

    boolean isForward();

    void setForward(boolean forward);
}
