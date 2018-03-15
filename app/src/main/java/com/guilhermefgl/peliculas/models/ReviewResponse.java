package com.guilhermefgl.peliculas.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

@SuppressWarnings("ALL")
public class ReviewResponse {

    @SerializedName("id")
    private Integer movieId;
    private ArrayList<Review> results;

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public ArrayList<Review> getResults() {
        return results;
    }

    public void setResults(ArrayList<Review> results) {
        this.results = results;
    }
}
