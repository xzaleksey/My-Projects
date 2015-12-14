package com.valyakinaleksey.rxjava;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import rx.Observable;
import rx.functions.Action1;

import static com.valyakinaleksey.rxjava.Consts.LOG_TAG;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {


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
//        checkRx();
        byte b = 2;
        byte c = 3;
        byte d = (byte) (b * c);
        Log.d(LOG_TAG, "" + d);
//        startApp();
    }

    private void checkRx() {
        myObservable.subscribe(onNextAction);
        Observable.just("Hello, world!")
                .map(s -> s + " -Dan")
                .map(String::hashCode)
                .map(i -> Integer.toString(i))
                .subscribe(s -> Log.d(LOG_TAG, s));
    }

    private void startApp() {
        final PackageManager pm = getContext().getPackageManager();
        List<PInfo> pInfos = MyPackageManager.getInstalledApps(getContext(), false);
        for (PInfo pInfo : pInfos) {
            pInfo.prettyPrint();
            Intent intent = pm.getLaunchIntentForPackage(pInfo.pname);
            try {
                startActivity(intent);
                break;
            } catch (Exception e) {
                Log.d(LOG_TAG, "null");
            }
        }
    }

    Observable<String> myObservable = Observable.just("Hello, world!");
    Action1<String> onNextAction = new Action1<String>() {
        @Override
        public void call(String s) {
            Log.d(LOG_TAG, s);
        }
    };


}