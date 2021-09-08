package com.alcachofra.elderoid;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.alcachofra.elderoid.utils.CallInfo;
import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

public class MissedCallsActivity extends ElderoidActivity {

    Netie netie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missed_calls);



        RecyclerView recycler = findViewById(R.id.missed_call_list);

        Set<CallInfo> missedCalls = Elderoid.getMissedCalls();

        if (missedCalls.size() > 0) netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_HOME_BUTTON,
                Arrays.asList(
                        new Cue(Elderoid.string(R.string.missed_calls_1), R.drawable.netie),
                        new Cue(Elderoid.string(R.string.missed_calls_2), R.drawable.netie)
                                .setOption1(Elderoid.string(R.string.see), v -> startActivity(new Intent(getApplicationContext(), CallLogActivity.class)))
                ),
                true
        );
        else netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_HOME_BUTTON,
                Arrays.asList(
                        new Cue(Elderoid.string(R.string.no_missed_calls), R.drawable.netie_happy)
                ),
                false
        );

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(new MissedCallsActivity.MissedCallListAdapter(missedCalls));
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

    /*
    public void DeleteCallLogByNumber(String number) {
        String queryString = "NUMBER='" + number + "'";
        this.getContentResolver().delete(CallLog.Calls.CONTENT_URI, queryString, null);
    }
    */

    class MissedCallListAdapter extends RecyclerView.Adapter<MissedCallsActivity.MissedCallViewHolder> {
        private final CallInfo[] missed_calls_array;

        public MissedCallListAdapter(Set<CallInfo> missed_calls_list) {
            super();
            this.missed_calls_array = missed_calls_list.toArray(new CallInfo[0]);
            Arrays.sort(this.missed_calls_array, Comparator.reverseOrder());
        }

        @NonNull
        @Override
        public MissedCallsActivity.MissedCallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MissedCallsActivity.MissedCallViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull MissedCallsActivity.MissedCallViewHolder holder, int position) {
            holder.bind(this.missed_calls_array[position]);
        }

        @Override
        public int getItemCount() {
            return this.missed_calls_array.length;
        }
    }

    class MissedCallViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatTextView contact_name;
        private final AppCompatTextView contact_telephone;
        private final AppCompatImageButton call;
        public MissedCallViewHolder(ViewGroup container) {
            super(LayoutInflater.from(MissedCallsActivity.this).inflate(R.layout.row_contact, container, false));
            contact_name = itemView.findViewById(R.id.contact_name);
            contact_telephone = itemView.findViewById(R.id.contact_telephone);
            call = itemView.findViewById(R.id.call);
        }
        public void bind (CallInfo missedCall) {
            contact_name.setText((missedCall.getName() != null && missedCall.getName().length() > 0) ? missedCall.getName() : missedCall.getNumber());
            contact_telephone.setText(missedCall.getDateTime());
            call.setOnClickListener(v -> startCall(missedCall.getNumber()));
        }
    }
}