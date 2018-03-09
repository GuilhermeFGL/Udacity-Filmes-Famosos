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
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class TheMovieDBService {

    private static final String BASE_URL = "http://api.themoviedb.org/3/movie/";
    private static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185/";
    private static final String WEB_URL = "https://www.themoviedb.org/movie/";
    private static final String DATE_FORMATTER = "yyyy-MM-dd";
    public static final String ORDER_POPULAR = "popular";
    public static final String ORDER_TOP_RATED = "top_rated";
    public static final int LISTING_FIRST_PAGE = 1;

    private static final OkHttpClient httpClient;
    private static final Gson gson;
    static {
        httpClient = new OkHttpClient.Builder()
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
                }).addInterceptor(new HttpLoggingInterceptor().setLevel(
                        BuildConfig.LOGGER_ENABLED ?
                                HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE)
                ).build();
    }
    static {
         gson = new GsonBuilder()
                .setDateFormat(DATE_FORMATTER)
                .create();
    }

    private TheMovieDBService() { }

    public static TheMovieDBClient getClient() {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(TheMovieDBClient.class);
    }

    @NonNull
    public static String buildImageURL(String movieId) {
        return IMAGE_BASE_URL.concat(movieId);
    }

    @NonNull
    public static String buildWebURL(int movieId) {
        return WEB_URL.concat(String.valueOf(movieId));
    }

    public interface TheMovieDBClient {

        @GET("{order}")
        Call<MovieResponse> list(@Path("order") String order, @Query("page") int page);
    }
}
