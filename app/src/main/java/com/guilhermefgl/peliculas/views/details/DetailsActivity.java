package com.guilhermefgl.peliculas.views.details;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import com.guilhermefgl.peliculas.models.ReviewResponse;
import com.guilhermefgl.peliculas.models.Video;
import com.guilhermefgl.peliculas.models.VideoResponse;
import com.guilhermefgl.peliculas.services.TheMovieDBService;
import com.guilhermefgl.peliculas.views.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    private VideoAdapter videoAdapter;
    private ReviewAdapter reviewAdapter;
    private Movie movie;
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

        if (movie == null || movie.getMovieId() == null) {
            finish();
        } else {
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

            requestVideosAndReviews();
        }
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
                    R.string.error_open_trailer,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.error_connection_action)
    public void requestVideosAndReviews() {
        requestVideos(movie.getMovieId());
        requestReviews(movie.getMovieId());
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
    private void requestVideos(Integer movieId) {
        videosLoadingPB.setVisibility(View.VISIBLE);
        TheMovieDBService.getClient().listVideos(movieId).enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(@NonNull Call<VideoResponse> call,
                                   @NonNull Response<VideoResponse> response) {
                videosLoadingPB.setVisibility(View.GONE);
                if (response.isSuccessful()
                        && response.body() != null && !response.body().getResults().isEmpty()) {
                    videoAdapter.setItems(response.body().getResults());
                    setErrorLayout(R.id.details_trailer_error_layout, false);
                } else {
                    setErrorLayout(R.id.details_trailer_error_layout, videoAdapter.isEmpty());
                }
            }

            @Override
            public void onFailure(@NonNull Call<VideoResponse> call, @NonNull Throwable t) {
                videosLoadingPB.setVisibility(View.GONE);
                setErrorLayout(R.id.details_trailer_error_layout, videoAdapter.isEmpty());
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    private void requestReviews(Integer movieId) {
        reviewsLoadingPB.setVisibility(View.VISIBLE);
        TheMovieDBService.getClient().listReviews(movieId).enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(@NonNull Call<ReviewResponse> call,
                                   @NonNull Response<ReviewResponse> response) {
                reviewsLoadingPB.setVisibility(View.GONE);
                if (response.isSuccessful()
                        && response.body() != null && !response.body().getResults().isEmpty()) {
                    reviewAdapter.setItems(response.body().getResults());
                    setErrorLayout(R.id.details_review_error_layout, false);
                } else {
                    setErrorLayout(R.id.details_review_error_layout, reviewAdapter.isEmpty());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReviewResponse> call, @NonNull Throwable t) {
                reviewsLoadingPB.setVisibility(View.GONE);
                setErrorLayout(R.id.details_review_error_layout, reviewAdapter.isEmpty());
            }
        });
    }

    private void setErrorLayout(int idLayout, boolean hasError) {
        findViewById(idLayout).setVisibility(hasError ? View.VISIBLE : View.GONE);
    }
}
