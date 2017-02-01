package cz.justarrived.activities;


import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import java.io.FileNotFoundException;
import java.io.InputStream;

import cz.justarrived.models.Contact;
import cz.justarrived.R;
import cz.justarrived.fragments.ContactsFragment;


public class ContactsActivity extends AppCompatActivity implements View.OnClickListener, ContactsFragment.OnShowMessageListener {
  private static final int REQUEST_CODE_PICK_CONTACT = 1;

  private FloatingActionButton mAddButton;
  private ContactsFragment mContactFragment;
  private View mSnackBarLayout;
  private Snackbar mSnackBar;

  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_contacts);

    mSnackBarLayout = findViewById(R.id.snackbar_layout);
    mSnackBar = Snackbar.make(mSnackBarLayout, R.string.contact_removed,
        Snackbar.LENGTH_INDEFINITE);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    if (toolbar != null) {
      setSupportActionBar(toolbar);
    }
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    mAddButton = (FloatingActionButton) findViewById(R.id.start_updating_fab);
    mAddButton.setOnClickListener(this);

    mContactFragment = new ContactsFragment();
    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager.beginTransaction().replace(R.id.contact_content, mContactFragment).commit();

  }

  @Override
  public void onClick(View view) {
    if (view.equals(mAddButton)) {
      if (mSnackBar.isShown()) {
        mSnackBar.dismiss();
      }
      Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
      startActivityForResult(intent, REQUEST_CODE_PICK_CONTACT);
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
              mContactFragment.addContact(-1, new Contact(number, name, Integer.parseInt(id), imgUri));
            } else {
              Toast.makeText(this, R.string.contact_does_not_have_a_number, Toast.LENGTH_LONG).show();
            }
          }
        }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater menuInflater = getMenuInflater();
    menuInflater.inflate(R.menu.menu_contacts, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_save) {
      finish();
    }
    return super.onOptionsItemSelected(item);
  }


  @Override
  public void onContactRemoved(final Contact contact) {
    mSnackBar.setAction(R.string.undo, new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            mContactFragment.restoreContact(contact);
          }
        })
        .show();
  }
}
