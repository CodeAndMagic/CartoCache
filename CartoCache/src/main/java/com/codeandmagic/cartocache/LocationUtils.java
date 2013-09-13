package com.codeandmagic.cartocache;

import android.graphics.Point;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import static java.lang.Math.PI;
import static java.lang.Math.atan;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;

/**
 * A collection of utility methods.
 */
public class LocationUtils {

    public static final int TILE_SIZE = 256;

    public static final double MIN_LATITUDE = -85.05112877;
    public static final double MAX_LATITUDE = 85.05112877;
    public static final double MIN_LONGITUDE = -179.999;
    public static final double MAX_LONGITUDE = 179.999;

    /**
     * Make sure a value stays within a minimum and maximum values.
     *
     * @param n
     * @param min
     * @param max
     * @return
     */
    public static double clip(double n, double min, double max) {
        return min(max(n, min), max);
    }

    /**
     * Same as {@link LocationUtils#clip(double, double, double)} for integers.
     *
     * @param n
     * @param min
     * @param max
     * @return
     */
    public static int clip(int n, int min, int max) {
        return min(max(n, min), max);
    }

    /**
     * Make sure latitude stays within correct bounds.
     *
     * @param latitude
     * @return
     */
    public static double clipLatitude(double latitude) {
        return clip(latitude, MIN_LATITUDE, MAX_LATITUDE);
    }

    /**
     * Make sure longitude stays within correct bounds.
     *
     * @param longitude
     * @return
     */
    public static double clipLongitude(double longitude) {
        return clip(longitude, MIN_LONGITUDE, MAX_LONGITUDE);
    }

    /**
     * Converts a point from geographical coordinates (latitude, longitude)
     * to absolute world pixel (not screen pixel) for a zoom level.
     *
     * @param latLng
     * @return
     */
    public static Point latLngToWorldPoint(LatLng latLng, ZoomLevel zoom) {
        final double latitude = clipLatitude(latLng.latitude);
        final double longitude = clipLongitude(latLng.longitude);
        final double sinLatitude = sin(latitude * (PI / 180.0));

        final double x = (longitude + 180.0) / 360.0;
        final double y = 0.5 - log((1.0 + sinLatitude) / (1.0 - sinLatitude)) / (4.0 * PI);

        final double pixelX = clip(x * zoom.mapSize + 0.5, 0, zoom.mapSize - 1);
        final double pixelY = clip(y * zoom.mapSize + 0.5, 0, zoom.mapSize - 1);

        return new Point((int) pixelX, (int) pixelY);
    }

    /**
     * Converts a world point back into geographical coordinates for a zoom level.
     *
     * @param point
     * @return
     */
    public static LatLng worldPointToLatLng(Point point, ZoomLevel zoom) {
        final int mapSize = zoom.mapSize;
        final double x = (clip(point.x, 0, mapSize - 1.0) / mapSize) - 0.5;
        final double y = 0.5 - ((double) clip(point.y, 0, mapSize - 1) / mapSize);
        double latitude = 90 - 360.0 * atan(exp(-y * 2 * PI)) / PI;
        double longitude = 360.0 * x;
        return new LatLng(latitude, longitude);
    }

    /**
     * Computes the X, Y coordinates of the tile containing this point.
     *
     * @param point
     * @return
     */
    public static Point worldPointToTileXY(Point point) {
        final int tileX = (int) Math.floor((double) point.x / TILE_SIZE);
        final int tileY = (int) Math.ceil((double) point.y / TILE_SIZE);
        return new Point(tileX, tileY);
    }

    public static float distanceBetween(double startLatitude, double startLongitude,
                                        double endLatitude, double endLongitude) {
        float[] results = new float[1];
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);
        return results[0];
    }

    /**
     * Computes the QuadKey string for a tile at a certain level.
     * @return
     */
    public static String getQuadKey(int x, int y, int zoom) {
        StringBuilder quadKey = new StringBuilder();
        for (int i = zoom; i > 0; i--) {
            char digit = '0';
            int mask = 1 << (i - 1);
            if ((x & mask) != 0) {
                digit++;
            }
            if ((y & mask) != 0) {
                digit++;
                digit++;
            }
            quadKey.append(digit);
        }
        return quadKey.toString();
    }

    public static String getQuadKey(LatLng latLng, ZoomLevel zoom) {
        Point tile = worldPointToTileXY(latLngToWorldPoint(latLng, zoom));
        return getQuadKey(tile.x, tile.y, zoom.zoom);
    }
}
