package com.zectan.soundroid.viewmodels;

import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.classes.StrictLiveData;
import com.zectan.soundroid.models.Song;

public class SongEditViewModel extends ViewModel {
    public final StrictLiveData<Song> song = new StrictLiveData<>(Song.getEmpty());
    public final StrictLiveData<Integer> navigateNow = new StrictLiveData<>(0);
    public final StrictLiveData<Boolean> saving = new StrictLiveData<>(false);

    public SongEditViewModel() {
        // Required empty public constructor
    }
}