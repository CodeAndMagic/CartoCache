package com.codeandmagic.cartocache;

import com.google.android.gms.maps.model.LatLng;


/**
 * A {@link Place} is an abstract concept of a geographical
 * location given by latitude and longitude.
 *
 * It is {@link android.os.Parcelable} so it can be saved and restored.
 * It must have an unique identifier.
 */
public interface Place {

    String getId();

    LatLng getPosition();
}
