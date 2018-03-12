package com.guilhermefgl.peliculas.models;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.guilhermefgl.peliculas.models.provider.MovieDBHelper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import static com.guilhermefgl.peliculas.models.provider.MovieContract.MovieEntry;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Movie implements Parcelable{

    @SerializedName("id")
    private Integer movieId;
    @SerializedName("original_title")
    private String title;
    @SerializedName("overview")
    private String overview;
    @SerializedName("release_date")
    private Date releaseDate;
    @SerializedName("original_language")
    private String language;
    @SerializedName("vote_count")
    private Integer voteCount;
    @SerializedName("vote_average")
    private Double voteAverage;
    @SerializedName("popularity")
    private Double popularity;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("video")
    private Boolean video;
    @SerializedName("adult")
    private Boolean adult;

    public Movie() { }

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

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
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

    public Boolean isAdult() {
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

    public static ArrayList<Movie> createFromCursor(final Cursor cursor) {
        ArrayList<Movie> movies = new ArrayList<>();

        if (!cursor.moveToFirst()) {
            return movies;
        }

        do {
            movies.add(new Movie() {{
                setMovieId(cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_API_ID)));
                setTitle(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TITLE)));
                setOverview(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW)));
                setLanguage(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_LANGUAGE)));
                setVoteCount(cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_COUNT)));
                setVoteAverage(cursor.getDouble(cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_AVERAGE)));
                setPopularity(cursor.getDouble(cursor.getColumnIndex(MovieEntry.COLUMN_POPULARITY)));
                setPosterPath(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER)));
                setVideo(cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_VIDEO)) == 1);
                setAdult(cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_ADULT)) == 1);
                try {
                    setReleaseDate(
                            MovieDBHelper.DATE_FORMATTER.parse(
                                    cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_DATE))));
                } catch (ParseException ignored) { }
            }});
        } while (cursor.moveToNext());

        return movies;
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
        out.writeSerializable(releaseDate);
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
        releaseDate = (Date) in.readSerializable();
        voteCount = in.readInt();
        voteAverage = in.readDouble();
        posterPath = in.readString();
        language = in.readString();
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
