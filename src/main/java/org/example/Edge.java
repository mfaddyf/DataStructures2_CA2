package org.example;

public class Edge {
    public Room target;
    public double weight;

    public Edge(Room target, double weight) {
        this.target = target;
        this.weight = weight;
    }
}