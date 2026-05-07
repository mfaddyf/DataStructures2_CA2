package org.example;

import java.util.*;

public class Dijkstra {

    // -----------------------
    // STANDARD SHORTEST PATH
    // -----------------------
    public static List<Room> findPath(Graph graph, Room start, Room end,
                                      Set<Room> avoid) {

        Map<Room, Double> dist = new HashMap<>();
        Map<Room, Room> prev = new HashMap<>();

        PriorityQueue<Room> pq = new PriorityQueue<>(
                Comparator.comparingDouble(r -> dist.getOrDefault(r, Double.POSITIVE_INFINITY))
        );

        for (Room r : graph.getNodes()) {
            dist.put(r, Double.POSITIVE_INFINITY);
        }

        dist.put(start, 0.0);
        pq.add(start);

        while (!pq.isEmpty()) {

            Room current = pq.poll();
            if (current.equals(end)) break;

            for (Edge e : graph.getNeighbors(current)) {

                if (avoid.contains(e.target)) continue;

                double newDist = dist.get(current) + e.weight;

                if (newDist < dist.getOrDefault(e.target, Double.POSITIVE_INFINITY)) {
                    dist.put(e.target, newDist);
                    prev.put(e.target, current);
                    pq.add(e.target);
                }
            }
        }

        return reconstruct(prev, start, end);
    }

    // -----------------------
    // MOST INTERESTING PATH
    // -----------------------
    public static List<Room> findInterestingPath(Graph graph,
                                                 Room start,
                                                 Room end,
                                                 Set<Room> avoid,
                                                 Map<String, List<Exhibit>> exhibitsByRoom,
                                                 Set<String> preferredArtists) {

        // find all rooms containing a preferred artist
        List<Room> interestingRooms = new ArrayList<>();
        for (Room r : graph.getNodes()) {
            if (avoid.contains(r) || r.equals(start) || r.equals(end)) continue;
            List<Exhibit> exhibits = exhibitsByRoom.getOrDefault(
                    r.getId(), Collections.emptyList()
            );
            boolean hasInterest = exhibits.stream()
                    .anyMatch(ex -> preferredArtists.contains(ex.getArtist().toLowerCase()));
            if (hasInterest) {
                interestingRooms.add(r);
            }
        }

        if (interestingRooms.isEmpty()) {
            return findPath(graph, start, end, avoid);
        }

        // greedily order interesting rooms by proximity from current position
        // start -> nearest interesting room -> next nearest -> ... -> end
        List<Room> orderedWaypoints = new ArrayList<>();
        List<Room> remaining = new ArrayList<>(interestingRooms);
        Room current = start;

        while (!remaining.isEmpty()) {
            Room nearest = null;
            double nearestCost = Double.MAX_VALUE;

            for (Room candidate : remaining) {
                List<Room> path = findPath(graph, current, candidate, avoid);
                double cost = pathCost(path);
                if (cost < nearestCost) {
                    nearestCost = cost;
                    nearest = candidate;
                }
            }

            if (nearest == null) break;
            orderedWaypoints.add(nearest);
            remaining.remove(nearest);
            current = nearest;
        }

        // build full path: start -> wp1 -> wp2 -> ... -> end
        List<Room> fullPath = new ArrayList<>();
        List<Room> stops = new ArrayList<>();
        stops.add(start);
        stops.addAll(orderedWaypoints);
        stops.add(end);

        for (int i = 0; i < stops.size() - 1; i++) {
            List<Room> segment = findPath(graph, stops.get(i), stops.get(i + 1), avoid);
            if (segment.isEmpty()) continue;
            if (!fullPath.isEmpty()) segment.remove(0);
            fullPath.addAll(segment);
        }

        return fullPath;
    }

    // -----------------------
    // HELPERS
    // -----------------------
    private static double pathCost(List<Room> path) {
        return path.isEmpty() ? Double.MAX_VALUE : path.size() - 1;
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