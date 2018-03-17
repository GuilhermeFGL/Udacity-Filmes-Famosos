package com.guilhermefgl.peliculas.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

@SuppressWarnings("ALL")
public class VideoResponse {

    @SerializedName("id")
    private Integer movieId;
    private ArrayList<Video> results;

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public ArrayList<Video> getResults() {
        return results;
    }

    public void setResults(ArrayList<Video> results) {
        this.results = results;
    }
}
