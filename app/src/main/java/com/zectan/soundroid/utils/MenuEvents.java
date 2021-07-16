package com.zectan.soundroid.utils;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.connection.DeletePlaylistRequest;
import com.zectan.soundroid.connection.SavePlaylistRequest;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Playlist;
import com.zectan.soundroid.models.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MenuEvents {
    private final MainActivity mActivity;
    private final Info mInfo;
    private final Song mSong;
    private final MenuItem mItem;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Runnable mRunnable;

    public MenuEvents(MainActivity activity, Info info, Song song, MenuItem item) {
        mActivity = activity;
        mInfo = info;
        mSong = song;
        mItem = item;
    }

    public MenuEvents(MainActivity activity, Info info, Song song, MenuItem item, Runnable runnable) {
        mActivity = activity;
        mInfo = info;
        mSong = song;
        mItem = item;
        mRunnable = runnable;
    }

    @SuppressLint("NonConstantResourceId")
    public boolean handle() {
        switch (mItem.getItemId()) {
            case MenuBuilder.ADD_TO_PLAYLIST:
                addToPlaylist();
                break;
            case MenuBuilder.ADD_TO_QUEUE:
                addToQueue();
                break;
            case MenuBuilder.OPEN_QUEUE:
                openQueue();
                break;
            case MenuBuilder.CLEAR_QUEUE:
                clearQueue();
                break;
            case MenuBuilder.START_DOWNLOADS:
                startDownloads();
                break;
            case MenuBuilder.STOP_DOWNLOADS:
                stopDownloads();
                break;
            case MenuBuilder.CLEAR_DOWNLOADS:
                clearDownloads();
                break;
            case MenuBuilder.REMOVE_DOWNLOAD:
                removeDownload();
                break;
            case MenuBuilder.SAVE_PLAYLIST:
                savePlaylist();
                break;
            case MenuBuilder.PLAY_PLAYLIST:
                playPlaylist();
                break;
            case MenuBuilder.EDIT_PLAYLIST:
                editPlaylist();
                break;
            case MenuBuilder.DELETE_PLAYLIST:
                deletePlaylist();
                break;
            default:
                break;
        }
        return true;
    }

    private void addToPlaylist() {
        List<Info> infos = new ArrayList<>();
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(mActivity).setTitle("Add To Playlist");

        DialogInterface.OnClickListener onClickListener = (dialog_, i) -> {
            Info info = infos.get(i);

            AtomicInteger completed = new AtomicInteger(0);
            OnSuccessListener<Object> onSuccessListener = __ -> {
                if (completed.incrementAndGet() == 2) {
                    mActivity.snack("Song added");
                }
            };

            boolean inPlaylist = mActivity
                .mainVM
                .getSongsFromPlaylist(info.getId())
                .stream()
                .anyMatch(song -> song.getSongId().equals(mSong.getSongId()));

            if (inPlaylist) {
                mActivity.handleError(new Exception("Song already in playlist!"));
            } else {
                mSong.setPlaylistId(info.getId());
                mSong.setUserId(mActivity.mainVM.userId);
                db.collection("songs")
                    .add(mSong.toMap())
                    .addOnSuccessListener(onSuccessListener)
                    .addOnFailureListener(mActivity::handleError);
                db.collection("playlists")
                    .document(info.getId())
                    .update("order", FieldValue.arrayUnion(mSong.getSongId()))
                    .addOnSuccessListener(onSuccessListener)
                    .addOnFailureListener(mActivity::handleError);
            }
        };

        infos.addAll(mActivity.mainVM.myInfos.getValue());
        dialog.setItems(ListArrayUtils.toArray(CharSequence.class, infos.stream().map(Info::getName).collect(Collectors.toList())), onClickListener);
        dialog.show();
    }

    private void addToQueue() {
        mActivity.playingVM.addToQueue(mSong);
        mActivity.snack("Song added to queue");
    }

    private void openQueue() {
        mRunnable.run();
    }

    private void clearQueue() {
        mActivity.playingVM.clearQueue(mActivity);
        mActivity.snack("Cleared queue");
    }

    private void startDownloads() {
        new DownloadPlaylist(mActivity, mInfo, mActivity.mainVM.myUser.getValue().getHighDownloadQuality());
    }

    private void stopDownloads() {
        List<String> downloading = mActivity.mainVM.downloading.getValue();
        List<String> newDownloading = downloading.stream().filter(id -> !id.equals(mInfo.getId())).collect(Collectors.toList());
        mActivity.mainVM.downloading.setValue(newDownloading);
    }

    private void clearDownloads() {
        List<Song> songs = mActivity.mainVM.getSongsFromPlaylist(mInfo.getId());

        for (Song song : songs) {
            song.deleteLocally(mActivity);
        }

        mActivity.snack("Songs deleted");
    }

    private void removeDownload() {
        mSong.deleteLocally(mActivity);
        mActivity.snack("Song deleted locally");
    }

    private void savePlaylist() {
        mInfo.setUserId(mActivity.mainVM.userId);
        new SavePlaylistRequest(mInfo, new SavePlaylistRequest.Callback() {
            @Override
            public void onComplete() {
                mActivity.snack("Saved playlist");
            }

            @Override
            public void onError(String message) {
                mActivity.handleError(new Exception(message));
            }
        });
    }

    private void playPlaylist() {
        List<Song> songs = mActivity.mainVM.getSongsFromPlaylist(mInfo.getId());
        Playlist playlist = new Playlist(mInfo, songs);
        mActivity.playingVM.startPlaylist(mActivity, playlist, songs.get(0).getSongId(), mActivity.mainVM.myUser.getValue().getHighStreamQuality());

        if (mActivity.mainVM.myUser.getValue().getOpenPlayingScreen()) {
            mActivity.navController.navigate(R.id.fragment_playing);
        }
    }

    private void editPlaylist() {
        mActivity.playlistEditVM.playlistId.setValue(mInfo.getId());
        mRunnable.run();
    }

    private void deletePlaylist() {
        new DeletePlaylistRequest(mInfo.getId(), new DeletePlaylistRequest.Callback() {
            @Override
            public void onComplete() {
                mActivity.snack("Playlist deleted");
            }

            @Override
            public void onError(String message) {
                mActivity.handleError(new Exception(message));
            }
        });
    }

}