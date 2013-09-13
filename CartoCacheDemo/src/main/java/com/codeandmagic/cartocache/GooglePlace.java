package com.codeandmagic.cartocache;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by evelyne24.
 */
public class GooglePlace implements Place {

    public final String id;
    public final String name;
    public final LatLng latLng;

    public GooglePlace(String id, String name, LatLng latLng) {
        this.id = id;
        this.name = name;
        this.latLng = latLng;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public LatLng getPosition() {
        return latLng;
    }
}
