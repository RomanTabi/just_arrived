package cz.justarrived.models;

import android.content.Context;
import android.graphics.Color;
import android.preference.Preference;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import cz.justarrived.R;

public class CustomColorPreference extends Preference {

  private Context mContext;

  public CustomColorPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    mContext = context;
  }

  public CustomColorPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mContext = context;
  }

  public CustomColorPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
  }

  public CustomColorPreference(Context context) {
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
