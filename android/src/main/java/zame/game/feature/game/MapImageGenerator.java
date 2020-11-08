package zame.game.feature.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import zame.game.R;
import zame.game.core.util.Common;

final class MapImageGenerator {
    private static final int MAP_WIDTH = 9;
    private static final int MAP_HEIGHT = 5;
    private static final int CELL_WIDTH = 40;
    private static final int CELL_HEIGHT = 30;

    private static class MapPathItem {
        int x;
        int y;

        MapPathItem(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class MapPath {
        List<MapPathItem> path;
        int width;
        int height;

        MapPath() {
            this.path = new ArrayList<>();
            this.width = 0;
            this.height = 0;
        }
    }

    static class MapImageBitmaps {
        Bitmap cell;
        Bitmap cellHl;
        Bitmap connHor;
        Bitmap connVert;

        MapImageBitmaps(Resources resources) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inDither = false;
            opts.inPurgeable = true;
            opts.inInputShareable = true;

            cell = BitmapFactory.decodeResource(resources, R.drawable.map_cell, opts);
            cellHl = BitmapFactory.decodeResource(resources, R.drawable.map_cell_hl, opts);
            connHor = BitmapFactory.decodeResource(resources, R.drawable.map_conn_hor, opts);
            connVert = BitmapFactory.decodeResource(resources, R.drawable.map_conn_vert, opts);
        }
    }

    private MapImageGenerator() {
    }

    static Bitmap generateMapImage(MapPath mapPath, int highlighted, MapImageBitmaps bmps) {
        //noinspection MagicNumber
        float xoff = (float)Math.floor((float)(MAP_WIDTH - mapPath.width) / 2.0f * (float)CELL_WIDTH);

        //noinspection MagicNumber
        float yoff = (float)Math.floor((float)(MAP_HEIGHT - mapPath.height) / 2.0f * (float)CELL_HEIGHT);

        Bitmap img = Common.createBitmap(
                MAP_WIDTH * CELL_WIDTH,
                MAP_HEIGHT * CELL_HEIGHT,
                "Can't alloc bitmap for map");
        Canvas canvas = new Canvas(img);
        List<MapPathItem> path = mapPath.path;

        for (int i = 0, len = path.size(); i < len; i++) {
            MapPathItem item = path.get(i);

            if (i < len - 1) {
                MapPathItem nextItem = path.get(i + 1);

                if (nextItem.x == item.x) {
                    if (nextItem.y < item.y) {
                        canvas.drawBitmap(
                                bmps.connVert,
                                (float)(item.x * CELL_WIDTH) + xoff,
                                (float)(nextItem.y * CELL_HEIGHT) + yoff,
                                null);
                    } else {
                        canvas.drawBitmap(
                                bmps.connVert,
                                (float)(item.x * CELL_WIDTH) + xoff,
                                (float)(item.y * CELL_HEIGHT) + yoff,
                                null);
                    }
                } else { // nextItem.y == item.y
                    if (nextItem.x < item.x) {
                        canvas.drawBitmap(
                                bmps.connHor,
                                (float)(nextItem.x * CELL_WIDTH) + xoff,
                                (float)(item.y * CELL_HEIGHT) + yoff,
                                null);
                    } else {
                        canvas.drawBitmap(
                                bmps.connHor,
                                (float)(item.x * CELL_WIDTH) + xoff,
                                (float)(item.y * CELL_HEIGHT) + yoff,
                                null);
                    }
                }
            }

            canvas.drawBitmap(
                    (i < highlighted ? bmps.cellHl : bmps.cell),
                    (float)(item.x * CELL_WIDTH) + xoff,
                    (float)(item.y * CELL_HEIGHT) + yoff,
                    null);
        }

        //noinspection UnusedAssignment
        canvas = null;

        return img;
    }

    @SuppressWarnings("MagicNumber")
    static MapPath generateMapPath(int seed, int len) {
        if (seed < 0) {
            seed = 100 - seed;
        }

        Random random = new Random(seed + 20);
        boolean[][] map = new boolean[MAP_HEIGHT][MAP_WIDTH];
        MapPath result = new MapPath();

        if (generateMapPathInternal(random, map, MAP_WIDTH / 2, MAP_HEIGHT / 2, result.path, len)) {
            optimizeMapPath(result);
        }

        return result;
    }

    private static boolean generateMapPathInternal(
            Random random,
            boolean[][] map,
            int x,
            int y,
            List<MapPathItem> path,
            int len) {

        if (x < 0 || y < 0 || x >= MAP_WIDTH || y >= MAP_HEIGHT || map[y][x]) {
            return false;
        }

        map[y][x] = true;
        path.add(new MapPathItem(x, y));

        if (len <= 1) {
            return true;
        }

        boolean[] already = { false, false, false, false };
        int tries = 4;

        do {
            int cnt = random.nextInt(tries);
            int dir = 0;

            while (already[dir] || cnt > 0) {
                if (!already[dir]) {
                    cnt--;
                }

                dir++;
            }

            already[dir] = true;

            int nx;
            int ny;

            switch (dir) {
                case 0:
                    nx = x;
                    ny = y - 1;
                    break;

                case 1:
                    nx = x + 1;
                    ny = y;
                    break;

                case 2:
                    nx = x;
                    ny = y + 1;
                    break;

                default:
                    nx = x - 1;
                    ny = y;
                    break;
            }

            if (generateMapPathInternal(random, map, nx, ny, path, len - 1)) {
                return true;
            }

            tries--;
        } while (tries > 0);

        path.remove(path.size() - 1);
        map[y][x] = false;

        return false;
    }

    private static void optimizeMapPath(MapPath mapPath) {
        int minX = MAP_WIDTH;
        int minY = MAP_HEIGHT;

        mapPath.width = 0;
        mapPath.height = 0;

        for (MapPathItem item : mapPath.path) {
            if (item.x < minX) {
                minX = item.x;
            }

            if (item.y < minY) {
                minY = item.y;
            }
        }

        for (MapPathItem item : mapPath.path) {
            item.x -= minX;
            item.y -= minY;

            if (item.x + 1 > mapPath.width) {
                mapPath.width = item.x + 1;
            }

            if (item.y + 1 > mapPath.height) {
                mapPath.height = item.y + 1;
            }
        }
    }
}
