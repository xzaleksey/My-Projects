package com.valyakinaleksey.retrofitexample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.SimpleXmlConverterFactory;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements Callback<Curator> {
    private static final String API_URL = "http://freemusicarchive.org/api";
    private static final String API_KEY = "6T1LX3ZZM0QXPC1A";
    private static final String TAG = "RetroFit";
    private TextView textView;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textView = (TextView) getView().findViewById(R.id.textView);
        Retrofit restAdapter = new Retrofit.Builder().baseUrl(API_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();
        IApiMethods service = restAdapter.create(IApiMethods.class);
        Call<Curator> call = service.getCurators(API_KEY);
        call.enqueue(this);
    }


    @Override
    public void onResponse(Response<Curator> response, Retrofit retrofit) {
        Log.d(TAG, "success");
        Curator curators = response.body();
        textView.setText(curators.title + "\n\n");
        for (Curator.Dataset dataset : curators.dataset) {
            textView.setText(textView.getText() + dataset.curator_title +
                    " - " + dataset.curator_tagline + "\n");
        }
    }

    @Override
    public void onFailure(Throwable t) {
        Log.d(TAG, t.getCause().toString());
        Log.d(TAG, t.toString());
    }
}
