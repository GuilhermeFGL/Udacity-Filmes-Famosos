package com.guilhermefgl.peliculas.services;


import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.guilhermefgl.peliculas.BuildConfig;
import com.guilhermefgl.peliculas.models.MovieResponse;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class TheMovieDBService {

    private static OkHttpClient httpClient;
    private static Gson gson;

    static { httpClient = new OkHttpClient.Builder()
            .addInterceptor(new Interceptor() {
                @Override
                public Response intercept(@NonNull Chain chain) throws IOException {
                    Request request = chain.request();
                    HttpUrl originalHttpUrl = request.url();
                    HttpUrl url = originalHttpUrl.newBuilder()
                            .addQueryParameter("api_key", BuildConfig.API_KEY)
                            .build();
                    return chain.proceed(request.newBuilder().url(url).build());
                }
            }).build();
    }

    static {
         gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .create();
    }

    private TheMovieDBService() { }

    public static TheMovieDBClient getClient() {
        return new Retrofit.Builder()
                .baseUrl("http://api.themoviedb.org/3/movie/")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(TheMovieDBClient.class);
    }

    public interface TheMovieDBClient {

        @GET("popular")
        Call<MovieResponse> listPopular();

        @GET("top_rated")
        Call<MovieResponse> listTopRated();

    }

}
