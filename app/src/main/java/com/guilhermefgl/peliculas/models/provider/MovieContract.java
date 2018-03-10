package com.guilhermefgl.peliculas.models.provider;

import android.net.Uri;
import android.provider.BaseColumns;

import com.guilhermefgl.peliculas.helpers.Constants;

@SuppressWarnings("ALL")
public final class MovieContract {

    private MovieContract() { }

    public static final String PATH = "movie";
    public static final String PATH_WITH_ID = PATH.concat("/#");

    public static final class MovieEntry implements BaseColumns {

        private MovieEntry() { }

        public static final Uri CONTENT_URI;
        static {
            CONTENT_URI = Constants.URI_PROVIDER.buildUpon().appendPath(PATH).build();

        }

        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_API_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_DATE = "releaseDate";
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_VOTE_COUNT = "vote_count";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_POSTER = "poster_path";
        public static final String COLUMN_VIDEO = "has_video";
        public static final String COLUMN_ADULT = "is_adult";
        public static final String COLUMN_ORDER = "order";
    }

    public static final class Orders {

        private Orders() { }

        public static final String RATED = "rated";
        public static final String VIEWS = "views";
        public static final String FAVORITE = "favorite";
    }
}
