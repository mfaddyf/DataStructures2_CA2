package org.example;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import java.util.*;

public class PixelBFS {

    // returns the path as a list of int[]{x, y} pixel points, or empty if no path
    public static List<int[]> findPath(Image bwImage, int startX, int startY, int endX, int endY) {

        int width  = (int) bwImage.getWidth();
        int height = (int) bwImage.getHeight();

        PixelReader pr = bwImage.getPixelReader();

        // check start and end are on walkable (white) pixels
        if (!isWalkable(pr, startX, startY) || !isWalkable(pr, endX, endY)) {
            return new ArrayList<>();
        }

        // prev[y][x] stores the pixel we came from — used to reconstruct path
        int[][][] prev = new int[height][width][];

        boolean[][] visited = new boolean[height][width];

        Queue<int[]> queue = new LinkedList<>();

        queue.add(new int[]{startX, startY});
        visited[startY][startX] = true;

        // 4-directional movement (up, down, left, right)
        int[][] dirs = {{0,-1},{0,1},{-1,0},{1,0}};

        while (!queue.isEmpty()) {

            int[] current = queue.poll();
            int cx = current[0];
            int cy = current[1];

            if (cx == endX && cy == endY) break;

            for (int[] dir : dirs) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];

                if (nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
                if (visited[ny][nx]) continue;
                if (!isWalkable(pr, nx, ny)) continue;

                visited[ny][nx] = true;
                prev[ny][nx] = new int[]{cx, cy};
                queue.add(new int[]{nx, ny});
            }
        }

        // reconstruct path by walking back through prev
        return reconstruct(prev, startX, startY, endX, endY);
    }

    private static boolean isWalkable(PixelReader pr, int x, int y) {
        Color c = pr.getColor(x, y);
        // treat light pixels as walkable — threshold at 0.5 brightness
        return c.getBrightness() > 0.5;
    }

    private static List<int[]> reconstruct(int[][][] prev,
                                           int startX, int startY,
                                           int endX,   int endY) {
        List<int[]> path = new ArrayList<>();
        int[] current = {endX, endY};

        while (current != null) {
            path.add(current);
            int[] p = prev[current[1]][current[0]];
            if (p == null && (current[0] != startX || current[1] != startY)) {
                // no path found
                return new ArrayList<>();
            }
            current = p;
        }

        Collections.reverse(path);
        return path;
    }
}