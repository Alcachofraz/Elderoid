package com.alcachofra.elderoid.configuration;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.alcachofra.elderoid.Elderoid;
import com.alcachofra.elderoid.R;
import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.utils.GameSuggestion;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GameSuggestionsActivity extends ElderoidActivity {

    Netie netie;
    RecyclerView recycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_suggestions);

        recycler = findViewById(R.id.app_list);

        netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_BACK_BUTTON,
                Collections.singletonList(
                        new Cue(Elderoid.string(R.string.game_suggestions), R.drawable.netie)
                ),
                false,
                v-> {
                    Intent intent = new Intent(getApplicationContext(), ChooseAppsConfigurationActivity.class);
                    if (getIntent().getExtras() != null && getIntent().getExtras().getString(Elderoid.COMING_FROM, "").equals("APP LIST"))
                        intent.putExtra(Elderoid.COMING_FROM, "APP LIST");
                    startActivity(intent);
                }
        );

        List<GameSuggestion> gameSuggestions = getGamesSuggestion();

        gameSuggestions.add(new GameSuggestion(
                Elderoid.string(R.string.click_to_see_more),
                "https://play.google.com/store/search?q=games%20for%20elderly%20people%20free&c=apps",
                null,
                Elderoid.string(R.string.click_see_more)
        ));

        recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recycler.setAdapter(new GameSuggestionsActivity.GameSuggestionsListAdapter(gameSuggestions));
    }

    private List<GameSuggestion> getGamesSuggestion() {
        return new ArrayList<>(Arrays.asList(
                new GameSuggestion(Elderoid.string(R.string.bubble_shooter),
                        "http://play.google.com/store/apps/details?id=linkdesks.pop.bubblegames.bubbleshooter",
                        "https://play-lh.googleusercontent.com/cWXDy6pzL3Hl71K1GXP3dj1otlVm7Zx0xmHHTzvXv-hUwDcKHNmrGgqllzGV7SCNvXI=s180-rw",
                        Elderoid.string(R.string.bubble_shooter_description)
                ),
                new GameSuggestion(Elderoid.string(R.string.word_connect),
                        "https://play.google.com/store/apps/details?id=com.wordgame.words.connect",
                        "https://play-lh.googleusercontent.com/x32_5ZIVnk7gbk0kdpTJ3Y_IcEUbSXzPFfEzHKWoHNtoF3Vam2icDeg6Sqs1nRuTDg7T=s180-rw",
                        Elderoid.string(R.string.word_connect_description)
                ),
                new GameSuggestion(Elderoid.string(R.string.angry_birds),
                        "https://play.google.com/store/apps/details?id=com.rovio.baba",
                        "https://play-lh.googleusercontent.com/sCTtUn9FTBYj2HW_i1Y-wN9j9Tk1uui7SsvGDxaeXGWUgoA_k0pQ-iDGnCADnt22kyI=s180-rw",
                        Elderoid.string(R.string.angry_birds_description)
                ),
                new GameSuggestion(Elderoid.string(R.string.block_puzzle),
                        "https://play.google.com/store/apps/details?id=game.puzzle.blockpuzzle",
                        "https://play-lh.googleusercontent.com/suwFZ5vpDsLh6MqSyqfn05gYjDsf9_nvbrNFU7r9bSeIW1xrH3zm7reu_WBKA2bJEz8=s180-rw",
                        Elderoid.string(R.string.block_puzzle_description)
                ),
                new GameSuggestion(Elderoid.string(R.string.words_with_friends),
                        "https://play.google.com/store/apps/details?id=com.zynga.words",
                        "https://play-lh.googleusercontent.com/1R0fuU3KnmUV-8ySeHkKsb4NOC9AL3VSbAVlMnKW8OLfLOYX3uNC6f4TQMVYfM3ZOJk=s180-rw",
                        Elderoid.string(R.string.words_with_friends_description)
                )
        ));
    }

    public static boolean isResolveActivity(Intent intent) {
        return Elderoid.getInstance().getPackageManager().resolveActivity(intent, PackageManager.GET_RESOLVED_FILTER) != null;
    }

    class GameSuggestionsListAdapter extends RecyclerView.Adapter<GameSuggestionsActivity.GameSuggestionsListAdapter.GameSuggestionsViewHolder> {
        private final List<GameSuggestion> gameSuggestions;

        class GameSuggestionsViewHolder extends RecyclerView.ViewHolder {
            final View game_item;
            final TextView game_name;
            final AppCompatImageView game_icon;
            final AppCompatImageView loading;

            public GameSuggestionsViewHolder(ViewGroup container) {
                super(LayoutInflater.from(GameSuggestionsActivity.this).inflate(R.layout.row_game_suggestion, container, false));
                game_item = itemView.findViewById(R.id.game_item);
                game_name = itemView.findViewById(R.id.game_name);
                game_icon = itemView.findViewById(R.id.game_icon);
                loading = itemView.findViewById(R.id.loading);
            }
        }

        public GameSuggestionsListAdapter(List<GameSuggestion> GameSuggestions) {
            super();
            this.gameSuggestions = new ArrayList<>(GameSuggestions);
        }

        @NonNull
        @Override
        public GameSuggestionsActivity.GameSuggestionsListAdapter.GameSuggestionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new GameSuggestionsActivity.GameSuggestionsListAdapter.GameSuggestionsViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull GameSuggestionsActivity.GameSuggestionsListAdapter.GameSuggestionsViewHolder holder, int position) {
            if (gameSuggestions.get(position).getImagePath() == null) {
                holder.game_icon.setVisibility(View.GONE);
                holder.loading.setVisibility(View.GONE);
                holder.game_name.setText(R.string.see_more);
                holder.game_name.setTypeface(Typeface.DEFAULT_BOLD);
                holder.game_name.setGravity(Gravity.CENTER);

                holder.game_item.getLayoutParams().height = (int) ((double) holder.game_item.getLayoutParams().height * 0.57);
                holder.game_item.requestLayout();

                holder.game_item.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(gameSuggestions.get(position).getPath()));
                    intent.setPackage("com.android.vending");
                    if (isResolveActivity(intent))
                        startActivity(intent);
                    else netie.setBalloon(R.string.google_play_error)
                            .setExpression(R.drawable.netie_confused);
                });
            }
            else {
                // Start loading image animation:
                Animation rotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.loading);
                holder.loading.startAnimation(rotate);

                // Set name:
                holder.game_name.setText(gameSuggestions.get(position).getName());

                // Set item listener:
                holder.game_item.setOnClickListener(v1 -> netie.setBalloon(gameSuggestions.get(position).getDescription() + " " + Elderoid.string(R.string.do_you_want_to_install))
                        .setExpression(R.drawable.netie_happy)
                        .setOption1(R.string.install, v2 -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(gameSuggestions.get(position).getPath()));
                            intent.setPackage("com.android.vending");
                            if (isResolveActivity(intent))
                                startActivity(intent);
                            else netie.setBalloon(R.string.google_play_error)
                                    .setExpression(R.drawable.netie_confused);
                        })
                );

                RequestOptions requestOptions = new RequestOptions()
                        .transform(new CenterCrop(), new RoundedCorners(16))
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // It will cache image after loaded for first time
                        .override(holder.game_icon.getWidth(), holder.game_icon.getHeight()); // Overrides size of downloaded image and converts it's bitmaps to desired image size;

                Glide.with(getApplicationContext())
                        .load(gameSuggestions.get(position).getImagePath())
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                holder.loading.clearAnimation();
                                holder.loading.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                holder.loading.clearAnimation();
                                holder.loading.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .apply(requestOptions)
                        .into(holder.game_icon);
            }
        }

        @Override
        public int getItemCount() {
            return gameSuggestions.size();
        }

        public void removeAt(int position) {
            gameSuggestions.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, gameSuggestions.size());
        }
    }
}