package com.alcachofra.elderoid.configuration;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.alcachofra.elderoid.AppListActivity;
import com.alcachofra.elderoid.Elderoid;
import com.alcachofra.elderoid.R;
import com.alcachofra.elderoid.utils.AppInfo;
import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.utils.dialog.DialogAppFilter;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.pm.ApplicationInfo.CATEGORY_GAME;
import static android.content.pm.ApplicationInfo.CATEGORY_SOCIAL;

public class ChooseAppsConfigurationActivity extends ElderoidActivity {

    Netie netie;
    private final Map<String, String> apps = new HashMap<>(); // <App name, App Package Name>

    private boolean socialState = true;
    private boolean gamesState = true;
    private boolean otherState = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_apps_configuration);



        AppCompatButton proceed = findViewById(R.id.proceed);
        RecyclerView recycler = findViewById(R.id.app_list);
        AppCompatImageButton gears = findViewById(R.id.gears);
        AppCompatImageView loading = findViewById(R.id.loading);
        AppCompatImageView new_games = findViewById(R.id.new_games);

        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.loading);
        loading.startAnimation(rotate);

        gears.setVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? View.VISIBLE : View.GONE);

        netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_BACK_BUTTON,
                Collections.singletonList(
                        new Cue(Elderoid.string(R.string.wait), R.drawable.netie_confused)
                ),
                false,
                v-> {
                    if (getIntent().getExtras() != null && getIntent().getExtras().getString(Elderoid.COMING_FROM, "").equals("APP LIST"))
                        startActivity(new Intent(getApplicationContext(), AppListActivity.class));
                    else startActivity(new Intent(getApplicationContext(), AppsConfigurationActivity.class));
                }
        );

        Thread onAllApps = new Thread() {
            public void run() {
                List<AppInfo> saved_apps = getApps(Elderoid.loadAppPackagesFromSimplePrefs());
                List<AppInfo> app_list = getInstalledApps();
                for (AppInfo app : saved_apps) {
                    if (app_list.contains(app)) checkApp(app_list.get(app_list.indexOf(app)));
                }

                runOnUiThread(() -> {

                    netie.resetCuePool(
                            Arrays.asList(
                                    new Cue(Elderoid.string(R.string.config_choose_apps_1), R.drawable.netie),
                                    new Cue(Elderoid.string(R.string.config_choose_apps_2), R.drawable.netie),
                                    new Cue(Elderoid.string(R.string.config_choose_apps_3), R.drawable.netie_happy)
                                            .setOption1(Elderoid.string(R.string.yes), v -> {
                                                Intent intent = new Intent(getApplicationContext(), GameSuggestionsActivity.class);
                                                if (getIntent().getExtras() != null && getIntent().getExtras().getString(Elderoid.COMING_FROM, "").equals("APP LIST"))
                                                    intent.putExtra(Elderoid.COMING_FROM, "APP LIST");
                                                startActivity(intent);
                                            })
                            ),
                            false
                    );

                    loading.clearAnimation();
                    loading.setVisibility(View.GONE);

                    recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    recycler.setAdapter(new ChooseAppsConfigurationActivity.AppListAdapter(app_list));

                    gears.setOnClickListener(v -> {
                        // Filter apps:
                        DialogAppFilter dialog = new DialogAppFilter(Elderoid.string(R.string.dialog_filter_apps),
                                (String) null,
                                Elderoid.string(R.string.dialog_ready),
                                Elderoid.string(R.string.dialog_cancel),
                                c -> ((AppListAdapter) recycler.getAdapter()).filterAppList(socialState = c.get(0), gamesState = c.get(1), otherState = c.get(2)),
                                (d, w) -> {},
                                socialState, gamesState, otherState);
                        dialog.setImage(R.drawable.ic_gears);
                        dialog.show(getSupportFragmentManager(), "Filter Apps Dialog");
                    });

                    new_games.setOnClickListener(v -> {
                        Intent intent = new Intent(getApplicationContext(), GameSuggestionsActivity.class);
                        if (getIntent().getExtras() != null && getIntent().getExtras().getString(Elderoid.COMING_FROM, "").equals("APP LIST"))
                            intent.putExtra(Elderoid.COMING_FROM, "APP LIST");
                        startActivity(intent);
                    });

                    proceed.setOnClickListener(v -> {
                        Elderoid.saveAppPackagesToSimplePrefs(apps.values());
                        // Next Activity:
                        if (getIntent().getExtras() != null && getIntent().getExtras().getString(Elderoid.COMING_FROM, "").equals("APP LIST")) {
                            startActivity(new Intent(getApplicationContext(), AppListActivity.class));
                        }
                        else startActivity(new Intent(getApplicationContext(), WeatherConfigurationActivity.class));
                    });
                });
            }
        };
        onAllApps.start();
    }

    public List<AppInfo> getApps(List<String> app_packages) {
        try {
            PackageManager package_manager = getPackageManager();
            List<AppInfo> app_list = new ArrayList<>();

            for (String app_package : app_packages) {
                ApplicationInfo ai = package_manager.getApplicationInfo(app_package, 0);
                AppInfo app = new AppInfo();
                app.setIcon(package_manager.getApplicationIcon(app_package));
                app.setName((String) package_manager.getApplicationLabel(ai));
                app.setPackageName(app_package);
                app_list.add(app);
            }
            Collections.sort(app_list);
            return app_list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<AppInfo> getInstalledApps() {
        try {
            PackageManager package_manager = getPackageManager();
            List<AppInfo> app_list = new ArrayList<>();
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveInfoList = package_manager.queryIntentActivities(intent, 0);

            for (ResolveInfo resolveInfo : resolveInfoList) {
                String package_name = resolveInfo.activityInfo.packageName;
                if (package_name.equals(Elderoid.getInstance().getPackageName())) continue;
                AppInfo app = new AppInfo();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    app.setCategory(resolveInfo.activityInfo.applicationInfo.category);
                }
                app.setIcon(resolveInfo.activityInfo.loadIcon(package_manager));
                app.setName(resolveInfo.loadLabel(package_manager).toString());
                app.setPackageName(package_name);
                app_list.add(app);
            }
            Collections.sort(app_list);
            return app_list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {
        private final List<AppInfo> appList = new ArrayList<>();
        private final List<AppInfo> appListAll;

        public AppListAdapter(List<AppInfo> appList) {
            super();
            this.appListAll = appList;
            filterAppList(socialState, gamesState, otherState);
        }

        class AppViewHolder extends RecyclerView.ViewHolder {
            private final AppCompatImageView app_icon;
            private final AppCompatCheckBox app_checkbox;
            private final AppCompatTextView app_name;

            public AppViewHolder(ViewGroup container) {
                super(LayoutInflater.from(ChooseAppsConfigurationActivity.this).inflate(R.layout.row_app_checkbox, container, false));
                app_icon = itemView.findViewById(R.id.app_icon);
                app_name = itemView.findViewById(R.id.app_name);
                app_checkbox = itemView.findViewById(R.id.app_checkbox);
            }
        }

        @NonNull
        @Override
        public AppListAdapter.AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AppListAdapter.AppViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull AppListAdapter.AppViewHolder holder, int position) {
            AppInfo app = appList.get(position);

            holder.app_icon.setBackgroundDrawable(app.getIcon());
            holder.app_name.setText(app.getName());
            holder.app_checkbox.setChecked(app.isSelected());

            holder.app_checkbox.setOnClickListener(v -> {
                if (holder.app_checkbox.isChecked()) checkApp(app);
                else uncheckApp(app);
            });
        }

        @Override
        public int getItemCount() {
            return this.appList.size();
        }

        public void filterAppList(boolean social, boolean games, boolean other) {
            this.appList.clear();
            for (AppInfo app : appListAll) {
                if (social && app.getCategory() == CATEGORY_SOCIAL) {
                    this.appList.add(app);
                }
                else if (games && app.getCategory() == CATEGORY_GAME) {
                    this.appList.add(app);
                }
                else if (other) {
                    this.appList.add(app);
                }
            }
            notifyDataSetChanged();
        }
    }

    private void checkApp(AppInfo app) {
        app.setSelected(true);
        apps.put(app.getName(), app.getPackageName());
    }

    private void uncheckApp(AppInfo app) {
        app.setSelected(false);
        apps.remove(app.getName());
    }
}
















 