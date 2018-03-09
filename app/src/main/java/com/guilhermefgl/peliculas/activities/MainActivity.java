package com.guilhermefgl.peliculas.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.guilhermefgl.peliculas.R;
import com.guilhermefgl.peliculas.adapters.MovieAdapter;
import com.guilhermefgl.peliculas.helpers.Constants;
import com.guilhermefgl.peliculas.helpers.SnackBarHelper;
import com.guilhermefgl.peliculas.models.Movie;
import com.guilhermefgl.peliculas.models.MovieResponse;
import com.guilhermefgl.peliculas.services.TheMovieDBService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity
        implements MovieAdapter.OnLoadMoreListener, View.OnClickListener, MovieAdapter.OnMovieItemClick, BottomNavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.main_toolbar)
    Toolbar toolbar;
    @BindView(R.id.main_progress_bar)
    ProgressBar connectingPB;
    @BindView(R.id.main_swipe)
    SwipeRefreshLayout mainSR;
    @BindView(R.id.main_list)
    RecyclerView mainRV;
    @BindView(R.id.main_navigation)
    BottomNavigationView orderBNV;
    @BindView(R.id.error_conection_layout)
    LinearLayout errorConnectionLL;

    private MovieAdapter movieAdapter;
    private Snackbar errorSB;
    private Integer currentOrder;

    private final String STATE_MOVIES = MovieResponse.class.getName();
    private final String STATE_ORDER = MenuItem.class.getName();

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
        movieAdapter = new MovieAdapter(mainRV, spanCount, this, this);
        mainRV.setAdapter(movieAdapter);

        mainSR.setColorSchemeColors(getResources().getColor(R.color.accent));
        mainSR.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestMovies(TheMovieDBService.LISTING_FIRST_PAGE);
            }
        });

        orderBNV.setOnNavigationItemSelectedListener(this);

        errorSB = SnackBarHelper.make(this,
                findViewById(R.id.main_layout),
                R.string.error_connection_label,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.error_connection_action, this);

        if(savedInstanceState == null || !savedInstanceState.containsKey(STATE_MOVIES)) {
            currentOrder = R.id.menu_main_popular;
            requestMovies(TheMovieDBService.LISTING_FIRST_PAGE);
        } else {
            movieAdapter.insertItems((MovieResponse) savedInstanceState.getParcelable(STATE_MOVIES));
            if (savedInstanceState.containsKey(STATE_ORDER)) {
                int orderState = savedInstanceState.getInt(STATE_ORDER);
                currentOrder = orderState > 0 ? orderState : R.id.menu_main_popular;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putParcelable(STATE_MOVIES, new MovieResponse(){{
            setResults(movieAdapter.getItems());
            setPage(movieAdapter.getCurrentPage());
            setTotalPages(movieAdapter.getNextPage());
        }});
        bundle.putInt(STATE_ORDER, currentOrder != null ? currentOrder : -1);
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onLoadMore() {
        movieAdapter.insertLoading();
        requestMovies(movieAdapter.getNextPage());
    }

    @Override
    public void onMovieItemClick(Movie movie, ImageView moviePoster) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.Bundles.DETAILS_MOVIE, movie);

        Bundle transition = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            transition = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(this,
                            moviePoster,
                            moviePoster.getTransitionName())
                    .toBundle();
        }

        DetailsActivity.startActivity(this, bundle, transition);
    }

    @Override
    public void onClick(View v) {
        actionRetry();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        currentOrder = item.getItemId();
        requestMovies(TheMovieDBService.LISTING_FIRST_PAGE);
        return true;
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
            if (movieAdapter.getItemCount() > 0) {
                errorSB.show();
            } else {
                errorConnectionLL.setVisibility(View.VISIBLE);
                mainRV.setVisibility(View.GONE);
            }
        } else {
            errorConnectionLL.setVisibility(View.GONE);
            mainRV.setVisibility(View.VISIBLE);
            if (errorSB.isShown()) {
                errorSB.dismiss();
            }
        }
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
                            movieAdapter.insertItems(response.body());
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
