package com.guilhermefgl.peliculas.models.provider;

import android.content.ContentValues;
import android.support.test.runner.AndroidJUnit4;

import com.guilhermefgl.peliculas.models.Movie;
import com.guilhermefgl.peliculas.services.TheMovieDBService;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class MovieDBHelperTest {

    @Test
    public void testApiResponseDateFormatter() {
        assertEquals("Error: api date formmat was altered",
                TheMovieDBService.RESPONSE_DATE_FORMAT, "yyyy-MM-dd");
        Date formattedDate = null;
        try {
             formattedDate = MovieDBHelper.DATE_FORMATTER.parse("2001-02-01");
        } catch (ParseException e) {
            fail("Error: unnable to parse api date");
        }
        assertNotNull("Error: unable to parse api date",
                formattedDate);

        Calendar cal = Calendar.getInstance();
        cal.setTime(formattedDate);
        assertEquals("Error: unable to parse api day",
                1,
                cal.get(Calendar.DAY_OF_MONTH));
        assertEquals("Error: unable to parse api month",
                1,
                cal.get(Calendar.MONTH)); // month starts with 0
        assertEquals("Error: unable to parse api year",
                2001,
                cal.get(Calendar.YEAR));
    }

    @Test
    public void testBuildContentValue() {
        ArrayList<Movie> movies = null;
        try {
             movies = new ArrayList<Movie>() {{
                add(new Movie() {{
                    setMovieId(1);
                    setTitle("title 1");
                    setOverview("overview");
                    setLanguage("language");
                    setVoteCount(1);
                    setVoteAverage(1.);
                    setPopularity(1.);
                    setPosterPath("poster/path");
                    setVideo(true);
                    setAdult(true);
                    setReleaseDate(MovieDBHelper.DATE_FORMATTER.parse("2001-01-01"));
                }});
                add(new Movie() {{
                    setMovieId(2);
                    setTitle("title 2");
                    setOverview("overview");
                    setLanguage("language");
                    setVoteCount(1);
                    setVoteAverage(1.);
                    setPopularity(1.);
                    setPosterPath("poster/path");
                    setVideo(true);
                    setAdult(true);
                    setReleaseDate(MovieDBHelper.DATE_FORMATTER.parse("2001-01-01"));
                }});
            }};
        } catch (ParseException e) {
            fail("Error: unable to parse api date");
        }

        ContentValues contentValue =
                MovieDBHelper.buildContentValue(movies.get(0), TheMovieDBService.ORDER_FAVORITE);
        assertNotNull(contentValue);
        assertThat(contentValue.getAsInteger(MovieContract.MovieEntry.COLUMN_API_ID), is(1));
        assertThat(contentValue.getAsString(MovieContract.MovieEntry.COLUMN_TITLE), is("title 1"));
        assertThat(contentValue.getAsString(MovieContract.MovieEntry.COLUMN_OVERVIEW), is("overview"));
        assertThat(contentValue.getAsString(MovieContract.MovieEntry.COLUMN_LANGUAGE), is("language"));
        assertThat(contentValue.getAsInteger(MovieContract.MovieEntry.COLUMN_VOTE_COUNT), is(1));
        assertThat(contentValue.getAsDouble(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE), is(1.));
        assertThat(contentValue.getAsDouble(MovieContract.MovieEntry.COLUMN_POPULARITY), is(1.));
        assertThat(contentValue.getAsString(MovieContract.MovieEntry.COLUMN_POSTER), is("poster/path"));
        assertThat(contentValue.getAsInteger(MovieContract.MovieEntry.COLUMN_VIDEO), is(1));
        assertThat(contentValue.getAsInteger(MovieContract.MovieEntry.COLUMN_ADULT), is(1));
        assertThat(contentValue.getAsString(MovieContract.MovieEntry.COLUMN_ORDER), is(TheMovieDBService.ORDER_FAVORITE));

        ContentValues[] contentValues =
                MovieDBHelper.buildContentValues(movies, TheMovieDBService.ORDER_FAVORITE);
        assertNotNull(contentValues);
        assertEquals("Error: wrong content values builder",
                movies.size(),
                contentValues.length);
    }
}
