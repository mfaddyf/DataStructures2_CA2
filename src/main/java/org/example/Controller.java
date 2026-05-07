package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import java.util.*;
import java.util.stream.Collectors;
import javafx.scene.image.ImageView;

public class Controller {

    private Graph graph;
    private Map<String, List<Exhibit>> exhibitsByRoom;
    private Map<String, Room> roomById;
    private Image bwImage;
    private boolean pixelBFSMode = false;
    private int[] pixelStart = null;

    @FXML private ComboBox<Room> startBox;
    @FXML private ComboBox<Room> endBox;
    @FXML private TextArea outputArea;
    @FXML private TextArea artistInput;
    @FXML private TextField avoidInput;
    @FXML private TextField waypointInput;
    @FXML private Spinner<Integer> dfsLimitSpinner;
    @FXML private Pane mapPane;
    @FXML private ImageView mapImage;


    // -----------------------
    // INIT
    // -----------------------
    @FXML
    public void initialize() {
        try {
            loadGraph();
            loadImages();
            setupMapClickHandler();
        } catch (Exception e) {
            outputArea.setText("Error loading graph: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadGraph() throws Exception {
        graph = GraphLoader.load("/data/rooms.csv", "/data/connections.csv");
        exhibitsByRoom = GraphLoader.loadExhibits("/data/exhibits.csv");

        roomById = new HashMap<>();
        for (Room r : graph.getNodes()) {
            roomById.put(r.getId(), r);
        }

        List<Room> sorted = new ArrayList<>(graph.getNodes());
        sorted.sort(Comparator.comparing(id -> extractSortKey(id.getId())));
        startBox.getItems().addAll(sorted);
        endBox.getItems().addAll(sorted);
    }

    private void loadImages() {
        java.io.InputStream imgStream = getClass().getResourceAsStream("/images/gallery_floorplan.jpg");
        if (imgStream != null) {
            mapImage.setImage(new Image(imgStream));
        } else {
            outputArea.setText("Warning: floor plan image not found.");
        }

        java.io.InputStream bwStream = getClass().getResourceAsStream("/images/gallery_bwver.jpg");
        if (bwStream != null) {
            bwImage = new Image(bwStream);
        } else {
            System.err.println("Warning: B&W floor plan not found.");
        }
    }

    private void setupMapClickHandler() {
        mapPane.setOnMouseClicked(event -> {
            if (!pixelBFSMode) return;

            double scaleX = bwImage.getWidth()  / mapPane.getWidth();
            double scaleY = bwImage.getHeight() / mapPane.getHeight();

            int imgX = (int)(event.getX() * scaleX);
            int imgY = (int)(event.getY() * scaleY);

            if (pixelStart == null) {
                pixelStart = new int[]{imgX, imgY};
                outputArea.setText("Start point selected at (" + imgX + ", " + imgY + ").\nNow click your destination.");

                javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(event.getX(), event.getY(), 6);
                dot.setFill(javafx.scene.paint.Color.GREEN);
                dot.setId("pixelBFSMarker");
                mapPane.getChildren().add(dot);

            } else {
                int[] end = new int[]{imgX, imgY};
                outputArea.setText("Running pixel BFS...");

                List<int[]> path = PixelBFS.findPath(bwImage, pixelStart[0], pixelStart[1], end[0], end[1]);

                mapPane.getChildren().removeIf(n -> "pixelBFSMarker".equals(n.getId()));

                if (path.isEmpty()) {
                    outputArea.setText("No pixel path found. Try clicking inside a room.");
                } else {
                    drawPixelPath(path);
                    outputArea.setText("Pixel BFS complete.\nDistance: " + path.size() + " pixels.");
                }

                pixelBFSMode = false;
                pixelStart = null;
            }
        });
    }

    // -----------------------
    // BFS
    // -----------------------
    @FXML
    public void handleBFS() {

        Room start = startBox.getValue();
        Room end = endBox.getValue();

        if (!validate(start, end)) return;

        Set<Room> avoid = parseAvoid();
        List<Room> path = BFS.findPath(graph, start, end, avoid);

        outputArea.setText("BFS Shortest Route:\n" + formatPath(path));
    }

    @FXML
    public void handlePixelBFS() {
        pixelBFSMode = true;
        pixelStart = null;

        // clear any existing routes
        mapPane.getChildren().removeIf(n ->
                n instanceof javafx.scene.shape.Line ||
                        n instanceof javafx.scene.shape.Circle ||
                        n instanceof javafx.scene.shape.Polyline
        );

        outputArea.setText("Pixel BFS mode active.\nClick your starting point on the map.");
    }

    private void drawPixelPath(List<int[]> path) {

        double scaleX = mapPane.getWidth()  / bwImage.getWidth();
        double scaleY = mapPane.getHeight() / bwImage.getHeight();

        // use a Polyline for efficiency — one shape instead of thousands of lines
        javafx.scene.shape.Polyline polyline = new javafx.scene.shape.Polyline();
        polyline.setStroke(javafx.scene.paint.Color.RED);
        polyline.setStrokeWidth(2);
        polyline.setOpacity(0.8);

        // sample every 3rd pixel to keep rendering fast
        for (int i = 0; i < path.size(); i += 3) {
            int[] p = path.get(i);
            polyline.getPoints().addAll(
                    p[0] * scaleX,
                    p[1] * scaleY
            );
        }

        mapPane.getChildren().add(polyline);

        // green dot at start, red dot at end
        int[] start = path.get(0);
        int[] end   = path.get(path.size() - 1);

        javafx.scene.shape.Circle startDot = new javafx.scene.shape.Circle(
                start[0] * scaleX, start[1] * scaleY, 6,
                javafx.scene.paint.Color.GREEN
        );
        javafx.scene.shape.Circle endDot = new javafx.scene.shape.Circle(
                end[0] * scaleX, end[1] * scaleY, 6,
                javafx.scene.paint.Color.RED
        );

        mapPane.getChildren().addAll(startDot, endDot);
    }

    // -----------------------
    // DIJKSTRA
    // -----------------------
    @FXML
    public void handleDijkstra() {

        Room start = startBox.getValue();
        Room end = endBox.getValue();

        if (!validate(start, end)) return;

        Set<Room> avoid = parseAvoid();
        List<Room> waypoints = parseWaypoints();
        List<Room> path = runWithWaypoints(start, end, waypoints, avoid, false, null);

        outputArea.setText("Dijkstra Shortest Route:\n" + formatPath(path));
    }

    // -----------------------
    // DFS
    // -----------------------
    @FXML
    public void handleDFS() {
        Room start = startBox.getValue();
        Room end = endBox.getValue();

        if (!validate(start, end)) return;

        int limit = dfsLimitSpinner.getValue();
        Set<Room> avoid = parseAvoid();
        List<Room> waypoints = parseWaypoints();

        List<Room> stops = new ArrayList<>();
        stops.add(start);
        stops.addAll(waypoints);
        stops.add(end);

        StringBuilder sb = new StringBuilder();
        int count = 1;

        if (stops.size() == 2) {
            // no waypoints — normal DFS
            List<List<Room>> paths = DFSRoutes.findAllPaths(graph, start, end, limit, avoid);
            sb.append("DFS Routes (").append(paths.size()).append(" found):\n\n");
            for (List<Room> path : paths) {
                sb.append("Route ").append(count++).append(": ");
                sb.append(formatPath(path)).append("\n");
            }
        } else {
            // waypoints — find routes between each pair of stops
            sb.append("DFS Routes with waypoints:\n\n");
            for (int i = 0; i < stops.size() - 1; i++) {
                List<List<Room>> paths = DFSRoutes.findAllPaths(
                        graph, stops.get(i), stops.get(i + 1), limit, avoid
                );
                sb.append("Segment ").append(i + 1)
                        .append(" (").append(stops.get(i).getName())
                        .append(" → ").append(stops.get(i + 1).getName()).append("):\n");
                for (List<Room> path : paths) {
                    sb.append("  Route ").append(count++).append(": ");
                    sb.append(formatPath(path)).append("\n");
                }
            }
        }

        outputArea.setText(sb.toString());
    }

    // -----------------------
    // MOST INTERESTING
    // -----------------------
    @FXML
    public void handleInteresting() {

        Room start = startBox.getValue();
        Room end = endBox.getValue();

        if (!validate(start, end)) return;

        Set<String> artists = parseArtists();

        if (artists.isEmpty()) {
            outputArea.setText("Enter at least one preferred artist.");
            return;
        }

        Set<Room> avoid = parseAvoid();
        List<Room> waypoints = parseWaypoints();
        List<Room> path = runWithWaypoints(start, end, waypoints, avoid, true, artists);

        outputArea.setText("Most Interesting Route (artists: " + artists + "):\n" + formatPath(path));
    }

    // -----------------------
    // WAYPOINT RUNNER
    // Chains Dijkstra calls: start -> wp1 -> wp2 -> ... -> end
    // -----------------------
    private List<Room> runWithWaypoints(Room start, Room end,
                                        List<Room> waypoints, Set<Room> avoid,
                                        boolean interesting, Set<String> artists) {
        List<Room> fullPath = new ArrayList<>();
        List<Room> stops = new ArrayList<>();
        stops.add(start);
        stops.addAll(waypoints);
        stops.add(end);

        for (int i = 0; i < stops.size() - 1; i++) {
            Room from = stops.get(i);
            Room to   = stops.get(i + 1);

            List<Room> segment = interesting
                    ? Dijkstra.findInterestingPath(graph, from, to, avoid, exhibitsByRoom, artists)
                    : Dijkstra.findPath(graph, from, to, avoid);

            if (segment.isEmpty()) {
                outputArea.setText("No path found between " + from.getName() + " and " + to.getName());
                return new ArrayList<>();
            }

            // avoid duplicating the join node between segments
            if (!fullPath.isEmpty()) segment.remove(0);
            fullPath.addAll(segment);
        }

        return fullPath;
    }

    // -----------------------
    // PARSE HELPERS
    // -----------------------
    private Set<Room> parseAvoid() {
        String text = avoidInput.getText().trim();
        if (text.isEmpty()) return Collections.emptySet();

        return Arrays.stream(text.split(","))
                .map(String::trim)
                .map(roomById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private List<Room> parseWaypoints() {
        String text = waypointInput.getText().trim();
        if (text.isEmpty()) return Collections.emptyList();

        return Arrays.stream(text.split(","))
                .map(String::trim)
                .map(roomById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Set<String> parseArtists() {
        String text = artistInput.getText().trim();
        if (text.isEmpty()) return Collections.emptySet();

        return Arrays.stream(text.split(","))
                .map(String::trim)
                .map(String::toLowerCase) // normalise input
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private boolean validate(Room start, Room end) {
        if (start == null || end == null) {
            outputArea.setText("Please select both a start and end room.");
            return false;
        }
        if (start.equals(end)) {
            outputArea.setText("Start and end rooms are the same.");
            return false;
        }
        return true;
    }

    // -----------------------
    // FORMAT
    // -----------------------
    private String formatPath(List<Room> path) {
        if (path == null || path.isEmpty()) return "No path found.";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i).getName());
            if (i < path.size() - 1) sb.append("\n → ");
        }
        sb.append("\n(").append(path.size() - 1).append(" steps)");
        return sb.toString();
    }

    // -----------------------
    // HELPER
    // -----------------------
    private static String extractSortKey(String id) {
        // separate leading digits from trailing letters/suffix
        // e.g. "17a" -> sorts as "0000000017a"
        //      "51a" -> sorts as "0000000051a"
        //      "main_ves" -> sorts last as-is
        StringBuilder digits = new StringBuilder();
        StringBuilder rest = new StringBuilder();
        boolean digitsDone = false;

        for (char c : id.toCharArray()) {
            if (Character.isDigit(c) && !digitsDone) {
                digits.append(c);
            } else {
                digitsDone = true;
                rest.append(c);
            }
        }

        if (digits.length() == 0) {
            // no leading number — sort after all numbered rooms
            return "zz_" + id;
        }

        // zero-pad the numeric part so string sort == numeric sort
        return String.format("%010d", Integer.parseInt(digits.toString())) + rest.toString();
    }
}