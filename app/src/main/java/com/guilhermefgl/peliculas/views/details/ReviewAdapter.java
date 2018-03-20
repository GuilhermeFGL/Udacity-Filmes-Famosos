package com.guilhermefgl.peliculas.views.details;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guilhermefgl.peliculas.R;
import com.guilhermefgl.peliculas.models.Review;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private ArrayList<Review> reviews;

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReviewAdapter.ReviewViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_review, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.bind(reviews.get(position));
    }

    @Override
    public int getItemCount() {
        return reviews != null ? reviews.size() : 0;
    }

    boolean isEmpty() {
        return getItemCount() == 0;
    }

    void setItems(ArrayList<Review> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    ArrayList<Review> getItens() {
        if (reviews != null) {
            return new ArrayList<>(reviews);
        }
        return new ArrayList<>();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_review_author)
        TextView authorTextView;
        @BindView(R.id.item_review_content)
        TextView contentTextView;

        ReviewViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(Review review) {
            authorTextView.setText(review.getAuthor());
            contentTextView.setText(review.getContent());
        }
    }
}
