package com.guilhermefgl.peliculas.views.details;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.guilhermefgl.peliculas.R;
import com.guilhermefgl.peliculas.helpers.Constants;
import com.guilhermefgl.peliculas.helpers.PicassoHelper;
import com.guilhermefgl.peliculas.helpers.SnackBarHelper;
import com.guilhermefgl.peliculas.models.Movie;
import com.guilhermefgl.peliculas.models.Review;
import com.guilhermefgl.peliculas.models.ReviewResponse;
import com.guilhermefgl.peliculas.models.Video;
import com.guilhermefgl.peliculas.models.VideoResponse;
import com.guilhermefgl.peliculas.services.LocalStorageFavorite;
import com.guilhermefgl.peliculas.services.TheMovieDBService;
import com.guilhermefgl.peliculas.services.loaders.FavoriteLoader;
import com.guilhermefgl.peliculas.services.loaders.ReviewLoader;
import com.guilhermefgl.peliculas.services.loaders.VideoLoader;
import com.guilhermefgl.peliculas.views.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailsActivity extends BaseActivity implements VideoAdapter.OnVideoItemClick {

    @BindView(R.id.details_toolbar)
    Toolbar toolbar;
    @BindView(R.id.details_image)
    ImageView posterImageView;
    @BindView(R.id.details_title)
    TextView titleTextView;
    @BindView(R.id.details_rating)
    RatingBar voteRatingBar;
    @BindView(R.id.details_views_text)
    TextView viewsTextView;
    @BindView(R.id.details_views_language)
    TextView languageTextView;
    @BindView(R.id.details_adult)
    TextView adultTextView;
    @BindView(R.id.details_date)
    TextView dateTextView;
    @BindView(R.id.details_overview)
    TextView overviewTextView;
    @BindView(R.id.details_videos)
    RecyclerView videosRecyclerView;
    @BindView(R.id.details_reviews)
    RecyclerView reviewsRecyclerView;
    @BindView(R.id.details_videos_layout)
    View videosLayout;
    @BindView(R.id.details_reviews_layout)
    View reviewsLayout;
    @BindView(R.id.details_videos_loading)
    ProgressBar videosLoadingProgressBar;
    @BindView(R.id.details_reviews_loading)
    ProgressBar reviewsLoadingProgressBar;

    private static final Integer RESULT_DETAILS = 1001;
    private final String STATE_VIDEOS = VideoResponse.class.getName();
    private final String STATE_REVIEWS = ReviewResponse.class.getName();

    private VideoAdapter videoAdapter;
    private ReviewAdapter reviewAdapter;
    private Movie movie;
    private boolean isFavorite;
    private LoaderManager.LoaderCallbacks<VideoResponse> videoLoaderCallback;
    private LoaderManager.LoaderCallbacks<ReviewResponse> reviewLoaderCallback;
    private final SimpleDateFormat DATE_FORMATTER
            = new SimpleDateFormat(Constants.DATE_FORMATTER, Locale.getDefault());

    @SuppressLint("RestrictedApi")
    public static void startActivity(BaseActivity activity, Bundle bundle, Bundle transition) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && transition != null) {
            activity.startActivityForResult(
                    new Intent(activity, DetailsActivity.class).putExtras(bundle),
                    RESULT_DETAILS,
                    transition);
        } else {
            activity.startActivityForResult(
                    new Intent(activity, DetailsActivity.class).putExtras(bundle),
                    RESULT_DETAILS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setResult(Activity.RESULT_CANCELED, new Intent());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        if (getIntent().getExtras() != null) {
            isFavorite = getIntent().getBooleanExtra(Constants.Bundles.DETAILS_FAVOTITE, false);
            if (getIntent().hasExtra(Constants.Bundles.DETAILS_MOVIE)) {
                Object extra = getIntent().getExtras().get(Constants.Bundles.DETAILS_MOVIE);
                if (extra != null && extra instanceof Movie) {
                    movie = (Movie) extra;
                }
            }
        }

        setupLoaders();
        if (movie == null || movie.getMovieId() == null) {
            finish();
        } else {
            setupView();

            if (savedInstanceState == null || !savedInstanceState.containsKey(STATE_VIDEOS)
                    || !savedInstanceState.containsKey(STATE_REVIEWS)) {
                requestVideos();
                requestReviews();
            } else {
                if (savedInstanceState.containsKey(STATE_VIDEOS)) {
                    ArrayList<Video> videosResponse =
                            savedInstanceState.getParcelableArrayList(STATE_VIDEOS);
                    if (videosResponse != null && !videosResponse.isEmpty()) {
                        videoAdapter.setItems(videosResponse);
                    } else {
                        requestVideos();
                    }
                } else {
                    requestVideos();
                }

                if (savedInstanceState.containsKey(STATE_REVIEWS)) {
                    ArrayList<Review> reviewsState =
                            savedInstanceState.getParcelableArrayList(STATE_REVIEWS);
                    if (reviewsState != null && !reviewsState.isEmpty()) {
                        reviewAdapter.setItems(reviewsState);
                    } else {
                        requestReviews();
                    }
                } else {
                    requestReviews();
                }
            }
        }
    }

    private void setupLoaders() {
        videoLoaderCallback = new LoaderManager.LoaderCallbacks<VideoResponse>() {
            @NonNull
            @Override
            public Loader<VideoResponse> onCreateLoader(int id, @Nullable Bundle args) {
                videosLoadingProgressBar.setVisibility(View.VISIBLE);
                Integer movieId = args != null ? args.getInt(VideoLoader.BUNDLE_ID) : null;
                return new VideoLoader(DetailsActivity.this, movieId);
            }

            @Override
            public void onLoadFinished(@NonNull Loader<VideoResponse> loader, VideoResponse data) {
                videosLoadingProgressBar.setVisibility(View.GONE);
                if(data != null) {
                    if (data.getResults() != null && !data.getResults().isEmpty()){
                        videoAdapter.setItems(data.getResults());
                    } else if (videoAdapter.isEmpty()) {
                        videosLayout.setVisibility(View.GONE);
                    }
                }
                setErrorLayout(R.id.details_video_error_layout, videoAdapter.isEmpty());
            }

            @Override
            public void onLoaderReset(@NonNull Loader<VideoResponse> loader) { }
        };
        reviewLoaderCallback = new LoaderManager.LoaderCallbacks<ReviewResponse>() {
            @NonNull
            @Override
            public Loader<ReviewResponse> onCreateLoader(int id, @Nullable Bundle args) {
                reviewsLoadingProgressBar.setVisibility(View.VISIBLE);
                Integer movieId = args != null ? args.getInt(ReviewLoader.BUNDLE_ID) : null;
                return new ReviewLoader(DetailsActivity.this, movieId);
            }

            @Override
            public void onLoadFinished(@NonNull Loader<ReviewResponse> loader, ReviewResponse data) {
                reviewsLoadingProgressBar.setVisibility(View.GONE);
                if(data != null) {
                    if (data.getResults() != null && !data.getResults().isEmpty()) {
                        reviewAdapter.setItems(data.getResults());
                    } else if (reviewAdapter.isEmpty()) {
                        reviewsLayout.setVisibility(View.GONE);
                    }
                }
                setErrorLayout(R.id.details_review_error_layout, reviewAdapter.isEmpty());
            }

            @Override
            public void onLoaderReset(@NonNull Loader<ReviewResponse> loader) { }
        };
    }

    private void setupView() {
        titleTextView.setText(movie.getTitle());
        voteRatingBar.setRating((float) (movie.getVoteAverage() / 2));
        viewsTextView.setText(String.valueOf(movie.getPopularity()));
        languageTextView.setText(movie.getLanguage());
        adultTextView.setVisibility(movie.isAdult() ? View.VISIBLE : View.GONE);
        overviewTextView.setText(movie.getOverview());
        if (movie.getReleaseDate() != null) {
            dateTextView.setText(DATE_FORMATTER.format(movie.getReleaseDate()));
        }
        PicassoHelper.loadImage(this,
                TheMovieDBService.buildImageURL(movie.getPosterPath()),
                posterImageView, R.mipmap.movie_background, R.mipmap.error_background);

        videoAdapter = new VideoAdapter(null, this);
        videosRecyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));
        videosRecyclerView.setAdapter(videoAdapter);

        reviewAdapter = new ReviewAdapter();
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        reviewsRecyclerView.setAdapter(reviewAdapter);
        reviewsRecyclerView.setNestedScrollingEnabled(false);
        reviewsRecyclerView.setHasFixedSize(false);
        reviewsRecyclerView.addItemDecoration(
                new DividerItemDecoration(reviewsRecyclerView.getContext(), LinearLayoutManager.VERTICAL));
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putParcelableArrayList(STATE_VIDEOS, videoAdapter.getItens());
        bundle.putParcelableArrayList(STATE_REVIEWS, reviewAdapter.getItens());
        super.onSaveInstanceState(bundle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);

        final MenuItem menuFavorite = menu.findItem(R.id.menu_details_favorite);
        if (isFavorite) {
            menuFavorite.setVisible(true);
            menuFavorite.setIcon(R.drawable.ic_favorite_full);
        } else {
            Bundle queryBundle = new Bundle();
            queryBundle.putInt(FavoriteLoader.BUNDLE_ID, movie.getMovieId());
            getLoaderManager().initLoader(
                    FavoriteLoader.LOADER_ID,
                    queryBundle,
                    new android.app.LoaderManager.LoaderCallbacks<Cursor>() {
                        @Override
                        public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
                            Integer movieId = args != null ? args.getInt(FavoriteLoader.BUNDLE_ID) : null;
                            return new FavoriteLoader(DetailsActivity.this, movieId);
                        }

                        @Override
                        public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
                            menuFavorite.setVisible(true);
                            if (data != null && data.getCount() > 0) {
                                menuFavorite.setIcon(R.drawable.ic_favorite_full);
                                isFavorite = true;
                            }
                        }

                        @Override
                        public void onLoaderReset(android.content.Loader<Cursor> loader) { }
                    });
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_details_share:
                shareMovie();
                return true;
            case R.id.menu_details_favorite:
                toggleFavoriteMovie(item);
                return true;
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onVideoItemClick(Video video) {
        try {
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(TheMovieDBService.buildYoutubeUrl(video.getKey()))));
        } catch (Exception e) {
            SnackBarHelper.make(this,
                    findViewById(R.id.details_layout),
                    R.string.error_open_movie,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    private void shareMovie() {
        if (movie == null) {
            return;
        }

        try {
            startActivity(Intent.createChooser(
                    new Intent(android.content.Intent.ACTION_SEND)
                            .setType("text/plain")
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                            .putExtra(Intent.EXTRA_SUBJECT, movie.getTitle())
                            .putExtra(Intent.EXTRA_TEXT, TheMovieDBService.buildWebURL(movie.getMovieId())),
                    getString(R.string.menu_details_share)));
        } catch (Exception e) {
            SnackBarHelper.make(this,
                    findViewById(R.id.details_layout),
                    R.string.error_share_label,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    private void toggleFavoriteMovie(MenuItem item) {
        isFavorite = !isFavorite;
        item.setIcon(isFavorite ? R.drawable.ic_favorite_full : R.drawable.ic_favorite_empty);
        new LocalStorageFavorite().execute(
                new LocalStorageFavorite.MovieParams(
                        this,
                        movie,
                        isFavorite));
        setResult(Activity.RESULT_OK,
                new Intent()
                        .putExtra(Constants.Bundles.DETAILS_FAVOTITE, isFavorite)
                        .putExtra(Constants.Bundles.DETAILS_MOVIE, movie));
    }

    @SuppressWarnings("ConstantConditions")
    @OnClick(R.id.details_video_error_layout)
    void requestVideos() {
        if (isDeviceConnected()) {
            Bundle queryBundle = new Bundle();
            queryBundle.putInt(VideoLoader.BUNDLE_ID, movie.getMovieId());
            LoaderManager loaderManager = getSupportLoaderManager();
            if (loaderManager.getLoader(VideoLoader.LOADER_ID) == null) {
                loaderManager.initLoader(VideoLoader.LOADER_ID, queryBundle, videoLoaderCallback);
            } else {
                loaderManager.restartLoader(VideoLoader.LOADER_ID, queryBundle, videoLoaderCallback);
            }
        } else {
            setErrorLayout(R.id.details_video_error_layout, videoAdapter.isEmpty());
        }
    }

    @SuppressWarnings("ConstantConditions")
    @OnClick(R.id.details_review_error_layout)
    void requestReviews() {
        if (isDeviceConnected()) {
            Bundle queryBundle = new Bundle();
            queryBundle.putInt(ReviewLoader.BUNDLE_ID, movie.getMovieId());
            LoaderManager loaderManager = getSupportLoaderManager();
            if (loaderManager.getLoader(ReviewLoader.LOADER_ID) == null) {
                loaderManager.initLoader(ReviewLoader.LOADER_ID, queryBundle, reviewLoaderCallback);
            } else {
                loaderManager.restartLoader(ReviewLoader.LOADER_ID, queryBundle, reviewLoaderCallback);
            }
        } else {
            setErrorLayout(R.id.details_review_error_layout, reviewAdapter.isEmpty());
        }
    }

    private void setErrorLayout(int idLayout, boolean hasError) {
        findViewById(idLayout).setVisibility(hasError ? View.VISIBLE : View.GONE);
    }
}
