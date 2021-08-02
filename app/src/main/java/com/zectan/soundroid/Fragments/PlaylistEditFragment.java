package com.zectan.soundroid.Fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.zectan.soundroid.Adapters.PlaylistEditAdapter;
import com.zectan.soundroid.Classes.Fragment;
import com.zectan.soundroid.Connections.EditPlaylistRequest;
import com.zectan.soundroid.Models.Playlist;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.R;
import com.zectan.soundroid.Utils.ListArrayUtils;
import com.zectan.soundroid.databinding.FragmentPlaylistEditBinding;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlaylistEditFragment extends Fragment<FragmentPlaylistEditBinding> {
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final List<String> removed = new ArrayList<>();
    private final PlaylistEditAdapter.Callback callback = new PlaylistEditAdapter.Callback() {
        @Override
        public void onRemove(String songId) {
            removed.add(songId);
        }

        @Override
        public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
            mItemTouchHelper.startDrag(viewHolder);
        }
    };
    private PlaylistEditAdapter playlistEditAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private Uri newFilePath;
    private final ActivityResultLauncher<Intent> chooseCoverImage = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                newFilePath = result.getData().getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(mActivity.getContentResolver(), newFilePath);
                    Glide
                        .with(mActivity)
                        .load(bitmap)
                        .transition(new DrawableTransitionOptions().crossFade())
                        .centerCrop()
                        .into(B.coverImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    );

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentPlaylistEditBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        // Recycler Views
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mActivity);
        playlistEditAdapter = new PlaylistEditAdapter(callback);
        mItemTouchHelper = new ItemTouchHelper(new PlaylistEditAdapter.PlaylistEditItemTouchHelper(playlistEditAdapter));
        B.recyclerView.setAdapter(playlistEditAdapter);
        B.recyclerView.setLayoutManager(layoutManager);
        mItemTouchHelper.attachToRecyclerView(B.recyclerView);

        // Live Observers
        mPlaylistEditVM.saving.observe(this, this::onSavingChange);

        B.backImage.setOnClickListener(__ -> mNavController.navigateUp());
        B.saveImage.setOnClickListener(this::onSaveClicked);
        B.coverImage.setOnClickListener(this::onCoverClicked);
        B.parent.setTransitionListener(mActivity.getTransitionListener());

        Playlist playlist = mMainVM.getInfoFromPlaylist(mPlaylistEditVM.playlistId.getValue());
        List<Song> songs = mMainVM.getSongsFromPlaylist(mPlaylistEditVM.playlistId.getValue());
        assert playlist != null;
        mPlaylistEditVM.playlist.setValue(playlist);
        mPlaylistEditVM.songs.setValue(songs);

        B.nameTextInput.setText(playlist.getName());
        Glide
            .with(mActivity)
            .load(playlist.getCover())
            .placeholder(R.drawable.playing_cover_loading)
            .error(R.drawable.playing_cover_failed)
            .transition(new DrawableTransitionOptions().crossFade())
            .centerCrop()
            .into(B.coverImage);
        playlistEditAdapter.updateSongs(ListArrayUtils.sortSongs(songs, mPlaylistEditVM.playlist.getValue().getOrder()));
        removed.clear();

        return B.getRoot();
    }

    @Override
    public void onStop() {
        super.onStop();
        mActivity.hideKeyboard(requireView());
    }

    private void onCoverClicked(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        chooseCoverImage.launch(intent);
    }

    private void onSaveClicked(View view) {
        mPlaylistEditVM.saving.setValue(true);
        Playlist playlist = mPlaylistEditVM.playlist.getValue();
        List<String> order = playlistEditAdapter
            .getSongs()
            .stream()
            .map(Song::getSongId)
            .collect(Collectors.toList());

        String newName;
        if (B.nameTextInput.getText() == null) {
            newName = playlist.getName();
        } else {
            newName = B.nameTextInput.getText().toString();
        }

        Playlist newPlaylist = new Playlist(
            playlist.getId(),
            newName,
            playlist.getCover(),
            playlist.getColorHex(),
            playlist.getUserId(),
            order
        );

        StorageReference ref = storage.getReference().child(String.format("playlists/%s.png", playlist.getId()));
        if (newFilePath != null) {
            ref.putFile(newFilePath)
                .addOnSuccessListener(snap -> ref.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        newPlaylist.setCover(uri.toString());
                        sendEditPlaylistRequest(newPlaylist);
                    })
                    .addOnFailureListener(error -> {
                        mPlaylistEditVM.saving.postValue(false);
                        mMainVM.error.postValue(error);
                    }))
                .addOnFailureListener(error -> {
                    mPlaylistEditVM.saving.postValue(false);
                    mMainVM.error.postValue(error);
                });
        } else {
            sendEditPlaylistRequest(newPlaylist);
        }
    }

    private void sendEditPlaylistRequest(Playlist playlist) {
        new EditPlaylistRequest(playlist, removed, new EditPlaylistRequest.Callback() {
            @Override
            public void onComplete(String response) {
                mPlaylistEditVM.songs
                    .getValue()
                    .stream()
                    .filter(song -> removed.contains(song.getSongId()))
                    .forEach(song -> song.deleteIfNotUsed(mActivity, mMainVM.mySongs.getValue()));

                mPlaylistEditVM.saving.postValue(false);
                new Handler(Looper.getMainLooper()).post(mActivity::onBackPressed);
            }

            @Override
            public void onError(String message) {
                mMainVM.error.postValue(new Exception(message));
                mPlaylistEditVM.saving.postValue(false);
            }
        });
    }

    private void onSavingChange(Boolean saving) {
        B.saveImage.setEnabled(!saving);
        if (saving) {
            B.saveImage.setAlpha(0f);
            B.loadingCircle.setAlpha(1f);
        } else {
            B.saveImage.setAlpha(1f);
            B.loadingCircle.setAlpha(0f);
        }
    }
}