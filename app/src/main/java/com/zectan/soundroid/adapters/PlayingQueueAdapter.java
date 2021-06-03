package com.zectan.soundroid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zectan.soundroid.R;
import com.zectan.soundroid.objects.Song;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlayingQueueAdapter extends RecyclerView.Adapter<PlayingQueueAdapter.ViewHolder> {
    private static final String TAG = "(SounDroid) PlayingQueueAdapter";
    private final PlayingQueueAdapter.onSongClicked onSongClicked;
    private List<Song> queue;

    public PlayingQueueAdapter(PlayingQueueAdapter.onSongClicked onSongClicked) {
        this.queue = new ArrayList<>();
        this.onSongClicked = onSongClicked;
    }

    @NonNull
    @NotNull
    @Override
    public PlayingQueueAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.song_list_item, parent, false);

        return new PlayingQueueAdapter.ViewHolder(itemView);
    }

    public void updateQueue(List<Song> queue) {
        this.queue = queue;
        notifyDataSetChanged();
    }

    public void onBindViewHolder(@NonNull PlayingQueueAdapter.ViewHolder holder, int position) {
        Song song = queue.get(position);
        Context context = holder.itemView.getContext();
    
        String title = song.getTitle();
        String artiste = song.getArtiste();
        String cover = song.getCover();
    
        holder.titleText.setText(title);
        holder.artisteText.setText(artiste);
        Glide
            .with(context)
            .load(cover)
            .centerCrop()
            .into(holder.coverImage);
        holder.itemView.setOnClickListener(__ -> onSongClicked.run(queue, position));
    }

    @Override
    public int getItemCount() {
        return queue.size();
    }

    public interface onSongClicked {
        void run(List<Song> playlist, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View itemView;
        public final ImageView coverImage;
        public final TextView titleText, artisteText;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            coverImage = itemView.findViewById(R.id.song_list_item_cover);
            titleText = itemView.findViewById(R.id.song_list_item_title);
            artisteText = itemView.findViewById(R.id.song_list_item_artiste);
        }
    }
}
