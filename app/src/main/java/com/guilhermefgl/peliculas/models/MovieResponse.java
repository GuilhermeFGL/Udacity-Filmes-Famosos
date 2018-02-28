package com.guilhermefgl.peliculas.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MovieResponse implements Parcelable {

    private ArrayList<Movie> results;
    private Integer page;
    @SerializedName("total_results")
    private Integer totalResults;
    @SerializedName("total_pages")
    private Integer totalPages;

    protected MovieResponse() {}

    public ArrayList<Movie> getResults() {
        return results;
    }

    public void setResults(ArrayList<Movie> results) {
        this.results = results;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeTypedList(results != null ? results : new ArrayList<Movie>());
        out.writeInt(page);
        out.writeInt(totalResults != null ? totalResults : 0);
        out.writeInt(totalPages);
    }

    private MovieResponse(Parcel in) {
        in.readTypedList(results, Movie.CREATOR);
        page = in.readInt();
        totalResults = in.readInt();
        totalPages = in.readInt();
    }

    public static final Parcelable.Creator<MovieResponse> CREATOR
            = new Parcelable.Creator<MovieResponse>() {
        public MovieResponse createFromParcel(Parcel in) {
            return new MovieResponse(in);
        }

        public MovieResponse[] newArray(int size) {
            return new MovieResponse[size];
        }
    };
}
