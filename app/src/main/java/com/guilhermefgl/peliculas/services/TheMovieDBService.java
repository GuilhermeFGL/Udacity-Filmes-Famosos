package com.guilhermefgl.peliculas.services;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.guilhermefgl.peliculas.BuildConfig;
import com.guilhermefgl.peliculas.models.MovieResponse;
import com.guilhermefgl.peliculas.models.ReviewResponse;
import com.guilhermefgl.peliculas.models.VideoResponse;

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

public final class TheMovieDBService {

    private static final String BASE_URL = "http://api.themoviedb.org/3/movie/";
    private static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185/";
    private static final String IMAGE_YOUTUBE_BASE_URL = "http://www.youtube.com/watch?v=";
    private static final String IMAGE_YOUTUBE_THUMBNAIL_BASE_URL = "https://img.youtube.com/vi/%s/0.jpg";
    private static final String WEB_URL = "https://www.themoviedb.org/movie/";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String ORDER_POPULAR = "popular";
    public static final String ORDER_TOP_RATED = "top_rated";
    public static final String ORDER_FAVORITE = "favorite";
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
                .setDateFormat(DATE_FORMAT)
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
    public static String buildYoutubeUrl(String youtubeId) {
        return IMAGE_YOUTUBE_BASE_URL.concat(youtubeId);
    }

    @NonNull
    public static String buildYoutubeThumbnailUrl(String youtubeId) {
        return String.format(IMAGE_YOUTUBE_THUMBNAIL_BASE_URL, youtubeId);
    }

    @NonNull
    public static String buildWebURL(int movieId) {
        return WEB_URL.concat(String.valueOf(movieId));
    }

    public interface TheMovieDBClient {

        @GET("{order}")
        Call<MovieResponse> listMovies(@Path("order") String order, @Query("page") Integer page);

        @GET("{id}/videos")
        Call<VideoResponse> listVideos(@Path("id") Integer movieId);

        @GET("{id}/reviews")
        Call<ReviewResponse> listReviews(@Path("id") Integer movieId);
    }
}
