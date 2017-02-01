package cz.justarrived.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import cz.justarrived.Constants;
import cz.justarrived.R;
import cz.justarrived.handlers.SharedPreferencesHandler;

/**
 * Created by roman on 1/16/17.
 */

public class SelectRadiusDialog extends DialogFragment {

  private SetRadiusDialogCallback mCallback;

  private Context mContext;
  private AlertDialog mDialog;

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    mContext = getActivity();

    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
    builder.setTitle(R.string.trigger_radius);

    try {
      mCallback = (SetRadiusDialogCallback) getActivity();
    } catch (ClassCastException e) {
      throw new ClassCastException(getTargetFragment().toString()
          + " must implement SetRadiusDialogCallback");
    }

    final String[] items = mContext.getResources().getStringArray(R.array.TriggerRadius);
    final int checkedItem = SharedPreferencesHandler.getSharedPreference(mContext, Constants.TRIGGER_RADIUS_KEY, 0);

    builder.setSingleChoiceItems(items, checkedItem,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            SharedPreferencesHandler.setSharedPreference(mContext, Constants.TRIGGER_RADIUS_KEY, which);
            mCallback.OnSetRadiusDialogResult(items[which]);

            mDialog.dismiss();
          }
        });

    String negativeText = mContext.getString(android.R.string.cancel);
    builder.setNegativeButton(negativeText,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            // negative button logic
            mDialog.dismiss();
          }
        });
    mDialog = builder.create();
    return mDialog;
  }

  public interface SetRadiusDialogCallback {
    void OnSetRadiusDialogResult(String result);
  }
}
