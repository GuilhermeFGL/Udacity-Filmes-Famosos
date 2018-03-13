package com.guilhermefgl.peliculas.services.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.guilhermefgl.peliculas.models.MovieResponse;
import com.guilhermefgl.peliculas.services.LocalStorageWriter;
import com.guilhermefgl.peliculas.services.TheMovieDBService;

public class MainLoader extends AsyncTaskLoader<MovieResponse> {

    public static final Integer LOADER_ID = 1001;
    public static final String BUNDLE_ORDER = MainLoader.class.getName().concat(".BUNDLE_ORDER");
    public static final String BUNDLE_PAGE = MainLoader.class.getName().concat(".BUNDLE_PAGE");

    private MovieResponse moviesCached;
    private String order;
    private Integer page;

    public MainLoader(Context context, String order, Integer page) {
        super(context);
        this.order = order;
        this.page = page;
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
    public MovieResponse loadInBackground() {
        if (order == null || page == null) {
            return null;
        }

        try {
            final MovieResponse response =
                    TheMovieDBService.getClient().listMovies(order, page).execute().body();

            if (response != null) {
                new LocalStorageWriter().execute(
                        new LocalStorageWriter.MovieParams(
                                getContext(), response.getResults(), page, order));
            }

            return response;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void deliverResult(MovieResponse data) {
        this.moviesCached = data;
        super.deliverResult(data);
    }
}
