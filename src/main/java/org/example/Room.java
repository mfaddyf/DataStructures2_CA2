package org.example;

public class Room {

    private String id;
    private String name;

    public Room(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId()   { return id; }
    public String getName() { return name; }

    @Override
    public String toString() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room)) return false;
        return id.equals(((Room) o).id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}