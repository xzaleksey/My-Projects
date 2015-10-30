package com.valyakinaleksey.myapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.valyakinaleksey.myapp.dialogs.AddDialogFragment;
import com.valyakinaleksey.myapp.dialogs.DeleteItemDialogFragment;
import com.valyakinaleksey.myapp.dialogs.EditDialogFragment;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class MyActivity extends Activity implements DatePickerDialog.OnDateSetListener {
    private List<Task> tasks = new ArrayList<Task>();
    private ListView lvMain;
    private int currentPosition;
    private DateTime chosenDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initFields();
        initTasks();
        fillList();
    }

    private void initFields() {
        lvMain = (ListView) findViewById(R.id.lvMain);
        Button btnAdd = (Button) findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });
        findViewById(R.id.btn_scroll_top).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("My_Logs","test");
                lvMain.setSelectionAfterHeaderView();
            }
        });
    }

    private void showAddDialog() {
        AddDialogFragment addFragment = new AddDialogFragment();
        addFragment.show(getFragmentManager(), "AddFragmentDialog");
    }

    private void fillList() {
        lvMain.setAdapter(new TasksAdapter(this, tasks));
        registerForContextMenu(lvMain);
    }

    private void initTasks() {
        DateTime dateTime = new DateTime();
        for (int i = 0; i < 100; i++) {
            tasks.add(new Task("task" + i, dateTime.plusDays(i)));
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        DateTime dateTime = new DateTime();
        chosenDate = dateTime.withDate(year, monthOfYear + 1, dayOfMonth);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.lvMain) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            currentPosition = info.position;
            menu.setHeaderTitle(getString(R.string.choose_action));
            getMenuInflater().inflate(R.menu.context_menu, menu);
            if (tasks.get(currentPosition).isChecked()) {
                menu.findItem(R.id.done).setTitle("Отменить");
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                new DeleteItemDialogFragment().show(getFragmentManager(), "DeleteItemDialogFragment");
                break;
            case R.id.edit:
                new EditDialogFragment().show(getFragmentManager(), "EditItemDialogFragment");
                break;
            case R.id.done:
                Task task = tasks.get(currentPosition);
                task.setChecked(!task.isChecked());
                ((BaseAdapter) getLvMain().getAdapter()).notifyDataSetChanged();
                break;
        }
        return true;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public ListView getLvMain() {
        return lvMain;
    }

    public DateTime getChosenDate() {
        if (chosenDate == null) {
            return new DateTime();
        } else {
            try {
                return chosenDate;
            } finally {
                chosenDate = null;
            }
        }

    }

    public void setChosenDate(DateTime chosenDate) {
        this.chosenDate = chosenDate;
    }
}
