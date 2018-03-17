package com.guilhermefgl.peliculas.services;

import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;

import com.guilhermefgl.peliculas.models.Movie;
import com.guilhermefgl.peliculas.models.provider.MovieContract;
import com.guilhermefgl.peliculas.models.provider.MovieDBHelper;

public class LocalStorageFavorite
        extends AsyncTask<LocalStorageFavorite.MovieParams, Void, Void> {

    public static class MovieParams {

        Context context;
        Movie movie;
        boolean isFavorite;

        public MovieParams(Context context, Movie movie, boolean isFavorite) {
            this.context = context;
            this.movie = movie;
            this.isFavorite = isFavorite;
        }
    }

    @Override
    protected Void doInBackground(MovieParams... movieParams) {
        LocalStorageFavorite.MovieParams movieParam = movieParams[0];

        if (movieParam != null) {
            ContentResolver resolver = movieParam.context.getContentResolver();
            if (movieParam.isFavorite) {
                resolver.insert(
                        MovieContract.MovieEntry.CONTENT_URI,
                        MovieDBHelper.buildContentValue(
                                movieParam.movie,
                                TheMovieDBService.ORDER_FAVORITE));
            } else {
                resolver.delete(
                        MovieContract.MovieEntry.CONTENT_URI,
                        MovieDBHelper.buildSelection(
                                MovieContract.MovieEntry.COLUMN_API_ID,
                                MovieContract.MovieEntry.COLUMN_ORDER),
                        new String[] {
                                String.valueOf(movieParam.movie.getMovieId()),
                                TheMovieDBService.ORDER_FAVORITE });
            }
        }
        return null;
    }
}
