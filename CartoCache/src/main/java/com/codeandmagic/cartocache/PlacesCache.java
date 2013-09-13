package com.codeandmagic.cartocache;

import java.util.Collection;

/**
 * Created by evelyne24.
 */
public interface PlacesCache<P extends Place> {

    public Collection<P> get(QTile qTile);

    public void add(QTile qTile, Collection<P> places);

    public boolean isMultiLevelCache();
}
