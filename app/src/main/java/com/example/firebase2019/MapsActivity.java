package com.example.firebase2019;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnPolylineClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "MapsActivity";
    private static final String TAG2 = "MapsActivity2";
    private static final float DEFAULT_ZOOM = 15.5F;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));

    private boolean mPermissionDenied = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mMap;
    private LatLng myLoc;

    //    Widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mGps;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;

    boolean do1 = true;
    private GeoApiContext mGeoApiContext = null;


    private LatLng targetPosition;
    //    Polyline Data
    private ArrayList<PolylineData> mPolylinesData = new ArrayList<>();
    private ArrayList<Marker> mTripMarkers = new ArrayList<>();
    private Marker mSelectedMarker;

    private String nextInstruction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mSearchText = (AutoCompleteTextView) findViewById(R.id.input_search);
        mGps = (ImageView) findViewById(R.id.ic_gps);


        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private void init() {

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {

                    //execute our method for searching
                    geoLocate();
                }

                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked gps icon");
                getLastKnowLocation();
            }
        });

        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_key))
                    .build();

        }
    }

    private void calculateDirections(double lat, double lng) {
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                lat,
                lng
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(false);
        directions.origin(
                new com.google.maps.model.LatLng(
                        myLoc.latitude,
                        myLoc.longitude
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());


                Log.e(TAG, "onResult: Testing Path");

                int steps = result.routes[0].legs[0].steps.length;
                for (int i = 0; i < steps; i++) {
                    try {
                        String shouldTurn = result.routes[0].legs[0].steps[i].maneuver;

//                        Toast.makeText(MapsActivity.this, "Input Received"+shouldTurn, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Turning Part: " + shouldTurn);
                        if (shouldTurn != null) {
                            nextInstruction = shouldTurn;
                            break;
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "onResult: ShouldTurn" + e.toString());
                        continue;
                    }
                }

                addPolyLinesToMap(result);
                if (do1) {
                    do1 = false;
                    startLooper();
                }


            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage());

            }
        });


//        Toast.makeText(this, "Step Instruction : " + nextInstruction, Toast.LENGTH_SHORT).show();
        Log.e(TAG2, "addPolyLinesToMap: Step Instruction" + nextInstruction);
    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate: Geolocating");


        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
        }

        boolean doOnce = true;
        if (list.size() > 0) {
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
//            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM,address.getAddressLine(0));


            if (doOnce) {
                targetPosition = new LatLng(address.getLatitude(), address.getLongitude());
                ArrayList<LatLng> transform = new ArrayList<>();
                transform.add(new LatLng(myLoc.latitude, myLoc.longitude));
                transform.add(new LatLng(address.getLatitude(), address.getLongitude()));
                zoomRoute(transform);
                LatLng target = new LatLng(address.getLatitude(), address.getLongitude());
                MarkerOptions options = new MarkerOptions()
                        .position(target)
                        .title(address.getAddressLine(0));
                mMap.addMarker(options);
                calculateDirections(address.getLatitude(), address.getLongitude());
            }
        }
    }

    private void addPolyLinesToMap(final DirectionsResult result) {


        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes " + result.routes.length);

                if (mPolylinesData.size() > 0) {
                    for (PolylineData polylineData : mPolylinesData) {
                        polylineData.getPolyline().remove();
                    }
                    mPolylinesData.clear();
                    mPolylinesData = new ArrayList<>();
                }

                for (DirectionsRoute route : result.routes) {
                    Log.d(TAG, "run: leg:" + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    for (com.google.maps.model.LatLng latLng : decodedPath) {
                        Log.d(TAG, "run: Latlang" + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));

                        Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                        polyline.setColor(ContextCompat.getColor(getApplicationContext(), R.color.apploizc_darker_gray_color));
                        polyline.setClickable(true);

                        mPolylinesData.add(new PolylineData(polyline, route.legs[0]));
                    }
                }

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {

        mMap = map;

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnPolylineClickListener(this);

        enableMyLocation();
        Log.d(TAG, "onMapReady: Getting Fused Location Provider");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        getLastKnowLocation();
        init();
    }


    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

//        resetSelectedMarkers();


//        MarkerOptions options = new MarkerOptions()
//                .position(latLng)
//                .title(title);
//        mMap.addMarker(options);

//        if(!title.equals("My Location")){
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(title);
        Marker marker = mMap.addMarker(options);
        mMap.addMarker(options);
        mSelectedMarker = marker;
        mTripMarkers.add(marker);
//        }

        hideSoftKeyboard();

    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }

    }

    @SuppressLint("MissingPermission")
    private void getLastKnowLocation() {
        Log.d(TAG, "getLastKnowLocation: Last Location Call");

        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
//                    GeoPoint geoPoint = new GeoPint(location.get)

                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    myLoc = new LatLng(lat, lng);
                    Log.d(TAG, "Location Settings:" + myLoc.toString());
                    String latlng = "Your Location:" + Double.toString(lat) + " , " + Double.toString(lng);

                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(myLoc).zoom(15.5F).build()));

                    Toast.makeText(MapsActivity.this, latlng, Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPolylineClick(Polyline polyline) {


        for (PolylineData polylineData : mPolylinesData) {
            Log.d(TAG, "onPolylineClick: to String" + polylineData.toString());
            if (polyline.getId().equals(polylineData.getPolyline().getId())) {
                polylineData.getPolyline().setColor(ContextCompat.getColor(getApplicationContext(), R.color.holo_blue));
                polyline.setZIndex(10);


//                mTripMarkers.add(marker);
            } else {
                polylineData.getPolyline().setColor(ContextCompat.getColor(getApplicationContext(), R.color.apploizc_darker_gray_color));
                polyline.setZIndex(0);
            }
        }

        zoomRoute(polyline.getPoints());

    }

//    private void removeTripMarkers()
//    {
//        for(Marker marker:mTripMarkers)
//        {
//            marker.remove();
//        }
//    }
//
//    private void resetSelectedMarkers()
//    {
//        if(mSelectedMarker != null)
//        {
//            mSelectedMarker.setVisible(true);
//            mSelectedMarker = null;
//            removeTripMarkers();
//        }
//    }

    public void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (mMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }


    public void startLooper() {

        Log.e(TAG2, "startLooper: Starting Looper");
        Thread t = new Thread() {
            @Override
            public void run() {
                int i = 0;
                while (i < 50) {
                    Log.e(TAG, "onMyLocationChange: OnLocation Change Listener");
                    getLastKnowLocation();
                    calculateDirections(targetPosition.latitude, targetPosition.longitude);

                    Log.e(TAG2, "run: CurrentStatus:" + nextInstruction );

                    if(nextInstruction.equals("turn-right") || nextInstruction.equals("turn-sharp-right"))
                    {
                        Log.d(TAG2, "run: " + "Turning Right" );

                        AsyncHttpClient client = new AsyncHttpClient();
                        String url = "http://192.168.43.43/r";

                        Log.d(TAG2, "RVibrates: Vibrating R Start");

                        client.get(url, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                            }
                        });
                        try {
                            sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG2, "RVibrates: Vibrating R Stop");
                        client = new AsyncHttpClient();
                        url="http://192.168.43.43/s";

                        client.get(url, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                            }
                        });
                    }

                    if(nextInstruction.equals("turn-left") || nextInstruction.equals("turn-sharp-left"))
                    {
                        Log.d(TAG2, "run: " + "Turning Left" );

                        Log.d(TAG2, "RVibrates: Vibrating L Start");
                        AsyncHttpClient client = new AsyncHttpClient();
                        String url = "http://192.168.43.53/r";

                        client.get(url, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                            }
                        });



                        try {
                            sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                        Log.d(TAG2, "RVibrates: Vibrating R Stop");
                        client = new AsyncHttpClient();
                        url="http://192.168.43.53/s";

                        client.get(url, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                            }
                        });
                    }


                    try {
                        sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    i++;
                }

            }
        };

        Log.e(TAG2, "startLooper: Starting Thread");
        t.start();

//        Handler handler1 = new Handler();
//        for (int a = 1;  a<10;a++) {
//            handler1.postDelayed(new Runnable() {
//
//                @Override
//                public void run() {
//                    Log.e(TAG, "onMyLocationChange: OnLocation Change Listener");
//                    getLastKnowLocation();
//                    calculateDirections(targetPosition.latitude,targetPosition.longitude);
//                }
//            }, 3000 * a);
//        }
//    }


    }


}
