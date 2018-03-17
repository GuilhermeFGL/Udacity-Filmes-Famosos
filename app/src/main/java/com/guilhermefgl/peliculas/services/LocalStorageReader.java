package com.guilhermefgl.peliculas.services;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.guilhermefgl.peliculas.models.Movie;
import com.guilhermefgl.peliculas.models.provider.MovieContract;
import com.guilhermefgl.peliculas.models.provider.MovieDBHelper;

import java.util.ArrayList;

public class LocalStorageReader extends AsyncTask<LocalStorageReader.MovieParams, Void, ArrayList<Movie>> {

    private ReaderCallBack callBack;

    public LocalStorageReader(ReaderCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    protected ArrayList<Movie> doInBackground(MovieParams... movieParams) {
        LocalStorageReader.MovieParams movieParam = movieParams[0];

        if (movieParam != null) {
            ContentResolver resolver = movieParam.context.getContentResolver();
            Cursor cursor = resolver.query(MovieContract.MovieEntry.CONTENT_URI,
                    null,
                    MovieDBHelper.buildSelection(MovieContract.MovieEntry.COLUMN_ORDER),
                    new String[]{movieParam.requestOrder},
                    null);

            if (cursor != null) {
                ArrayList<Movie> movies = Movie.createFromCursor(cursor);
                cursor.close();
                return movies;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<Movie> movies) {
        super.onPostExecute(movies);
        callBack.onReadMovies(movies);
    }

    public static class MovieParams {
        private Context context;
        String requestOrder;

        public MovieParams(Context context, String requestOrder) {
            this.context = context;
            this.requestOrder = requestOrder;
        }
    }

    public interface ReaderCallBack {
        void onReadMovies(ArrayList<Movie> movies);
    }
}
