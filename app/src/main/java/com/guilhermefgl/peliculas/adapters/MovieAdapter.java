package com.guilhermefgl.peliculas.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.guilhermefgl.peliculas.R;
import com.guilhermefgl.peliculas.models.Movie;
import com.guilhermefgl.peliculas.services.TheMovieDBService;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder>{

    private Context context;
    private List<Movie> movieList;

    public MovieAdapter(Context context, List<Movie> moviesList) {
        this.context = context;
        this.movieList = moviesList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_movie, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(movieList.get(position));
    }

    @Override
    public int getItemCount() {
        return movieList != null ? movieList.size() : 0;
    }

    public void updateAdapter(List<Movie> moviesList) {
        this.movieList = moviesList;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_movie_title)
        TextView itemTitleTV;
        @BindView(R.id.item_movie_ratio)
        RatingBar itemAverageRB;
        @BindView(R.id.item_movie_thumbnail)
        ImageView itemThumbnailIV;

        ViewHolder(View itemView) {
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
