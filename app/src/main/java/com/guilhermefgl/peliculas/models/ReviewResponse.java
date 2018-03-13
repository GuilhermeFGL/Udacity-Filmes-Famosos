package com.guilhermefgl.peliculas.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@SuppressWarnings("ALL")
public class ReviewResponse {

    @SerializedName("id")
    private Integer movieId;
    private List<Review> results;

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public List<Review> getResults() {
        return results;
    }

    public void setResults(List<Review> results) {
        this.results = results;
    }
}
