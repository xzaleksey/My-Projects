package com.valyakinaleksey.followplan.followplan2.followplan.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.melnykov.fab.FloatingActionButton;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.Iconics;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.valyakinaleksey.followplan.followplan2.followplan.DatabaseHelper;
import com.valyakinaleksey.followplan.followplan2.followplan.R;
import com.valyakinaleksey.followplan.followplan2.followplan.dialogs.PlansDialogFragment;
import com.valyakinaleksey.followplan.followplan2.followplan.fragments.PlanFragment;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Period;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Plan;
import com.valyakinaleksey.followplan.followplan2.followplan.main_classes.Task;
import net.danlew.android.joda.JodaTimeAndroid;


public class MainActivity extends AppCompatActivity {
    public static final int PLANS_ID = 4;
    private static final String CURRENT_FRAGMENT_POSITION = "position";
    public static final int SETTINGS_ID = 7;
    public static final int ALL_TASKS_ID = 1;
    public static final int TODAY_ID = 3;
    public static final int FILTERS_ID = 5;

    private Toolbar mainToolBar;
    private int currentFragmentPosition = 1;
    private FragmentManager fragmentManager;
    private Drawer drawer;
    private InputMethodManager imm;
    private SecondaryDrawerItem drawerItemPlans;
    private TextView mainToolbarTitle;
    private FloatingActionButton floatingActionButton;
    private Fragment currentFragment;
    private ListView listViewMain;

    public ListView getListViewMain() {
        return listViewMain;
    }

    public void setListViewMain(ListView listViewMain) {
        this.listViewMain = listViewMain;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initActivity();
        if (savedInstanceState == null) {
            createFragment(getString(R.string.all_tasks));
        } else {
            currentFragmentPosition = savedInstanceState.getInt(CURRENT_FRAGMENT_POSITION);
        }
        initDrawer(savedInstanceState);
//        NotificationScheduler.scheduleNotification(this, NotificationScheduler.getNotification(this, "Test"), 5000);
//        startService(new Intent(this, MyService.class));
    }


    public void updateProjectsCount() {
        drawerItemPlans.withBadge("" + Plan.getPlans().size());
        drawer.updateItem(drawerItemPlans);
    }

    private void initActivity() {
        JodaTimeAndroid.init(this);
        if (Plan.getPlans().isEmpty()) {
            initFromDb();
        }
        initMainToolBar();
        Iconics.init(getApplicationContext());
        Iconics.registerFont(new FontAwesome());
        fragmentManager = getSupportFragmentManager();
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mainToolbarTitle = ((TextView) mainToolBar.findViewById(R.id.title));
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
    }

    private void initFromDb() {
        initPlans();
        initPeriods();
        initTasks();
    }

    private void initTasks() {
        Task.initTasks(new DatabaseHelper(getBaseContext()));
        Period.fillTasks();
        Plan.fillPeriods();
        Plan.fillTasks();
    }

    private void initPlans() {
        Plan.initPlans(new DatabaseHelper(getBaseContext()));
    }

    private void initPeriods() {
        Period.initPeriods(new DatabaseHelper(getBaseContext()));
    }

    private void initDrawer(final Bundle savedInstanceState) {
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem().withName("Aleksey Valyakin").withEmail("xzaleksey@gmail.com").withIcon(FontAwesome.Icon.faw_user)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();
        drawerItemPlans = new SecondaryDrawerItem().withName(R.string.plans).withIcon(FontAwesome.Icon.faw_calendar_check_o).withBadge(""
                + Plan.getPlans().size()).withSelectable(false).withIdentifier(PLANS_ID);
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mainToolBar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.all_tasks).withIcon(FontAwesome.Icon.faw_home).withIdentifier(ALL_TASKS_ID),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.today).withIcon(FontAwesome.Icon.faw_calendar_o).withIdentifier(TODAY_ID),
                        drawerItemPlans,
                        new SecondaryDrawerItem().withName(R.string.filters).withIcon(FontAwesome.Icon.faw_filter).withIdentifier(FILTERS_ID),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.settings).withIcon(FontAwesome.Icon.faw_gear).withSelectable(false)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        switch (position) {
                            case ALL_TASKS_ID:
                                createFragment(getString(R.string.all_tasks));
                                break;
                            case TODAY_ID:
                                createFragment(getString(R.string.today));
                                break;
                            case PLANS_ID:
                                PlansDialogFragment.show(MainActivity.this);
                                break;
                            case FILTERS_ID:
                                break;
                            case SETTINGS_ID:
                                startActivity(new Intent(MainActivity.this, MyPreferenceActivity.class));
                                break;
                        }
                        if (position != PLANS_ID && position != SETTINGS_ID) {
                            currentFragmentPosition = position;
                        }
                        return false;
                    }
                })
                .build();
        drawer.setSelection(currentFragmentPosition,false);
    }

    public void createFragment(Fragment fragment, String fragmentName) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fragment_container, fragment, fragmentName);
//        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        currentFragment = fragment;
        mainToolbarTitle.setText(fragmentName);
    }

    public void createFragment(String fragmentName) {
        Fragment fragment = fragmentManager.findFragmentByTag(fragmentName);
        if (fragment == null) {
            if (fragmentName.equals(getString(R.string.all_tasks))) {
                fragment = PlanFragment.newInstance(PlanFragment.TASKS_ALL);
            } else if (fragmentName.equals(getString(R.string.today))) {
                fragment = PlanFragment.newInstance(PlanFragment.TASKS_TODAY);
            } else if (fragmentName.equals(getString(R.string.settings))) {
//                fragment = new  MyPreferenceActivity();
            }
        }
        createFragment(fragment, fragmentName);
        mainToolbarTitle.setText(fragmentName);
    }

    private void initMainToolBar() {
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setTitle(null);
        }
        mainToolBar = toolbar;
        final SearchView searchView = (SearchView) mainToolBar.findViewById(R.id.sv_search_tasks);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    mainToolBar.findViewById(R.id.title).setVisibility(View.GONE);
                } else {
                    mainToolBar.findViewById(R.id.title).setVisibility(View.VISIBLE);
                    searchView.onActionViewCollapsed();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_notifications) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateFragment() {
        if (currentFragment instanceof PlanFragment && currentFragment.isVisible()) {
            final Plan plan = ((PlanFragment) currentFragment).getPlan();
            if (plan != null) {
                if (!Plan.getPlans().containsKey(plan.getId())) {
                    createFragment(getString(R.string.all_tasks));
                }
            }
        }
    }

    public void updateListViewMain() {
        ((BaseAdapter) listViewMain.getAdapter()).notifyDataSetChanged();
    }

    public FloatingActionButton getFloatingActionButton() {
        return floatingActionButton;
    }

    public InputMethodManager getImm() {
        return imm;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_FRAGMENT_POSITION, currentFragmentPosition);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean notif = sp.getBoolean("notification_switch", false);
        String address = sp.getString("address", "");
        String text = "Notifications are "
                + ((notif) ? "enabled, address = " + address : "disabled");
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}

