package com.alcachofra.elderoid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;
import com.alcachofra.elderoid.utils.SimplePrefs;

import java.util.Arrays;

public class DialerActivity extends ElderoidActivity {

    Netie netie;
    private AppCompatTextView phone_number;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialer);



        phone_number = findViewById(R.id.phone_number);
        AppCompatImageButton call = findViewById(R.id.b_call);
        AppCompatImageButton delete = findViewById(R.id.b_delete);

        Cue cue = new Cue(Elderoid.string(R.string.dial_number) +
                (
                        SimplePrefs.getBoolean(Elderoid.FUNC_CONTACTS) ?
                                " " + Elderoid.string(R.string.phone_contact) :
                                ""
                ), R.drawable.netie
        );

        if (SimplePrefs.getBoolean(Elderoid.FUNC_CONTACTS))
            cue.setOption1(Elderoid.string(R.string.call), v -> startActivity(new Intent(getApplicationContext(), ContactListActivity.class)));

        netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_HOME_BUTTON,
                Arrays.asList(
                        cue
                ),
                false
        );

        call.setOnClickListener(v -> startCall(phone_number.getText().toString().trim()));

        delete.setOnClickListener(v -> {
            String number = phone_number.getText().toString();
            phone_number.setText(number.substring(0, number.length() > 0 ? number.length() - 1 : number.length()));
        });

        findViewById(R.id.b_0).setOnClickListener(v -> writeNumber("0"));

        findViewById(R.id.b_1).setOnClickListener(v -> writeNumber("1"));

        findViewById(R.id.b_2).setOnClickListener(v -> writeNumber("2"));

        findViewById(R.id.b_3).setOnClickListener(v -> writeNumber("3"));

        findViewById(R.id.b_4).setOnClickListener(v -> writeNumber("4"));

        findViewById(R.id.b_5).setOnClickListener(v -> writeNumber("5"));

        findViewById(R.id.b_6).setOnClickListener(v -> writeNumber("6"));

        findViewById(R.id.b_7).setOnClickListener(v -> writeNumber("7"));

        findViewById(R.id.b_8).setOnClickListener(v -> writeNumber("8"));

        findViewById(R.id.b_9).setOnClickListener(v -> writeNumber("9"));
    }

    private void writeNumber(String n) {
        if (phone_number.getText().toString().trim().length() < 12) phone_number.append(n);
    }

    private void startCall(String number) {
        if (PhoneNumberUtils.isGlobalPhoneNumber(number)) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            startActivity(intent);
        }
        else netie.setBalloon(Elderoid.string(R.string.number_not_valid))
                .setExpression(R.drawable.netie_concerned);
    }
}
