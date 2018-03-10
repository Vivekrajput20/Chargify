package com.example.vivek.chargify;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements LocationListener {

    final float[] longitude = new float[1];
    final float[] latitude = new float[1];

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {

        @Override
        //When Event is published, onReceive method is called
        public void onReceive(Context context, Intent intent) {
            Log.d("A:","1");
            Intent iintent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int plugged = iintent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean a = plugged == BatteryManager.BATTERY_PLUGGED_AC ||
                    plugged == BatteryManager.BATTERY_PLUGGED_USB;
            TextView tv = (TextView)findViewById(R.id.tv);
            if (true) {
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                        (context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                //lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, MainActivity.this);

                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(location == null){
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, MainActivity.this);
                }
                else {
                    longitude[0] = (float) location.getLongitude();
                    latitude[0] = (float) location.getLatitude();
                    postRequest();

                }
                tv.setText("Location Updated!");
            }
            else {
                tv.setText("");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter ifilter = new IntentFilter();
        ifilter.addAction(Intent.ACTION_POWER_CONNECTED);
        ifilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(mBatInfoReceiver, ifilter);
//        postRequest();
    }
    public void openMaps(View view) {
        Intent intent = new Intent(this, mapsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onLocationChanged(Location location) {
        longitude[0] = (float) location.getLongitude();
        latitude[0] = (float) location.getLatitude();
//        longitude[0].setPrecision(6);
        //tv.setText("Location Updated!");
        postRequest();

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


    public void postRequest(){
        Log.d("A:","2");
        new AsyncTask<Void,Void,Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    String url = "http://chargify.pythonanywhere.com/place/";
                    URL obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                    con.setRequestMethod("POST");
                    String urlParameters = "x_cor=" + String.valueOf(latitude[0]) + "&y_cor=" + String.valueOf(longitude[0]);


                    // Send post request
                    con.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    wr.writeBytes(urlParameters);
                    wr.flush();
                    wr.close();

                    int responseCode = con.getResponseCode();
                    Log.d("A:", String.valueOf(responseCode));

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                }catch(Exception e){
                }
                Log.d("A:","3");
                return null;
            }
        }.execute();
    }
}
