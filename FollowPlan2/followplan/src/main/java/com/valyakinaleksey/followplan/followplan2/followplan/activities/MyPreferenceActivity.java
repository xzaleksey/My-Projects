package com.valyakinaleksey.followplan.followplan2.followplan.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.valyakinaleksey.followplan.followplan2.followplan.MyService;
import com.valyakinaleksey.followplan.followplan2.followplan.R;
import com.valyakinaleksey.followplan.followplan2.followplan.preferences.TimePreference;

import java.util.List;
import java.util.Map;

public class MyPreferenceActivity extends PreferenceActivity {
    public static final String PREF_FRAGMENT_NAME = "com.valyakinaleksey.followplan.followplan2.followplan.activities.MyPreferenceActivity$MyPrefsFragment";
    private TextView mToolBarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        Resources resources = getResources();
        View content = root.getChildAt(0);
        LinearLayout toolbarContainer = (LinearLayout) View.inflate(this, R.layout.activity_prefs, null);
        root.removeAllViews();
        toolbarContainer.addView(content);
        root.addView(toolbarContainer);
        Toolbar mToolBar = (Toolbar) toolbarContainer.findViewById(R.id.tool_bar);
        mToolBarTitle = ((TextView) mToolBar.findViewById(R.id.title));
        mToolBarTitle.setText(R.string.settings);
        Drawable upArrow = resources.getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        if (upArrow != null) {
            upArrow.setColorFilter(resources.getColor(R.color.md_white_1000), PorterDuff.Mode.SRC_IN);
            mToolBar.setNavigationIcon(upArrow);
        }
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return fragmentName.equals(PREF_FRAGMENT_NAME);
    }

    private void setmToolBarTitle(String title) {
        mToolBarTitle.setText(title);
    }

    public static class MyPrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            String category = getArguments().getString(getString(R.string.settings_category));
            MyPreferenceActivity myPreferenceActivity = (MyPreferenceActivity) getActivity();
            if (category != null) {
                if (category.equals(getString(R.string.notifications))) {
                    addPreferencesFromResource(R.xml.notifications);
                    myPreferenceActivity.setmToolBarTitle(getString(R.string.notifications));
                }
            }
            SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
            for (Map.Entry<String, ?> entry : sp.getAll().entrySet()) {
                onSharedPreferenceChanged(sp, entry.getKey());
            }
        }

        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            Preference pref = findPreference(s);
            if (pref instanceof EditTextPreference) {
                EditTextPreference etp = (EditTextPreference) pref;
                pref.setSummary(etp.getText());
            } else if (pref instanceof TimePreference) {
                pref.setSummary(sharedPreferences.getString(s, TimePreference.DEFAULT_NOTIFICATION_VALUE));
            } else if (pref instanceof SwitchPreference) {
                if (!((SwitchPreference) pref).isChecked()) {
                    Intent intent = new Intent(getActivity(), MyService.class);
                    intent.putExtra(MyService.TYPE, MyService.ACTION_CANCEL_NOTIFICATIONS);
                    getActivity().startService(intent);
                }
            }
        }
    }
}
