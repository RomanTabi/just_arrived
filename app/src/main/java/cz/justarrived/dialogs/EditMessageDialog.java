package cz.justarrived.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

import cz.justarrived.Constants;
import cz.justarrived.R;

public class EditMessageDialog extends DialogFragment {

  private EditMessageDialogCallback mCallback;

  private Context mContext;
  private AlertDialog mDialog;

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    mContext = getActivity();

    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
    builder.setTitle(R.string.enter_sms_text);

    try {
      mCallback = (EditMessageDialogCallback) getTargetFragment();
    } catch (ClassCastException e) {
      throw new ClassCastException(getTargetFragment().toString()
          + " must implement EditMessageDialogCallback");
    }

    LayoutInflater inflater = getActivity().getLayoutInflater();
    LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_edit_message, null);
    final EditText editText = (EditText) layout.findViewById(R.id.message);
    editText.setText(this.getArguments().getString(Constants.LATEST_TEXT_KEY));

    builder.setView(layout)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            String message = editText.getText().toString();

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
            editor.putString(Constants.LATEST_TEXT_KEY, message);
            editor.apply();

            mCallback.OnEditMessageDialogOk(message);
          }
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            // User cancelled the dialog
            EditMessageDialog.this.getDialog().cancel();
          }
        });
    // Create the AlertDialog object and return it
    return builder.create();
  }

  public interface EditMessageDialogCallback {
    void OnEditMessageDialogOk(String message);
  }
}
