package com.valyakinaleksey.followplan.followplan2.followplan.presentation.activities;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.valyakinaleksey.followplan.followplan2.followplan.R;
import com.valyakinaleksey.followplan.followplan2.followplan.domain.adapters.TasksArrayAdapter;
import com.valyakinaleksey.followplan.followplan2.followplan.presentation.fragments.PlanFragment;
import com.valyakinaleksey.followplan.followplan2.followplan.util.Constants;

public class SearchableActivity extends AppCompatActivity {
    private Toolbar mainToolBar;
    private PlanFragment fragment;
    private TasksArrayAdapter tasksArrayAdapter;
    private SearchView searchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Constants.LOG_TAG, "SearchActivity onCreate");
        setContentView(R.layout.activity_search);
        handleIntent(getIntent());
        initFragment();
        initToolBar();
    }

    private void initFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        fragment = PlanFragment.newInstance(PlanFragment.TASKS_ALL);
        ft.replace(R.id.fragment_container, fragment, getString(R.string.all_tasks));
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void initToolBar() {
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mainToolBar = toolbar;
        mainToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        initSearchView();
    }

    private void initSearchView() {
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) mainToolBar.findViewById(R.id.search);
        final SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        Log.d(Constants.LOG_TAG, searchableInfo == null ? "null" : searchableInfo.toString());
        searchView.setSearchableInfo(
                searchableInfo);
        searchView.setSubmitButtonEnabled(true);
        searchView.setIconifiedByDefault(true);
        searchView.setIconified(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                fragment.unSelectItem();
                if (TextUtils.isEmpty(newText)) {
                    tasksArrayAdapter.getFilter().filter(newText);
                } else {
                    tasksArrayAdapter.getFilter().filter(newText);
                }
                return true;
            }
        });
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
//            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
        }
    }

    public void setTasksArrayAdapter(TasksArrayAdapter tasksArrayAdapter) {
        this.tasksArrayAdapter = tasksArrayAdapter;
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        searchView.requestFocus();
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
    }
}
