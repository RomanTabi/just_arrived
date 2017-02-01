package cz.justarrived.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import cz.justarrived.handlers.DBHandler;
import cz.justarrived.models.Contact;
import cz.justarrived.R;
import cz.justarrived.adapters.ContactsViewAdapter;
import cz.justarrived.models.DividerItemDecoration;
public class ContactsFragment extends Fragment {
  private static final int REQUEST_CODE_PICK_CONTACT = 1;

  private RecyclerView mRecyclerView;
  private View mRootView;
  private ContactsViewAdapter mAdapter;
  private ArrayList<Contact> contact_data = new ArrayList<>();
  private OnShowMessageListener mMessageCallbacks;
  private DBHandler mDBHandler;
  private TextView mAddContactLabel;


  public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)  {
    mRootView = inflater.inflate(R.layout.fragment_contacts, container, false);

    mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycle_list);


    mAddContactLabel = (TextView) mRootView.findViewById(R.id.add_contact_label);
    mDBHandler = new DBHandler(getActivity().getApplicationContext());

    contact_data = mDBHandler.getContacts();

    mAdapter = new ContactsViewAdapter(getActivity(), contact_data);
    mRecyclerView.setAdapter(mAdapter);
//    mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

    mRecyclerView.setHasFixedSize(true);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    setContactLabel();

    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(createHelperCallback());
    itemTouchHelper.attachToRecyclerView(mRecyclerView);

    mMessageCallbacks = (OnShowMessageListener) getActivity();

    return mRootView;
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
      };
    return simpleItemTouchCallback;
  }

  private void setContactLabel() {
    if (contact_data.size() > 0) {
      hideAddContactLabel();
    } else {
      showAddContactLabel();
    }
  }

  private void showAddContactLabel() {
    mAddContactLabel.setVisibility(View.VISIBLE);
  }

  private void hideAddContactLabel() {
    mAddContactLabel.setVisibility(View.INVISIBLE);
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
      setContactLabel();
    }
  }

  private void deleteItem(final int position) {
    Contact contact = contact_data.get(position);
    if(contact != null){
      mDBHandler.removeContact(contact.mID);

      contact.mListPosition = contact_data.indexOf(contact);
      contact_data.remove(contact);

      mMessageCallbacks.onContactRemoved(contact);
    }
    mAdapter.notifyItemRemoved(position);
    setContactLabel();
  }
  public void restoreContact(Contact contact) {
    addContact(contact.mListPosition, contact);
  }

  public interface OnShowMessageListener {
    void onContactRemoved(Contact contact);
  }
}


