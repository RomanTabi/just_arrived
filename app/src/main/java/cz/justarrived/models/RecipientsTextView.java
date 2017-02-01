package cz.justarrived.models;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cz.justarrived.R;

/**
 * Custom TextView
 * Max. lines = 1
 * Displays first x words that fit + count of the rest
 */

public class RecipientsTextView extends TextView {
  public RecipientsTextView(Context context) {
    super(context);
    setParams();
  }

  public RecipientsTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setParams();
  }

  public RecipientsTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setParams();
  }

  //public RecipientsTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
  //  super(context, attrs, defStyleAttr, defStyleRes);
  //  setParams();
  //}

  private void setParams() {
    this.setMaxLines(1);
  }

  private boolean isTooLarge (String text) {
    float width = this.getPaint().measureText(text);
    /*
     * Need to reserve space for count of the rest words indicator, i.e. 7
     */
    Log.i("RecipientsTextView", "text width = " + width + " view width = " + this.getMeasuredWidth());
    return (width >= (this.getMeasuredWidth() - 7));
  }

  public void initSetArrayText(final ArrayList<Contact> contacts) {
    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        /*
         * Remove listener
         */
        getViewTreeObserver().removeOnGlobalLayoutListener(this);

        setArrayText(contacts);
      }
    });
  }

  public void setArrayText(final ArrayList<Contact> contacts) {
    if (getWidth() > 0) {
      if (contacts.size() == 0) {
        setText(R.string.select_contacts);
        return;
      }
      int i = 0;
      Log.i("RecipientsTextView", "init");

      String separator = ", ";
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(contacts.get(i).mFullName);

      while (i < contacts.size() - 1) {
        if (isTooLarge(stringBuilder.toString())) {
          break;
        }
        ++i;
        stringBuilder.append(separator);
        stringBuilder.append(contacts.get(i).mFullName);
      }
      int restCount = contacts.size() - i - 1;
      Log.i("RecipientsTextView", "rest count = " + restCount);
      if (restCount > 0) {
        stringBuilder.append(", +" + restCount);
      }
      setText(stringBuilder.toString());
    }
  }
}
