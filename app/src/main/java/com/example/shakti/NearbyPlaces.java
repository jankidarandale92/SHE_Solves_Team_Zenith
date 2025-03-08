package com.example.shakti;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class NearbyPlaces extends FragmentActivity {

    //  Declaration of all the Required Variables
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private MapView mapView;
    private RecyclerView recyclerView;
    private PlacesAdapter placesAdapter;
    private double userLat, userLon;
    private RequestQueue requestQueue;
    private List<Place> placesList;
    private FusedLocationProviderClient fusedLocationClient;
    ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_places);

        // Initialize Map
        Configuration.getInstance().setUserAgentValue(getApplicationContext().getPackageName());
        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        backButton = findViewById(R.id.back_button);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        placesList = new ArrayList<>();
        placesAdapter = new PlacesAdapter(placesList, this::openGoogleMaps);
        recyclerView.setAdapter(placesAdapter);

        backButton.setOnClickListener(view -> onBackPressed());

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this);

        // Request Location Permission
        requestLocationPermission();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            userLat = location.getLatitude();
                            userLon = location.getLongitude();
                            showUserLocation(userLat, userLon);
                            findNearbyPlaces(userLat, userLon);
                        } else {
                            Toast.makeText(this, "Unable to get location. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Location Error", "Failed to get location: " + e.getMessage()));
        } else {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void showUserLocation(double latitude, double longitude) {
        GeoPoint userLocation = new GeoPoint(latitude, longitude);
        mapView.getController().setZoom(14.0);
        mapView.getController().setCenter(userLocation);

        Marker userMarker = new Marker(mapView);
        userMarker.setPosition(userLocation);
        userMarker.setTitle("You are here");
        mapView.getOverlays().add(userMarker);
    }

    private void findNearbyPlaces(double latitude, double longitude) {
        String placesUrl = "https://overpass-api.de/api/interpreter?data=[out:json];" +
                "node(around:5000," + latitude + "," + longitude + ")[amenity=hospital];" +
                "node(around:5000," + latitude + "," + longitude + ")[amenity=police];out;";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, placesUrl, null,
                response -> {
                    try {
                        placesList.clear();
                        JSONArray elementsArray = response.getJSONArray("elements");

                        for (int i = 0; i < elementsArray.length(); i++) {
                            JSONObject obj = elementsArray.getJSONObject(i);
                            double lat = obj.getDouble("lat");
                            double lon = obj.getDouble("lon");
                            String name = obj.has("tags") ? obj.getJSONObject("tags").optString("name", "Unknown Place") : "Unknown Place";

                            placesList.add(new Place(name, lat, lon));
                        }

                        placesAdapter.notifyDataSetChanged();
                        Log.d("API Success", "Places loaded successfully.");
                    } catch (JSONException e) {
                        Log.e("API Error", "JSON Parsing error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("API Error", "Volley request failed: " + error.toString());
                    Toast.makeText(this, "Failed to fetch places. Please try again.", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }

    private void openGoogleMaps(double lat, double lon) {
        String uri = "https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lon + "&travelmode=driving";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");

        // Check if Google Maps is installed
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Open in a web browser as a fallback
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(webIntent);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}