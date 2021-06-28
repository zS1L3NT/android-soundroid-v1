package com.zectan.soundroid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.zectan.soundroid.R;
import com.zectan.soundroid.databinding.SongListItemBinding;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.SearchResult;
import com.zectan.soundroid.models.Song;
import com.zectan.soundroid.utils.MenuItemsBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {
    private static final String TAG = "(SounDroid) SearchAdapter";
    //    private static final int FOOTER_VIEW = 1;
    private final Callback mCallback;
    private final List<SearchResult> mResults;

    public SearchAdapter(Callback callback) {
        mCallback = callback;
        mResults = new ArrayList<>();
    }

    @NonNull
    @NotNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
//        if (viewType == FOOTER_VIEW) {
//            View itemView = LayoutInflater
//                .from(parent.getContext())
//                .inflate(R.layout.footer_search, parent, false);
//
//            return new SearchViewHolder(itemView);
//        } else {
        View itemView = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.song_list_item, parent, false);

        return new SearchViewHolder(itemView, mCallback);
//        }
    }

    public void updateResults(List<SearchResult> results) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallback(mResults, results));
        diffResult.dispatchUpdatesTo(this);
        mResults.clear();
        mResults.addAll(results);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SearchViewHolder holder, int position) {
        holder.bind(mResults, position);
    }

    @Override
    public int getItemCount() {
        return mResults.size();
    }

//    @Override
//    public int getItemViewType(int position) {
//        if (mResults.get(position).getId().equals("")) {
//            return FOOTER_VIEW;
//        }
//        return super.getItemViewType(position);
//    }

    public interface Callback extends MenuItemsBuilder.MenuItemCallback<SearchResult> {
        void onSongClicked(Song song);

        void onPlaylistClicked(Info info);
    }

}

class SearchViewHolder extends RecyclerView.ViewHolder {
    private SongListItemBinding B;
    private SearchAdapter.Callback mCallback;

    public SearchViewHolder(@NonNull @NotNull View itemView, SearchAdapter.Callback callback) {
        super(itemView);
        B = SongListItemBinding.bind(itemView);
        mCallback = callback;
    }

    public SearchViewHolder(@NonNull @NotNull View itemView) {
        super(itemView);
    }

    public void bind(List<SearchResult> results, int position) {
        if (B == null) return;

        SearchResult result = results.get(position);
        Context context = B.parent.getContext();

        if (result.getSong() != null) {
            Song song = result.getSong();
            String id = song.getId();
            String title = song.getTitle();
            String artiste = song.getArtiste();
            String cover = song.getCover();
            String transitionName = String.format("%s %s", context.getString(R.string.TRANSITION_song_cover), id);

            B.titleText.setText(title);
            B.descriptionText.setText(String.format("%s • Song • %s", result.getLocation(), artiste));
            B.coverImage.setTransitionName(transitionName);
            Glide
                .with(context)
                .load(cover)
                .placeholder(R.drawable.playing_cover_default)
                .error(R.drawable.playing_cover_default)
                .transition(new DrawableTransitionOptions().crossFade())
                .centerCrop()
                .into(B.coverImage);
            B.songClickable.setOnClickListener(__ -> mCallback.onSongClicked(song));
            B.menuClickable.setOnClickListener(v -> MenuItemsBuilder.createMenu(
                v,
                R.menu.song_menu_search,
                result,
                mCallback
            ));
        } else if (result.getPlaylistInfo() != null) {
            Info info = result.getPlaylistInfo();
            String id = info.getId();
            String name = info.getName();
            String cover = info.getCover();
            String transitionName = String.format("%s %s", context.getString(R.string.TRANSITION_song_cover), id);


            B.titleText.setText(name);
            B.descriptionText.setText(String.format("%s • Playlist", result.getLocation()));
            B.coverImage.setTransitionName(transitionName);
            Glide
                .with(context)
                .load(cover)
                .error(R.drawable.playing_cover_default)
                .centerCrop()
                .into(B.coverImage);
            B.songClickable.setOnClickListener(__ -> mCallback.onPlaylistClicked(info));
            B.menuClickable.setOnClickListener(v -> MenuItemsBuilder.createMenu(
                v,
                R.menu.playlist_menu_search,
                result,
                mCallback
            ));
        }
    }
}

class DiffCallback extends DiffUtil.Callback {

    private final List<SearchResult> oldResults, newResults;

    public DiffCallback(List<SearchResult> oldResults, List<SearchResult> newResults) {
        this.oldResults = oldResults;
        this.newResults = newResults;
    }

    @Override
    public int getOldListSize() {
        return oldResults.size();
    }

    @Override
    public int getNewListSize() {
        return newResults.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        SearchResult oldResult = oldResults.get(oldItemPosition);
        SearchResult newResult = newResults.get(newItemPosition);
        return oldResult.getId().equals(newResult.getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        SearchResult oldResult = oldResults.get(oldItemPosition);
        SearchResult newResult = newResults.get(newItemPosition);
        return oldResult.equals(newResult);
    }
}