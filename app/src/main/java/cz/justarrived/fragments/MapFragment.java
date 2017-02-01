package cz.justarrived.fragments;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import cz.justarrived.Constants;
import cz.justarrived.R;

public class MapFragment extends Fragment implements OnMapReadyCallback {

  private static final int ZOOM_PADDING = 10;

  private MapFragmentCallbacks mLocationCallback;
  private GoogleMap mGoogleMap;
  private MapView mMapView;
  private LatLng mSelectedLocation;
  private SharedPreferences mSharedPreferences;

  public MapFragment() {

  }

  @Override
  public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_map, container, false);

    MapsInitializer.initialize(getActivity());

    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

    mMapView = (MapView) rootView.findViewById(R.id.map);
    mMapView.onCreate(savedInstanceState);
    mMapView.getMapAsync(this);

    mLocationCallback = (MapFragmentCallbacks) getActivity();

    return rootView;
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    mGoogleMap = googleMap;
    mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
    mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
    mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);

    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      return;
    }
    mGoogleMap.setMyLocationEnabled(true);

    mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
      @Override
      public void onMapLongClick(LatLng latLng) {
        if (!mSharedPreferences.getBoolean(Constants.SERVICE_RUNNING_KEY, false)) {
          selectLocation(latLng);
        } else {
          Toast.makeText(getActivity(), R.string.unavailable_while_location_updating, Toast.LENGTH_LONG).show();
        }
      }
    });

    mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
      @Override
      public boolean onMarkerClick(Marker marker) {
        return true;
      }
    });

    Bundle bundle = getArguments();
    if (bundle.getBoolean(Constants.MY_LOCATION, true)) {
      if (mLocationCallback.OnGpsRequired()) {
        Location currentLocation = mLocationCallback.OnGetCurrentLocation();
        Log.i("Map Ready", "my location true");
        if (currentLocation != null) {
          setMyLocationCamera(currentLocation);
        }
      }
    } else {
      setChosenLocationCamera();
    }
    setChosenLocationMarker();
  }

  public void selectLocation(LatLng latLng) {
    mSelectedLocation = latLng;
    addMarker(latLng);
  }

  public void setMyLocationCamera(Location location) {
    Log.i("Map Ready", "set my location camera");
    mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
        new LatLng(location.getLatitude(), location.getLongitude()), Constants.DEFAULT_CAMERA_ZOOM, Constants.DEFAULT_CAMERA_TILT, Constants.DEFAULT_CAMERA_BEARING)));
  }

  private void setChosenLocationCamera() {
    String sLat = mSharedPreferences.getString(Constants.CHOSEN_LOCATION_CAMERA_LAT, "");
    String sLng = mSharedPreferences.getString(Constants.CHOSEN_LOCATION_CAMERA_LNG, "");

    if (sLat.isEmpty() || sLat.isEmpty()) {
      return;
    }

    LatLng latLng = new LatLng(Double.valueOf(sLat), Double.valueOf(sLng));
    float zoom = Constants.DEFAULT_CAMERA_ZOOM;
    float bearing = Constants.DEFAULT_CAMERA_BEARING;
    float tilt = Constants.DEFAULT_CAMERA_TILT;

    mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
        latLng, zoom, tilt, bearing
    )));
  }

  private void setChosenLocationMarker() {
    String sLat = mSharedPreferences.getString(Constants.CHOSEN_LOCATION_CAMERA_LAT, "");
    String sLng = mSharedPreferences.getString(Constants.CHOSEN_LOCATION_CAMERA_LNG, "");

    if (sLat.isEmpty() || sLat.isEmpty()) {
      return;
    }

    LatLng latLng = new LatLng(Double.valueOf(sLat), Double.valueOf(sLng));

    mSelectedLocation = latLng;
    addMarker(latLng);
  }

  public void addMarker(LatLng latLng) {
    mGoogleMap.clear();
    mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(String.valueOf(R.string.destination)));
  }

  public void animateCamera(LatLng latLng) {
    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, Constants.DEFAULT_CAMERA_ZOOM));
  }

  public void animateCamera(LatLngBounds bounds) {
    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, ZOOM_PADDING));
  }

  public void updateChosenLocationSharedPreferences(){
    if (mSelectedLocation == null) {
      Toast.makeText(getActivity(), R.string.choose_place, Toast.LENGTH_LONG).show();
      return;
    }
    Toast.makeText(getActivity(), R.string.place_saved, Toast.LENGTH_LONG).show();

    CameraPosition cameraPosition = mGoogleMap.getCameraPosition();
    SharedPreferences.Editor editor = mSharedPreferences.edit();

    editor.putString(Constants.CHOSEN_LOCATION_CAMERA_LAT, String.valueOf(mSelectedLocation.latitude));
    editor.putString(Constants.CHOSEN_LOCATION_CAMERA_LNG, String.valueOf(mSelectedLocation.longitude));
    editor.apply();

    getActivity().finish();
  }

  @Override
  public void onCreate(Bundle savedInstanceBundle) {
    super.onCreate(savedInstanceBundle);
  }

  @Override
  public void onStart() {
    super.onStart();
  }

  @Override
  public void onResume() {
    super.onResume();
    mMapView.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    mMapView.onPause();
  }

  @Override
  public void onStop() {
    super.onStop();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
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

  public interface MapFragmentCallbacks {
    Location OnGetCurrentLocation();
    boolean OnGpsRequired();
  }
}
