package com.guilhermefgl.peliculas.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.guilhermefgl.peliculas.R;
import com.guilhermefgl.peliculas.adapters.MovieAdapter;
import com.guilhermefgl.peliculas.models.MovieResponse;
import com.guilhermefgl.peliculas.services.TheMovieDBService;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements MovieAdapter.OnLoadMoreListener {

    @BindView(R.id.main_swipe)
    SwipeRefreshLayout mainSR;
    @BindView(R.id.main_list)
    RecyclerView mainRV;

    private MovieAdapter movieAdapter;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mainRV.setLayoutManager(new GridLayoutManager(this, 2));
        movieAdapter = new MovieAdapter(this, mainRV, this);
        mainRV.setAdapter(movieAdapter);
        mainSR.setColorSchemeColors(getResources().getColor(R.color.accent));
        mainSR.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestMovies(1);
            }
        });

        requestMovies(1);
    }

    @Override
    public void onLoadMore() {
        movieAdapter.insertLoading();
        requestMovies(movieAdapter.getNextPage());
    }

    private void endRequest() {
        mainSR.setRefreshing(false);
        movieAdapter.setFinishLoading();
    }

    private void requestMovies(int page) {
        TheMovieDBService.getClient().listPopular(page).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call,
                                   @NonNull final Response<MovieResponse> response) {

                endRequest();
                movieAdapter.removeLoading();
                if(response.isSuccessful() && response.body() != null) {
                    movieAdapter.insertItens(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                endRequest();
            }
        });
    }
}
