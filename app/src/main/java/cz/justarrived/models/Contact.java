package cz.justarrived.models;

import android.net.Uri;
import android.support.annotation.Nullable;

public class Contact {

  private static final String DEFAULT_NAME = "NoName";

  public String mNumber;
  public String mFullName;
  public int mID;
  public int mListPosition;
  public Uri mImgUri;

  public Contact(String number, @Nullable String fullName, int ID, Uri imgUri ){
    mNumber = number;
    mID = ID;
    mImgUri = imgUri;

    if (fullName == null) {
      mFullName = DEFAULT_NAME;
    } else {
      mFullName = fullName;
    }
  }
}
