package com.guilhermefgl.peliculas.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.guilhermefgl.peliculas.R;
import com.guilhermefgl.peliculas.helpers.Constants;
import com.guilhermefgl.peliculas.helpers.PicassoHelper;
import com.guilhermefgl.peliculas.helpers.SnackBarHelper;
import com.guilhermefgl.peliculas.models.Movie;
import com.guilhermefgl.peliculas.services.TheMovieDBService;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends BaseActivity {

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

    private Movie movie;
    private final SimpleDateFormat DATE_FORMATER
            = new SimpleDateFormat(Constants.DATE_FORMATER, Locale.getDefault());

    public static void startActivity(BaseActivity activity, Bundle bundle, Bundle transition) {
        if (Build.VERSION.SDK_INT >= 21 && transition != null) {
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

        if (getIntent().getExtras() != null && !getIntent().getExtras().isEmpty()) {
            Object extra = getIntent().getExtras().get(Constants.Bundles.DETAILS_MOVIE);
            if (extra != null && extra instanceof Movie) {
                movie = (Movie) extra;
            }
        }

        if (movie == null) {
            finish();
        } else {
            titleTV.setText(movie.getTitle());
            voteRB.setRating((float) (movie.getVoteAverage() / 2));
            viewsTV.setText(String.valueOf(movie.getPopularity()));
            languageTV.setText(movie.getLanguage());
            adultTV.setVisibility(movie.isAdult() ? View.VISIBLE : View.GONE);
            dateTV.setText(DATE_FORMATER.format(movie.getReleaseDate()));
            overviewTV.setText(movie.getOverview());
            PicassoHelper.loadImage(this,
                    TheMovieDBService.buildImageURL(movie.getPosterPath()),
                    posterIV);
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
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
}
