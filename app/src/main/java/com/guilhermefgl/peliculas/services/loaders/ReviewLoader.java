package com.guilhermefgl.peliculas.services.loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.guilhermefgl.peliculas.models.ReviewResponse;
import com.guilhermefgl.peliculas.services.TheMovieDBService;

public class ReviewLoader extends AsyncTaskLoader<ReviewResponse> {

    public static final Integer LOADER_ID = 1003;
    public static final String BUNDLE_ID = VideoLoader.class.getName().concat(".BUNDLE_ID");

    private final Integer movieId;
    private ReviewResponse reviewsCached;

    public ReviewLoader(@NonNull Context context, Integer movieId) {
        super(context);
        this.movieId = movieId;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (reviewsCached != null) {
            deliverResult(reviewsCached);
        } else {
            forceLoad();
        }
    }

    @Nullable
    @Override
    public ReviewResponse loadInBackground() {
        if (movieId == null) {
            return null;
        }

        try {
            return TheMovieDBService.getClient().listReviews(movieId).execute().body();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void deliverResult(ReviewResponse data) {
        this.reviewsCached = data;
        super.deliverResult(data);
    }
}
