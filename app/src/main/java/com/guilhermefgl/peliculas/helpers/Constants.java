package com.guilhermefgl.peliculas.helpers;

import android.net.Uri;

import com.guilhermefgl.peliculas.BuildConfig;
import com.guilhermefgl.peliculas.models.Movie;

public final class Constants {

    private Constants() { }

    public static final String AUTHORITY_PROVIDER = BuildConfig.APPLICATION_ID;
    public static final Uri URI_PROVIDER = Uri.parse("content://".concat(AUTHORITY_PROVIDER));

    public static final String DATE_FORMATTER = "dd MMMM yyyy";

    public static class Bundles {

        private Bundles() { }

        public static final String DETAILS_MOVIE = Movie.class.getName();

    }
}
