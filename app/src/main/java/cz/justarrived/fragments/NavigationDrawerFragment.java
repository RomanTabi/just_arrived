package cz.justarrived.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import cz.justarrived.Constants;
import cz.justarrived.R;

public class NavigationDrawerFragment extends Fragment implements View.OnClickListener {

  private RelativeLayout mMyLocationLayoutItem;
  private RelativeLayout mSettingsLayoutItem;
  private MainFragment.OnRequestPermissionListener mPermissionCallback;

  @Override
  public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

    mPermissionCallback = (MainFragment.OnRequestPermissionListener) getActivity();

    mMyLocationLayoutItem = (RelativeLayout) rootView.findViewById(R.id.my_location_item);
    mMyLocationLayoutItem.setOnClickListener(this);

//    mSettingsLayoutItem = (RelativeLayout) rootView.findViewById(R.id.settings_item);
//    mSettingsLayoutItem.setOnClickListener(this);

    return rootView;
  }

  @Override
  public void onClick(View view) {
    if (view.equals(mMyLocationLayoutItem)) {
      if (!mPermissionCallback.onFragmentRequestPermission(Constants.REQUEST_ACCESS_FINE_LOCATION)) {
        return;
      }
      Intent intent = new Intent();
      intent.setClassName(getActivity(), "cz.justarrived.activities.MapActivity");
      intent.putExtra(Constants.MY_LOCATION, true);
      startActivity(intent);
    } else if (view.equals(mSettingsLayoutItem)) {
      Intent intent = new Intent();
      intent.setClassName(getActivity(), "cz.justarrived.activities.SettingsActivity");
      startActivity(intent);
    }
  }
}
