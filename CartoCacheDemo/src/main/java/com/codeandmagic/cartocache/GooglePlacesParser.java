package com.codeandmagic.cartocache;


import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by evelyne24.
 */
public class GooglePlacesParser {

    private static final String RESULTS = "results";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String GEOMETRY = "geometry";
    private static final String LOCATION = "location";
    private static final String LATITUDE = "lat";
    private static final String LONGITUDE = "lng";

    public Collection<GooglePlace> parse(String response) {
        try {
            final JSONArray results = new JSONObject(response).getJSONArray(RESULTS);
            if(results == null || results.length() == 0) {
                return Collections.emptyList();
            }

            final Collection<GooglePlace> places = new ArrayList<GooglePlace>();
            final int length = results.length();
            for(int i = 0; i < length; ++i) {
                final JSONObject result = results.getJSONObject(i);
                final GooglePlace place = new GooglePlace(result.getString(ID),
                        result.getString(NAME),
                        parseLocation(result.getJSONObject(GEOMETRY).getJSONObject(LOCATION)));
                places.add(place);
            }
            return places;

        } catch (JSONException e) {
            return Collections.emptyList();
        }
    }

    private LatLng parseLocation(JSONObject json) throws JSONException{
       return new LatLng(json.getDouble(LATITUDE), json.getDouble(LONGITUDE));
    }
}
