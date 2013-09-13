package com.codeandmagic.cartocache;

import android.graphics.Color;

/**
 * Created by evelyne24.
 */
public class CartoCacheConfig<P extends Place> {

    public static class Builder<P extends Place> {
        private boolean debug = true;
        public QTileDrawConfig currentTileConfig = new QTileDrawConfig(Color.parseColor("#77ff8282"), Color.RED, 2);
        public QTileDrawConfig tileConfig = new QTileDrawConfig(Color.parseColor("#77b0b1ff"), Color.BLUE, 1);

        private ZoomLevel maxDataFetchZoom = ZoomLevel.Z17;
        private ZoomLevel minDataFetchZoom = ZoomLevel.Z11;
        private ZoomLevel clusteringZoom = ZoomLevel.Z13;
        private MarkerConfig<P> markerConfig = new DefaultMarkerConfig<P>();
        private PlacesCache<P> placesCache = new DefaultPlaceCache<P>(false);
        private DataFetcher<P> dataFetcher;


        public Builder<P> setDebug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public Builder<P> setCurrentTileConfig(QTileDrawConfig currentTileConfig) {
            this.currentTileConfig = currentTileConfig;
            return this;
        }

        public Builder<P> setTileConfig(QTileDrawConfig tileConfig) {
            this.tileConfig = tileConfig;
            return this;
        }

        public Builder<P> setMinDataFetchZoom(ZoomLevel minDataFetchZoom) {
            this.minDataFetchZoom = minDataFetchZoom;
            return this;
        }

        public Builder<P> setMaxDataFetchZoom(ZoomLevel maxDataFetchZoom) {
            this.maxDataFetchZoom = maxDataFetchZoom;
            return this;
        }

        public Builder<P> setClusteringZoom(ZoomLevel clusteringZoom) {
            this.clusteringZoom = clusteringZoom;
            return this;
        }

        public Builder<P> setMarkerConfig(MarkerConfig<P> markerConfig) {
            this.markerConfig = markerConfig;
            return this;
        }

        public Builder<P> setPlacesCache(PlacesCache<P> placesCache) {
            this.placesCache = placesCache;
            return this;
        }

        public Builder<P> setDataFetcher(DataFetcher<P> dataFetcher) {
            this.dataFetcher = dataFetcher;
            return this;
        }


        public CartoCacheConfig<P> build() {
            if(dataFetcher == null) {
                throw new IllegalArgumentException("Missing required DataFetcher configuration.");
            }
            return new CartoCacheConfig<P>(this);
        }
    }

    public final boolean debug;
    public final QTileDrawConfig currentTileConfig;
    public final QTileDrawConfig tileConfig;

    public final ZoomLevel maxDataFetchZoom;
    public final ZoomLevel minDataFetchZoom;
    public final ZoomLevel clusteringZoom;
    public final MarkerConfig<P> markerConfig;
    public final PlacesCache<P> placesCache;
    public final DataFetcher<P> dataFetcher;


    private CartoCacheConfig(Builder<P> builder) {
        this.debug = builder.debug;
        this.currentTileConfig = builder.currentTileConfig;
        this.tileConfig = builder.tileConfig;

        this.minDataFetchZoom = builder.minDataFetchZoom;
        this.maxDataFetchZoom = builder.maxDataFetchZoom;
        this.clusteringZoom = builder.clusteringZoom;
        this.markerConfig = builder.markerConfig;
        this.placesCache = builder.placesCache;
        this.dataFetcher = builder.dataFetcher;
    }

}
