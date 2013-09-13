package com.codeandmagic.cartocache;

import java.util.Collection;

/**
 * Created by evelyne24.
 */
public interface DataFetcher<P extends Place> {

    public static interface Callback<P extends Place> {
        void onSuccess(final QTile qTile, final Collection<P> places);
        void onError(final QTile qTile, final Throwable e);
    }

    public void requestPlaces(final QTile qTile, final Callback<P> callback);
}
