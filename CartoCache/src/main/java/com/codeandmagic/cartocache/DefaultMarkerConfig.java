package com.codeandmagic.cartocache;

import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by evelyne24.
 */
public class DefaultMarkerConfig<P extends Place> implements MarkerConfig<P> {

    @Override
    public MarkerOptions getMarker(P place) {
        return new MarkerOptions().position(place.getPosition()).title("Place " + place.getId());
    }
}
