package com.guilhermefgl.peliculas.models.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.guilhermefgl.peliculas.helpers.Constants;

import static com.guilhermefgl.peliculas.models.provider.MovieContract.MovieEntry;

public class MovieContentProvider extends ContentProvider {

    public static final int TAG_MOVIES = 100;
    public static final int TAG_MOVIE_WITH_ID = 101;

    private static final UriMatcher URI_MATCHER;
    static {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(Constants.AUTHORITY_PROVIDER, MovieContract.PATH, TAG_MOVIES);
        uriMatcher.addURI(Constants.AUTHORITY_PROVIDER, MovieContract.PATH_WITH_ID, TAG_MOVIE_WITH_ID);
        URI_MATCHER = uriMatcher;
    }

    private MovieDBHelper movieDBHelper;

    @Override
    public boolean onCreate() {
        movieDBHelper = new MovieDBHelper(getContext());
        return true;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = movieDBHelper.getWritableDatabase();
        final Uri itemUri;

        switch (URI_MATCHER.match(uri)) {
            case TAG_MOVIES:
                long id = db.insert(MovieEntry.TABLE_NAME, null, values);
                if ( id > 0 ) {
                    itemUri = ContentUris.withAppendedId(MovieEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return itemUri;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = movieDBHelper.getWritableDatabase();
        int insertCount = 0;

        switch (URI_MATCHER.match(uri)) {
            case TAG_MOVIES:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        insert(uri, value);
                        insertCount++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return insertCount;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = movieDBHelper.getReadableDatabase();
        final Cursor cursor;

        switch (URI_MATCHER.match(uri)) {
            case TAG_MOVIES:
                cursor =  db.query(MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String... selectionArgs) {
        return -1;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String... selectionArgs) {
        final SQLiteDatabase db = movieDBHelper.getWritableDatabase();
        final int itemId;

        switch (URI_MATCHER.match(uri)) {
            case TAG_MOVIES:
                itemId = db.delete(MovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case TAG_MOVIE_WITH_ID:
                itemId = db.delete(MovieEntry.TABLE_NAME,
                        MovieDBHelper.buildSelection(MovieEntry._ID),
                        new String[]{uri.getPathSegments().get(1)});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (itemId != 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return itemId;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

}
