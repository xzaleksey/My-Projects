package com.valyakinaleksey.rxjava;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import rx.Observable;
import rx.functions.Action1;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public static final String LOG_TAG = "MyTag";

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        myObservable.subscribe(onNextAction);
        Observable.just("Hello, world!")
                .map(s -> s + " -Dan")
                .map(String::hashCode)
                .map(i -> Integer.toString(i))
                .subscribe(s -> Log.d(LOG_TAG, s));
    }

    Observable<String> myObservable = Observable.just("Hello, world!");
    Action1<String> onNextAction = new Action1<String>() {
        @Override
        public void call(String s) {
            Log.d(LOG_TAG, s);
        }
    };


}