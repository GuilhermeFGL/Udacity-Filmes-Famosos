package com.guilhermefgl.peliculas.services.loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.guilhermefgl.peliculas.models.VideoResponse;
import com.guilhermefgl.peliculas.services.TheMovieDBService;

public class VideoLoader extends AsyncTaskLoader<VideoResponse> {

    public static final Integer LOADER_ID = 1002;
    public static final String BUNDLE_ID = VideoLoader.class.getName().concat(".BUNDLE_ID");

    private final Integer movieId;
    private VideoResponse videosCached;

    public VideoLoader(@NonNull Context context, Integer movieId) {
        super(context);
        this.movieId = movieId;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (videosCached != null) {
            deliverResult(videosCached);
        } else {
            forceLoad();
        }
    }

    @Nullable
    @Override
    public VideoResponse loadInBackground() {
        if (movieId == null) {
            return null;
        }

        try {
            return TheMovieDBService.getClient().listVideos(movieId).execute().body();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void deliverResult(VideoResponse data) {
        this.videosCached = data;
        super.deliverResult(data);
    }
}
