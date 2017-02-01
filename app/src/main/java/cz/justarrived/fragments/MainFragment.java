package cz.justarrived.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import cz.justarrived.Constants;
import cz.justarrived.R;
import cz.justarrived.adapters.ContactsViewAdapter;
import cz.justarrived.dialogs.EditMessageDialog;
import cz.justarrived.dialogs.SelectRadiusDialog;
import cz.justarrived.handlers.ApplicationHandler;
import cz.justarrived.handlers.DBHandler;
import cz.justarrived.handlers.SharedPreferencesHandler;
import cz.justarrived.models.Contact;
import cz.justarrived.models.RecipientsTextView;

public class MainFragment extends Fragment implements OnMapReadyCallback,
    View.OnClickListener, EditMessageDialog.EditMessageDialogCallback,
    SharedPreferences.OnSharedPreferenceChangeListener, CompoundButton.OnCheckedChangeListener {

  private static final int REQUEST_CODE_PICK_CONTACT = 1;

  private RelativeLayout mContactItemLayout;
  private RelativeLayout mMessageItemLayout;
  private RelativeLayout mPlaceItemLayout;
  private RelativeLayout mMessageLabelLayout;
  private RelativeLayout mRecipientsLabelLayout;
  private Switch mSMSSwitch;
  private RecipientsTextView mContactField;
  private TextView mMessageField;
  private GoogleMap mGoogleMap;
  private OnShowMapListener mMapCallback;
  private MapView mMapPreviewField;
  private MapView mMapView;
  private OnRequestPermissionListener mPermissionCallback;
  private TextView mProgressLabel;
  private LinearLayout mProgressLayout;
  private SharedPreferences mSharedPreferences;
  private DBHandler mDBHandler;
  private boolean returnFromContactsActivity;
  private ApplicationHandler mApplication;
  private RecyclerView mRecyclerView;
  private RelativeLayout mAddContactButton;
  private ArrayList<Contact> contact_data;
  private ContactsViewAdapter mAdapter;
  private OnContactEventListener mContactsCallbacks;

  public MainFragment() {
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
    MapsInitializer.initialize(getActivity());

    Log.i("MainFragment", "" + getActivity());

    mApplication = ApplicationHandler.getInstance();

    mDBHandler = new DBHandler(getActivity().getApplicationContext());

    mMapCallback = (OnShowMapListener) getActivity();
    mPermissionCallback = (OnRequestPermissionListener) getActivity();

    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

    mMapView = (MapView) rootView.findViewById(R.id.location_preview);
		mMapView.onCreate(savedInstanceState);
		mMapView.getMapAsync(this);

    mAddContactButton = (RelativeLayout) rootView.findViewById(R.id.add_contact_layout);
    mAddContactButton.setOnClickListener(this);

    mContactItemLayout = (RelativeLayout) rootView.findViewById(R.id.add_contact_layout);
    mMessageItemLayout = (RelativeLayout) rootView.findViewById(R.id.message_layout);
    mPlaceItemLayout = (RelativeLayout) rootView.findViewById(R.id.place_layout);

    mMessageLabelLayout = (RelativeLayout) rootView.findViewById(R.id.message_label_layout);
    mRecipientsLabelLayout = (RelativeLayout) rootView.findViewById(R.id.recipients_label_layout);

    contact_data = mDBHandler.getContacts();

    mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycle_list);
    mAdapter = new ContactsViewAdapter(getActivity(), contact_data);
    mRecyclerView.setAdapter(mAdapter);
    mRecyclerView.setHasFixedSize(false);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(createHelperCallback());
    itemTouchHelper.attachToRecyclerView(mRecyclerView);

    mContactsCallbacks = (OnContactEventListener) getActivity();

//    mContactField = (RecipientsTextView) rootView.findViewById(R.id.contact);
    mMessageField = (TextView) rootView.findViewById(R.id.message);
    mSMSSwitch = (Switch) rootView.findViewById(R.id.sms_switch);

    mProgressLayout = (LinearLayout) rootView.findViewById(R.id.progress_layout);
    mProgressLabel = (TextView) rootView.findViewById(R.id.progress_layout_label);

    mContactItemLayout.setOnClickListener(this);
    mMessageItemLayout.setOnClickListener(this);
    mPlaceItemLayout.setOnClickListener(this);
    mSMSSwitch.setOnCheckedChangeListener(this);

    if (mApplication.getApplicationState(getActivity()).equals(ApplicationHandler.AppState.TRACKING)) {
      mSMSSwitch.setEnabled(false);
    } else {
      mSMSSwitch.setEnabled(true);
    }
    setupCurrentValues();

		return rootView;
	}

  private void setupCurrentValues() {
    String message = mSharedPreferences.getString(Constants.LATEST_TEXT_KEY, "");

    if (!message.isEmpty()) {
      mMessageField.setText(message);
    }

    if (mSharedPreferences.getBoolean(Constants.SERVICE_RUNNING_KEY, false)) {
      showProgressLayout();
    }

    boolean smsSwitch = mSharedPreferences.getBoolean(Constants.SMS_OR_NOTIFY_KEY, true);
    mSMSSwitch.setChecked(smsSwitch);
    showContactsAndMessage(smsSwitch);
  }

  @Override
	public void onMapReady(GoogleMap googleMap) {
    mGoogleMap = googleMap;
    /*
     * Disable all gestures on preview
     */
    mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
    mGoogleMap.getUiSettings().setAllGesturesEnabled(false);
    mGoogleMap.getUiSettings().setRotateGesturesEnabled(false);
    mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);
    mGoogleMap.getUiSettings().setZoomGesturesEnabled(false);
    mGoogleMap.getUiSettings().setMapToolbarEnabled(false);

//    mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//      @Override
//      public void onMapClick(LatLng latLng) {
//        if (!mPermissionCallback.onFragmentRequestPermission(Constants.REQUEST_ACCESS_FINE_LOCATION)) {
//          return;
//        }
//        mMapCallback.onShowMap();
//      }
//    });

    mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
      @Override
      public boolean onMarkerClick(Marker marker) {
        return true;
      }
    });

    setChosenLocationMarkerAndView();
  }

  private void setChosenLocationMarkerAndView() {
    String sLat = mSharedPreferences.getString(Constants.CHOSEN_LOCATION_CAMERA_LAT, "");
    String sLng = mSharedPreferences.getString(Constants.CHOSEN_LOCATION_CAMERA_LNG, "");

    if (sLat.isEmpty() || sLat.isEmpty()) {
      return;
    }

    LatLng latLng = new LatLng(Double.valueOf(sLat), Double.valueOf(sLng));
    float zoom = Constants.DEFAULT_CAMERA_ZOOM;
    float bearing = Constants.DEFAULT_CAMERA_BEARING;
    float tilt = Constants.DEFAULT_CAMERA_TILT;

    mGoogleMap.clear();
    mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(String.valueOf(R.string.destination)));
    mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
        latLng, zoom, tilt, bearing
    )));
  }

  @Override
  public void onClick(View view) {
//    if (view.equals(mContactItemLayout)) {
//      if (!mPermissionCallback.onFragmentRequestPermission(Constants.REQUEST_READ_CONTACTS_PERMISSION)) {
//        return;
//      }
//      Intent intent = new Intent();
//      intent.setClassName(getContext(), "cz.justarrived.activities.ContactsActivity");
//      startActivity(intent);
//
//      returnFromContactsActivity = true;
//    } else
    if (view.equals(mMessageItemLayout)) {
      if (mApplication.getApplicationState(getContext()).equals(ApplicationHandler.AppState.TRACKING)) {
        Toast.makeText(getContext(), R.string.unavailable_while_location_updating, Toast.LENGTH_SHORT).show();
        return;
      }
      Bundle bundle = new Bundle();
      bundle.putString(Constants.LATEST_TEXT_KEY, mSharedPreferences.getString(Constants.LATEST_TEXT_KEY, ""));

      DialogFragment dialog = new EditMessageDialog();
      dialog.setTargetFragment(this, 0);
      dialog.setArguments(bundle);
      dialog.show(getActivity().getSupportFragmentManager(), "");
    } else if (view.equals(mPlaceItemLayout)) {
      if (!mPermissionCallback.onFragmentRequestPermission(Constants.REQUEST_ACCESS_FINE_LOCATION)) {
        return;
      }
      mMapCallback.onShowMap();
    } else if (view.equals(mAddContactButton)) {
      if (mApplication.getApplicationState(getContext()).equals(ApplicationHandler.AppState.TRACKING)) {
        Toast.makeText(getContext(), R.string.unavailable_while_location_updating, Toast.LENGTH_SHORT).show();
        return;
      }
      if (!mPermissionCallback.onFragmentRequestPermission(Constants.REQUEST_READ_CONTACTS_PERMISSION)) {
        return;
      }
      mContactsCallbacks.onAddContact();
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceBundle) {
    super.onCreate(savedInstanceBundle);
  }

  @Override
  public void onStart() {
    Log.i("MainFragment", "onStart");
    super.onStart();
  }

  @Override
  public void onResume() {
    super.onResume();
    mMapView.onResume();

//    if (returnFromContactsActivity) {
//      listContacts();
//      returnFromContactsActivity = false;
//    }
    Log.i("MainFragment", "onResume");
  }

//  private void listContacts() {
//    Log.i("MainFragment", "show contacts");
//    mContactField.setArrayText(mDBHandler.getContacts());
//  }

  @Override
  public void onPause() {
    super.onPause();
    mMapView.onPause();
    Log.i("MainFragment", "onPause");
  }

  @Override
  public void onStop() {
    super.onStop();
    Log.i("MainFragment", "onStop");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.i("MainFragment", "onDestroy");
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mMapView.onSaveInstanceState(outState);
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mMapView.onLowMemory();
  }

  @Override
  public void OnEditMessageDialogOk(String message) {
    if (message.isEmpty()) {
      mMessageField.setText(R.string.enter_sms_text);
    } else {
      mMessageField.setText(message);
    }
  }

  private void hideProgressLayout() {
    mProgressLayout.setVisibility(View.GONE);
  }

  private void showProgressLayout() {
    mProgressLayout.setVisibility(View.VISIBLE);

    setProgressText();
  }

  private void setProgressText() {
    if (mSharedPreferences.getBoolean(Constants.SMS_OR_NOTIFY_KEY, false)) {
      mProgressLabel.setText(R.string.sms_will_be_sent);
    } else {
      mProgressLabel.setText(R.string.notification_will_be_shown);
    }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
    if (s.equals(Constants.SERVICE_RUNNING_KEY)) {
      boolean value = sharedPreferences.getBoolean(s, false);

      if (value) {
        showProgressLayout();

        mSMSSwitch.setEnabled(false);
      } else {
        hideProgressLayout();

        mSMSSwitch.setEnabled(true);
      }
    } else if (s.equals(Constants.SMS_OR_NOTIFY_KEY)) {
      setProgressText();
    } else if (s.equals(Constants.CHOSEN_LOCATION_CAMERA_LAT) || s.equals(Constants.CHOSEN_LOCATION_CAMERA_LNG)) {
      setChosenLocationMarkerAndView();
    } else if (s.equals(Constants.SMS_OR_NOTIFY_KEY)) {
      setProgressText();
    }
  }

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (isVisibleToUser) {
      Log.i("MainFragment", "Visible");
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
    SharedPreferencesHandler.setSharedPreference(getContext(), Constants.SMS_OR_NOTIFY_KEY, b);
    showContactsAndMessage(b);
  }

  private void showContactsAndMessage(boolean b) {
    if (b) {
      mContactItemLayout.setVisibility(View.VISIBLE);
      mMessageItemLayout.setVisibility(View.VISIBLE);
      mRecipientsLabelLayout.setVisibility(View.VISIBLE);
      mMessageLabelLayout.setVisibility(View.VISIBLE);
      mRecyclerView.setVisibility(View.VISIBLE);
//      mContactField.initSetArrayText(mDBHandler.getContacts());
    } else {
      mContactItemLayout.setVisibility(View.GONE);
      mMessageItemLayout.setVisibility(View.GONE);
      mRecipientsLabelLayout.setVisibility(View.GONE);
      mMessageLabelLayout.setVisibility(View.GONE);
      mRecyclerView.setVisibility(View.GONE);
    }
  }

  private ItemTouchHelper.Callback createHelperCallback() {
    ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
        new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

          @Override
          public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                RecyclerView.ViewHolder target) {
            return false;
          }

          @Override
          public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
            deleteItem(viewHolder.getAdapterPosition());
          }

          @Override
          public boolean isLongPressDragEnabled() {
            return false;
          }

          @Override
          public boolean isItemViewSwipeEnabled() { return mApplication.getApplicationState(getContext()).equals(ApplicationHandler.AppState.IDLE); }
        };
    return simpleItemTouchCallback;
  }

  public void addContact(int position, Contact contact){
    Log.i("id", String.valueOf(contact.mID));

    if (mDBHandler.insertContact(contact)) {
      if (position == -1) {
        contact_data.add(contact);
      } else {
        contact_data.add(position, contact);
      }
      mAdapter.notifyItemInserted(contact_data.indexOf(contact));
    }
  }

  private void deleteItem(final int position) {
    Contact contact = contact_data.get(position);
    if(contact != null){
      mDBHandler.removeContact(contact.mID);

      contact.mListPosition = contact_data.indexOf(contact);
      contact_data.remove(contact);

      mContactsCallbacks.onContactRemoved(contact);
    }
    mAdapter.notifyItemRemoved(position);
  }
  public void restoreContact(Contact contact) {
    addContact(contact.mListPosition, contact);
  }

  public interface OnContactEventListener {
    void onContactRemoved(Contact contact);
    void onAddContact();
  }

  public interface OnShowMapListener {
    void onShowMap();
  }

  public interface OnRequestPermissionListener {
    boolean onFragmentRequestPermission(int key);
  }
}
