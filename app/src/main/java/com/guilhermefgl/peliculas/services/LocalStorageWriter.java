package com.guilhermefgl.peliculas.services;

import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;

import com.guilhermefgl.peliculas.models.Movie;
import com.guilhermefgl.peliculas.models.provider.MovieContract;
import com.guilhermefgl.peliculas.models.provider.MovieDBHelper;

import java.util.List;

public class LocalStorageWriter
        extends AsyncTask<LocalStorageWriter.MovieParams, Void, Void> {

    public static class MovieParams {
        private Context context;
        List<Movie> movies;
        Integer requestPage;
        String requestOrder;

        public MovieParams(Context context, List<Movie> movies,
                           Integer requestPage, String requestOrder) {
            this.context = context;
            this.movies = movies;
            this.requestPage = requestPage;
            this.requestOrder = requestOrder;
        }
    }

    @Override
    protected Void doInBackground(MovieParams... movieParams) {
        MovieParams movieParam = movieParams[0];

        if (movieParam != null) {
            ContentResolver resolver = movieParam.context.getContentResolver();
            if (movieParam.requestPage == TheMovieDBService.LISTING_FIRST_PAGE) {
                resolver.delete(
                        MovieContract.MovieEntry.CONTENT_URI,
                        MovieDBHelper.buildSelection(MovieContract.MovieEntry.COLUMN_ORDER),
                        new String[]{movieParam.requestOrder});
            }
            resolver.bulkInsert(
                    MovieContract.MovieEntry.CONTENT_URI,
                    MovieDBHelper.buildContentValues(
                            movieParam.movies,
                            movieParam.requestOrder));
        }
        return null;
    }
}