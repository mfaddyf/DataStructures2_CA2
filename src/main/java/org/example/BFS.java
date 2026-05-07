package org.example;

import java.util.*;

public class BFS {

    public static List<Room> findPath(Graph graph, Room start, Room end, Set<Room> avoid) {

        Queue<Room> queue = new LinkedList<>();
        Map<Room, Room> prev = new HashMap<>();
        Set<Room> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {

            Room current = queue.poll();

            if (current.equals(end)) break;

            for (Edge e : graph.getNeighbors(current)) {

                Room next = e.target;

                if (!visited.contains(next) && !avoid.contains(next)) {
                    visited.add(next);
                    prev.put(next, current);
                    queue.add(next);
                }
            }
        }

        return reconstruct(prev, start, end);
    }

    private static List<Room> reconstruct(Map<Room, Room> prev, Room start, Room end) {

        List<Room> path = new ArrayList<>();
        Room current = end;

        while (current != null) {
            path.add(current);
            current = prev.get(current);
        }

        Collections.reverse(path);

        if (!path.isEmpty() && path.get(0).equals(start)) {
            return path;
        }

        return new ArrayList<>();
    }
}