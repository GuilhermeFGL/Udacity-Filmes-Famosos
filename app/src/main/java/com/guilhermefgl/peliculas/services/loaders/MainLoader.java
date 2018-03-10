package com.guilhermefgl.peliculas.services.loaders;

import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.content.AsyncTaskLoader;

import com.guilhermefgl.peliculas.models.Movie;
import com.guilhermefgl.peliculas.models.MovieResponse;
import com.guilhermefgl.peliculas.models.provider.MovieDBHelper;
import com.guilhermefgl.peliculas.services.TheMovieDBService;

import java.util.List;

import static com.guilhermefgl.peliculas.models.provider.MovieContract.MovieEntry;

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
                    TheMovieDBService.getClient().list(order, page).execute().body();

            if (response != null) {
                new LocalStorageWorker().execute(new LocalStorageWorker.MovieParams() {{
                    context = getContext();
                    responseMovies = response.getResults();
                    requestPage = page;
                    requestOrder = order;
                }});
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

    private static class LocalStorageWorker
            extends AsyncTask<LocalStorageWorker.MovieParams, Void, Void> {

        static class MovieParams {
            Context context;
            List<Movie> responseMovies;
            Integer requestPage;
            String requestOrder;
        }

        @Override
        protected Void doInBackground(MovieParams... movieParams) {
            MovieParams movieParam = movieParams[0];

            if (movieParam != null) {
                ContentResolver resolver = movieParam.context.getContentResolver();
                if (movieParam.requestPage == TheMovieDBService.LISTING_FIRST_PAGE) {
                    resolver.delete(
                            MovieEntry.CONTENT_URI,
                            MovieDBHelper.buildSelection(MovieEntry.COLUMN_ORDER),
                            new String[]{movieParam.requestOrder});
                }
                resolver.bulkInsert(
                        MovieEntry.CONTENT_URI,
                        MovieDBHelper.buildContentValues(
                                movieParam.responseMovies,
                                movieParam.requestOrder));
            }
            return null;
        }
    }
}
