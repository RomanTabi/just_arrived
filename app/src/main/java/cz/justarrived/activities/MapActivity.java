package cz.justarrived.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import cz.justarrived.Constants;
import cz.justarrived.R;
import cz.justarrived.dialogs.SelectRadiusDialog;
import cz.justarrived.fragments.MapFragment;
import cz.justarrived.handlers.ApplicationHandler;

public class MapActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    View.OnClickListener, SelectRadiusDialog.SetRadiusDialogCallback, com.google.android.gms.location.LocationListener, PlaceSelectionListener, MapFragment.MapFragmentCallbacks {

  private static final int REQUEST_SELECT_PLACE = 1001;
  private static final long UPDATE_INTERVAL = 1000;

  private GoogleApiClient mGoogleApiClient;

  private PlaceAutocompleteFragment mAutocompleteFragment;
  private Location mCurrentLocation;
  private FloatingActionButton mFloatingButton;
  private MapFragment mMapFragment;
  private SharedPreferences mSharedPreferences;
  private View mSnackBarLayout;
  private boolean mFlagShowMyLocation;
  private RelativeLayout mRadiusItemLayout;
  private TextView mRadiusField;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);

    mFlagShowMyLocation = false;

    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    if (toolbar != null) {
      setSupportActionBar(toolbar);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeButtonEnabled(true);
    }

    if (mGoogleApiClient == null) {
      mGoogleApiClient = new GoogleApiClient.Builder(this)
          .addConnectionCallbacks(this)
          .addOnConnectionFailedListener(this)
          .addApi(LocationServices.API)
          .build();
    }

    mAutocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_fragment);
    mAutocompleteFragment.setOnPlaceSelectedListener(this);
    mAutocompleteFragment.setHint(getResources().getString(R.string.search_hint));

    mSnackBarLayout = findViewById(R.id.coord_layout);

    mRadiusItemLayout = (RelativeLayout) findViewById(R.id.radius_layout);
    mRadiusItemLayout.setOnClickListener(this);

    mRadiusField = (TextView) findViewById(R.id.radius);
    setRadiusText(mSharedPreferences.getInt(Constants.TRIGGER_RADIUS_KEY, 0));

    mFloatingButton = (FloatingActionButton) findViewById(R.id.fab);
    mFloatingButton.setOnClickListener(this);

    boolean myLocationRequest = getIntent().getExtras().getBoolean(Constants.MY_LOCATION, true);
    Bundle bundle = new Bundle();
    bundle.putBoolean(Constants.MY_LOCATION, myLocationRequest);

    mMapFragment = new MapFragment();
    mMapFragment.setArguments(bundle);
    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager.beginTransaction().replace(R.id.content, mMapFragment).commit();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater menuInflater = getMenuInflater();
    menuInflater.inflate(R.menu.menu_map, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_search) {
      try {
        Intent i = new PlaceAutocomplete.IntentBuilder
            (PlaceAutocomplete.MODE_OVERLAY)
            .build(MapActivity.this);
        startActivityForResult(i, REQUEST_SELECT_PLACE);
      } catch (GooglePlayServicesRepairableException |
          GooglePlayServicesNotAvailableException e) {
        e.printStackTrace();
      }
    } else if (item.getItemId() == R.id.action_my_position) {
      if (OnGpsRequired()) {
        if (mCurrentLocation != null) {
          mMapFragment.animateCamera(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
        }
      }
    } else if (item.getItemId() == R.id.action_destination) {
      mMapFragment.animateCamera(new LatLng(
          Double.valueOf(mSharedPreferences.getString(Constants.CHOSEN_LOCATION_CAMERA_LAT, "0.0")),
          Double.valueOf(mSharedPreferences.getString(Constants.CHOSEN_LOCATION_CAMERA_LNG, "0.0"))));
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_SELECT_PLACE) {
      if (resultCode == RESULT_OK) {
        Place place = PlaceAutocomplete.getPlace(this, data);
        this.onPlaceSelected(place);
      } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
        Status status = PlaceAutocomplete.getStatus(this, data);
        this.onError(status);
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public void onPlaceSelected(Place place) {
    LatLngBounds bounds = place.getViewport();

    Log.i("Place", "Selected");

    if (bounds != null) {
      mMapFragment.animateCamera(bounds);
    } else {
      mMapFragment.animateCamera(place.getLatLng());
    }
    if (mSharedPreferences.getBoolean(Constants.SERVICE_RUNNING_KEY, false)) {
      Toast.makeText(this, R.string.place_not_updated, Toast.LENGTH_LONG).show();
    } else {
      mMapFragment.selectLocation(place.getLatLng());
      mMapFragment.addMarker(place.getLatLng());
    }
  }

  @Override
  public void onError(Status status) {
    Log.e("Search", "onError: Status = " + status.toString());
    Toast.makeText(this, R.string.place_selection_failed,
        Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onClick(View view) {
    if (view.equals(mFloatingButton)) {
      if (ApplicationHandler.getInstance().getApplicationState(this).equals(ApplicationHandler.AppState.TRACKING)) {
        Toast.makeText(this, R.string.unavailable_while_location_updating, Toast.LENGTH_SHORT).show();
      } else {
//        if (mCurrentLocation != null) {
          mMapFragment.updateChosenLocationSharedPreferences();
//        }
      }
    } else if (view.equals(mRadiusItemLayout)) {
      SelectRadiusDialog selectRadiusDialog = new SelectRadiusDialog();
      selectRadiusDialog.show(getSupportFragmentManager(), "");
    }
  }

  protected void stopLocationUpdates() {
    if (mGoogleApiClient.isConnected()) {
      LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (mGoogleApiClient.isConnected()) {
      startLocationUpdates();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    stopLocationUpdates();
  }

  @Override
  protected void onStart() {
    mGoogleApiClient.connect();
    super.onStart();
  }

  @Override
  protected void onStop() {
    mGoogleApiClient.disconnect();
    super.onStop();
  }

  @Override
  public void onConnected(@Nullable Bundle bundle) {
    startLocationUpdates();
  }

  @Override
  public void onConnectionSuspended(int i) {

  }

  protected void startLocationUpdates() {
    if (!mGoogleApiClient.isConnected()) {
      return;
    }
    LocationRequest locationRequest = createLocationRequest();

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      return;
    }
    LocationServices.FusedLocationApi.requestLocationUpdates(
        mGoogleApiClient, locationRequest, this);
  }

  protected LocationRequest createLocationRequest() {
    LocationRequest locationRequest = new LocationRequest();
    locationRequest.setInterval(UPDATE_INTERVAL);
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    return locationRequest;
  }

  @Override
  public void onLocationChanged(Location location) {
    mCurrentLocation = location;
    Log.i("Location changed", "location");

    if (mFlagShowMyLocation) {
      mFlagShowMyLocation = false;
      mMapFragment.setMyLocationCamera(mCurrentLocation);
    }
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

  }

  @Override
  public Location OnGetCurrentLocation() {
    if (mCurrentLocation == null) {
      Log.i("Current location", "null");
      mFlagShowMyLocation = true;
      return null;
    }
    Log.i("Current location", "not null = " + mCurrentLocation);
    return mCurrentLocation;
  }

  /**
   * Check if GPS enabled. Show snackbar if not.
   * @return true if GPS enabled, false otherwise.
   */
  @Override
  public boolean OnGpsRequired() {
    final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    boolean gpsEnabled = false;

    try {
      gpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (!gpsEnabled) {

      final Snackbar snackbar = Snackbar.make(mSnackBarLayout, R.string.enable_gps, Snackbar.LENGTH_INDEFINITE);
      snackbar.setAction(R.string.ok, new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          snackbar.dismiss();
        }
      });
      snackbar.show();

    }
    return gpsEnabled;
  }

  @Override
  public void OnSetRadiusDialogResult(String result) {
    setRadiusText(result);
  }

  private void setRadiusText(int index) {
    final String[] items = getResources().getStringArray(R.array.TriggerRadius);

    mRadiusField.setText(items[index]);
  }

  private void setRadiusText(String text) {
    mRadiusField.setText(text);
  }
}
