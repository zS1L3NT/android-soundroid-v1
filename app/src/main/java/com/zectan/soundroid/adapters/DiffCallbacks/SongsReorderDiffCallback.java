package com.zectan.soundroid.adapters.DiffCallbacks;

import androidx.recyclerview.widget.DiffUtil;

import com.zectan.soundroid.models.Song;

import java.util.List;

public class SongsReorderDiffCallback extends DiffUtil.Callback {

    private final List<Song> oldSongs, newSongs;

    public SongsReorderDiffCallback(List<Song> oldSongs, List<Song> newSongs) {
        this.oldSongs = oldSongs;
        this.newSongs = newSongs;
    }

    @Override
    public int getOldListSize() {
        return oldSongs.size();
    }

    @Override
    public int getNewListSize() {
        return newSongs.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        Song oldSong = oldSongs.get(oldItemPosition);
        Song newSong = newSongs.get(newItemPosition);
        return oldSong.getSongId().equals(newSong.getSongId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Song oldSong = oldSongs.get(oldItemPosition);
        Song newSong = newSongs.get(newItemPosition);
        return oldSong.equals(newSong);
    }
}