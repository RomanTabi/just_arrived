package cz.justarrived.handlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cz.justarrived.Constants;
import cz.justarrived.models.Contact;

public class DBHandler extends SQLiteOpenHelper {

  /*
   * Usage:
   * DBHandler db = DBHandler(this, "JustArrivedDB", null, 1);
   *
   * Insert contact:
   * db.insertContact(new Contact("+421123456789", "Name Surname"));
   *
   * Get contacts:
   * List<Contact> contacts = db.getContacts();
   * for (Contact contact: contacts) {
   *   Log.i("DB contacts", "" + contact.mFullName + " " + contact.mNumber);
   * }
   */

  private static final String CONTACTS_TABLE_NAME = "CONTACTS";
  private static final String FULL_NAME_COL = "FULL_NAME";
  private static final String NUMBER_COL = "NUMBER";
  private static final String ID_COL = "mID";
  private static final String IMG_URI_COL = "IMG_URI";

  private static final String CONTACTS_TABLE_CREATE =
      "CREATE TABLE IF NOT EXISTS " + CONTACTS_TABLE_NAME + "(" +
          FULL_NAME_COL + " TEXT, " +
          NUMBER_COL + " TEXT UNIQUE, " +
          IMG_URI_COL + " TEXT, " +
          ID_COL + " INTEGER" +
          ");";

  private static final String CONTACTS_TABLE_DROP =
      "DROP TABLE IF EXISTS " + CONTACTS_TABLE_NAME;
  private final Context mContext;

  public DBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
    super(context, name, factory, version);
    mContext = context;
  }

  public DBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
    super(context, name, factory, version, errorHandler);
    mContext = context;
  }

  public DBHandler(Context context)
  {
    super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    mContext = context;
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
    if (mContext != null) {
      if (SharedPreferencesHandler.getSharedPreference(mContext, Constants.FIRST_RUN, true)) {
        sqLiteDatabase.execSQL(CONTACTS_TABLE_DROP);
      }
    }
    sqLiteDatabase.execSQL(CONTACTS_TABLE_CREATE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

  }

  /**
   * Insert contact to database
   * @param contact
   * @return success
   */
  public boolean insertContact(Contact contact) {
    SQLiteDatabase db = getWritableDatabase();

    ContentValues contentValues = new ContentValues();
    contentValues.put(FULL_NAME_COL, contact.mFullName);
    contentValues.put(NUMBER_COL, contact.mNumber);
    contentValues.put(IMG_URI_COL, contact.mImgUri.toString());
    contentValues.put(ID_COL, contact.mID);

    try {
      db.insertWithOnConflict(CONTACTS_TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_ROLLBACK);
    } catch (SQLiteConstraintException e){
      Log.e("SQLite insert", "UNIQUE Constraint violation " + contact.mNumber);
      db.close();
      return false;
    }
    db.close();
    return true;
  }

  public void removeContact(int ID)
  {
    Log.i("Delete", "Item with id " + ID);

    SQLiteDatabase db = getWritableDatabase();

    String where = ID_COL + "=?";
    String[] args = new String[]{String.valueOf(ID)};

    db.delete(CONTACTS_TABLE_NAME, where, args);
    db.close();
  }

  public ArrayList<Contact> getContacts() {

    ArrayList<Contact> contacts = new ArrayList<Contact>();

    SQLiteDatabase db = getReadableDatabase();

    Cursor c = db.rawQuery(
        "SELECT * FROM " + CONTACTS_TABLE_NAME,
        null);

    if(c.moveToFirst())
    {
      while(!c.isAfterLast())
      {
        String number = c.getString(c.getColumnIndex(NUMBER_COL));
        String fullName = c.getString(c.getColumnIndex(FULL_NAME_COL));
        String imgUri = c.getString(c.getColumnIndex(IMG_URI_COL));
        int ID = c.getInt(c.getColumnIndex(ID_COL));

        Contact contact = new Contact(number, fullName, ID, Uri.parse(imgUri));
        contacts.add(contact);

        c.moveToNext();
      }
    }
    else
    {
      Log.e("Cursor", "empty");
    }
    c.close();
    db.close();
    return contacts;
  }

  public boolean hasContacts() {
    if (getContacts().size() > 0) {
      return true;
    }
    return false;
  }
}
