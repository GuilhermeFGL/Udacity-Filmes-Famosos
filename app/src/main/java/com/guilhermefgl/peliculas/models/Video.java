package com.guilhermefgl.peliculas.models;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("ALL")
public class Video {

    private String id;
    private String key;
    private String site;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }
}