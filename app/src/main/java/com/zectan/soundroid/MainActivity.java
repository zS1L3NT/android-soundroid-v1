package com.zectan.soundroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.material.snackbar.Snackbar;
import com.zectan.soundroid.databinding.ActivityMainBinding;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Song;
import com.zectan.soundroid.utils.MenuItemEvents;
import com.zectan.soundroid.viewmodels.MainViewModel;
import com.zectan.soundroid.viewmodels.PlayingViewModel;
import com.zectan.soundroid.viewmodels.PlaylistViewViewModel;
import com.zectan.soundroid.viewmodels.PlaylistsViewModel;
import com.zectan.soundroid.viewmodels.SearchViewModel;

// https://www.glyric.com/2018/merlin/aagaya-nilave

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "(SounDroid) MainActivity";
    public ActivityMainBinding B;
    private InputMethodManager imm;
    private FirebaseRepository repository;

    public PlayingViewModel playingVM;
    public PlaylistViewViewModel playlistViewVM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        B = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(B.getRoot());
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        repository = new FirebaseRepository();

        // View Model
        MainViewModel mainVM = new ViewModelProvider(this).get(MainViewModel.class);
        playingVM = new ViewModelProvider(this).get(PlayingViewModel.class);
        SearchViewModel searchVM = new ViewModelProvider(this).get(SearchViewModel.class);
        PlaylistsViewModel playlistsVM = new ViewModelProvider(this).get(PlaylistsViewModel.class);
        playlistViewVM = new ViewModelProvider(this).get(PlaylistViewViewModel.class);

        // Live Observers
        mainVM.error.observe(this, this::handleError);
        searchVM.watch(this);
        playlistsVM.watch(this);
        playlistViewVM.watch(this);

        NavHostFragment navHostFragment =
            (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(B.bottomNavigator, navController);

        SimpleExoPlayer player = new SimpleExoPlayer.Builder(this).build();
        player.setShuffleModeEnabled(true);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        playingVM.setPlayer(player);
    }

    public FirebaseRepository getRepository() {
        return repository;
    }

    public void showKeyboard() {
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void hideKeyboard(View currentFocus) {
        imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
    }

    public void handleError(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
        Snackbar
            .make(B.navHostFragment, message, Snackbar.LENGTH_SHORT)
            .setAction(R.string.done, __ -> {
            })
            .show();
        Log.e(TAG, e.getMessage());
    }

    public int getAttributeResource(int id) {
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(id, value, true);
        return value.data;
    }

    @SuppressLint("NonConstantResourceId")
    public boolean handleMenuItemClick(Info info, Song song, MenuItem item) {
        return new MenuItemEvents(this, info, song, item).handle();
    }
}