package com.valyakinaleksey.followplan.followplan2.followplan.presentation.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.avast.android.dialogs.iface.ISimpleDialogListener;
import com.valyakinaleksey.followplan.followplan2.followplan.R;
import com.valyakinaleksey.followplan.followplan2.followplan.data.DatabaseHelper;
import com.valyakinaleksey.followplan.followplan2.followplan.domain.adapters.ColorArrayAdapter;
import com.valyakinaleksey.followplan.followplan2.followplan.presentation.model.Period;
import com.valyakinaleksey.followplan.followplan2.followplan.presentation.model.Plan;
import com.valyakinaleksey.followplan.followplan2.followplan.util.ColorsUtils;

import static com.valyakinaleksey.followplan.followplan2.followplan.util.Constants.RESULT_CREATE;
import static com.valyakinaleksey.followplan.followplan2.followplan.util.Constants.RESULT_DELETE;
import static com.valyakinaleksey.followplan.followplan2.followplan.util.Constants.RESULT_EDIT;

public class PlanActivity extends AppCompatActivity implements ISimpleDialogListener {
    public static final int CREATE_PLAN = -1;
    private Plan currentPlan;
    private int color;
    private AppCompatSpinner colorPicker;
    private int[] intColors;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);
        Intent intent = getIntent();
        long planId = intent.getLongExtra("listPlanId", CREATE_PLAN);
        if (planId != CREATE_PLAN) {
            currentPlan = Plan.getPlans().get(planId);
            EditText etProjectName = (EditText) findViewById(R.id.et_project_name);
            etProjectName.setText(currentPlan.getName());
            color = currentPlan.getColor();
        } else {
            color = getResources().getColor(R.color.md_grey_500);
        }
        intColors = ColorsUtils.getPickerColors(getBaseContext());
        ColorArrayAdapter colorArrayAdapter = new ColorArrayAdapter(this, intColors);
        colorPicker = (AppCompatSpinner) findViewById(R.id.color_picker);
        colorPicker.setAdapter(colorArrayAdapter);
        for (int i = 0; i < intColors.length; i++) {
            if (color == intColors[i]) {
                colorPicker.setSelection(i);
                break;
            }
        }
        initToolbar();
    }

    private void initToolbar() {
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.tool_bar);
        ((TextView) findViewById(R.id.title)).setText(currentPlan == null ? R.string.create_plan : R.string.edit_plan);
//        IconicsImageView iconicsImageView = (IconicsImageView) findViewById(R.id.tool_bar_iv_send);
//        iconicsImageView.setIcon(new IconicsDrawable(this, FontAwesome.Icon.faw_send_o).sizeDp(24));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        final EditText etProjectName = (EditText) findViewById(R.id.et_project_name);
        findViewById(R.id.tool_bar_iv_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String planName = etProjectName.getText().toString();
                color = intColors[colorPicker.getSelectedItemPosition()];

                if (!planName.equals("")) {
                    DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
                    if (currentPlan == null) {
                        int orderNum = databaseHelper.getPlanMaxOrder() + 1;
                        long id = databaseHelper.createPlan(planName, orderNum, color);
                        Plan plan = new Plan(id, planName, orderNum, color);
                        Plan.addPlan(plan);
                        Period.createBasePeriods(getBaseContext(), plan);
                        setResult(RESULT_CREATE);
                    } else {
                        currentPlan.setName(planName);
                        currentPlan.setColor(color);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(Plan.NAME, planName);
                        contentValues.put(Plan.COLOR, color);
                        databaseHelper.updatePlan(currentPlan.getId(), contentValues);
                        setResult(RESULT_EDIT);
                    }
                    databaseHelper.close();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.input_plan_name, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (currentPlan != null) {
            menu.add(R.string.delete);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals(getResources().getString(R.string.delete))) {
            if (currentPlan != null) {
                SimpleDialogFragment.createBuilder(getBaseContext(), getSupportFragmentManager())
                        .setTitle(R.string.delete_plan_question)
                        .setPositiveButtonText(R.string.delete)
                        .setNegativeButtonText(R.string.cancel).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNegativeButtonClicked(int i) {

    }

    @Override
    public void onNeutralButtonClicked(int i) {

    }

    @Override
    public void onPositiveButtonClicked(int i) {
        setResult(RESULT_DELETE);
        finish();
    }
}
