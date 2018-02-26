package com.guilhermefgl.peliculas.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.guilhermefgl.peliculas.R;
import com.guilhermefgl.peliculas.adapters.MovieAdapter;
import com.guilhermefgl.peliculas.models.MovieResponse;
import com.guilhermefgl.peliculas.services.TheMovieDBService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements MovieAdapter.OnLoadMoreListener {

    @BindView(R.id.main_toolbar)
    Toolbar toolbar;
    @BindView(R.id.main_progress_bar)
    ProgressBar connectingPB;
    @BindView(R.id.main_swipe)
    SwipeRefreshLayout mainSR;
    @BindView(R.id.main_list)
    RecyclerView mainRV;
    @BindView(R.id.error_conection_layout)
    LinearLayout errorConnectionLL;

    private MovieAdapter movieAdapter;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

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

    @OnClick(R.id.error_conection_action)
    public void actionRetry() {
        requestMovies(movieAdapter.getNextPage());
    }

    private void startRequest() {
        connectingPB.setVisibility(View.VISIBLE);
    }

    private void endRequest() {
        mainSR.setRefreshing(false);
        connectingPB.setVisibility(View.GONE);
        movieAdapter.setFinishLoading();
    }

    private void setErrorLayout(boolean hasError) {
        if (hasError) {
            errorConnectionLL.setVisibility(View.VISIBLE);
            mainRV.setVisibility(View.GONE);
        } else {
            errorConnectionLL.setVisibility(View.GONE);
            mainRV.setVisibility(View.VISIBLE);
        }
    }

    private void requestMovies(int page) {
        startRequest();
        TheMovieDBService.getClient().listPopular(page).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call,
                                   @NonNull final Response<MovieResponse> response) {
                movieAdapter.removeLoading();
                if(response.isSuccessful() && response.body() != null) {
                    movieAdapter.insertItens(response.body());
                    setErrorLayout(false);
                } else {
                    setErrorLayout(true);
                }
                endRequest();
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                endRequest();
                setErrorLayout(true);
            }
        });
    }
}
