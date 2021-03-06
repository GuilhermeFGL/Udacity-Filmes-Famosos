package com.guilhermefgl.peliculas.views.main;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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
import com.guilhermefgl.peliculas.helpers.Constants;
import com.guilhermefgl.peliculas.helpers.SnackBarHelper;
import com.guilhermefgl.peliculas.models.Movie;
import com.guilhermefgl.peliculas.models.MovieResponse;
import com.guilhermefgl.peliculas.services.LocalStorageReader;
import com.guilhermefgl.peliculas.services.TheMovieDBService;
import com.guilhermefgl.peliculas.services.loaders.MovieLoader;
import com.guilhermefgl.peliculas.views.BaseActivity;
import com.guilhermefgl.peliculas.views.details.DetailsActivity;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity
        implements MainAdapter.OnLoadMoreListener, View.OnClickListener,
        MainAdapter.OnMovieItemClick, LoaderManager.LoaderCallbacks<MovieResponse>,
        LocalStorageReader.ReaderCallBack, SwipeRefreshLayout.OnRefreshListener, OnTabSelectListener {

    @BindView(R.id.main_toolbar)
    Toolbar toolbar;
    @BindView(R.id.main_progress_bar)
    ProgressBar connectingProgressBar;
    @BindView(R.id.main_swipe)
    SwipeRefreshLayout mainSwipeRefreshLayout;
    @BindView(R.id.main_list)
    RecyclerView movieRecyclerView;
    @BindView(R.id.error_conection_layout)
    LinearLayout errorConnectionLayout;
    @BindView(R.id.main_navigation)
    BottomBar navigationBottomBar;

    private MainAdapter mainAdapter;
    private Snackbar errorSnackbar;
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

        errorSnackbar = SnackBarHelper.make(this,
                findViewById(R.id.main_navigation),
                R.string.error_connection_label,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.error_connection_action, this);

        int spanCount = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT ?
                MainAdapter.GRID_PORTRAIT : MainAdapter.GRID_LANDSCAPE;
        movieRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        mainAdapter = new MainAdapter(movieRecyclerView, spanCount, this, this);
        movieRecyclerView.setAdapter(mainAdapter);

        mainSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.accent));
        mainSwipeRefreshLayout.setOnRefreshListener(this);

        navigationBottomBar.setOnTabSelectListener(this);

        if(savedInstanceState == null || !savedInstanceState.containsKey(STATE_MOVIES)) {
            currentOrder = R.id.menu_main_popular;
        } else {
            mainAdapter.insertItems((MovieResponse) savedInstanceState.getParcelable(STATE_MOVIES));
            if (savedInstanceState.containsKey(STATE_ORDER)) {
                int orderState = savedInstanceState.getInt(STATE_ORDER);
                currentOrder = orderState > 0 ? orderState : R.id.menu_main_popular;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putParcelable(STATE_MOVIES, new MovieResponse(){{
            setResults(mainAdapter.getItems());
            setPage(mainAdapter.getCurrentPage());
            setTotalPages(mainAdapter.getNextPage());
        }});
        bundle.putInt(STATE_ORDER, currentOrder != null ? currentOrder : -1);
        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && currentOrder == R.id.menu_main_favorite) {
            boolean isFavorite = data.getBooleanExtra(Constants.Bundles.DETAILS_FAVOTITE, false);
            Movie movie = data.getParcelableExtra(Constants.Bundles.DETAILS_MOVIE);
            if (!isFavorite) {
                mainAdapter.removeItem(movie);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onLoadMore() {
        mainAdapter.insertLoading();
        requestMovies(mainAdapter.getNextPage());
    }

    @Override
    public void onRefresh() {
        requestMovies(TheMovieDBService.LISTING_FIRST_PAGE);
    }

    @Override
    public void onTabSelected(int tabId) {
        currentOrder = tabId;
        requestMovies(TheMovieDBService.LISTING_FIRST_PAGE);
    }

    @Override
    public void onMovieItemClick(Movie movie, ImageView moviePoster) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.Bundles.DETAILS_MOVIE, movie);
        if(currentOrder == R.id.menu_main_favorite) {
            bundle.putBoolean(Constants.Bundles.DETAILS_FAVOTITE, true);
        }

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

    @OnClick(R.id.error_conection_action)
    public void actionRetry() {
        requestMovies(mainAdapter.getNextPage());
    }

    @NonNull
    @Override
    public Loader<MovieResponse> onCreateLoader(int id, @Nullable Bundle args) {
        startRequest();
        String order = args != null ? args.getString(MovieLoader.BUNDLE_ORDER) : null;
        Integer page = args != null ? args.getInt(MovieLoader.BUNDLE_PAGE) : null;
        return new MovieLoader(this, order, page);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<MovieResponse> loader, MovieResponse data) {
        if (isRequesting()) {
            if (data != null) {
                mainAdapter.removeLoading();
                mainAdapter.insertItems(data);
                setErrorLayout(false);
            } else {
                setErrorLayout(true);
            }
            endRequest();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<MovieResponse> loader) { }

    @Override
    public void onReadMovies(final ArrayList<Movie> movies) {
        if (movies != null) {
            mainAdapter.insertItems(new MovieResponse() {{
                setPage(TheMovieDBService.LISTING_FIRST_PAGE);
                setTotalPages(TheMovieDBService.LISTING_FIRST_PAGE);
                setResults(movies);
            }});
            errorConnectionLayout.setVisibility(View.GONE);
            movieRecyclerView.setVisibility(View.VISIBLE);
        }

        if (currentOrder == R.id.menu_main_favorite) {
            endRequest();
            setErrorLayout(false);
        }
    }

    private boolean isRequesting() {
        return connectingProgressBar.getVisibility() == View.VISIBLE;
    }

    private void startRequest() {
        connectingProgressBar.setVisibility(View.VISIBLE);
    }

    private void endRequest() {
        mainSwipeRefreshLayout.setRefreshing(false);
        connectingProgressBar.setVisibility(View.GONE);
        mainAdapter.setFinishLoading();
    }

    private void setErrorLayout(boolean hasError) {
        if (hasError) {
            errorSnackbar.show();
            if (mainAdapter.isEmpty()) {
                errorConnectionLayout.setVisibility(View.VISIBLE);
                movieRecyclerView.setVisibility(View.GONE);
            }
        } else {
            errorConnectionLayout.setVisibility(View.GONE);
            movieRecyclerView.setVisibility(View.VISIBLE);
            if (errorSnackbar.isShown()) {
                errorSnackbar.dismiss();
            }
        }
    }

    private void requestMovies(int page) {
        String order = getOrder();

        if (page == TheMovieDBService.LISTING_FIRST_PAGE) {
            new LocalStorageReader(this).execute(
                    new LocalStorageReader.MovieParams(this, order));
        }

        if (order.equals(TheMovieDBService.ORDER_FAVORITE)) {
            return;
        }

        if (isDeviceConnected()) {
            Bundle queryBundle = new Bundle();
            queryBundle.putInt(MovieLoader.BUNDLE_PAGE, page);
            queryBundle.putString(MovieLoader.BUNDLE_ORDER, order);
            LoaderManager loaderManager = getSupportLoaderManager();
            if (loaderManager.getLoader(MovieLoader.LOADER_ID) == null) {
                loaderManager.initLoader(MovieLoader.LOADER_ID, queryBundle, this);
            } else {
                loaderManager.restartLoader(MovieLoader.LOADER_ID, queryBundle, this);
            }
        } else {
            setErrorLayout(true);
        }
    }

    private String getOrder() {
        switch (currentOrder) {
            case R.id.menu_main_popular:
                return TheMovieDBService.ORDER_POPULAR;
            case R.id.menu_main_top_rated:
                return TheMovieDBService.ORDER_TOP_RATED;
            case R.id.menu_main_favorite:
                return TheMovieDBService.ORDER_FAVORITE;
            default:
                return TheMovieDBService.ORDER_POPULAR;
        }
    }
}
