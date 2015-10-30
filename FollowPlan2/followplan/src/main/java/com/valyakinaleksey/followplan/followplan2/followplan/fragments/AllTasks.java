package com.valyakinaleksey.followplan.followplan2.followplan.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.valyakinaleksey.followplan.followplan2.followplan.DatabaseHelper;
import com.valyakinaleksey.followplan.followplan2.followplan.R;
import com.valyakinaleksey.followplan.followplan2.followplan.task.Plan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AllTasks extends android.support.v4.app.ListFragment {
    private String mText;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mText = getTag();
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // This is called to define the layout for the fragment;
//        // we just create a TextView and set its text to be the fragment tag
//        View view = inflater.inflate(R.layout.listview,
//                container, false);
//        DateTime dateTime = new DateTime();
//        ArrayList<Task> tasks = new ArrayList<>();
//        for (int i = 0; i < 100; i++) {
//            tasks.add(new Task("task" + i, dateTime.plusDays(i)));
//        }
//        ((ListView) view).setAdapter(new TasksArrayAdapter(getActivity(), tasks));
//        return view;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.listview_tasks, null);
        Spinner spinner = (Spinner) viewGroup.findViewById(R.id.spinner_period);
        List<String> strings = new ArrayList<>(Arrays.asList(getActivity().getResources().getStringArray(R.array.periods_base)));
        strings.add("Создать новый");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(), R.layout.spinner_textview, strings);
//        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
//                R.array.action_list, R.layout.spinner_textview);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {

                String[] choose = getResources().getStringArray(R.array.periods_base);
//                Toast toast = Toast.makeText(getApplicationContext(),
//                        "Ваш выбор: " + choose[selectedItemPosition], Toast.LENGTH_SHORT);
//                toast.show();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return viewGroup;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        DateTime dateTime = new DateTime();
//        ArrayList<Task> tasks = new ArrayList<>();
//        for (int i = 0; i < 100; i++) {
//            tasks.add(new Task(i,"task" + i, dateTime.plusDays(i)));
//        }
//        setListAdapter(new TasksArrayAdapter(getActivity(), tasks));
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        Cursor cursor = databaseHelper.getAllPlans();
        String[] from = new String[]{Plan.NAME};
        int[] to = new int[]{R.id.tv_task_name};

        // Теперь создадим адаптер массива и установим его для отображения наших
        // данных
        SimpleCursorAdapter notes = new SimpleCursorAdapter(getActivity(),
                R.layout.task, cursor, from, to);
        setListAdapter(notes);
        databaseHelper.close();
    }
}
