package com.guilhermefgl.peliculas.models;

import android.database.MatrixCursor;
import android.support.test.runner.AndroidJUnit4;

import com.guilhermefgl.peliculas.models.provider.MovieContract;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class MovieTest {

    @Test
    public void testCreateFromCursor() {
        String[] movieColumns = {
                MovieContract.MovieEntry.COLUMN_API_ID,
                MovieContract.MovieEntry.COLUMN_TITLE,
                MovieContract.MovieEntry.COLUMN_OVERVIEW,
                MovieContract.MovieEntry.COLUMN_DATE,
                MovieContract.MovieEntry.COLUMN_LANGUAGE,
                MovieContract.MovieEntry.COLUMN_VOTE_COUNT,
                MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
                MovieContract.MovieEntry.COLUMN_POPULARITY,
                MovieContract.MovieEntry.COLUMN_POSTER,
                MovieContract.MovieEntry.COLUMN_VIDEO,
                MovieContract.MovieEntry.COLUMN_ADULT,
                MovieContract.MovieEntry.COLUMN_ORDER
        };

        MatrixCursor cursor = new MatrixCursor(movieColumns, 2);
        cursor.newRow()
                .add(MovieContract.MovieEntry.COLUMN_API_ID, 1)
                .add(MovieContract.MovieEntry.COLUMN_TITLE, "title 1")
                .add(MovieContract.MovieEntry.COLUMN_OVERVIEW, "overview")
                .add(MovieContract.MovieEntry.COLUMN_DATE, "2001-01-01")
                .add(MovieContract.MovieEntry.COLUMN_LANGUAGE, "language")
                .add(MovieContract.MovieEntry.COLUMN_VOTE_COUNT, 1)
                .add(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, 1)
                .add(MovieContract.MovieEntry.COLUMN_POPULARITY, 1)
                .add(MovieContract.MovieEntry.COLUMN_POSTER, "poster_path")
                .add(MovieContract.MovieEntry.COLUMN_VIDEO, 1)
                .add(MovieContract.MovieEntry.COLUMN_ADULT, 1)
                .add(MovieContract.MovieEntry.COLUMN_ORDER, "order");
        cursor.newRow()
                .add(MovieContract.MovieEntry.COLUMN_API_ID, 2)
                .add(MovieContract.MovieEntry.COLUMN_TITLE, "title 2")
                .add(MovieContract.MovieEntry.COLUMN_OVERVIEW, "overview")
                .add(MovieContract.MovieEntry.COLUMN_DATE, "2001-01-01")
                .add(MovieContract.MovieEntry.COLUMN_LANGUAGE, "language")
                .add(MovieContract.MovieEntry.COLUMN_VOTE_COUNT, 1)
                .add(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, 1)
                .add(MovieContract.MovieEntry.COLUMN_POPULARITY, 1)
                .add(MovieContract.MovieEntry.COLUMN_POSTER, "poster_path")
                .add(MovieContract.MovieEntry.COLUMN_VIDEO, 1)
                .add(MovieContract.MovieEntry.COLUMN_ADULT, 1)
                .add(MovieContract.MovieEntry.COLUMN_ORDER, "order");

        List<Movie> movies = Movie.createFromCursor(cursor);
        assertEquals("Error: all movies was not created from cursor", movies.size(), 2);

        assertThat("Error: movie builded wrong (0)", movies.get(0).getTitle(), is("title 1"));
        assertThat("Error: movie builded wrong (1)", movies.get(1).getTitle(), is("title 2"));

        Movie movie1 = movies.get(0);
        assertThat(movie1.getMovieId(), is(1));
        assertThat(movie1.getTitle(), is("title 1"));
        assertThat(movie1.getOverview(), is("overview"));
        assertThat(movie1.getLanguage(), is("language"));
        assertThat(movie1.getVoteCount(), is(1));
        assertThat(movie1.getVoteAverage(), is(1.));
        assertThat(movie1.getPopularity(), is(1.));
        assertThat(movie1.getPosterPath(), is("poster_path"));
        assertThat(movie1.hasVideo(), is(true));
        assertThat(movie1.isAdult(), is(true));
    }

}
