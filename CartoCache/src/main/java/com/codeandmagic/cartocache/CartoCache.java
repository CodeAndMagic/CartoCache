package com.codeandmagic.cartocache;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.codeandmagic.cartocache.QTile.EMPTY_ARRAY;
import static com.codeandmagic.cartocache.QTile.getTile;
import static com.codeandmagic.cartocache.Utils.diff;

/**
 * Created by evelyne24.
 */
public class CartoCache<P extends Place> implements GoogleMap.OnCameraChangeListener, DataFetcher.Callback<P> {

    private static final L log = L.getLog(CartoCache.class);

    private final GoogleMap map;
    private final CartoCacheConfig<P> config;

    private Map<QTile, Polygon> drawnTiles = new HashMap<QTile, Polygon>();
    private Map<QTile, List<Marker>> markers = new HashMap<QTile, List<Marker>>();
    private QTile[] visibleTiles = EMPTY_ARRAY;

    public CartoCache(CartoCacheConfig<P> config, GoogleMap map) {
        this.config = config;
        this.map = map;
        this.map.setOnCameraChangeListener(this);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        final LatLng latLng = cameraPosition.target;
        final ZoomLevel zoom = getDataFetchZoom(cameraPosition.zoom);
        final QTile centerTile = getTile(latLng, zoom);
        centerTile.initNeighbourTiles();

        QTile[] newTiles = new QTile[4];
        newTiles[0] = centerTile;

        QTile[] neighbours = centerTile.getClosestNeighbourTiles(latLng);
        System.arraycopy(neighbours, 0, newTiles, 1, neighbours.length);

        QTile[] removedTiles = diff(visibleTiles, newTiles, EMPTY_ARRAY);
        QTile[] addedTiles = diff(newTiles, visibleTiles, EMPTY_ARRAY);

        // Delete markers for all remove drawnTiles
        for (QTile tile : removedTiles) {
            removePlacesForTile(tile);
        }

        // Delete all tile debug squares
        for (Map.Entry<QTile, Polygon> entry : drawnTiles.entrySet()) {
            entry.getValue().remove();
        }

        if (config.debug) {
            drawnTiles.clear();

            // Add all new tile debug squares
            for (QTile tile : newTiles) {
                drawnTiles.put(tile, drawTile(tile, tile.equals(centerTile) ? config.currentTileConfig : config.tileConfig));
            }
        }

        // Add markers for new added drawnTiles:
        // First verify if the drawnTiles are in cache.
        // If not, request places for the new drawnTiles.
        for (QTile qTile : addedTiles) {
            Collection<P> places = config.placesCache.get(qTile);
            if (places != null) {
                if(config.debug) {
                    log.i("Cache HIT for QTile " + qTile.quadKey);
                }
                addPlacesForTile(qTile, places);
            } else {
                if(config.debug) {
                    log.w("Cache MISS for QTile " + qTile.quadKey);
                }
                config.dataFetcher.requestPlaces(qTile, this);
            }
        }

        visibleTiles = newTiles;
    }

    private ZoomLevel getDataFetchZoom(float cameraZoom) {
        final int seeAllScreenZoom = (int) (cameraZoom - 1);
        if (seeAllScreenZoom < 1) {
            return ZoomLevel.Z1;
        }
        if (seeAllScreenZoom > config.maxDataFetchZoom.zoom) {
            return config.maxDataFetchZoom;
        }
        return ZoomLevel.get(seeAllScreenZoom);
    }

    private void removePlacesForTile(QTile qTile) {
        final List<Marker> list = markers.get(qTile);
        if (list != null) {
            for (Marker marker : list) {
                marker.remove();
            }
        }
        markers.remove(qTile);
    }

    private void addPlacesForTile(QTile qTile, Collection<P> places) {
        final List<Marker> list = new ArrayList<Marker>();
        for (P place : places) {
            list.add(map.addMarker(config.markerConfig.getMarker(place)));
        }
        markers.put(qTile, list);
    }

    private Polygon drawTile(QTile tile, QTileDrawConfig config) {
        return map.addPolygon(new PolygonOptions()
                .add(tile.topLeft, tile.topRight, tile.bottomRight, tile.bottomLeft)
                .fillColor(config.fillColor)
                .strokeColor(config.strokeColor)
                .strokeWidth(config.strokeWidth));
    }


    @Override
    public void onSuccess(QTile qTile, Collection<P> places) {
        config.placesCache.add(qTile, places);
        addPlacesForTile(qTile, config.placesCache.get(qTile));
    }

    @Override
    public void onError(QTile qTile, Throwable e) {

    }
}
