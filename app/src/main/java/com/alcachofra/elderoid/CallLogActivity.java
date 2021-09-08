package com.alcachofra.elderoid;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alcachofra.elderoid.utils.CallInfo;
import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

public class CallLogActivity extends ElderoidActivity {

    Netie netie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_log);



        RecyclerView recycler = findViewById(R.id.missed_call_list);

        Set<CallInfo> contact_list = Elderoid.getCalls();

        if (contact_list.size() > 0) netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_HOME_BUTTON,
                Arrays.asList(
                        new Cue(Elderoid.string(R.string.call_log_1), R.drawable.netie),
                        new Cue(Elderoid.string(R.string.call_log_2), R.drawable.netie)
                                .setOption1(Elderoid.string(R.string.see), v -> startActivity(new Intent(getApplicationContext(), MissedCallsActivity.class)))
                ),
                true
        );
        else netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_HOME_BUTTON,
                Arrays.asList(
                        new Cue(Elderoid.string(R.string.no_call_log), R.drawable.netie_confused)
                                .setOption1(Elderoid.string(R.string.yes), v -> startActivity(new Intent(getApplicationContext(), DialerActivity.class)))
                ),
                false
        );

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(new CallLogActivity.CallListAdapter(contact_list));
    }

    class CallListAdapter extends RecyclerView.Adapter<CallLogActivity.CallViewHolder> {
        private final CallInfo[] calls_array;

        public CallListAdapter(Set<CallInfo> calls_list) {
            super();
            this.calls_array = calls_list.toArray(new CallInfo[0]);
            Arrays.sort(this.calls_array, Comparator.reverseOrder());
        }

        @NonNull
        @Override
        public CallLogActivity.CallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CallLogActivity.CallViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull CallLogActivity.CallViewHolder holder, int position) {
            holder.bind(this.calls_array[position]);
        }

        @Override
        public int getItemCount() {
            return this.calls_array.length;
        }
    }

    class CallViewHolder extends RecyclerView.ViewHolder {
        private final View call_item;
        private final AppCompatTextView call_name;
        private final AppCompatTextView call_date;
        private final AppCompatImageView call_type;
        private String call_format;
        private int drawable;
        public CallViewHolder(ViewGroup container) {
            super(LayoutInflater.from(CallLogActivity.this).inflate(R.layout.row_call, container, false));
            call_item = itemView.findViewById(R.id.call_item);
            call_name = itemView.findViewById(R.id.call_name);
            call_date = itemView.findViewById(R.id.call_date);
            call_type = itemView.findViewById(R.id.call_type);
        }
        public void bind (CallInfo callInfo) {
            call_name.setText((callInfo.getName() != null && callInfo.getName().length() > 0)  ? callInfo.getName() : callInfo.getNumber());
            call_date.setText(callInfo.getDateTime());
            switch(callInfo.getCallType()) {
                case OUTGOING:
                    call_type.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_call_made, getTheme()));
                    call_format = Elderoid.string(R.string.format_outgoing_call);
                    drawable = R.drawable.netie;
                    break;
                case INCOMING:
                    call_type.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_call_received, getTheme()));
                    call_format = Elderoid.string(R.string.format_incoming_call);
                    drawable = R.drawable.netie;
                    break;
                default:
                    call_type.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_call_missed, getTheme()));
                    call_format = Elderoid.string(R.string.format_missed_call);
                    drawable = R.drawable.netie_confused;
                    break;
            }
            call_item.setOnClickListener(
                    v -> netie.setBalloon(
                            String.format(
                                    call_format,
                                    ((callInfo.getName() != null && callInfo.getName().length() > 0) ? callInfo.getName() : callInfo.getNumber()),
                                    callInfo.getDate(),
                                    callInfo.getTime(),
                                    callInfo.getDuration()
                            )
                    ).setExpression(drawable)
            );
        }
    }
}