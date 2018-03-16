package com.guilhermefgl.peliculas.models.provider;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.guilhermefgl.peliculas.utils.TestUtilities;
import com.guilhermefgl.peliculas.helpers.Constants;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class MovieContentProvider_Test {

    private final Context context = InstrumentationRegistry.getTargetContext();

    @Before
    public void setUp() {
        deleteAllRecords();
    }

    @Test
    public void testProviderRegistry() {
        String packageName = context.getPackageName();
        ComponentName componentName
                = new ComponentName(packageName, MovieContentProvider.class.getName());

        try {
            PackageManager pm = context.getPackageManager();
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);
            String actualAuthority = providerInfo.authority;
            String expectedAuthority = Constants.AUTHORITY_PROVIDER;

            String incorrectAuthority =
                    "Error: MovieContentProvider registered with authority: " + actualAuthority +
                            " instead of expected authority: " + expectedAuthority;
            assertEquals(incorrectAuthority,
                    actualAuthority,
                    expectedAuthority);
        } catch (PackageManager.NameNotFoundException e) {
            fail("Error: MovieContentProvider not registered at " + context.getPackageName());
        }
    }

    @Test
    public void testUriMatcher() {
        UriMatcher testMatcher = MovieContentProvider.URI_MATCHER;

        int actualMatchCode = testMatcher.match(MovieContract.MovieEntry.CONTENT_URI);
        int expectedMatchCode = MovieContentProvider.TAG_MOVIES;
        assertEquals("Error: The TAG_MOVIES URI was matched incorrectly.",
                actualMatchCode,
                expectedMatchCode);

        int actualMovieWithIdCode = testMatcher.match(
                MovieContract.MovieEntry.CONTENT_URI.buildUpon().appendPath("1").build());
        int expectedMovieWithIdCode = MovieContentProvider.TAG_MOVIE_WITH_ID;
        assertEquals("Error: The TAG_MOVIE_WITH_ID URI was matched incorrectly.",
                actualMovieWithIdCode,
                expectedMovieWithIdCode);
    }

    @Test
    public void testInsert() {
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_API_ID, 1);
        values.put(MovieContract.MovieEntry.COLUMN_TITLE, "title");
        values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, "overview");
        values.put(MovieContract.MovieEntry.COLUMN_DATE, "2001-01-01");
        values.put(MovieContract.MovieEntry.COLUMN_LANGUAGE, "language");
        values.put(MovieContract.MovieEntry.COLUMN_VOTE_COUNT, 1);
        values.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, 1);
        values.put(MovieContract.MovieEntry.COLUMN_POPULARITY, 1);
        values.put(MovieContract.MovieEntry.COLUMN_POSTER, "poster_path");
        values.put(MovieContract.MovieEntry.COLUMN_VIDEO, 1);
        values.put(MovieContract.MovieEntry.COLUMN_ADULT, 1);
        values.put(MovieContract.MovieEntry.COLUMN_ORDER, "order");

        TestUtilities.TestContentObserver observer = TestUtilities.getTestContentObserver();
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.registerContentObserver(
                MovieContract.MovieEntry.CONTENT_URI,
                true,
                observer);

        Uri actualUri = contentResolver.insert(MovieContract.MovieEntry.CONTENT_URI, values);
        Uri expectedUri = ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI, 1);
        assertEquals("Unable to insert item through Provider",
                actualUri,
                expectedUri);

        observer.waitForNotificationOrFail();
        contentResolver.unregisterContentObserver(observer);
    }

    @Test
    public void testBulkInsert() {
        ContentValues values_1 = new ContentValues();
        values_1.put(MovieContract.MovieEntry.COLUMN_API_ID, 2);
        values_1.put(MovieContract.MovieEntry.COLUMN_TITLE, "title 2");
        values_1.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, "overview");
        values_1.put(MovieContract.MovieEntry.COLUMN_DATE, "2001-01-01");
        values_1.put(MovieContract.MovieEntry.COLUMN_LANGUAGE, "language");
        values_1.put(MovieContract.MovieEntry.COLUMN_VOTE_COUNT, 1);
        values_1.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, 1);
        values_1.put(MovieContract.MovieEntry.COLUMN_POPULARITY, 1);
        values_1.put(MovieContract.MovieEntry.COLUMN_POSTER, "poster_path");
        values_1.put(MovieContract.MovieEntry.COLUMN_VIDEO, 1);
        values_1.put(MovieContract.MovieEntry.COLUMN_ADULT, 1);
        values_1.put(MovieContract.MovieEntry.COLUMN_ORDER, "order");

        ContentValues values_2 = new ContentValues();
        values_2.put(MovieContract.MovieEntry.COLUMN_API_ID, 3);
        values_2.put(MovieContract.MovieEntry.COLUMN_TITLE, "title 3");
        values_2.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, "overview");
        values_2.put(MovieContract.MovieEntry.COLUMN_DATE, "2001-01-01");
        values_2.put(MovieContract.MovieEntry.COLUMN_LANGUAGE, "language");
        values_2.put(MovieContract.MovieEntry.COLUMN_VOTE_COUNT, 1);
        values_2.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, 1);
        values_2.put(MovieContract.MovieEntry.COLUMN_POPULARITY, 1);
        values_2.put(MovieContract.MovieEntry.COLUMN_POSTER, "poster_path");
        values_2.put(MovieContract.MovieEntry.COLUMN_VIDEO, 1);
        values_2.put(MovieContract.MovieEntry.COLUMN_ADULT, 1);
        values_2.put(MovieContract.MovieEntry.COLUMN_ORDER, "order");

        TestUtilities.TestContentObserver observer = TestUtilities.getTestContentObserver();
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.registerContentObserver(
                MovieContract.MovieEntry.CONTENT_URI,
                true,
                observer);

        int insertCount = contentResolver.bulkInsert(
                MovieContract.MovieEntry.CONTENT_URI,
                new ContentValues[] {values_1, values_2});
        assertTrue("Unable to bulk insert itens through Provider",insertCount > 0);

        observer.waitForNotificationOrFail();
        contentResolver.unregisterContentObserver(observer);
    }

    @Test
    public void testQuery() {
        MovieDBHelper dbHelper = new MovieDBHelper(context);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_API_ID, 4);
        values.put(MovieContract.MovieEntry.COLUMN_TITLE, "title 4");
        values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, "overview");
        values.put(MovieContract.MovieEntry.COLUMN_DATE, "2001-01-01");
        values.put(MovieContract.MovieEntry.COLUMN_LANGUAGE, "language");
        values.put(MovieContract.MovieEntry.COLUMN_VOTE_COUNT, 1);
        values.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, 1);
        values.put(MovieContract.MovieEntry.COLUMN_POPULARITY, 1);
        values.put(MovieContract.MovieEntry.COLUMN_POSTER, "poster_path");
        values.put(MovieContract.MovieEntry.COLUMN_VIDEO, 1);
        values.put(MovieContract.MovieEntry.COLUMN_ADULT, 1);
        values.put(MovieContract.MovieEntry.COLUMN_ORDER, "order");
        long rowId = database.insert(
                MovieContract.MovieEntry.TABLE_NAME,
                null,
                values);
        assertTrue("Unable to insert directly into the database",
                rowId != -1);
        database.close();

        Cursor cursor = context.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
        assertTrue("Query failed to return a valid Cursor", cursor != null);
        cursor.close();
    }

    @Test
    public void testDelete() {
        MovieDBHelper dbHelper = new MovieDBHelper(context);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_API_ID, 5);
        values.put(MovieContract.MovieEntry.COLUMN_TITLE, "title 5");
        values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, "overview");
        values.put(MovieContract.MovieEntry.COLUMN_DATE, "2001-01-01");
        values.put(MovieContract.MovieEntry.COLUMN_LANGUAGE, "language");
        values.put(MovieContract.MovieEntry.COLUMN_VOTE_COUNT, 1);
        values.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, 1);
        values.put(MovieContract.MovieEntry.COLUMN_POPULARITY, 1);
        values.put(MovieContract.MovieEntry.COLUMN_POSTER, "poster_path");
        values.put(MovieContract.MovieEntry.COLUMN_VIDEO, 1);
        values.put(MovieContract.MovieEntry.COLUMN_ADULT, 1);
        values.put(MovieContract.MovieEntry.COLUMN_ORDER, "order");
        long rowId = database.insert(
                MovieContract.MovieEntry.TABLE_NAME,
                null,
                values);
        database.close();
        assertTrue("Unable to insert into the database", rowId != -1);

        TestUtilities.TestContentObserver observer = TestUtilities.getTestContentObserver();
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.registerContentObserver(
                MovieContract.MovieEntry.CONTENT_URI,
                true,
                observer);

        Uri uriToDelete = MovieContract.MovieEntry.CONTENT_URI.buildUpon().appendPath("1").build();
        int moviesDeleted = contentResolver.delete(uriToDelete, null, null);
        assertTrue("Unable to delete item in the database", moviesDeleted != 0);

        observer.waitForNotificationOrFail();
        contentResolver.unregisterContentObserver(observer);
    }

    @After
    public void tearDown() {
        deleteAllRecords();
    }

    private void deleteAllRecords() {
        SQLiteDatabase database =
                new MovieDBHelper(InstrumentationRegistry.getTargetContext()).getWritableDatabase();
        database.delete(MovieContract.MovieEntry.TABLE_NAME, null, null);
        database.close();
    }
}
