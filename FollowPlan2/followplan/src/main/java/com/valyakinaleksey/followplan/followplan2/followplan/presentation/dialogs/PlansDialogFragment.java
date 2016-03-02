package com.valyakinaleksey.followplan.followplan2.followplan.presentation.dialogs;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.avast.android.dialogs.core.BaseDialogFragment;
import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.avast.android.dialogs.iface.ISimpleDialogListener;
import com.valyakinaleksey.followplan.followplan2.followplan.R;
import com.valyakinaleksey.followplan.followplan2.followplan.data.DatabaseHelper;
import com.valyakinaleksey.followplan.followplan2.followplan.domain.adapters.PlanArrayAdapter;
import com.valyakinaleksey.followplan.followplan2.followplan.presentation.activities.MainActivity;
import com.valyakinaleksey.followplan.followplan2.followplan.presentation.activities.PlanActivity;
import com.valyakinaleksey.followplan.followplan2.followplan.presentation.fragments.PlanFragment;
import com.valyakinaleksey.followplan.followplan2.followplan.presentation.model.Plan;

import java.util.ArrayList;

import static com.valyakinaleksey.followplan.followplan2.followplan.util.Constants.REQUEST_CODE_CREATE;
import static com.valyakinaleksey.followplan.followplan2.followplan.util.Constants.REQUEST_CODE_EDIT_DELETE;
import static com.valyakinaleksey.followplan.followplan2.followplan.util.Constants.RESULT_CANCELED;
import static com.valyakinaleksey.followplan.followplan2.followplan.util.Constants.RESULT_CREATE;
import static com.valyakinaleksey.followplan.followplan2.followplan.util.Constants.RESULT_DELETE;

public class PlansDialogFragment extends SimpleDialogFragment implements ISimpleDialogListener {

    public static final String EDITABLE = "Editable";
    private static final int REQUEST_CODE_REFRESH = 10;

    public static String TAG = "PlansDialogFragment";
    private PlanArrayAdapter planArrayAdapter;
    private Plan currentPlan;
    private ListView listViewProjects;

    private boolean customizePlansVisible = false;
    private boolean editable;
    private Button ib_customize;

    public static void show(FragmentActivity activity) {
        new PlansDialogFragment().show(activity.getSupportFragmentManager(), TAG);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            editable = savedInstanceState.getBoolean(EDITABLE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(EDITABLE, editable);
        super.onSaveInstanceState(outState);
    }

    @Override
    public BaseDialogFragment.Builder build(final BaseDialogFragment.Builder builder) {
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.dialog_plan, null);
        initFields(viewGroup);
        listViewProjects.setAdapter(planArrayAdapter);
        if (planArrayAdapter.getCount() != 0) {
            customizePlansVisible = true;
            ib_customize.setVisibility(View.VISIBLE);
        }
        initListeners();
        return builder.setTitle(R.string.plans)
                .setView(viewGroup)
                .setPositiveButton(R.string.create_plan, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "Успех", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getContext(), PlanActivity.class);
                        startActivityForResult(intent, REQUEST_CODE_CREATE);
//                for (IPositiveButtonDialogListener listener : getPositiveButtonDialogListeners()) {
//                    listener.onPositiveButtonClicked(mRequestCode);
//                }
                    }
                }).setNegativeButton(R.string.cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();
                    }
                });
    }

    private void initFields(ViewGroup viewGroup) {
        listViewProjects = (ListView) viewGroup.findViewById(R.id.plan_listview);
        ib_customize = (Button) viewGroup.findViewById(R.id.iv_customize_plans);
        ib_customize.setText(getResources().getString(R.string.customize_plans) + " {faw-gear}");
        planArrayAdapter = new PlanArrayAdapter(getContext(), R.layout.spinner_textview, new ArrayList<>(Plan.getPlans().values()), this);
        planArrayAdapter.setEditable(editable);
    }

    private void initListeners() {
        listViewProjects.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!planArrayAdapter.isEditable()) {
                    getDialog().dismiss();
                    Plan plan = (Plan) listViewProjects.getAdapter().getItem(i);
                    ((MainActivity) getActivity()).createFragment(PlanFragment.newInstance(plan.getId()), plan.getName());
                }
            }
        });

        ib_customize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editable = !editable;
                planArrayAdapter.setEditable(editable);
                planArrayAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == REQUEST_CODE_CREATE) {
                if (resultCode == RESULT_CREATE) {
                    planArrayAdapter.add(Plan.getLastPlan());
                    if (!customizePlansVisible) {
                        ib_customize.setVisibility(View.VISIBLE);
                        customizePlansVisible = true;
                    }
                }
            }
            if (requestCode == REQUEST_CODE_EDIT_DELETE) {
                if (resultCode == RESULT_DELETE) {
                    if (currentPlan != null) {
                        planArrayAdapter.remove(Plan.getLastPlan());
                        Plan.deletePlan(currentPlan.getId(), DatabaseHelper.getInstance(getContext()));
                        Toast.makeText(getContext(), getResources().getString(R.string.plan_delete_success), Toast.LENGTH_SHORT).show();
                        if (planArrayAdapter.getCount() == 0) {
                            customizePlansVisible = false;
                            ib_customize.setVisibility(View.GONE);
                        }
                    }
                }
            }
            updateMainActivity();
            notifyListViewAdapter();
        }
    }

    @Override
    public void onPositiveButtonClicked(int i) {
        if (currentPlan != null) {
            if (i == REQUEST_CODE_EDIT_DELETE) {
                onActivityResult(REQUEST_CODE_EDIT_DELETE, RESULT_DELETE, null);
            } else if (i == REQUEST_CODE_REFRESH) {
                currentPlan.setTotalTasksCount(currentPlan.getCurrentTasksCount());
                currentPlan.setTotalDoneTasksCount(currentPlan.getCurrentDoneTasksCount());
                ContentValues contentValues = new ContentValues();
                contentValues.put(Plan.TOTAL_TASKS_COUNT, currentPlan.getTotalTasksCount());
                contentValues.put(Plan.TOTAL_DONE_TASKS_COUNT, currentPlan.getTotalDoneTasksCount());
                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getContext());
                databaseHelper.updatePlan(currentPlan.getId(), contentValues);
                notifyListViewAdapter();
            }
        }
    }

    @Override
    public void onNegativeButtonClicked(int i) {
    }

    @Override
    public void onNeutralButtonClicked(int i) {

    }

    private void updateMainActivity() {
        final MainActivity activity = (MainActivity) getActivity();
        activity.updateProjectsCount();
        activity.updateFragment();
    }

    private void notifyListViewAdapter() {
        planArrayAdapter.notifyDataSetChanged();
    }


    public void resetConfirmDialogShow(Plan plan) {
        currentPlan = plan;
        SimpleDialogFragment.createBuilder(getContext(), getActivity().getSupportFragmentManager())
                .setTitle(R.string.refresh_plan_question)
                .setPositiveButtonText(R.string.refresh)
                .setNegativeButtonText(R.string.cancel).show().setTargetFragment(this, REQUEST_CODE_REFRESH);
    }

    public void deleteConfirmDialogShow(Plan plan) {
        currentPlan = plan;
        SimpleDialogFragment.createBuilder(getContext(), getActivity().getSupportFragmentManager())
                .setTitle(R.string.delete_plan_question)
                .setPositiveButtonText(R.string.delete)
                .setNegativeButtonText(R.string.cancel).show().setTargetFragment(this, REQUEST_CODE_EDIT_DELETE);
    }

    public void editPlan(Plan plan) {
        currentPlan = plan;
        if (plan != null) {
            Intent intent = new Intent(getContext(), PlanActivity.class);
            intent.putExtra("listPlanId", plan.getId());
            startActivityForResult(intent, REQUEST_CODE_EDIT_DELETE);
        }
    }
}