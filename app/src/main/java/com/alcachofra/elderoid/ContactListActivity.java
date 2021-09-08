package com.alcachofra.elderoid;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.alcachofra.elderoid.utils.ContactInfo;
import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.utils.dialog.DialogOption;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContactListActivity extends ElderoidActivity {

    Netie netie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);



        RecyclerView recycler = findViewById(R.id.contact_list);
        AppCompatImageButton add = findViewById(R.id.add);
        AppCompatImageView loading = findViewById(R.id.loading);

        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.loading);
        loading.startAnimation(rotate);

        netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_HOME_BUTTON,
                Collections.singletonList(
                        new Cue(Elderoid.string(R.string.wait), R.drawable.netie_confused)
                ),
                false
        );

        Thread onContacts = new Thread() {
            public void run() {
                Set<ContactInfo> contact_list = loadContacts();

                runOnUiThread(() -> {
                    if (contact_list.size() > 0) netie.resetCuePool(
                            Arrays.asList(
                                    new Cue(Elderoid.string(R.string.call_contact_1), R.drawable.netie),
                                    new Cue(Elderoid.string(R.string.call_contact_2), R.drawable.netie_happy),
                                    new Cue(Elderoid.string(R.string.call_contact_3), R.drawable.netie)
                                            .setOption1(Elderoid.string(R.string.yes), v -> startActivity(new Intent(getApplicationContext(), AddContactActivity.class)))
                            ),
                            true
                    );
                    else netie.resetCuePool(
                            Collections.singletonList(
                                    new Cue(Elderoid.string(R.string.no_contacts), R.drawable.netie_concerned)
                                            .setOption1(Elderoid.string(R.string.yes), v -> startActivity(new Intent(getApplicationContext(), AddContactActivity.class)))
                            ),
                            false
                    );

                    if (getIntent().getBooleanExtra(Elderoid.ADDED_CONTACT, false))
                        netie.setBalloon(Elderoid.string(R.string.added_contact))
                                .setExpression(R.drawable.netie_happy);

                    loading.clearAnimation();
                    loading.setVisibility(View.GONE);

                    recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    recycler.setAdapter(new ContactListAdapter(contact_list));

                    add.setOnClickListener(v -> {
                        startActivity(new Intent(getApplicationContext(), AddContactActivity.class));
                    });
                });
            }
        };
        onContacts.start();
    }

    private void startCall(String number) {
        if (PhoneNumberUtils.isGlobalPhoneNumber(number)) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            startActivity(intent);
        } else netie.setBalloon(Elderoid.string(R.string.number_not_valid))
                .setExpression(R.drawable.netie_concerned);
    }

    private Set<ContactInfo> loadContacts() {
        Set<ContactInfo> contact_list = new HashSet<>();

        // Get all contacts:
        Cursor cursor = null;
        ContentResolver contentResolver = getContentResolver();
        try {
            cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        } catch (Exception ex) {

            // ERROR LOADING CONTACTS

        }

        // Check if there's any contacts:
        if (cursor != null && cursor.getCount() > 0) {

            // Loop through contacts:
            while (cursor.moveToNext()) {

                ContactInfo contact = new ContactInfo();
                String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String contact_display_name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                // Set name:
                contact.setName(contact_display_name);

                // Get telephone number:
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {
                    Cursor phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{contact_id},
                            null);

                    if (phoneCursor.moveToNext()) {
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        // Set telephone number:
                        contact.setTelephone(phoneNumber);
                    }
                    phoneCursor.close();
                }
                if (contact.getName() != null && contact.getTelephone() != null)
                    contact_list.add(contact);
            }
        }
        cursor.close();
        return contact_list;
    }

    private boolean deleteContact(ContactInfo c) {
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(c.getTelephone()));
        try (Cursor cur = getContentResolver().query(contactUri, null, null, null, null)) {
            if (cur.moveToFirst()) {
                do {
                    if (cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)).equalsIgnoreCase(c.getName())) {
                        String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        getContentResolver().delete(uri, null, null);
                        return true;
                    }
                } while (cur.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            netie.setBalloon(Elderoid.string(R.string.remove_contact_error))
                    .setExpression(R.drawable.netie_confused);
        }
        return false;
    }

    class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ContactViewHolder> {
        private final List<ContactInfo> contacts;

        class ContactViewHolder extends RecyclerView.ViewHolder {
            final View contact_item;
            final AppCompatTextView contact_name;
            final AppCompatTextView contact_telephone;
            final AppCompatImageButton call;
            final AppCompatImageButton delete;

            public ContactViewHolder(ViewGroup container) {
                super(LayoutInflater.from(ContactListActivity.this).inflate(R.layout.row_delete_contact, container, false));
                contact_item = itemView.findViewById(R.id.contact_item);
                contact_name = itemView.findViewById(R.id.contact_name);
                contact_telephone = itemView.findViewById(R.id.contact_telephone);
                call = itemView.findViewById(R.id.call);
                delete = itemView.findViewById(R.id.delete);
            }
        }

        public ContactListAdapter(Set<ContactInfo> contacts_list) {
            super();
            this.contacts = new ArrayList<>(contacts_list);
            Collections.sort(this.contacts);
        }

        @NonNull
        @Override
        public ContactListAdapter.ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ContactListAdapter.ContactViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ContactListAdapter.ContactViewHolder holder, int position) {
            holder.contact_name.setText(contacts.get(position).getName());
            holder.contact_telephone.setText(contacts.get(position).getTelephone());
            holder.call.setOnClickListener(v -> startCall(contacts.get(position).getTelephone()));
            holder.contact_item.setOnClickListener(v -> netie.setBalloon(
                    contacts.get(position).getName() + ", " + contacts.get(position).getTelephone() + "."
            ).setExpression(R.drawable.netie));
            holder.delete.setOnClickListener(v -> {
                DialogOption dialog = new DialogOption(
                        Elderoid.string(R.string.dialog_remove_contact),
                        Html.fromHtml(String.format(Elderoid.string(R.string.dialog_remove_contact_message), "<b>" + contacts.get(position).getName() + "</b>")),
                        Elderoid.string(R.string.dialog_remove),
                        Elderoid.string(R.string.dialog_cancel),
                        (d, w) -> {
                            if (deleteContact(contacts.get(position))) removeAt(position);
                        },
                        (d, w) -> {}
                );
                dialog.show(getSupportFragmentManager(), "Remove Contact Dialog");
            });
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }

        public void removeAt(int position) {
            contacts.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, contacts.size());
        }
    }
}