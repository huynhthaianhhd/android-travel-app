package com.ygaps.travelapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ygaps.travelapp.Modal.UserCoordinate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FollowActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private GoogleMap map;
    private Marker stopPointTemp;
    private Marker currentUserLocationMarker;
    private FusedLocationProviderClient client;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;
    String latCurr="",longCurr="";
    private Marker markerSelect;
    public ArrayList<UserCoordinate> listUserGet= new ArrayList <UserCoordinate>();
    public static final int Request_User_Location_Code=99;
    private Geocoder geocoder;
    public String message="";
    ImageView messButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);

        //Nút xem mess
        messButton=(ImageView)findViewById(R.id.follow_mess_icon);
        messButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FollowActivity.this, Conversation.class);
                intent.putExtra("tourId",TourDetail.idOfTour);
                intent.putExtra("userId",LoginPage.userId);
                startActivity(intent);
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkUserLocationPermission();
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.followmap);
        mapFragment.getMapAsync(FollowActivity.this);
    }
    public void sendLocation()
    {
        if(latCurr.isEmpty() || longCurr.isEmpty()){
            return;
        }
        final RequestQueue requestQueue5 = Volley.newRequestQueue(FollowActivity.this);
        String URL = "http://35.197.153.192:3000/tour/current-users-coordinate";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("userId", "1");
        params.put("tourId", TourDetail.idOfTour);
        params.put("lat",latCurr);
        params.put("long",longCurr);

        JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, URL, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(FollowActivity.this, "Send Thanh cong", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                message = error.getMessage();
                showLocationMember(message);
//                Toast.makeText(FollowActivity.this, listUserGet.size()+"--size", Toast.LENGTH_SHORT).show();
                for (int i=0;i<listUserGet.size();i++)
                {
                    MarkerOptions markerOptions = new MarkerOptions();
                    LatLng temp = new LatLng(Double.parseDouble(listUserGet.get(i).getLat()),Double.parseDouble(listUserGet.get(i).getLongitute()));
                    markerOptions.position(temp);
                    markerOptions.title(listUserGet.get(i).getId());
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    map.addMarker(markerOptions);
                }

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                //headers.put("Content-Type", "application/json");
                headers.put("Authorization", LoginPage.token);
                return headers;
            }
        };
        requestQueue5.add(request_json);



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.clear();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            buildGoogleApiClient();
            map.setMyLocationEnabled(true);
        }

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        sendLocation();
                    }
                },
                500);

        final Handler ha=new Handler();
        ha.postDelayed(new Runnable() {
            @Override
            public void run() {
                //call function
                map.clear();
                sendLocation();

//                for (int i=0;i<listUserGet.size();i++)
//                {
//                    MarkerOptions markerOptions = new MarkerOptions();
//                    LatLng temp = new LatLng(Double.parseDouble(listUserGet.get(i).getLat()),Double.parseDouble(listUserGet.get(i).getLongitute()));
//                    markerOptions.position(temp);
//                    markerOptions.title(listUserGet.get(i).getId());
//                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//                    map.addMarker(markerOptions);
//                }
                ha.postDelayed(this, 10000);
            }
        }, 10000);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1100);
        locationRequest.setFastestInterval(1100);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        if (currentUserLocationMarker!=null)
        {
            currentUserLocationMarker.remove();
        }
        latCurr=location.getLatitude()+"";
        longCurr=location.getLongitude()+"";
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latLng);
//        markerOptions.title("Current Location");
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//        currentUserLocationMarker = map.addMarker(markerOptions);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));

        if (googleApiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
        }
    }



    public boolean checkUserLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_User_Location_Code);
            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_User_Location_Code);

            }
            return  false;
        }
        else
        {
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case Request_User_Location_Code:
                if (grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
                {
                    if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
                    {
                        if (googleApiClient == null)
                        {
                            buildGoogleApiClient();
                        }
                        map.setBuildingsEnabled(true);
                    }
                    else
                    {
                        Toast.makeText(this, "Permission Denied !!", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
        }
    }

    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }
    public void showLocationMember(String message)
    {
        int first = message.indexOf("[");
        int last = message.indexOf("]");
        String getArray = message.substring(first,last+1);
        try {
            JSONArray jsonArray = new JSONArray(getArray);
            listUserGet.clear();
            for (int i=0;i<jsonArray.length();i++)
            {
                JSONObject temp = jsonArray.getJSONObject(i);
                String id =temp.getString("id");
                String lat =temp.getString("lat");
                String longitute =temp.getString("long");
                UserCoordinate user = new UserCoordinate(id,lat,longitute);
                listUserGet.add(user);
            }
            //
            //
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
