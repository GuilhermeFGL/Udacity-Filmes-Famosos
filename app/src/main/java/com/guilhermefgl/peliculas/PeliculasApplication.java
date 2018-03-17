package com.guilhermefgl.peliculas;

import android.app.Application;

import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import pl.com.salsoft.sqlitestudioremote.SQLiteStudioService;

public class PeliculasApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.LOGGER_ENABLED) {
            SQLiteStudioService.instance().start(this);
        }

        setPicasso();
    }

    public void setPicasso() {
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setLoggingEnabled(BuildConfig.LOGGER_ENABLED);
        Picasso.setSingletonInstance(built);
    }
}
