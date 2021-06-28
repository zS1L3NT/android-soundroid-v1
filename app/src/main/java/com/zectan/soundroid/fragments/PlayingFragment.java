package com.zectan.soundroid.fragments;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView;
import com.zectan.soundroid.R;
import com.zectan.soundroid.adapters.QueueAdapter;
import com.zectan.soundroid.classes.Fragment;
import com.zectan.soundroid.databinding.FragmentPlayingBinding;
import com.zectan.soundroid.models.Playlist;
import com.zectan.soundroid.models.Song;
import com.zectan.soundroid.utils.Animations;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@SuppressLint("UseCompatLoadingForDrawables")
public class PlayingFragment extends Fragment<FragmentPlayingBinding> {
    private static final String TAG = "(SounDroid) PlayingFragment";
    private final QueueAdapter.Callback callback = new QueueAdapter.Callback() {
        @Override
        public void onSongSelected(Song song) {
            Playlist queue = playingVM.queue.getValue();

            playingVM.selectSong(queue, song.getId());
        }

        @Override
        public void onMove(int oldPosition, int newPosition) {
            playingVM.onMoveSong(oldPosition + 1, newPosition + 1);
        }

        @Override
        public void onRemove(String songId) {
            playingVM.onRemoveSong(songId);
        }
    };
    private QueueAdapter mQueueAdapter;

    @Override
    public void onViewCreated(@NonNull @NotNull View view,
                              @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String transitionName = PlayingFragmentArgs.fromBundle(getArguments()).getTransitionName();
        ViewCompat.setTransitionName(view.findViewById(R.id.cover_image), transitionName);
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setSharedElementEnterTransition(inflater.inflateTransition(R.transition.shared_image));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        B = FragmentPlayingBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // Recycler Views
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        mQueueAdapter = new QueueAdapter(callback);
        B.recyclerView.setAdapter(mQueueAdapter);
        B.recyclerView.setLayoutManager(layoutManager);
        B.recyclerView.setOrientation(DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING);
        B.recyclerView.setReduceItemAlphaOnSwiping(true);

        // Live Observers
        playingVM.currentSong.observe(activity, this::onCurrentSongChange);
        playingVM.isBuffering.observe(activity, this::onIsBufferingChange);
        playingVM.isPlaying.observe(activity, this::onIsPlayingChange);
        playingVM.isShuffling.observe(activity, this::onIsShufflingChange);
        playingVM.isLooping.observe(activity, this::onIsLoopingChange);
        playingVM.error.observe(activity, this::onErrorChange);

        B.playPauseImage.setOnClickListener(this::playPauseSong);
        B.playPauseImage.setOnTouchListener(Animations::animationSmallSqueeze);
        B.playPauseMiniImage.setOnClickListener(this::playPauseSong);
        B.playPauseMiniImage.setOnTouchListener(Animations::animationMediumSqueeze);
        B.shuffleImage.setOnClickListener(__ -> playingVM.toggleShuffle(mQueueAdapter));
        B.shuffleImage.setOnTouchListener(Animations::animationMediumSqueeze);
        B.backImage.setOnClickListener(__ -> playingVM.playPreviousSong());
        B.backImage.setOnTouchListener(Animations::animationMediumSqueeze);
        B.nextImage.setOnClickListener(__ -> playingVM.playNextSong());
        B.nextImage.setOnTouchListener(Animations::animationMediumSqueeze);
        B.loopImage.setOnClickListener(__ -> playingVM.toggleLoop(mQueueAdapter));
        B.loopImage.setOnTouchListener(Animations::animationMediumSqueeze);

        B.playingSeekbar.setPlayer(playingVM.getPlayer());

        return B.getRoot();
    }

    public void playPauseSong(View v) {
        if (playingVM.isBuffering.getValue()) return;
        if (playingVM.isPlaying.getValue()) {
            playingVM.pause();
        } else {
            playingVM.play();
        }
    }

    private void onCurrentSongChange(Song song) {
        String colorHex;

        if (song == null) {
            mQueueAdapter.updateQueue(new ArrayList<>());
            B.playlistNameText.setText("-");
            B.coverImage.setImageDrawable(activity.getDrawable(R.drawable.playing_cover_default));
            B.titleText.setText("-");
            B.descriptionText.setText("-");
            colorHex = "#7b828b";
        } else {
            mQueueAdapter.updateQueue(playingVM.getItemsInQueue());

            String title = song.getTitle();
            String artiste = song.getArtiste();
            String cover = song.getCover();
            colorHex = song.getColorHex();

            B.playlistNameText.setText(playingVM.queue.getValue().getInfo().getName());
            B.titleText.setText(title);
            B.descriptionText.setText(artiste);
            Glide
                .with(activity)
                .load(cover)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.playing_cover_default)
                .centerCrop()
                .into(B.coverImage);
        }

        Drawable oldGD = B.parent.getBackground();
        int[] colors = {Color.parseColor(colorHex), activity.getAttributeResource(R.attr.colorSecondary)};
        GradientDrawable newGD = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);

        Drawable[] layers = {oldGD, newGD};
        TransitionDrawable transition = new TransitionDrawable(layers);
        B.parent.setBackground(transition);
        transition.startTransition(500);
    }

    private void onIsPlayingChange(boolean isPlaying) {
        B.playPauseImage.setImageDrawable(
            activity.getDrawable(isPlaying ? R.drawable.controls_pause_filled : R.drawable.controls_play_filled));
        B.playPauseMiniImage
            .setImageDrawable(activity.getDrawable(isPlaying ? R.drawable.controls_pause : R.drawable.controls_play));
    }

    private void onIsBufferingChange(boolean loading) {
        B.loadingCircle.setAlpha(loading ? 1 : 0);
        B.parent.requestLayout();
    }

    private void onIsShufflingChange(boolean isShuffling) {
        B.shuffleImage.setColorFilter(
            ContextCompat.getColor(activity,
                isShuffling ? R.color.white : R.color.playing_inactive),
            android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    private void onIsLoopingChange(boolean isLooping) {
        B.loopImage.setColorFilter(
            ContextCompat.getColor(activity,
                isLooping ? R.color.white : R.color.playing_inactive),
            android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    private void onErrorChange(String error) {
        if (error != null) {
            B.errorText.setText(error);
            ValueAnimator darkenAnimation = ValueAnimator
                .ofArgb(activity.getColor(R.color.white), activity.getColor(R.color.playing_inactive))
                .setDuration(1000);
            darkenAnimation.addUpdateListener(animation -> B.coverImage.setColorFilter(
                (int) animation.getAnimatedValue(),
                PorterDuff.Mode.MULTIPLY));
            darkenAnimation.start();
            B.errorText.animate().alpha(1).setDuration(500).setStartDelay(500).start();
            B.retryText.animate().alpha(1).setDuration(500).setStartDelay(500).start();
            new Handler().postDelayed(() -> B.coverImage.setOnClickListener(__ -> {
                playingVM.retry();
                playingVM.error.setValue(null);
            }), 1000);
        } else {
            B.coverImage.setOnClickListener(__ -> {
            });
            B.coverImage.clearColorFilter();
            B.errorText.setAlpha(0f);
            B.retryText.setAlpha(0f);
        }
    }

}
