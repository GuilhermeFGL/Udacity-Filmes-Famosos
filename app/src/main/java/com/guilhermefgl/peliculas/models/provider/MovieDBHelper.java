package com.guilhermefgl.peliculas.models.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.guilhermefgl.peliculas.BuildConfig;
import com.guilhermefgl.peliculas.models.Movie;
import com.guilhermefgl.peliculas.services.TheMovieDBService;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static com.guilhermefgl.peliculas.models.provider.MovieContract.MovieEntry;

public class MovieDBHelper extends SQLiteOpenHelper {

    public static final SimpleDateFormat DATE_FORMATTER
            = new SimpleDateFormat(TheMovieDBService.DATE_FORMAT, Locale.getDefault());

    MovieDBHelper(Context context) {
        super(context, BuildConfig.DB_NAME, null, BuildConfig.VERSION_CODE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID                 + " INTEGER PRIMARY KEY, " +
                MovieEntry.COLUMN_API_ID       + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_TITLE        + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_OVERVIEW     + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_DATE         + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_LANGUAGE     + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_VOTE_COUNT   + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_VOTE_AVERAGE + " DOUBLE NOT NULL, " +
                MovieEntry.COLUMN_POPULARITY   + " DOUBLE NOT NULL, " +
                MovieEntry.COLUMN_POSTER       + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_VIDEO        + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_ADULT        + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_ORDER        + " TEXT NOT NULL);";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(db);
    }

    public static String buildSelection(String column) {
        return column.concat("=?");
    }

    public static ContentValues[] buildContentValues(List<Movie> movies, String order) {
        ContentValues[] contentValues = new ContentValues[movies.size()];
        int index = 0;
        for (Movie movie : movies) {
            contentValues[index] = buildContentValue(movie, order);
            index++;
        }
        return contentValues;
    }

    public static ContentValues buildContentValue(Movie movie, String order) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieEntry.COLUMN_API_ID, movie.getMovieId());
        contentValues.put(MovieEntry.COLUMN_TITLE, movie.getTitle());
        contentValues.put(MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
        contentValues.put(MovieEntry.COLUMN_DATE, DATE_FORMATTER.format(movie.getReleaseDate()));
        contentValues.put(MovieEntry.COLUMN_LANGUAGE, movie.getLanguage());
        contentValues.put(MovieEntry.COLUMN_VOTE_COUNT, movie.getVoteCount());
        contentValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
        contentValues.put(MovieEntry.COLUMN_POPULARITY, movie.getPopularity());
        contentValues.put(MovieEntry.COLUMN_POSTER, movie.getPosterPath());
        contentValues.put(MovieEntry.COLUMN_VIDEO, movie.hasVideo() ? 1 : 0);
        contentValues.put(MovieEntry.COLUMN_ADULT, movie.isAdult() ? 1 : 0);
        contentValues.put(MovieEntry.COLUMN_ORDER, order);
        return contentValues;
    }
}
