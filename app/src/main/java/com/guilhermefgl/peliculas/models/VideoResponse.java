package com.guilhermefgl.peliculas.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@SuppressWarnings("ALL")
public class VideoResponse {

    @SerializedName("id")
    private Integer movieId;
    private List<Video> results;

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public List<Video> getResults() {
        return results;
    }

    public void setResults(List<Video> results) {
        this.results = results;
    }
}
