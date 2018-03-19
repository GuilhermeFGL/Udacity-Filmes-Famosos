package com.guilhermefgl.peliculas.services.loaders;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.support.annotation.Nullable;

import com.guilhermefgl.peliculas.models.provider.MovieContract;
import com.guilhermefgl.peliculas.models.provider.MovieDBHelper;
import com.guilhermefgl.peliculas.services.TheMovieDBService;

public class FavoriteLoader extends CursorLoader {

    public static final Integer LOADER_ID = 1004;
    public static final String BUNDLE_ID = FavoriteLoader.class.getName().concat(".BUNDLE_ID");

    private final Integer movieId;
    private Cursor cursorCached;

    public FavoriteLoader(Context context, Integer movieId) {
        super(context);
        this.movieId = movieId;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (cursorCached != null) {
            deliverResult(cursorCached);
        } else {
            forceLoad();
        }
    }

    @Nullable
    @Override
    public Cursor loadInBackground() {
        if (movieId == null) {
            return null;
        }

        try {
            return getContext().getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                    null,
                    MovieDBHelper.buildSelection(
                            MovieContract.MovieEntry.COLUMN_API_ID,
                            MovieContract.MovieEntry.COLUMN_ORDER),
                    new String[]{String.valueOf(movieId), TheMovieDBService.ORDER_FAVORITE},
                    null);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void deliverResult(Cursor data) {
        this.cursorCached = data;
        super.deliverResult(data);
    }

}
