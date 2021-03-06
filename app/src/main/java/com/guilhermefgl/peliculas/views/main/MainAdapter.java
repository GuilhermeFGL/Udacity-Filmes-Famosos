package com.guilhermefgl.peliculas.views.main;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.guilhermefgl.peliculas.R;
import com.guilhermefgl.peliculas.helpers.PicassoHelper;
import com.guilhermefgl.peliculas.models.Movie;
import com.guilhermefgl.peliculas.models.MovieResponse;
import com.guilhermefgl.peliculas.services.TheMovieDBService;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Movie> movieList;
    private Integer currentPage, maxPages;
    private OnMovieItemClick onMovieItemClick;

    private boolean isLoading;
    private final int VISIBLE_THRESHOLD = 4;
    private int lastVisibleItem, totalItemCount;

    static final int GRID_PORTRAIT = 2;
    static final int GRID_LANDSCAPE = 4;
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    MainAdapter(RecyclerView recyclerView, final int spanCount,
                final OnLoadMoreListener listener, OnMovieItemClick onMovieItemClick) {
        this.movieList = new ArrayList<>();
        this.onMovieItemClick = onMovieItemClick;

        final GridLayoutManager gridLayoutManager = (GridLayoutManager)recyclerView.getLayoutManager();
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(getItemViewType(position)){
                    case MainAdapter.VIEW_TYPE_ITEM:
                        return 1;
                    case MainAdapter.VIEW_TYPE_LOADING:
                        return spanCount;
                    default:
                        return -1;
                }
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = gridLayoutManager.getItemCount();
                lastVisibleItem = gridLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD)
                        && currentPage < maxPages) {
                    isLoading = true;
                    if (listener != null) {
                        listener.onLoadMore();
                    }
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return movieList.get(position) != null ? VIEW_TYPE_ITEM : VIEW_TYPE_LOADING;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new MovieViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_movie, parent, false));
        } else {
            return new LoadingViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_movie_loading, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MovieViewHolder) {
            ((MovieViewHolder)holder).bind(movieList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return movieList != null ? movieList.size() : 0;
    }

    boolean isEmpty() {
        return getItemCount() == 0;
    }

    int getCurrentPage() {
        return currentPage != null ? currentPage : 0;
    }

    int getNextPage() {
        return getCurrentPage() + 1;
    }

    ArrayList<Movie> getItems() {
        if (!movieList.isEmpty() && movieList.get(movieList.size() - 1) == null) {
            ArrayList<Movie> movies = new ArrayList<>(movieList);
            movies.remove(movieList.size() - 1);
            return new ArrayList<>(movies);
        }
        return new ArrayList<>(movieList);
    }

    void insertItems(MovieResponse movieResponse) {
        currentPage = movieResponse.getPage();
        maxPages = movieResponse.getTotalPages();
        if (currentPage == TheMovieDBService.LISTING_FIRST_PAGE) {
            movieList = movieResponse.getResults();
        } else {
            movieList.addAll(movieResponse.getResults());
        }
        notifyDataSetChanged();
    }

    void insertLoading() {
        if (movieList.get(movieList.size() - 1) != null) {
            movieList.add(null);
            notifyItemInserted(movieList.size() - 1);
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    void removeLoading() {
        removeItem(null);
    }

    void removeItem(Movie movie) {
        if (movieList.removeAll(Collections.singleton(movie)) ) {
            notifyDataSetChanged();
        }
    }

    void setFinishLoading() {
        isLoading = false;
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        LoadingViewHolder(View view) {
            super(view);
        }
    }

    class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.item_movie_title)
        TextView itemTitleTextView;
        @BindView(R.id.item_movie_ratio)
        RatingBar itemAverageRatingBar;
        @BindView(R.id.item_movie_thumbnail)
        ImageView itemThumbnailImageView;

        private final View view;
        private Movie movie;

        MovieViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.view = itemView;
        }

        void bind(Movie movie) {
            this.movie = movie;
            itemTitleTextView.setText(movie.getTitle());
            itemAverageRatingBar.setRating((float) (movie.getVoteAverage() / TheMovieDBService.TOP_RATING_COUNT));
            PicassoHelper.loadImage(view.getContext(),
                    TheMovieDBService.buildImageURL(movie.getPosterPath()),
                    itemThumbnailImageView, R.mipmap.movie_background, R.mipmap.error_background);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onMovieItemClick.onMovieItemClick(movie, itemThumbnailImageView);
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public interface OnMovieItemClick {
        void onMovieItemClick(Movie movie, ImageView imageView);
    }
}
