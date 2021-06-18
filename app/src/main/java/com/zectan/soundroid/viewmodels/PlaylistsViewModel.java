package com.zectan.soundroid.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zectan.soundroid.objects.Info;

import java.util.List;

public class PlaylistsViewModel extends ViewModel {
    public MutableLiveData<List<Info>> infos = new MutableLiveData<>();
    public boolean requested = false;

    public PlaylistsViewModel() {
        // Required empty public constructor
    }

}
