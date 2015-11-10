package com.valyakinaleksey.followplan.followplan2.followplan;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
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
import com.valyakinaleksey.followplan.followplan2.followplan.dialogs.PlansDialogFragment;
import com.valyakinaleksey.followplan.followplan2.followplan.fragments.AllTasks;
import com.valyakinaleksey.followplan.followplan2.followplan.fragments.PlanFragment;
import com.valyakinaleksey.followplan.followplan2.followplan.task.Period;
import com.valyakinaleksey.followplan.followplan2.followplan.task.Plan;
import com.valyakinaleksey.followplan.followplan2.followplan.task.Task;
import net.danlew.android.joda.JodaTimeAndroid;


public class MainActivity extends AppCompatActivity {
    public static final int PROJECT_POSITION = 4;

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
            createFragment(new AllTasks(), getString(R.string.all_tasks));
        }

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
        initDrawer();
        Iconics.init(getApplicationContext());
        Iconics.registerFont(new FontAwesome());
        fragmentManager = getSupportFragmentManager();
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mainToolbarTitle = ((TextView) mainToolBar.findViewById(R.id.title));
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);

//

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

    private void initDrawer() {
        DatabaseHelper databaseHelper = new DatabaseHelper(getBaseContext());
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
        drawerItemPlans = new SecondaryDrawerItem().withName(R.string.plans).withIcon(FontAwesome.Icon.faw_calendar_check_o).withBadge("" + Plan.getPlans().size());
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mainToolBar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.all_tasks).withIcon(FontAwesome.Icon.faw_home),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.today).withIcon(FontAwesome.Icon.faw_calendar_o),
                        drawerItemPlans,
                        new SecondaryDrawerItem().withName(R.string.filters).withIcon(FontAwesome.Icon.faw_filter),
                        new SecondaryDrawerItem().withName(R.string.settings).withIcon(FontAwesome.Icon.faw_gear)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (currentFragmentPosition != position || position == PROJECT_POSITION) {
                            currentFragmentPosition = position;
                            switch (position) {
                                case 1:
                                    createFragment(getString(R.string.all_tasks));
                                    break;
                                case 2:
                                    break;
                                case 3:
                                    break;
                                case PROJECT_POSITION:
                                    PlansDialogFragment.show(MainActivity.this);
                                    break;
                            }
                        }
                        return false;

                    }
                })
                .build();
        databaseHelper.close();
    }

    private String getProjectsCount(DatabaseHelper databaseHelper) {
        return String.valueOf(databaseHelper.getProjectsCount());
    }

    public void createFragment(Fragment fragment, String fragmentName) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fragment_container, fragment, fragmentName);
//        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        currentFragment = fragment;
//        if (currentFragment instanceof PlanFragment){
//            final View currentFragmentView = currentFragment.getView();
//            if (currentFragmentView != null) {
//                final PlanFragment planFragment= (PlanFragment) currentFragment;
//                currentFragmentView.setFocusableInTouchMode(true);
//                currentFragmentView.requestFocus();
//                currentFragmentView.setOnKeyListener(new View.OnKeyListener() {
//                    @Override
//                    public boolean onKey(View view, int i, KeyEvent keyEvent) {
//                        if (i == KeyEvent.KEYCODE_BACK) {
//                            if (planFragment.isToolBarShown()) {
//                                planFragment.hideToolbar();
//                            }
//                            return true;
//                        }
//                        return false;
//                    }
//                });
//            }
//
//        }
        mainToolbarTitle.setText(fragmentName);
    }

    public void createFragment(String fragmentName) {
        Fragment fragment = fragmentManager.findFragmentByTag(fragmentName);
        if (fragment == null) {
            if (fragmentName.equals(getString(R.string.all_tasks))) {
                fragment = new AllTasks();
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
        //noinspection SimplifiableIfStatement
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
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // do something on back pressed.
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this,"Back",Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }
}
