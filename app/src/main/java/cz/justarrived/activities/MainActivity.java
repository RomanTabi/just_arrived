package cz.justarrived.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import cz.justarrived.Constants;
import cz.justarrived.R;
import cz.justarrived.fragments.MainFragment;
import cz.justarrived.handlers.ApplicationHandler;
import cz.justarrived.handlers.DBHandler;
import cz.justarrived.handlers.NotificationHandler;
import cz.justarrived.handlers.SharedPreferencesHandler;
import cz.justarrived.models.Contact;
import cz.justarrived.services.UpdateLocationService;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, View.OnClickListener, MainFragment.OnShowMapListener,
    SharedPreferences.OnSharedPreferenceChangeListener, MainFragment.OnRequestPermissionListener, MainFragment.OnContactEventListener {

  private static final int REQUEST_CODE_PICK_CONTACT = 1;

  private DrawerLayout mDrawerLayout;
  private ActionBarDrawerToggle mDrawerToggle;
  private SharedPreferences mSharedPreferences;
  private View mSnackBarLayout;
  private FloatingActionButton mStartButton;
  private DBHandler mDBHandler;
  private ApplicationHandler mApplication;
  private Snackbar mContactRemovedSnackBar;
  private MainFragment mMainFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.drawer_layout);

    mApplication = ApplicationHandler.getInstance();

    if (SharedPreferencesHandler.getSharedPreference(this, Constants.FIRST_RUN, true)) {
      SharedPreferencesHandler.setSharedPreference(this, Constants.FIRST_RUN, false);
    }

    mDBHandler = new DBHandler(this);

    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    mSnackBarLayout = findViewById(R.id.coord_layout);
    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

    mContactRemovedSnackBar = Snackbar.make(mSnackBarLayout, R.string.contact_removed,
        Snackbar.LENGTH_INDEFINITE);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    if (toolbar != null) {
      setSupportActionBar(toolbar);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeButtonEnabled(true);
      getSupportActionBar().setTitle(R.string.notification);
    }

    mDrawerToggle = new ActionBarDrawerToggle(
      this,
      mDrawerLayout,
      R.string.open_drawer,
      R.string.close_drawer
    );
    mDrawerLayout.addDrawerListener(mDrawerToggle);

    mStartButton = (FloatingActionButton) findViewById(R.id.start_updating_fab);
    mStartButton.setOnClickListener(this);

    checkPermission(Constants.REQUEST_ACCESS_FINE_LOCATION);
    checkPermission(Constants.REQUEST_SMS_SEND_PERMISSION);
    checkPermission(Constants.REQUEST_READ_CONTACTS_PERMISSION);

    mMainFragment = new MainFragment();
    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager.beginTransaction().replace(R.id.content, mMainFragment).commit();
  }

//  @Override
//  public boolean onCreateOptionsMenu(Menu menu) {
//    MenuInflater inflater = getMenuInflater();
//    inflater.inflate(R.menu.menu_main, menu);
//    return super.onCreateOptionsMenu(menu);
//  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Pass the event to ActionBarDrawerToggle, if it returns
    // true, then it has handled the app icon touch event
    if (mDrawerToggle.onOptionsItemSelected(item)) {
      return true;
    }
//    if (item.getItemId() == R.id.action_settings) {
//      Intent intent = new Intent();
//      intent.setClassName(this, "cz.justarrived.activities.SettingsActivity");
//      startActivity(intent);
//    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onClick(View view) {
    if (view.equals(mStartButton)) {
      if (mApplication.getApplicationState(getApplicationContext()).equals(ApplicationHandler.AppState.TRACKING)) {
        // Change button icon
        mStartButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_24dp));
        // Set app state to IDLE
        mApplication.setApplicationState(getApplicationContext(), ApplicationHandler.AppState.IDLE);

        stopService();
      } else {
        /*
         * START SERVICE HERE
         * Change button icon
         */
        if (!canStart()) {
          return;
        }
        // Change button icon
        mStartButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_close_24dp));
        // Set app state to TRACKING
        mApplication.setApplicationState(getApplicationContext(), ApplicationHandler.AppState.TRACKING);

        startService();
      }
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case (REQUEST_CODE_PICK_CONTACT):
        if (resultCode == Activity.RESULT_OK) {
          Uri contactData = data.getData();
          Cursor cursor = getContentResolver().query(contactData, null, null, null, null);

          if (cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
            String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String number;
            Uri imgUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));
            imgUri = Uri.withAppendedPath(imgUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
            if (hasPhone.equalsIgnoreCase("1")) {
              Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);

              phones.moveToFirst();
              number = phones.getString(phones.getColumnIndex("data1"));
              mMainFragment.addContact(-1, new Contact(number, name, Integer.parseInt(id), imgUri));
            } else {
              Toast.makeText(this, R.string.contact_does_not_have_a_number, Toast.LENGTH_LONG).show();
            }
          }
        }
    }
  }

  @Override
  public void onContactRemoved(final Contact contact) {
    mContactRemovedSnackBar.setAction(R.string.undo, new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mMainFragment.restoreContact(contact);
      }
    })
        .setDuration(1500)
        .show();
  }

  @Override
  public void onAddContact() {
    if (mContactRemovedSnackBar.isShown()) {
      mContactRemovedSnackBar.dismiss();
    }
    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
    startActivityForResult(intent, REQUEST_CODE_PICK_CONTACT);
  }

  private boolean canStart() {
    if (!gpsEnabled()) {
      return false;
    }
    if (mSharedPreferences.getString(Constants.CHOSEN_LOCATION_CAMERA_LAT, "").isEmpty() || mSharedPreferences.getString(Constants.CHOSEN_LOCATION_CAMERA_LNG, "").isEmpty()) {
      Toast.makeText(MainActivity.this, R.string.select_destination, Toast.LENGTH_SHORT).show();
      return false;
    }
    if (!SharedPreferencesHandler.getSharedPreference(this, Constants.SMS_OR_NOTIFY_KEY, true)) {
      return true;
    }
    if (!mDBHandler.hasContacts()) {
      Toast.makeText(MainActivity.this, R.string.select_contacts, Toast.LENGTH_SHORT).show();
      return false;
    }
    if (mSharedPreferences.getString(Constants.LATEST_TEXT_KEY, "").isEmpty()) {
//      final Snackbar snackbar = Snackbar.make(mSnackBarLayout, R.string.warning_empty_message, Snackbar.LENGTH_INDEFINITE);
//      snackbar.setAction(R.string.ok, new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//          snackbar.dismiss();
//        }
//      }).show();
      Toast.makeText(MainActivity.this, R.string.enter_sms_text, Toast.LENGTH_SHORT).show();
      return false;
    }
    if (!checkPermission(Constants.REQUEST_SMS_SEND_PERMISSION)) {
      return false;
    }
    return true;
  }

  public boolean gpsEnabled() {
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
  public void onShowMap() {
    Intent intent = new Intent();
    intent.setClassName(this, "cz.justarrived.activities.MapActivity");
    intent.putExtra(Constants.MY_LOCATION, false);
    startActivity(intent);
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.i("MainActivity", "onResume");

    if (mApplication.getApplicationState(getApplicationContext()).equals(ApplicationHandler.AppState.TRACKING)) {
      mStartButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_close_24dp));
    } else {
      mStartButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_24dp));
    }

    NotificationHandler notificationHandler = new NotificationHandler();
    notificationHandler.cancelNotification(this);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    mDrawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // Pass any configuration change to the drawer toggls
    mDrawerToggle.onConfigurationChanged(newConfig);
  }

  // Request permissions on API23 or higher
  @TargetApi(23)
  public boolean checkPermission(int PERMISSION_ID) {
    switch (PERMISSION_ID) {
      case Constants.REQUEST_SMS_SEND_PERMISSION:
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
          showRequestPermissionRationale(Manifest.permission.SEND_SMS, R.string.permission_sms_rationale, Constants.REQUEST_SMS_SEND_PERMISSION);
          return false;
        }
        break;
      case Constants.REQUEST_READ_CONTACTS_PERMISSION:
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
          showRequestPermissionRationale(Manifest.permission.READ_CONTACTS, R.string.permission_contacts_rationale, Constants.REQUEST_READ_CONTACTS_PERMISSION);
          return false;
        }
        break;
      case Constants.REQUEST_ACCESS_FINE_LOCATION:
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
          showRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION, R.string.permission_location_rationale, Constants.REQUEST_ACCESS_FINE_LOCATION);
          return false;
        }
        break;
    }
    return true;
  }

  private void showRequestPermissionRationale(final String permission, int rationale, final int requestCode) {
    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
        permission)) {
      // Provide an additional rationale to the user if the permission was not granted
      // and the user would benefit from additional context for the use of the permission.
      // For example if the user has previously denied the permission.
      Snackbar.make(mSnackBarLayout, rationale,
          Snackbar.LENGTH_INDEFINITE)
          .setAction(R.string.ok, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
          })
          .show();
    } else {
      ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    switch (requestCode) {
      case (Constants.REQUEST_SMS_SEND_PERMISSION):
        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          Snackbar.make(mSnackBarLayout, R.string.sms_permission_granted,
              Snackbar.LENGTH_LONG).show();
//          mStartButton.callOnClick();
//          Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show();
        } else {
//          Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
          Snackbar.make(mSnackBarLayout, R.string.permission_not_granted,
              Snackbar.LENGTH_LONG).show();
        }
        break;
      case (Constants.REQUEST_READ_CONTACTS_PERMISSION):
        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          Snackbar.make(mSnackBarLayout, R.string.read_contact_permission_granted,
              Snackbar.LENGTH_LONG).show();
//          Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show();
        } else {
          Snackbar.make(mSnackBarLayout, R.string.permission_not_granted,
              Snackbar.LENGTH_LONG).show();
//          Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
        }
        break;
      case (Constants.REQUEST_ACCESS_FINE_LOCATION):
        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          Snackbar.make(mSnackBarLayout, R.string.map_permission_granted,
              Snackbar.LENGTH_LONG).show();
//          Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show();
        } else {
          Snackbar.make(mSnackBarLayout, R.string.permission_not_granted,
              Snackbar.LENGTH_LONG).show();
//          Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
        }
        break;
      default:
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        break;
    }
  }

  private void startService() {
    startService(new Intent(getApplicationContext(), UpdateLocationService.class));
  }

  private void stopService() {
    stopService(new Intent(getApplicationContext(), UpdateLocationService.class));
  }

  private void restartService() {
    stopService();
    startService();
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
    if (s.equals(Constants.TRIGGER_RADIUS_KEY)) {
      Log.i("Radius Changed", "" + sharedPreferences.getInt(s, 0));
      if (mSharedPreferences.getBoolean(Constants.SERVICE_RUNNING_KEY, false)) {
        restartService();
      }
    } else if (s.equals(Constants.SERVICE_RUNNING_KEY)) {
      boolean value = sharedPreferences.getBoolean(s, false);
      if (!value) {
        mStartButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_24dp));
      }
    }
  }

  @Override
  public boolean onFragmentRequestPermission(int key) {
    return checkPermission(key);
  }

  @Override
  public void onStart() {
    Log.i("MainActivity", "onStart");
    super.onStart();
  }

  @Override
  public void onPause() {
    super.onPause();
    Log.i("MainActivity", "onPause");
  }

  @Override
  public void onStop() {
    super.onStop();
    Log.i("MainActivity", "onStop");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.i("MainActivity", "onDestroy");
  }
}