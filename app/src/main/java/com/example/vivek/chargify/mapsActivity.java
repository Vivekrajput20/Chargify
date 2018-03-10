package com.example.vivek.chargify;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class mapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    final double[] longitude = new double[1];
    final double[] latitude = new double[1];

    List xcor=new ArrayList();
    List ycor=new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
//        getRequest();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//
//        double longitude = location.getLongitude();
//        double latitude = location.getLatitude();

        if(location == null) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 10, mapsActivity.this);
//            longitude[0] = location.getLongitude();
//            latitude[0] = location.getLatitude();

        }
        else{
            longitude[0] = location.getLongitude();
            latitude[0] = location.getLatitude();
            getRequest();
        }

        LatLng my = new LatLng(latitude[0], longitude[0]);

        BitmapDescriptor myico = BitmapDescriptorFactory.fromResource(R.drawable.my);

        mMap.addMarker(new MarkerOptions().position(my).title("Current Location").snippet("Your current location on map").icon(myico));
        mMap.setMinZoomPreference(13.0f);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(my));
    }

    @Override
    public void onLocationChanged(Location location) {
        longitude[0] = location.getLongitude();
        latitude[0] = location.getLatitude();
        getRequest();
        LatLng my = new LatLng(latitude[0], longitude[0]);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.mark);
        BitmapDescriptor icon1 = BitmapDescriptorFactory.fromResource(R.drawable.mark1);
        BitmapDescriptor icon2 = BitmapDescriptorFactory.fromResource(R.drawable.mark2);
        BitmapDescriptor myico = BitmapDescriptorFactory.fromResource(R.drawable.my);

        mMap.addMarker(new MarkerOptions().position(my).title("Current Location").snippet("Your current location on map").icon(myico));
        mMap.setMinZoomPreference(0.0f);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(my));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
    @SuppressLint("StaticFieldLeak")
    public void getRequest(){
        Log.d("B;","23");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try{
                    String url = "http://chargify.pythonanywhere.com/place/?xcor="+String.valueOf(latitude[0])+"&ycor="+ String.valueOf(longitude[0])+"&r=5000";

                    URL obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                    con.setRequestMethod("GET");
//                    con.addRequestProperty("Content-Type","application/json");


                    int responseCode = con.getResponseCode();
                    Log.d("B;",String.valueOf(responseCode));
//                    System.out.println("\nSending 'GET' request to URL : " + url);
//                    System.out.println("Response Code : " + responseCode);

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    JSONArray jsonArray= new JSONArray(response.toString());
                    for(int i=0;i<jsonArray.length();i++){
                        xcor.add(jsonArray.getJSONObject(i).getDouble("x_cor"));
                        ycor.add(jsonArray.getJSONObject(i).getDouble("y_cor"));
                    }
                    Log.d("B;",response.toString());
                    in.close();
                }catch(Exception e){

                }
                return null;
            }
            @Override
            protected void onPostExecute(Void param){

                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.mark);
                BitmapDescriptor icon1 = BitmapDescriptorFactory.fromResource(R.drawable.mark1);
                BitmapDescriptor icon2 = BitmapDescriptorFactory.fromResource(R.drawable.mark2);
                BitmapDescriptor[] a={icon,icon1,icon2};
                for(int i=0;i<xcor.size();i++){
//                    int y = 0;
                    LatLng my = new LatLng((double) xcor.get(i), (double) ycor.get(i));
                    mMap.addMarker(new MarkerOptions().position(my).icon(a[i%3]));
                    mMap.setMinZoomPreference(13.0f);

                }
            }
        }.execute();
    }
}
