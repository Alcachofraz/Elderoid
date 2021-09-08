package com.alcachofra.elderoid;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.alcachofra.elderoid.configuration.ChooseAppsConfigurationActivity;
import com.alcachofra.elderoid.utils.AppInfo;
import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AppListActivity extends ElderoidActivity {

    Netie netie;
    RecyclerView recycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);



        recycler = findViewById(R.id.app_list);
        AppCompatImageButton gears = findViewById(R.id.gears);
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

        Thread onApps = new Thread() {
            public void run() {
                List<AppInfo> app_list = getApps(Elderoid.loadAppPackagesFromSimplePrefs());

                runOnUiThread(() -> {
                    if (app_list.size() > 0) netie.resetCuePool(
                            Arrays.asList(
                                    new Cue(Elderoid.string(R.string.choose_app_1), R.drawable.netie),
                                    new Cue(Elderoid.string(R.string.choose_app_2), R.drawable.netie_happy),
                                    new Cue(Elderoid.string(R.string.choose_app_3), R.drawable.netie)
                                            .setOption1(Elderoid.string(R.string.yes), v -> {
                                                Intent intent = new Intent(getApplicationContext(), ChooseAppsConfigurationActivity.class);
                                                intent.putExtra(Elderoid.COMING_FROM, "APP LIST");
                                                startActivity(intent);
                                            })
                            ),
                            true
                    );
                    else netie.resetCuePool(
                            Arrays.asList(
                                    new Cue(Elderoid.string(R.string.no_apps_1), R.drawable.netie_concerned)
                                            .setOption1(Elderoid.string(R.string.yes), v -> startActivity(new Intent(getApplicationContext(), ChooseAppsConfigurationActivity.class))),
                                    new Cue(Elderoid.string(R.string.no_apps_2), R.drawable.netie)
                            ),
                            false
                    );

                    loading.clearAnimation();
                    loading.setVisibility(View.GONE);

                    recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    recycler.setAdapter(new AppListAdapter(app_list));

                    gears.setOnClickListener(v -> {
                        Intent intent = new Intent(getApplicationContext(), ChooseAppsConfigurationActivity.class);
                        intent.putExtra(Elderoid.COMING_FROM, "APP LIST");
                        startActivity(intent);
                    });
                });
            }
        };
        onApps.start();
    }

    public List<AppInfo> getApps(List<String> app_packages) {
        PackageManager package_manager = getPackageManager();
        List<AppInfo> app_list = new ArrayList<>();

        for (String app_package : app_packages) {
            ApplicationInfo ai = null;
            AppInfo app = new AppInfo();
            try {
                ai = package_manager.getApplicationInfo(app_package, 0);
                app.setIcon(package_manager.getApplicationIcon(app_package));
            } catch (PackageManager.NameNotFoundException e) { // App package not found:
                Elderoid.removeAppPackageFromSimplePrefs(app_package);
                continue;
            }
            app.setName((String) package_manager.getApplicationLabel(ai));
            app.setPackageName(app_package);
            app_list.add(app);
        }
        Collections.sort(app_list);
        return app_list;
    }

    private boolean isPackageInstalled(String packageName, Context context) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {
        private final List<AppInfo> app_list;

        public AppListAdapter(List<AppInfo> app_list) {
            super();
            this.app_list = app_list;
        }

        class AppViewHolder extends RecyclerView.ViewHolder {
            final AppCompatImageView app_icon;
            final AppCompatTextView app_name;
            final AppCompatImageButton delete;
            final View app_item;

            public AppViewHolder (ViewGroup container) {
                super(LayoutInflater.from(AppListActivity.this).inflate(R.layout.row_app, container, false));
                app_icon = itemView.findViewById(R.id.app_icon);
                app_name = itemView.findViewById(R.id.app_name);
                app_item = itemView.findViewById(R.id.app_item);
                delete = itemView.findViewById(R.id.delete);
            }
        }

        @NonNull
        @Override
        public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AppViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
            holder.app_icon.setBackgroundDrawable(app_list.get(position).getIcon());
            holder.app_name.setText(app_list.get(position).getName());
            holder.delete.setOnClickListener(v -> {
                // Remove app from RecyclerView and SimplePrefs:
                Elderoid.removeAppPackageFromSimplePrefs(app_list.get(position).getPackageName());
                removeAt(position);
            });
            holder.app_item.setOnClickListener(v -> {
                if (isPackageInstalled(app_list.get(position).getPackageName(), getApplicationContext())) { // If app is installed:
                    // Launch app:
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(app_list.get(position).getPackageName());
                    startActivity(launchIntent);
                }
                else { // If app is not installed:
                    // Remove app from RecyclerView and SimplePrefs:
                    Elderoid.removeAppPackageFromSimplePrefs(app_list.get(position).getPackageName());
                    removeAt(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return this.app_list.size();
        }

        public void removeAt(int position) {
            app_list.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, app_list.size());
        }
    }
}