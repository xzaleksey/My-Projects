package com.valyakinaleksey.myapp.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.EditText;
import com.valyakinaleksey.myapp.MyActivity;
import com.valyakinaleksey.myapp.R;
import com.valyakinaleksey.myapp.Task;
import org.joda.time.DateTime;

public class EditDialogFragment extends DialogFragment implements View.OnClickListener {
    private EditText etView;
    private AlertDialog alertDialog;
    private Task task;
    private DateTime dateTime;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View viewGroup = getActivity().getLayoutInflater().inflate(R.layout.task_details, null);
        etView = (EditText) viewGroup.findViewById(R.id.et_description);
        MyActivity myActivity = (MyActivity) getActivity();
        task = myActivity.getTasks().get(myActivity.getCurrentPosition());
        alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.change_task)
                .setView(viewGroup).create();
        initListeners(viewGroup);
        initDialog(myActivity);
        return alertDialog;
    }

    private void initDialog(MyActivity myActivity) {
        etView.setText(task.getDescription());
        etView.setSelection(etView.getText().length());
        dateTime = task.getDate();
        myActivity.setChosenDate(dateTime);
    }

    private void initListeners(View viewGroup) {
        final View btnConfirm = viewGroup.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(this);
        viewGroup.findViewById(R.id.btn_cancel).setOnClickListener(this);
        viewGroup.findViewById(R.id.btn_choose_date).setOnClickListener(this);
        etView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    btnConfirm.setEnabled(true);
                } else {
                    btnConfirm.setEnabled(false);
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        MyActivity myActivity = (MyActivity) getActivity();
        switch (v.getId()) {
            case R.id.btn_choose_date:
                Bundle b = new Bundle();
                b.putInt(DatePickerDialogFragment.YEAR, dateTime.getYear());
                b.putInt(DatePickerDialogFragment.MONTH, dateTime.getMonthOfYear());
                b.putInt(DatePickerDialogFragment.DATE, dateTime.getDayOfMonth());
                DialogFragment picker = new DatePickerDialogFragment();
                picker.setArguments(b);
                picker.show(getFragmentManager(), "DateTimePickerFragment");
                break;
            case R.id.btn_confirm:
                task.setDescription(etView.getText().toString());
                task.setDate(myActivity.getChosenDate());
                ((BaseAdapter) myActivity.getLvMain().getAdapter()).notifyDataSetChanged();
                this.dismiss();
                break;
            case R.id.btn_cancel:
                this.dismiss();
                break;
        }
    }
}
