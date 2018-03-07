package com.guilhermefgl.peliculas.loaders;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;

import com.guilhermefgl.peliculas.models.MovieResponse;
import com.guilhermefgl.peliculas.services.TheMovieDBService;

import java.io.IOException;

public class MainLoader extends AsyncTaskLoader<MovieResponse>
        implements LoaderManager.LoaderCallbacks<MovieResponse> {

    public static final String BUNDLE_ORDER = MainLoader.class.getName().concat(".BUNDLE_ORDER");
    public static final String BUNDLE_PAGE = MainLoader.class.getName().concat(".BUNDLE_PAGE");

    private LoaderCallback loaderCallback;
    private MovieResponse moviesCached;
    private String order;
    private Integer page;

    public MainLoader(Context context, LoaderCallback loaderCallback) {
        super(context);
        this.loaderCallback = loaderCallback;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (moviesCached != null) {
            deliverResult(moviesCached);
        } else {
            forceLoad();
        }
    }

    @Override
    public Loader<MovieResponse> onCreateLoader(int id, Bundle args) {
        moviesCached = null;
        order = args.getString(BUNDLE_ORDER);
        page = args.getInt(BUNDLE_PAGE);
        return this;
    }

    @Override
    public MovieResponse loadInBackground() {
        try {
            return TheMovieDBService.getClient().list(order, page).execute().body();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void deliverResult(MovieResponse data) {
        this.moviesCached = data;
        super.deliverResult(data);
    }

    @Override
    public void onLoadFinished(Loader<MovieResponse> loader, MovieResponse data) {
        if (data == null) {
            loaderCallback.onLoaderError();
        } else {
            loaderCallback.onLoaderSuccess(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<MovieResponse> loader) { }

    public interface LoaderCallback {
        void onLoaderSuccess(MovieResponse movies);
        void onLoaderError();
    }
}
