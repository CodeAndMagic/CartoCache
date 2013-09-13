package com.codeandmagic.cartocache;

import android.graphics.Point;

import com.google.android.gms.maps.model.LatLng;

import static com.codeandmagic.cartocache.LocationUtils.*;

/**
 * A {@link com.codeandmagic.cartocache.QTile} is a portion of the world map viewed as a Mercador
 * projection. It can be imagined as a square tile of certain dimensions, for example 256 x 256 pixels.
 *
 * <br/><br/>
 * The world map has different dimensions based on the zoom level:
 *
 * <br/>
 * <p>256 x 256 pixels for zoom level 0</p>
 * <p>512 x 512 pixels for zoom level 1</p>
 * <p>...</p>
 * <p>256 * (2^level - 1) x 256 * (2^level - 1) pixels for zoom level level</p>
 *
 * <br/>
 * Since a tile has 256 x 256 pixels, the map will have a different number of tiles per each zoom level.
 * We can express the map's dimensions in the number of tiles: map width = map height = 2^level tiles.
 *
 * <br/>
 * Each tile is given a pair of (x, y) coordinates ranging from (0, 0) at the top left of the map
 * to (2^level - 1, 2^level - 1) at the bottom right.
 *
 * <br/>
 * For any {@link Place} that has a latitude and a longitude we can
 * compute its (x, y) coordinates on the map at a certain zoom level using the formulae below:
 * <p>sinLatitude = sin(latitude * pi/180)</p>
 * <p>pixelX = ((longitude + 180) / 360) * 256 * 2 level</p>
 * <p>pixelY = (0.5 – log((1 + sinLatitude) / (1 – sinLatitude)) / (4 * pi)) * 256 * 2 level</p>
 *
 * <br/>
 * We can also compute which tile the {@link Place} belongs to by
 * using tileX = floor(pixelX / 256), tileY = ceiling(pixelY / 256). The reason for using ceiling
 * for the y coordinate is that the latitude grows from the South Pole towards the North Pole
 * while in the coordinate system on Android Y-axis grows from top to bottom.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Mercator_projection">Mercador Projection</a>
 * @see <a href="http://msdn.microsoft.com/en-us/library/bb259689.aspx>Bings Maps Tile System</a>
 */
public class QTile {

    public static QTile[] EMPTY_ARRAY = new QTile[]{};


    /**
     * The x coordinate of the top left corner of the tile.
     */
    public final int x;
    /**
     * The y coordinate of the top left corner of the tile.
     */
    public final int y;
    /**
     * The zoom level for which we compute the coordinates.
     */
    public final ZoomLevel zoom;
    /**
     * The radius of the circle encircling the square tile.
     */
    public final double radius;
    /**
     * The quad key of this tile at the given zoom level.
     */
    public final String quadKey;

    /**
     * The distance between the edge of the world map to the top left corner of the tile.
     */
    public final Point worldLeftTop;
    /**
     * The distance between the edge of the world map and the bottom right corner of the tile.
     */
    public final Point worldRightBottom;
    /**
     * The latitude and longitude of the top left corner of the tile.
     */
    public final LatLng topLeft;
    /**
     * The latitude and longitude of the top right corner of the tile.
     */
    public final LatLng topRight;
    /**
     * The latitude and longitude of the bottom left corner of the tile.
     */
    public final LatLng bottomLeft;
    /**
     * The latitude and longitude of the bottom right corner of the tile.
     */
    public final LatLng bottomRight;
    /**
     * The latitude and longitude of the center of the tile.
     */
    public final LatLng center;

    /**
     * The top left neighbouring tile.
     */
    public QTile topLeftNeighbour;
    /**
     * The top neighbouring tile.
     */
    public QTile topNeighbour;
    /**
     * The top right neighbouring tile.
     */
    public QTile topRightNeighbour;
    /**
     * The left neighbouring tile.
     */
    public QTile leftNeighbour;
    /**
     * The right neighbouring tile.
     */
    public QTile rightNeighbour;
    /**
     * The bottom left neighbouring tile.
     */
    public QTile bottomLeftNeighbour;
    /**
     * The bottom neighbouring tile.
     */
    public QTile bottomNeighbour;
    /**
     * The bottom right neighbouring tile.
     */
    public QTile bottomRightNeighbour;

    /**
     * A flag for remembering whether the neighbouring tiles have been initialized or not.
     */
    private volatile boolean neighboursAreInit = false;

    /**
     * Computes the center {@link com.codeandmagic.cartocache.QTile} for a given geographical position
     * and a certain zoom level.
     * @param latLng
     * @param zoom
     * @return
     */
    public static QTile getTile(LatLng latLng, ZoomLevel zoom) {
        final Point center = latLngToWorldPoint(latLng, zoom);
        final Point tile = worldPointToTileXY(center);
        return new QTile(tile.x, tile.y, zoom);
    }

    public QTile(final int x, final int y, final ZoomLevel zoom) {
        this.x = x;
        this.y = y;
        this.zoom = zoom;

        worldLeftTop = new Point(x * TILE_SIZE, y * TILE_SIZE);
        worldRightBottom = new Point((x + 1) * TILE_SIZE, (y - 1) * TILE_SIZE);

        topLeft = worldPointToLatLng(worldLeftTop, zoom);
        bottomRight = worldPointToLatLng(worldRightBottom, zoom);

        bottomLeft = new LatLng(bottomRight.latitude, topLeft.longitude);
        topRight = new LatLng(topLeft.latitude, bottomRight.longitude);

        // (BR - TL) / 2 + TL
        double halfX = (bottomRight.longitude + topLeft.longitude) / 2.0;
        // (TL - BR) / 2 + BR
        double halfY = (bottomRight.latitude + topLeft.latitude) / 2.0;
        center = new LatLng(halfY, halfX);

        radius = distanceBetween(center.latitude, center.longitude, topLeft.latitude, topLeft.longitude);
        quadKey = getQuadKey();
    }


    public synchronized void initNeighbourTiles() {
        if (!neighboursAreInit) {
            this.topLeftNeighbour = getNeighbourTile(x - 1, y - 1);
            this.topNeighbour = getNeighbourTile(x, y - 1);
            this.topRightNeighbour = getNeighbourTile(x + 1, y - 1);
            this.leftNeighbour = getNeighbourTile(x - 1, y);
            this.rightNeighbour = getNeighbourTile(x + 1, y);
            this.bottomLeftNeighbour = getNeighbourTile(x - 1, y + 1);
            this.bottomNeighbour = getNeighbourTile(x, y + 1);
            this.bottomRightNeighbour = getNeighbourTile(x + 1, y + 1);
            neighboursAreInit = true;
        }
    }

    /**
     * Compute the closest 3 neighbours based on where the given latitude and longitude are in the current tile.
     *
     * @param latLng
     * @return
     */
    public QTile[] getClosestNeighbourTiles(LatLng latLng) {
        initNeighbourTiles();

        // center
        double x = latLng.longitude;
        double y = latLng.latitude;

        double centerX = center.longitude;
        double centerY = center.latitude;

        // Check in what quarter of this tile is the point situated
        if (x < centerX && y < centerY) {
            return new QTile[]{leftNeighbour, bottomLeftNeighbour, bottomNeighbour};
        } else if (x < centerX && y >= centerY) {
            return new QTile[]{leftNeighbour, topLeftNeighbour, topNeighbour};
        } else if (x >= centerX && y < centerY) {
            return new QTile[]{bottomNeighbour, bottomRightNeighbour, rightNeighbour};
        } else { //if(x >= halfX && y >= halfY) {
            return new QTile[]{topNeighbour, topRightNeighbour, rightNeighbour};
        }
    }

    public String getQuadKey() {
        return LocationUtils.getQuadKey(x, y, zoom.zoom);
    }

    public QTile getNeighbourTile(int x, int y) {
        if (x < 0) {
            x = zoom.maxTiles.x + x;
        } else if (x > zoom.maxTiles.x) {
            x = x - zoom.maxTiles.x;
        }
        if (y < 0) {
            y = zoom.maxTiles.y + y;
        } else if (y > zoom.maxTiles.y) {
            y = y - zoom.maxTiles.y;
        }

        return new QTile(x, y, zoom);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QTile{");
        sb.append("x=").append(x);
        sb.append(", y=").append(y);
        sb.append(", zoom=").append(zoom);
        sb.append(", worldLeftTop=").append(worldLeftTop);
        sb.append(", worldRightBottom=").append(worldRightBottom);
        sb.append(", topLeft=").append(topLeft);
        sb.append(", bottomRight=").append(bottomRight);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || QTile.class != o.getClass()) return false;

        final QTile qTile = (QTile) o;

        if (x != qTile.x) return false;
        if (y != qTile.y) return false;
        if (zoom != qTile.zoom) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + zoom.hashCode();
        return result;
    }
}
