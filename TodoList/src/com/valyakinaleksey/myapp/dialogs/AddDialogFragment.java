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

public class AddDialogFragment extends DialogFragment implements View.OnClickListener {
    private EditText etView;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View viewGroup = getActivity().getLayoutInflater().inflate(R.layout.task_details, null);
        etView = (EditText) viewGroup.findViewById(R.id.et_description);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.description_title)
                .setView(viewGroup).create();
        final View btnConfirm = viewGroup.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(this);
        viewGroup.findViewById(R.id.btn_cancel).setOnClickListener(this);
        viewGroup.findViewById(R.id.btn_choose_date).setOnClickListener(this);
        btnConfirm.setEnabled(false);
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

        return alertDialog;
    }



    @Override
    public void onClick(View v) {
        MyActivity myActivity = (MyActivity) getActivity();
        switch (v.getId()) {
            case R.id.btn_choose_date:
                Bundle b = new Bundle();
                DateTime dateTime = new DateTime();
                b.putInt(DatePickerDialogFragment.YEAR, dateTime.getYear());
                b.putInt(DatePickerDialogFragment.MONTH, dateTime.getMonthOfYear());
                b.putInt(DatePickerDialogFragment.DATE, dateTime.getDayOfMonth());
                DialogFragment picker = new DatePickerDialogFragment();
                picker.setArguments(b);
                picker.show(getFragmentManager(), "DateTimePickerFragment");
                break;
            case R.id.btn_confirm:
                Task newTask = new Task(etView.getText().toString(), myActivity.getChosenDate());
                myActivity.getTasks().add(newTask);
                ((BaseAdapter) myActivity.getLvMain().getAdapter()).notifyDataSetChanged();
                this.dismiss();
                break;
            case R.id.btn_cancel:
                this.dismiss();
                break;
        }
    }
}
