package cz.justarrived.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import cz.justarrived.models.Contact;
import cz.justarrived.R;

public class ContactsViewAdapter extends RecyclerView.Adapter<ContactsViewAdapter.MyViewHolder>{

  private final LayoutInflater inflater;
  List<Contact> data = Collections.emptyList();
  Context context;


  public ContactsViewAdapter(Context context, List<Contact> data){
    inflater = LayoutInflater.from(context);
    this.data = data;
    this.context = context;
  }

  @Override
  public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    View view = inflater.inflate(R.layout.list_item_contact, parent, false);
    MyViewHolder viewHolder = new MyViewHolder(view);

    return viewHolder;
  }

  @Override
  public void onBindViewHolder(MyViewHolder holder, int position) {
    Contact cur = data.get(position);
    holder.name.setText(cur.mFullName);
    holder.number.setText(cur.mNumber);

    try {
      holder.img.setImageBitmap(getContactBitmapFromURI(context, cur.mImgUri));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

  }

  @Override
  public int getItemCount() {
    return data.size();
  }

  class MyViewHolder extends RecyclerView.ViewHolder {

    TextView name;
    TextView number;
    ImageView img;

    public MyViewHolder(View itemView) {
      super(itemView);
      name = (TextView) itemView.findViewById(R.id.contact_name);
      number = (TextView) itemView.findViewById(R.id.contact_number);
      img = (ImageView) itemView.findViewById(R.id.contact_img);
    }

  }
  public static Bitmap getContactBitmapFromURI(Context context, Uri uri) throws FileNotFoundException {
    InputStream input = context.getContentResolver().openInputStream(uri);
    if (input == null) {
      return null;
    }
    return BitmapFactory.decodeStream(input);
  }

}
