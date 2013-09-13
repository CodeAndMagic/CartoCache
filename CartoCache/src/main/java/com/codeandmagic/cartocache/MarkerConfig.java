package com.codeandmagic.cartocache;

import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by evelyne24.
 */
public interface MarkerConfig<P extends Place> {

    public MarkerOptions getMarker(P place);
}
