package org.example;

import java.util.*;

public class DFSRoutes {

    public static List<List<Room>> findAllPaths(Graph graph, Room start, Room end,
                                                int limit, Set<Room> avoid) {

        List<List<Room>> results = new ArrayList<>();
        Set<Room> visited = new HashSet<>();

        dfs(graph, start, end, visited, new ArrayList<>(), results, limit, avoid);

        return results;
    }

    private static void dfs(Graph graph,
                            Room current,
                            Room end,
                            Set<Room> visited,
                            List<Room> path,
                            List<List<Room>> results,
                            int limit,
                            Set<Room> avoid) {

        path.add(current);
        visited.add(current);

        if (current.equals(end)) {
            results.add(new ArrayList<>(path));
        } else {
            for (Edge e : graph.getNeighbors(current)) {
                if (!visited.contains(e.target)
                        && !avoid.contains(e.target)
                        && results.size() < limit) {
                    dfs(graph, e.target, end, visited, path, results, limit, avoid);
                }
            }
        }

        path.remove(path.size() - 1);
        visited.remove(current);
    }
}