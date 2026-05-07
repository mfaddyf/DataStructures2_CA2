package org.example;

public class Exhibit {

    private String title;
    private String artist;

    public Exhibit(String id, String title, String artist, String roomId) {
        this.title = title;
        this.artist = artist;
    }

    public String getArtist() { return artist; }

    @Override
    public String toString() {
        return title + " — " + artist;
    }
}