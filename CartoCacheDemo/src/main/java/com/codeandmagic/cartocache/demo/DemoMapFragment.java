package com.codeandmagic.cartocache.demo;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.codeandmagic.cartocache.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import static com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;

/**
 * Created by evelyne24.
 */
public class DemoMapFragment extends SupportMapFragment implements ConnectionCallbacks, OnConnectionFailedListener,
    LocationListener {

    private static L log = L.getLog(DemoMapFragment.class);
    private static final int MILLIS_PER_SECOND = 1000;
    private static long HIGH_PRIORITY_UPDATE_INTERVAL = 5 * MILLIS_PER_SECOND;
    private static long HIGH_PRIORITY_FAST_INTERVAL = MILLIS_PER_SECOND;
    private static long FRESH_LOCATION_INTERVAL = 60 * MILLIS_PER_SECOND;
    private static int DEFAULT_RADIUS = 1000;
    private static int DISPLAY_ZOOM = 15;

    private GoogleMap map;
    private CartoCache<GooglePlace> cartoCache;
    private CartoCacheConfig<GooglePlace> config;
    private LocationClient locationClient;


    public static DemoMapFragment newInstance() {
        return new DemoMapFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        locationClient = initLocationClient();
        if (config == null) {
            config = new CartoCacheConfig.Builder<GooglePlace>()
                .setPlacesCache(new DefaultPlaceCache<GooglePlace>(true))
                .setDataFetcher(new GooglePlacesDataFetcher(getActivity()))
                .build();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        map = initMap(map);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) {
            map.setMyLocationEnabled(false);
        }
    }

    private GoogleMap initMap(GoogleMap existingMap) {
        final GoogleMap map;
        if (existingMap == null) {
            map = getMap();
            if (map == null) {
                log.e("Google Maps v2 not supported on this device.");
                return map;
            }
        } else {
            map = getMap();
        }
        map.setMyLocationEnabled(true);
        return map;
    }

    private LocationClient initLocationClient() {
        final LocationClient client = new LocationClient(getActivity(), this, this);
        client.connect();
        return client;
    }

    private LocationRequest getSingleUpdateRequest() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setFastestInterval(HIGH_PRIORITY_FAST_INTERVAL);
        request.setInterval(HIGH_PRIORITY_UPDATE_INTERVAL);
        request.setNumUpdates(1);
        return request;
    }

    private void initCartoCache() {
        if (cartoCache == null) {
            cartoCache = new CartoCache<GooglePlace>(config, map);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        final Location lastLocation = locationClient.getLastLocation();
        if (isLocationFresh(lastLocation)) {
            centerMapOnLocation(lastLocation);
            initCartoCache();
        } else {
            locationClient.requestLocationUpdates(getSingleUpdateRequest(), this);
        }
    }

    private boolean isLocationFresh(Location location) {
        return location != null &&
            System.currentTimeMillis() - location.getTime() <= FRESH_LOCATION_INTERVAL &&
            location.getAccuracy() <= DEFAULT_RADIUS;
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        initCartoCache();
        centerMapOnLocation(location);
        locationClient.removeLocationUpdates(this);
    }

    private void centerMapOnLocation(Location location) {
        if (map != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), DISPLAY_ZOOM));
        }
    }


    private static class GooglePlacesDataFetcher implements DataFetcher<GooglePlace> {

        private static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
        private static final String PARAM_KEY = "key";
        private static final String PARAM_LOCATION = "location";
        private static final String LOCATION_FORMAT = "{0},{1}";
        private static final String PARAM_RADIUS = "radius";
        private static final String PARAM_SENSOR = "sensor";
        private static final String PARAM_TYPES = "types";
        private static final String DEFAULT_TYPES = "food|restaurant|cafe|bar";
        private static final String ENCODING = "UTF-8";

        private String apiKey;
        private RequestQueue requestQueue;
        private GooglePlacesParser parser;

        public GooglePlacesDataFetcher(Context context) {
            this.apiKey = context.getString(R.string.google_places_api_key);
            this.requestQueue = Volley.newRequestQueue(context);
            this.parser = new GooglePlacesParser();
        }

        @Override
        public void requestPlaces(final QTile qTile, final Callback<GooglePlace> callback) {
            final String url = getUrl(qTile.center, qTile.radius);
            requestQueue.add(new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onSuccess(qTile, parser.parse(response));

                    }
                }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    callback.onError(qTile, new Throwable(error));
                }
            }
            ));
        }

        private String getUrl(LatLng latLng, double radius) {
            final List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
            params.add(new BasicNameValuePair(PARAM_KEY, apiKey));
            params.add(new BasicNameValuePair(PARAM_LOCATION,
                MessageFormat.format(LOCATION_FORMAT, latLng.latitude, latLng.longitude)));
            params.add(new BasicNameValuePair(PARAM_RADIUS, String.valueOf(radius)));
            params.add(new BasicNameValuePair(PARAM_SENSOR, "true"));
            params.add(new BasicNameValuePair(PARAM_TYPES, DEFAULT_TYPES));
            return BASE_URL + URLEncodedUtils.format(params, ENCODING);
        }
    }
}
