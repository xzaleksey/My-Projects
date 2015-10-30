package com.valyakinaleksey.myapp.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import com.valyakinaleksey.myapp.DeleteItemListener;
import com.valyakinaleksey.myapp.MyActivity;
import com.valyakinaleksey.myapp.R;

public class DeleteItemDialogFragment extends DialogFragment  {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DialogInterface.OnClickListener onClickListener= new DeleteItemListener((MyActivity) getActivity());
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity())
                .setTitle(getActivity().getString(R.string.delete_question));
        alertDialogBuilder.setPositiveButton(getActivity().getString(R.string.delete), onClickListener)
                .setNegativeButton(R.string.cancel, onClickListener);
        return alertDialogBuilder.create();
    }


}
