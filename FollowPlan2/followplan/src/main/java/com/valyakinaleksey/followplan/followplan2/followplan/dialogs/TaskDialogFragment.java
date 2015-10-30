package com.valyakinaleksey.followplan.followplan2.followplan.dialogs;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import com.avast.android.dialogs.core.BaseDialogFragment;
import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.valyakinaleksey.followplan.followplan2.followplan.DatabaseHelper;
import com.valyakinaleksey.followplan.followplan2.followplan.MainActivity;
import com.valyakinaleksey.followplan.followplan2.followplan.R;
import com.valyakinaleksey.followplan.followplan2.followplan.task.Period;
import com.valyakinaleksey.followplan.followplan2.followplan.task.Plan;
import com.valyakinaleksey.followplan.followplan2.followplan.task.Task;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class TaskDialogFragment extends SimpleDialogFragment {
    public static final String TAG = "TaskDialogFragment";
    public static final String PLAN_ID = "planId";
    private Plan plan;

    public static void show(FragmentActivity activity, Plan plan) {
        TaskDialogFragment taskDialogFragment = new TaskDialogFragment();
        taskDialogFragment.plan = plan;
        taskDialogFragment.show(activity.getSupportFragmentManager(), TAG);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            long id = savedInstanceState.getLong(PLAN_ID, 0);
            if (id != 0) {
                plan = Plan.getPlans().get(id);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (plan != null) {
            outState.putLong(PLAN_ID, plan.getId());
        }
        super.onSaveInstanceState(outState);
    }

    private void initSpinner(Spinner spinner) {
        List<String> periods = new ArrayList<>();
        for (Period period : plan.getPlanPeriods().values()) {
            periods.add(period.toString());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(), R.layout.spinner_textview, periods);
        spinner.setAdapter(adapter);
    }

    @Override
    public BaseDialogFragment.Builder build(final BaseDialogFragment.Builder builder) {
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.activity_task, null);
        final Spinner spinner = (Spinner) viewGroup.findViewById(R.id.spinner_period);
        final EditText etTaskName = (EditText) viewGroup.findViewById(R.id.et_task_name);
        final boolean disposable = ((CheckBox) viewGroup.findViewById(R.id.cb_disposable)).isChecked();
        initSpinner(spinner);


        builder.setTitle(R.string.create_task)
                .setView(viewGroup)
                .setPositiveButton(R.string.create_task, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String taskName = etTaskName.getText().toString();
                        if (!taskName.equals("")) {
                            DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
                            int orderNum = databaseHelper.getTaskMaxOrder() + 1;
                            DateTime dateCreated = new DateTime();
                            final Period period = plan.getPeriodByIndex(spinner.getSelectedItemPosition());
                            long id = databaseHelper.createTask(taskName, dateCreated, null, orderNum, plan.getId(), period.getId(), disposable ? 1 : 0);
                            Task task = new Task(id, taskName, dateCreated, null, orderNum, plan, period, false, disposable);
                            plan.getTasks().put(id, task);
                            period.getTasks().put(id, task);

                            final MainActivity activity = (MainActivity) getActivity();
                            activity.updateListViewMain();
                            dismiss();
                        }
                    }
                }).setNegativeButton(R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        }).setNeutralButton(R.string.edit, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        return builder;
    }

}
