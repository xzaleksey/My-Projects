package com.valyakinaleksey.retrofitexample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }
//    private class BackgroundTask extends AsyncTask<Void, Void,
//                    Curator> {
//        RestAdapter restAdapter;
//
//        @Override
//        protected void onPreExecute() {
//            restAdapter = new RestAdapter.Builder()
//                    .setEndpoint(API_URL)
//                    .build();
//        }
//
//        @Override
//        protected Curator doInBackground(Void... params) {
//            IApiMethods methods = restAdapter.create(IApiMethods.class);
//            Curator curators = methods.getCurators(API_KEY);
//
//            return curators;
//        }
//
//        @Override
//        protected void onPostExecute(Curator curators) {
//            textView.setText(curators.title + "\n\n");
//            for (Curator.Dataset dataset : curators.dataset) {
//                textView.setText(textView.getText() + dataset.curator_title +
//                        " - " + dataset.curator_tagline + "\n");
//            }
//        }
//    }
}
