package org.example;

import java.io.*;
import java.util.*;

public class GraphLoader {

    public static Graph load(String roomsFile, String connectionsFile) throws Exception {

        Graph graph = new Graph();
        Map<String, Room> roomMap = new HashMap<>();

        // -------------------
        // LOAD ROOMS
        // -------------------
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        GraphLoader.class.getResourceAsStream(roomsFile)
                )
        );

        br.readLine(); // skip header row

        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(",", 2);
            String id   = parts[0].trim();
            String name = parts[1].trim();

            Room room = new Room(id, name);
            roomMap.put(id, room);
            graph.addNode(room);
        }
        br.close();

        // -------------------
        // LOAD CONNECTIONS
        // -------------------
        br = new BufferedReader(
                new InputStreamReader(
                        GraphLoader.class.getResourceAsStream(connectionsFile)
                )
        );

        br.readLine(); // skip header row

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(",");
            String fromId  = parts[0].trim();
            String toId    = parts[1].trim();
            double weight  = Double.parseDouble(parts[2].trim());

            Room from = roomMap.get(fromId);
            Room to   = roomMap.get(toId);

            if (from == null || to == null) {
                System.err.println("Warning: unknown room in connection: " + fromId + " -> " + toId);
                continue;
            }

            graph.addEdge(from, to, weight);
        }
        br.close();

        return graph;
    }

    // -------------------
    // LOAD EXHIBITS
    // -------------------
    public static Map<String, List<Exhibit>> loadExhibits(String exhibitsFile) throws Exception {

        Map<String, List<Exhibit>> byRoom = new HashMap<>();

        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        GraphLoader.class.getResourceAsStream(exhibitsFile)
                )
        );

        br.readLine(); // skip header row

        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // limit 4 — title and artist name may contain commas
            String[] parts = line.split(",", 4);
            String id     = parts[0].trim();
            String title  = parts[1].trim();
            String artist = parts[2].trim();
            String roomId = parts[3].trim();

            Exhibit exhibit = new Exhibit(id, title, artist, roomId);
            byRoom.computeIfAbsent(roomId, k -> new ArrayList<>()).add(exhibit);
        }
        br.close();

        return byRoom;
    }
}