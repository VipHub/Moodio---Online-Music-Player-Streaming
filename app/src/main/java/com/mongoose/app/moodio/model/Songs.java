package com.mongoose.app.moodio.model;

/**
 * Created by vips on 7/7/15.
 */
public class Songs {
    public int songs_id;
    public int user_id;
    public String title;
    public String artist;
    public String album;
    public String lyrics;
    public String path;
    public int duration;
    public String albumArtUri;
    public int sourceType;
    public int viewType;

    public String getAlbumArtUri() {
        return albumArtUri;
    }

    public void setAlbumArtUri(String albumArtUri) {
        this.albumArtUri = albumArtUri;
    }


    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getSongs_id() {
        return songs_id;
    }

    public void setSongs_id(int songs_id) {
        this.songs_id = songs_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }


    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setSourceType(int type) { this.sourceType = type; }

    public int getSourceType() { return sourceType; }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }
}
