package cz.justarrived.models;

import android.content.Context;
import android.preference.SwitchPreference;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import cz.justarrived.R;

/**
 * Created by roman on 11/12/16.
 */

public class CustomColorSwitchPreference extends SwitchPreference {

  private Context mContext;

  public CustomColorSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    mContext = context;
  }

  public CustomColorSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mContext = context;
  }

  public CustomColorSwitchPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
  }

  public CustomColorSwitchPreference(Context context) {
    super(context);
    mContext = context;
  }

  @Override
  protected void onBindView(View view) {
    super.onBindView(view);
    TextView titleView = (TextView) view.findViewById(android.R.id.title);
    titleView.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryText));
    TextView textView = (TextView) view.findViewById(android.R.id.summary);
    textView.setTextColor(ContextCompat.getColor(mContext, R.color.colorSecondaryText));
  }
}
