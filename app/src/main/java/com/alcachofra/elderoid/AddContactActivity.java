package com.alcachofra.elderoid;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import android.content.ContentProviderOperation;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;

import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;

import java.util.ArrayList;
import java.util.Arrays;

public class AddContactActivity extends ElderoidActivity {

    Netie netie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);



        AppCompatButton add = findViewById(R.id.add);
        AppCompatEditText name = findViewById(R.id.name);
        AppCompatEditText phone = findViewById(R.id.phone);

        netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_HOME_BUTTON,
                Arrays.asList(
                        new Cue(Elderoid.string(R.string.add_contact_1), R.drawable.netie),
                        new Cue(Elderoid.string(R.string.add_contact_2), R.drawable.netie_happy)
                ),
                false
        );

        add.setOnClickListener(v -> {
            Editable editable_name = name.getText();
            Editable editable_phone = phone.getText();

            if (editable_name != null && editable_phone != null) {
                String contact_name = editable_name.toString().trim();
                String contact_phone = editable_phone.toString().trim();
                if (contact_name.length() >= 3) {
                    if (PhoneNumberUtils.isGlobalPhoneNumber(contact_phone)) saveContact(contact_name, contact_phone);
                    else netie.setBalloon(Elderoid.string(R.string.number_not_valid))
                            .setExpression(R.drawable.netie_confused);
                }
                else netie.setBalloon(Elderoid.string(R.string.name_not_valid))
                        .setExpression(R.drawable.netie_confused);
            }
            else netie.setBalloon(Elderoid.string(R.string.add_contact_error))
                    .setExpression(R.drawable.netie_concerned);
        });
    }

    private void saveContact(String contact_name, String contact_phone) {
        ArrayList<ContentProviderOperation> op_list = new ArrayList<>();
        op_list.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, contact_name)
                .build());

        op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact_phone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
                .build());

        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, op_list);
            Intent intent = new Intent(getApplicationContext(), ContactListActivity.class);
            intent.putExtra(Elderoid.ADDED_CONTACT, true);
            startActivity(intent);
        } catch(Exception e) {
            e.printStackTrace();
            netie.setBalloon(Elderoid.string(R.string.add_contact_error))
                    .setExpression(R.drawable.netie_concerned);
        }
    }
}