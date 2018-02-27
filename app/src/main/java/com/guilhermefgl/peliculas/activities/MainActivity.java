package com.guilhermefgl.peliculas.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
    private int currentOrder;

    public static void startActivity(BaseActivity activity) {
        activity.startActivity(
                new Intent(activity, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        int spanCount = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT ? 2 : 4;
        mainRV.setLayoutManager(new GridLayoutManager(this, spanCount));
        movieAdapter = new MovieAdapter(this, mainRV, this, spanCount);
        mainRV.setAdapter(movieAdapter);
        mainSR.setColorSchemeColors(getResources().getColor(R.color.accent));
        mainSR.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestMovies(1);
            }
        });

        currentOrder = R.id.menu_main_popular;
        requestMovies(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_main_popular
                || item.getItemId() == R.id.menu_main_top_rated) {
            requestMoviesBy(item);
        }
        return super.onOptionsItemSelected(item);
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

    private void requestMoviesBy(MenuItem order) {
        currentOrder = order.getItemId();
        toolbar.setTitle(order.getTitle());
        requestMovies(1);
    }

    private void requestMovies(int page) {
        startRequest();
        String order = currentOrder == R.id.menu_main_popular ?
                TheMovieDBService.ORDER_POPULAR : TheMovieDBService.ORDER_TOP_RATED;
        TheMovieDBService.getClient().list(order, page).enqueue(
                new Callback<MovieResponse>() {
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
