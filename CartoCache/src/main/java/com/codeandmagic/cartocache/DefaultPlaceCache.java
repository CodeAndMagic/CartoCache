package com.codeandmagic.cartocache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by evelyne24.
 */
public class DefaultPlaceCache<P extends Place> implements PlacesCache<P> {

    private final Map<String, Collection<P>> cache;
    private final boolean multiLevelCache;

    public DefaultPlaceCache(boolean multiLevelCache) {
        this.multiLevelCache = multiLevelCache;
        this.cache = new HashMap<String, Collection<P>>();
    }

    @Override
    public void add(QTile qTile, Collection<P> places) {
        cache.put(qTile.quadKey, places);
    }

    @Override
    public boolean isMultiLevelCache() {
        return multiLevelCache;
    }

    @Override
    public Collection<P> get(QTile qTile) {
        final String quadKey = qTile.quadKey;

        if (multiLevelCache) {
            return cache.get(quadKey);
        } else {
            // Try to find an exact match
            Collection<P> match = cache.get(quadKey);
            if (match != null) {
                return match;
            }

            // Try to find a parent tile that has cached locations
            for (String maybeParentQuadKey : cache.keySet()) {
                if (quadKey.startsWith(maybeParentQuadKey)) {
                    return cache.get(maybeParentQuadKey);
                }
            }
            // not found
            return null;
        }
    }
}
