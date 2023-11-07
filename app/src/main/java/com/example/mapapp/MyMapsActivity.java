package com.example.mapapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

public class MyMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 15;
    private GoogleMap mMap;
    private boolean locationPermissionGranted;
    private Location lastKnownLocation;
    private LatLng defaultLocation = new LatLng(57.536393, 25.424059);
    private FusedLocationProviderClient fusedLocationProviderClient;
    private PlacesClient placesClient;
    private String[] likelyPlaceNames;
    private String[] likelyPlaceAddresses;
    private List[] likelyPlaceAttributions;
    private LatLng[] likelyPlaceLatLngs;

    private static final int M_MAX_ENTRIES = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize Places SDK
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setBuildingsEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        updateLocationUI();
        getDeviceLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.getplaces:
                showCurrentPlace();
                return true;
            case R.id.readnews:
                startActivity(new Intent(MyMapsActivity.this, NewsReaderActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        }

                        LatLng mountainView = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.zoomIn());
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(mountainView)
                                .zoom(17)
                                .bearing(90)
                                .tilt(30)
                                .build();
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        PolylineOptions polylineOptions = new PolylineOptions()
                                .add(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()))
                                .add(new LatLng(lastKnownLocation.getLatitude() + 0.10, lastKnownLocation.getLongitude()))
                                .add(new LatLng(lastKnownLocation.getLatitude() + 0.10, lastKnownLocation.getLongitude() + 0.2))
                                .add(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude() + 0.2))
                                .add(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
                        Polyline polyline = mMap.addPolyline(polylineOptions);
                    } else {
                        Log.d("MAPSACTIVITY", "Current location is null. Using defaults.");
                        Log.e("MAPSACTIVITY", "Exception: %s", task.getException());
                        mMap.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void showCurrentPlace() {
        if (mMap == null) {
            return;
        }

        if (locationPermissionGranted) {
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
            FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

            @SuppressWarnings("MissingPermission") final Task<FindCurrentPlaceResponse> placeResult = placesClient.findCurrentPlace(request);
            placeResult.addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    FindCurrentPlaceResponse likelyPlaces = task.getResult();
                    int count = Math.min(likelyPlaces.getPlaceLikelihoods().size(), M_MAX_ENTRIES);

                    int i = 0;
                    likelyPlaceNames = new String[count];
                    likelyPlaceAddresses = new String[count];
                    likelyPlaceAttributions = new List[count];
                    likelyPlaceLatLngs = new LatLng[count];

                    for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {
                        likelyPlaceNames[i] = placeLikelihood.getPlace().getName();
                        likelyPlaceAddresses[i] = placeLikelihood.getPlace().getAddress();
                        likelyPlaceAttributions[i] = placeLikelihood.getPlace().getAttributions();
                        likelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();
                        i++;
                        if (i >= count) {
                            break;
                        }
                    }
                    openPlacesDialog();
                } else {
                    Log.e("MAPTAG", "Exception: %s", task.getException());
                }
            });
        } else {
            Log.i("MAPTAG", "The user did not grant location permission.");
            mMap.addMarker(new MarkerOptions()
                    .title("Default title")
                    .position(defaultLocation)
                    .snippet("Default snippet"));
            getLocationPermission();
        }
    }

    private void openPlacesDialog() {
        DialogInterface.OnClickListener listener = (dialog, which) -> {
            LatLng markerLatLng = likelyPlaceLatLngs[which];
            String markerSnippet = likelyPlaceAddresses[which];
            if (likelyPlaceAttributions[which] != null) {
                markerSnippet = markerSnippet + "\n" + likelyPlaceAttributions[which];
            }
            mMap.addMarker(new MarkerOptions()
                    .title(likelyPlaceNames[which])
                    .position(markerLatLng)
                    .snippet(markerSnippet));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, DEFAULT_ZOOM));
        };

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(likelyPlaceNames, listener)
                .show();
    }
}
