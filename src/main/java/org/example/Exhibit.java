package org.example;

/**
 * represents an art exhibit displayed in a room of the national gallery.
 * each exhibit has a title and artist name used for route interest scoring.
 * the id and room id are stored for csv compatibility and future use.
 */
public class Exhibit {

    private String id;
    private String title;
    private String artist;
    private String roomId;

    /**
     * constructs an exhibit with the given details.
     *
     * @param id the unique identifier for this exhibit
     * @param title the title of the artwork
     * @param artist the name of the artist
     * @param roomId the id of the room containing this exhibit
     */
    public Exhibit(String id, String title, String artist, String roomId) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.roomId = roomId;
    }

    /**
     * returns the name of the artist who created this exhibit.
     *
     * @return the artist name
     */
    public String getArtist() { return artist; }

    /**
     * returns a string representation of the exhibit
     * in the format "title — artist".
     *
     * @return formatted string of title and artist
     */
    @Override
    public String toString() {
        return title + " — " + artist;
    }
}