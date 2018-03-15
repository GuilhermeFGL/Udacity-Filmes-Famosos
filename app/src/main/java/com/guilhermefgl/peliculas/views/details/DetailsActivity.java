package com.guilhermefgl.peliculas.views.details;

import android.content.Intent;
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
import com.guilhermefgl.peliculas.services.TheMovieDBService;
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
    ImageView posterIV;
    @BindView(R.id.details_title)
    TextView titleTV;
    @BindView(R.id.details_rating)
    RatingBar voteRB;
    @BindView(R.id.details_views_text)
    TextView viewsTV;
    @BindView(R.id.details_views_language)
    TextView languageTV;
    @BindView(R.id.details_adult)
    TextView adultTV;
    @BindView(R.id.details_date)
    TextView dateTV;
    @BindView(R.id.details_overview)
    TextView overviewTV;
    @BindView(R.id.details_videos)
    RecyclerView videosRV;
    @BindView(R.id.details_reviews)
    RecyclerView reviewsRV;
    @BindView(R.id.details_videos_loading)
    ProgressBar videosLoadingPB;
    @BindView(R.id.details_reviews_loading)
    ProgressBar reviewsLoadingPB;

    private final String STATE_VIDEOS = VideoResponse.class.getName();
    private final String STATE_REVIEWS = ReviewResponse.class.getName();

    private VideoAdapter videoAdapter;
    private ReviewAdapter reviewAdapter;
    private Movie movie;
    private LoaderManager.LoaderCallbacks<VideoResponse> videoLoaderCallback;
    private LoaderManager.LoaderCallbacks<ReviewResponse> reviewLoaderCallback;
    private final SimpleDateFormat DATE_FORMATTER
            = new SimpleDateFormat(Constants.DATE_FORMATTER, Locale.getDefault());

    public static void startActivity(BaseActivity activity, Bundle bundle, Bundle transition) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && transition != null) {
            activity.startActivity(
                    new Intent(activity, DetailsActivity.class).putExtras(bundle),
                    transition);
        } else {
            activity.startActivity(
                    new Intent(activity, DetailsActivity.class).putExtras(bundle));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        if (getIntent().getExtras() != null && getIntent().hasExtra(Constants.Bundles.DETAILS_MOVIE)) {
            Object extra = getIntent().getExtras().get(Constants.Bundles.DETAILS_MOVIE);
            if (extra != null && extra instanceof Movie) {
                movie = (Movie) extra;
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

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putParcelableArrayList(STATE_VIDEOS, videoAdapter.getItens());
        bundle.putParcelableArrayList(STATE_REVIEWS, reviewAdapter.getItens());
        super.onSaveInstanceState(bundle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
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

    private void setupLoaders() {
        videoLoaderCallback = new LoaderManager.LoaderCallbacks<VideoResponse>() {
            @NonNull
            @Override
            public Loader<VideoResponse> onCreateLoader(int id, @Nullable Bundle args) {
                videosLoadingPB.setVisibility(View.VISIBLE);
                Integer movieId = args != null ? args.getInt(VideoLoader.BUNDLE_ID) : null;
                return new VideoLoader(DetailsActivity.this, movieId);
            }

            @Override
            public void onLoadFinished(@NonNull Loader<VideoResponse> loader, VideoResponse data) {
                videosLoadingPB.setVisibility(View.GONE);
                if(data != null && data.getResults() != null && !data.getResults().isEmpty()) {
                    videoAdapter.setItems(data.getResults());
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
                reviewsLoadingPB.setVisibility(View.VISIBLE);
                Integer movieId = args != null ? args.getInt(ReviewLoader.BUNDLE_ID) : null;
                return new ReviewLoader(DetailsActivity.this, movieId);
            }

            @Override
            public void onLoadFinished(@NonNull Loader<ReviewResponse> loader, ReviewResponse data) {
                reviewsLoadingPB.setVisibility(View.GONE);
                if(data != null && data.getResults() != null && !data.getResults().isEmpty()) {
                    reviewAdapter.setItems(data.getResults());
                }
                setErrorLayout(R.id.details_review_error_layout, reviewAdapter.isEmpty());
            }

            @Override
            public void onLoaderReset(@NonNull Loader<ReviewResponse> loader) { }
        };
        getSupportLoaderManager().initLoader(VideoLoader.LOADER_ID, null, videoLoaderCallback);
        getSupportLoaderManager().initLoader(ReviewLoader.LOADER_ID, null, reviewLoaderCallback);
    }

    private void setupView() {
        titleTV.setText(movie.getTitle());
        voteRB.setRating((float) (movie.getVoteAverage() / 2));
        viewsTV.setText(String.valueOf(movie.getPopularity()));
        languageTV.setText(movie.getLanguage());
        adultTV.setVisibility(movie.isAdult() ? View.VISIBLE : View.GONE);
        overviewTV.setText(movie.getOverview());
        if (movie.getReleaseDate() != null) {
            dateTV.setText(DATE_FORMATTER.format(movie.getReleaseDate()));
        }
        PicassoHelper.loadImage(this,
                TheMovieDBService.buildImageURL(movie.getPosterPath()),
                posterIV, R.mipmap.movie_background, R.mipmap.error_background);

        videoAdapter = new VideoAdapter(null, this);
        videosRV.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));
        videosRV.setAdapter(videoAdapter);

        reviewAdapter = new ReviewAdapter();
        reviewsRV.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        reviewsRV.setAdapter(reviewAdapter);
        reviewsRV.setNestedScrollingEnabled(false);
        reviewsRV.setHasFixedSize(false);
        reviewsRV.addItemDecoration(
                new DividerItemDecoration(reviewsRV.getContext(), LinearLayoutManager.VERTICAL));
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
        // TODO
    }

    @SuppressWarnings("ConstantConditions")
    @OnClick(R.id.details_video_error_layout)
    void requestVideos() {
        Bundle queryBundle = new Bundle();
        queryBundle.putInt(VideoLoader.BUNDLE_ID, movie.getMovieId());
        LoaderManager loaderManager = getSupportLoaderManager();
        if (loaderManager.getLoader(VideoLoader.LOADER_ID) == null) {
            loaderManager.initLoader(VideoLoader.LOADER_ID, queryBundle, videoLoaderCallback);
        } else {
            loaderManager.restartLoader(VideoLoader.LOADER_ID, queryBundle, videoLoaderCallback);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @OnClick(R.id.details_review_error_layout)
    void requestReviews() {
        Bundle queryBundle = new Bundle();
        queryBundle.putInt(ReviewLoader.BUNDLE_ID, movie.getMovieId());
        LoaderManager loaderManager = getSupportLoaderManager();
        if (loaderManager.getLoader(ReviewLoader.LOADER_ID) == null) {
            loaderManager.initLoader(ReviewLoader.LOADER_ID, queryBundle, reviewLoaderCallback);
        } else {
            loaderManager.restartLoader(ReviewLoader.LOADER_ID, queryBundle, reviewLoaderCallback);
        }
    }

    private void setErrorLayout(int idLayout, boolean hasError) {
        findViewById(idLayout).setVisibility(hasError ? View.VISIBLE : View.GONE);
    }
}
