package com.guilhermefgl.peliculas.views.details;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.guilhermefgl.peliculas.R;
import com.guilhermefgl.peliculas.helpers.PicassoHelper;
import com.guilhermefgl.peliculas.models.Video;
import com.guilhermefgl.peliculas.services.TheMovieDBService;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private ArrayList<Video> videos;
    private OnVideoItemClick onVideoItemClick;

    VideoAdapter (ArrayList<Video> videos, OnVideoItemClick onVideoItemClick) {
        this.videos = videos;
        this.onVideoItemClick = onVideoItemClick;
    }

    @NonNull
    @Override
    public VideoAdapter.VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VideoViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_video, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VideoAdapter.VideoViewHolder holder, int position) {
        holder.bind(videos.get(position));
    }

    @Override
    public int getItemCount() {
        return videos != null ? videos.size() : 0;
    }

    boolean isEmpty() {
        return getItemCount() == 0;
    }

    void setItems(ArrayList<Video> videos) {
        this.videos = videos;
        notifyDataSetChanged();
    }

    ArrayList<Video> getItens() {
        if (videos != null) {
            return new ArrayList<>(videos);
        }
        return new ArrayList<>();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.item_video_thumbnail)
        ImageView itemThumbnailImageView;
        @BindView(R.id.item_video_title)
        TextView itemTitleTextView;

        private View view;
        private Video video;

        VideoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.view = itemView;
        }

        void bind(Video video) {
            this.video = video;
            itemTitleTextView.setText(video.getTitle());
            PicassoHelper.loadImage(view.getContext(),
                    TheMovieDBService.buildYoutubeThumbnailUrl(video.getKey()),
                    itemThumbnailImageView,
                    R.mipmap.youtube_thumbnail_background,
                    R.mipmap.youtube_thumbnail_background);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onVideoItemClick.onVideoItemClick(video);
        }
    }

    public interface OnVideoItemClick {
        void onVideoItemClick(Video video);
    }
}
