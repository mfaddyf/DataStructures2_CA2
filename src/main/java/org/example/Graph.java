package org.example;

import java.util.*;


public class Graph {

    private final Map<Room, List<Edge>> adjList = new HashMap<>();

    public void addNode(Room room) {
        adjList.putIfAbsent(room, new ArrayList<>());
    }

    public void addEdge(Room from, Room to, double weight) {
        adjList.putIfAbsent(from, new ArrayList<>());
        adjList.putIfAbsent(to, new ArrayList<>());

        adjList.get(from).add(new Edge(to, weight));
        adjList.get(to).add(new Edge(from, weight));
    }

    public List<Edge> getNeighbors(Room room) {
        return adjList.getOrDefault(room, new ArrayList<>());
    }

    public Set<Room> getNodes() {
        return adjList.keySet();
    }
}