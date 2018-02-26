package com.guilhermefgl.peliculas.adapters;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.guilhermefgl.peliculas.R;
import com.guilhermefgl.peliculas.helpers.PicassoHelper;
import com.guilhermefgl.peliculas.models.Movie;
import com.guilhermefgl.peliculas.models.MovieResponse;
import com.guilhermefgl.peliculas.services.TheMovieDBService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context context;
    private List<Movie> movieList;
    private Integer currentPage, maxPages;

    private boolean isLoading;
    private int visibleThreshold = 4;
    private int lastVisibleItem, totalItemCount;

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    public MovieAdapter(Context context, RecyclerView recyclerView, final OnLoadMoreListener listiner) {
        this.context = context;
        this.movieList = new ArrayList<>();

        final GridLayoutManager gridLayoutManager = (GridLayoutManager)recyclerView.getLayoutManager();
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(getItemViewType(position)){
                    case MovieAdapter.VIEW_TYPE_LOADING:
                        return 2;
                    case MovieAdapter.VIEW_TYPE_ITEM:
                        return 1;
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
                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)
                        && currentPage < maxPages) {
                    isLoading = true;
                    if (listiner != null) {
                        listiner.onLoadMore();
                    }
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return movieList.get(position) != null ? VIEW_TYPE_ITEM : VIEW_TYPE_LOADING;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new MovieViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_movie, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_movie_loading, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MovieViewHolder) {
            ((MovieViewHolder)holder).bind(movieList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return movieList != null ? movieList.size() : 0;
    }

    public int getNextPage() {
        return (currentPage != null ? currentPage : 0) + 1;
    }

    public void insertItens(MovieResponse movieResponse) {
        currentPage = movieResponse.getPage();
        maxPages = movieResponse.getTotalPages();
        if (movieResponse.getPage() == 1) {
            movieList = movieResponse.getResults();
        } else {
            movieList.addAll(movieResponse.getResults());
        }
        notifyDataSetChanged();
    }

    public void insertLoading() {
        if (movieList.get(movieList.size() - 1) != null) {
            movieList.add(null);
            notifyItemInserted(movieList.size() - 1);
        }
    }

    public void removeLoading() {
        if (!movieList.isEmpty()) {
            movieList.remove(movieList.size() - 1);
            notifyItemRemoved(movieList.size());
        }
    }

    public void setFinishLoading() {
        isLoading = false;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        LoadingViewHolder(View view) {
            super(view);
        }
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_movie_title)
        TextView itemTitleTV;
        @BindView(R.id.item_movie_ratio)
        RatingBar itemAverageRB;
        @BindView(R.id.item_movie_thumbnail)
        ImageView itemThumbnailIV;
        @BindView(R.id.item_movie_progress_bar)
        ProgressBar loadingPB;

        MovieViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(Movie movie) {
            itemTitleTV.setText(movie.getTitle());
            itemAverageRB.setRating((float) (movie.getVoteAverage() / 10));
            PicassoHelper.loadImage(context,
                    TheMovieDBService.buildImageURL(movie.getPosterPath()),
                    itemThumbnailIV, loadingPB);
        }
    }
}
