package com.zectan.soundroid.fragments;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.zectan.soundroid.AnimatedFragment;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.adapters.PlayingQueueAdapter;
import com.zectan.soundroid.objects.Animations;
import com.zectan.soundroid.objects.Functions;
import com.zectan.soundroid.objects.Playlist;
import com.zectan.soundroid.objects.Song;
import com.zectan.soundroid.viewmodels.PlayingViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("UseCompatLoadingForDrawables")
public class PlayingFragment extends AnimatedFragment {
    private MainActivity activity;
    private PlayingQueueAdapter playingQueueAdapter;

    private LinearProgressIndicator convertingProgress;
    private ImageView coverImage, shuffleImage, backImage, playPauseImage, playPauseMiniImage, nextImage, loopImage;
    private TextView titleText, artisteText, songTimeText, songLengthText, playlistName, errorMessage, retryMessage;
    private ProgressBar loadingBar;
    private SeekBar timeSeekbar;
    private MotionLayout parent;

    private PlayingViewModel playingVM;

    private boolean touchingSeekbar = false;
    private int finalTouch = 0;

    public PlayingFragment() {
        // Required empty public constructor
    }
    
    @Override
    public void onViewCreated(@NonNull @NotNull View view,
                              @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String transitionName = PlayingFragmentArgs.fromBundle(getArguments()).getTransitionName();
        ViewCompat.setTransitionName(view.findViewById(R.id.song_cover), transitionName);
    }
    
    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playing, container, false);
        activity = (MainActivity) getActivity();
        assert activity != null;
        
        // View Models
        playingVM = new ViewModelProvider(activity).get(PlayingViewModel.class);
        
        // Reference views
        convertingProgress = view.findViewById(R.id.converting_progress);
        parent = view.findViewById(R.id.playing_motion_layout);
        coverImage = view.findViewById(R.id.song_cover);
        shuffleImage = view.findViewById(R.id.song_shuffle);
        backImage = view.findViewById(R.id.song_back);
        playPauseImage = view.findViewById(R.id.song_play_pause);
        playPauseMiniImage = view.findViewById(R.id.song_play_pause_mini);
        nextImage = view.findViewById(R.id.song_next);
        loopImage = view.findViewById(R.id.song_loop);
        titleText = view.findViewById(R.id.song_title);
        artisteText = view.findViewById(R.id.song_artiste);
        songTimeText = view.findViewById(R.id.song_time);
        songLengthText = view.findViewById(R.id.song_length);
        playlistName = view.findViewById(R.id.playlist_name);
        errorMessage = view.findViewById(R.id.error_message);
        retryMessage = view.findViewById(R.id.retry_message);
        loadingBar = view.findViewById(R.id.song_loading);
        timeSeekbar = view.findViewById(R.id.song_seekbar);
        RecyclerView recyclerView = view.findViewById(R.id.playing_queue_recycler_view);
        
        // Recycler Views
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        playingQueueAdapter = new PlayingQueueAdapter(this::onSongSelected);
        recyclerView.setAdapter(playingQueueAdapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        
        // Live Observers
        playingVM.order.observe(activity, this::onOrderChange);
        playingVM.songDuration.observe(activity, this::onSongDurationChange);
        playingVM.jitteringState.observe(activity, this::onJitteringStateChange);
        playingVM.loadingState.observe(activity, this::onLoadingStateChange);
        playingVM.playingState.observe(activity, this::onPlayingStateChange);
        playingVM.convertingState.observe(activity, this::onConvertingStateChange);
        playingVM.playProgress.observe(activity, this::onPlayProgressChange);
        playingVM.playTime.observe(activity, this::onPlayTimeChange);
        playingVM.convertingError.observe(activity, this::onConvertingErrorChange);
        playingVM.convertingProgress.observe(activity, this::onConvertingProgressChange);
        parent.addTransitionListener(activity.getTransitionListener());
        
        enableControls();
        updateShuffleColor();
        updateLoopColor();
        activity.showNavigator();
        shuffleImage.setOnClickListener(this::shufflePlaylist);
        shuffleImage.setOnTouchListener(Animations::mediumSqueeze);
        backImage.setOnClickListener(this::backSong);
        backImage.setOnTouchListener(Animations::mediumSqueeze);
        nextImage.setOnClickListener(this::nextSong);
        nextImage.setOnTouchListener(Animations::mediumSqueeze);
        loopImage.setOnClickListener(this::loopPlaylist);
        loopImage.setOnTouchListener(Animations::mediumSqueeze);
        
        return view;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity.showNavigator();
    }
    
    private void onSongSelected(List<Song> displayed, int position) {
        Playlist queue = playingVM.queue.getValue();
        if (queue == null)
            return;
        
        playingVM.selectSong(queue, queue.getSongs().indexOf(displayed.get(position)));
    }
    
    public void backSong(View v) {
        Integer duration = playingVM.playTime.getValue();
        System.out.println(duration);
        if (duration == null)
            return;
        
        if (duration <= 2) {
            playingVM.playPreviousSong();
        } else {
            playingVM.seekTo(0);
            playingVM.initialisePlayer();
        }
    }

    public void nextSong(View v) {
        playingVM.playNextSong();
    }

    public void shufflePlaylist(View v) {
        playingVM.shufflingState = !playingVM.shufflingState;
        playingVM.shuffleOrSortOrder();
        updateShuffleColor();
    }

    public void loopPlaylist(View v) {
        playingVM.loopingState = !playingVM.loopingState;
        updateLoopColor();
    }

    //
    public void playPauseSong(View v) {
        Boolean playingState = playingVM.playingState.getValue();
        Boolean loadingState = playingVM.loadingState.getValue();
        if (loadingState != null && loadingState)
            return;
        if (playingState != null && playingState) {
            playingVM.stopPlaying();
        } else {
            try {
                playingVM.startPlaying();
            } catch (Exception ignored) {
            
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void enableControls() {
        playPauseImage.setOnClickListener(this::playPauseSong);
        playPauseImage.setOnTouchListener(Animations::smallSqueeze);
        playPauseMiniImage.setOnClickListener(this::playPauseSong);
        playPauseMiniImage.setOnTouchListener(Animations::mediumSqueeze);
        timeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    finalTouch = progress;

                    double percent = (double) progress / 1000;
                    double durationS = (double) playingVM.getDuration() / 1000;
                    int selectedTime = (int) (percent * durationS);
                    songTimeText.setText(Functions.formatDate(selectedTime));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                touchingSeekbar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                playingVM.seekTo(finalTouch);
                touchingSeekbar = false;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void disableControls() {
        View.OnClickListener click = c -> {
        };
        View.OnTouchListener touch = (v, event) -> false;
        playPauseImage.setOnClickListener(click);
        playPauseImage.setOnTouchListener(touch);
        playPauseMiniImage.setOnClickListener(click);
        playPauseMiniImage.setOnTouchListener(touch);
        timeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void updateShuffleColor() {
        shuffleImage.setColorFilter(
            ContextCompat.getColor(activity,
                playingVM.shufflingState ? R.color.theme_button_on : R.color.theme_button_off),
            android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    private void updateLoopColor() {
        loopImage.setColorFilter(
            ContextCompat.getColor(activity,
                playingVM.loopingState ? R.color.theme_button_on : R.color.theme_button_off),
            android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    private void onOrderChange(List<Integer> order) {
        Playlist queue = playingVM.queue.getValue();
        String colorHex;
    
        if (queue == null) {
            playingQueueAdapter.updateQueue(new ArrayList<>());
            playlistName.setText("-");
            coverImage.setImageDrawable(activity.getDrawable(R.drawable.playing_cover_default));
            titleText.setText("-");
            artisteText.setText("-");
            colorHex = "#7b828b";
        } else {
            playingQueueAdapter.updateQueue(Functions.formQueue(queue.getSongs(), order));
        
            Song song = queue.getSong(order.get(0));
            String title = song.getTitle();
            String artiste = song.getArtiste();
            String cover = song.getCover();
            colorHex = song.getColorHex();
        
            playlistName.setText(queue.getInfo().getName());
            titleText.setText(title);
            artisteText.setText(artiste);
            Glide
                .with(activity)
                .load(cover)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.playing_cover_default)
                .into(coverImage);
        }
    
        Drawable oldGD = parent.getBackground();
        int[] colors = {Color.parseColor(colorHex), activity.getColor(R.color.theme_playing_bottom)};
        GradientDrawable newGD = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
    
        Drawable[] layers = {oldGD, newGD};
        TransitionDrawable transition = new TransitionDrawable(layers);
        parent.setBackground(transition);
        transition.startTransition(500);
    }

    private void onSongDurationChange(int duration) {
        songLengthText.setText(Functions.formatDate(duration));
    }

    private void onJitteringStateChange(boolean jittering) {
        loadingBar.setAlpha(jittering ? 1 : 0);
        parent.requestLayout();
    }

    private void onLoadingStateChange(boolean loading) {
        loadingBar.setAlpha(loading ? 1 : 0);
        parent.requestLayout();
    }

    private void onPlayingStateChange(boolean playing) {
        playPauseImage.setImageDrawable(
            activity.getDrawable(playing ? R.drawable.controls_pause_filled : R.drawable.controls_play_filled));
        playPauseMiniImage
            .setImageDrawable(activity.getDrawable(playing ? R.drawable.controls_pause : R.drawable.controls_play));
    }

    private void onConvertingStateChange(boolean converting) {
        float alpha = convertingProgress.getAlpha();
        ValueAnimator alphaAnimation = ValueAnimator.ofFloat(alpha, converting ? 1 : 0).setDuration(500);
        alphaAnimation
            .addUpdateListener(animation -> convertingProgress.setAlpha((float) animation.getAnimatedValue()));
        alphaAnimation.start();
    }

    private void onPlayProgressChange(int time) {
        if (!touchingSeekbar) {
            timeSeekbar.setProgress(time);
        }
    }

    private void onPlayTimeChange(int time) {
        if (!touchingSeekbar) {
            songTimeText.setText(Functions.formatDate(time));
        }
    }

    private void onConvertingErrorChange(String message) {
        if (!message.isEmpty()) {
            errorMessage.setText(message);
            new Handler().postDelayed(() -> coverImage.setOnClickListener(__ -> {
                playingVM.convertingError.setValue("");
                playingVM.recursivelyRunPlaylist();
            }), 1000);
    
            ValueAnimator darkenAnimation = ValueAnimator
                .ofArgb(activity.getColor(R.color.white), activity.getColor(R.color.theme_3)).setDuration(1000);
            darkenAnimation.addUpdateListener(animation -> coverImage.setColorFilter((int) animation.getAnimatedValue(),
                android.graphics.PorterDuff.Mode.MULTIPLY));
            darkenAnimation.start();
    
            ValueAnimator messageAnimation = ValueAnimator.ofFloat(0f, 1f).setDuration(500);
            messageAnimation.addUpdateListener(animation -> {
                float alpha = (float) animation.getAnimatedValue();
                errorMessage.setAlpha(alpha);
                retryMessage.setAlpha(alpha);
            });
            messageAnimation.setStartDelay(500);
            messageAnimation.start();

            disableControls();
            playingVM.loadingState.setValue(false);
        } else {
            coverImage.setOnClickListener(__ -> {
            });
            coverImage.clearColorFilter();
            errorMessage.setAlpha(0f);
            retryMessage.setAlpha(0f);
    
            enableControls();
        }
    }
    
    private void onConvertingProgressChange(int progress) {
        convertingProgress.setProgressCompat(progress, true);
    }
}
