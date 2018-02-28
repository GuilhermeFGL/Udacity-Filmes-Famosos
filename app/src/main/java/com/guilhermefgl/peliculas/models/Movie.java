package com.guilhermefgl.peliculas.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

@SuppressWarnings("unused")
public class Movie implements Parcelable{

    private String title;
    private String overview;
    private Double popularity;
    private Boolean video;
    private Boolean adult;
    @SerializedName("id")
    private Integer movieId;
    @SerializedName("release_date")
    private Date release_date;
    @SerializedName("vote_count")
    private Integer voteCount;
    @SerializedName("vote_average")
    private Double voteAverage;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("original_language")
    private String language;

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public Date getRelease_date() {
        return release_date;
    }

    public void setRelease_date(Date release_date) {
        this.release_date = release_date;
    }

    public Double getPopularity() {
        return popularity;
    }

    public void setPopularity(Double popularity) {
        this.popularity = popularity;
    }

    public Boolean hasVideo() {
        return video;
    }

    public void setVideo(Boolean video) {
        this.video = video;
    }

    public Boolean hasAdult() {
        return adult;
    }

    public void setAdult(Boolean adult) {
        this.adult = adult;
    }

    public Integer getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(Integer voteCount) {
        this.voteCount = voteCount;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(Double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(movieId);
        out.writeString(title);
        out.writeString(overview);
        out.writeDouble(popularity);
        out.writeByte((byte) (video ? 1 : 0));
        out.writeByte((byte) (adult ? 1 : 0));
        out.writeSerializable(release_date);
        out.writeInt(voteCount);
        out.writeDouble(voteAverage);
        out.writeString(posterPath);
        out.writeString(language);
    }

    private Movie(Parcel in) {
        movieId = in.readInt();
        title = in.readString();
        overview = in.readString();
        popularity = in.readDouble();
        video = in.readByte() == 1;
        adult = in.readByte() == 1;
        release_date = (Date) in.readSerializable();
        voteCount = in.readInt();
        voteAverage = in.readDouble();
        posterPath = in.readString();
        language = in.readString();
    }

    static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
