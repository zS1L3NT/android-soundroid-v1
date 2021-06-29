package com.zectan.soundroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zectan.soundroid.adapters.PlaylistsAdapter;
import com.zectan.soundroid.classes.Fragment;
import com.zectan.soundroid.databinding.FragmentPlaylistsBinding;
import com.zectan.soundroid.models.Info;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PlaylistsFragment extends Fragment<FragmentPlaylistsBinding> {
    private static final String TAG = "(SounDroid) PlayingFragment";

    private final PlaylistsAdapter.Callback callback = new PlaylistsAdapter.Callback() {
        @Override
        public void onPlaylistClicked(Info info) {
            playlistViewVM.info.setValue(info);
            playlistViewVM.songs.setValue(new ArrayList<>());
            NavDirections action = PlaylistsFragmentDirections.openPlaylistView();
            NavHostFragment.findNavController(PlaylistsFragment.this).navigate(action);
        }

        @Override
        public boolean onMenuItemClicked(Info info, MenuItem item) {
            return activity.handleMenuItemClick(info, null, item);
        }
    };

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentPlaylistsBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        // Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        PlaylistsAdapter playlistsAdapter = new PlaylistsAdapter(callback);
        B.recyclerView.setAdapter(playlistsAdapter);
        B.recyclerView.setLayoutManager(layoutManager);

        // Observers
        playlistsVM.infos.observe(activity, playlistsAdapter::updateInfos);
        playlistsVM.loading.observe(activity, B.swipeRefresh::setRefreshing);

        B.headerAddImage.setOnClickListener(this::createPlaylist);
        B.swipeRefresh.setOnRefreshListener(() -> playlistsVM.reload(activity::handleError));

        return B.getRoot();
    }

    private void createPlaylist(View view) {
        // TODO Implement method
    }
}