package com.guilhermefgl.peliculas.adapters;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.guilhermefgl.peliculas.R;
import com.guilhermefgl.peliculas.models.Movie;
import com.guilhermefgl.peliculas.models.MovieResponse;
import com.guilhermefgl.peliculas.services.TheMovieDBService;
import com.squareup.picasso.Picasso;

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

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    public MovieAdapter(Context context, RecyclerView recyclerView, final OnLoadMoreListener listiner) {
        this.context = context;
        this.movieList = new ArrayList<>();

        final GridLayoutManager gridLayoutManager = (GridLayoutManager)recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = gridLayoutManager.getItemCount();
                lastVisibleItem = gridLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)
                        && currentPage < maxPages) {
                    if (listiner != null) {
                        listiner.onLoadMore();
                    }
                    isLoading = true;
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
        movieList.addAll(movieResponse.getResults());
        notifyDataSetChanged();
    }

    public void insertLoading() {
        movieList.add(null);
        notifyItemInserted(movieList.size() - 1);
    }

    public void removeLoading() {
        if (!movieList.isEmpty()) {
            movieList.remove(movieList.size() - 1);
            notifyItemRemoved(movieList.size());
        }
    }

    public void setFinishLoading() {
        this.isLoading = false;
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

        MovieViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(Movie movie) {
            itemTitleTV.setText(movie.getTitle());
            itemAverageRB.setRating((float) (movie.getVoteAverage() / 10));
            Picasso.with(context)
                    .load(TheMovieDBService.IMAGE_BASE_URL.concat(movie.getPosterPath()))
                    .into(itemThumbnailIV);
        }
    }
}
