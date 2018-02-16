package com.example.perseus14.vehicletracking;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class DriverProfile extends AppCompatActivity implements ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener {

    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;
    ModelBusData modelBusData;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 10000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    // UI elements
    private TextView lblLocation;
    private Button  btnStartLocationUpdates;

    double latitude,longitude;
    JSONParser jsonParser = new JSONParser();

    UserLocalStore userLocalStore; int i = 0;

    TextView bus_ser_num,bus_reg_num,bus_occup_level;

    Button btnLow,btnMedium,btnHigh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        userLocalStore = new UserLocalStore(this);
        modelBusData = userLocalStore.getLoggedInBus();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopLocationUpdates();
                new BusLogout().execute(Config.URL_BUS_LOGOUT);
                userLocalStore.clearBusData();
                Intent intent = new Intent(DriverProfile.this,MainActivity.class);
                startActivity(intent);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bus_occup_level = (TextView) findViewById(R.id.busOccupLevel);
        bus_reg_num = (TextView) findViewById(R.id.busRegNum);
        bus_ser_num = (TextView) findViewById(R.id.busServiceNum);

        lblLocation = (TextView) findViewById(R.id.lblLocation);
        btnStartLocationUpdates = (Button) findViewById(R.id.btnLocationUpdates);
        btnLow = (Button) findViewById(R.id.low);
        btnHigh = (Button) findViewById(R.id.high);
        btnMedium = (Button) findViewById(R.id.medium);

        btnMedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UpdateOccupancyMedium().execute(Config.URL_UPDATE_OCCUPANCY_MEDIUM);
            }
        });
        btnHigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UpdateOccupancyHigh().execute(Config.URL_UPDATE_OCCUPANCY_HIGH);
            }
        });
        btnLow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UpdateOccupancyLow().execute(Config.URL_UPDATE_OCCUPANCY_LOW);
            }
        });

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }

        // Toggling the periodic location updates
        btnStartLocationUpdates.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                togglePeriodicLocationUpdates();
            }
        });

        if(modelBusData.bus_occupancy_level == 0){
            bus_occup_level.setText("Bus Occupancy: low");
        }
        else if(modelBusData.bus_occupancy_level == 1){
            bus_occup_level.setText("Bus Occupancy: Medium");
        }
        else{
            bus_occup_level.setText("Bus Occupancy: High");
        }
        bus_reg_num.setText("Bus Registraion Num: " + modelBusData.bus_reg_num);
        bus_ser_num.setText("Bus Service Num: " + modelBusData.bus_service_num);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();

            lblLocation.setText(latitude + ", " + longitude);

        } else {

            lblLocation
                    .setText("(Couldn't get the location. Make sure location is enabled on the device)");
        }
    }

    /**
     * Method to toggle periodic location updates
     * */
    private void togglePeriodicLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            // Changing the button text
            btnStartLocationUpdates
                    .setText("Stop Location Updates");

            mRequestingLocationUpdates = true;

            // Starting the location updates
            startLocationUpdates();

            Log.d(TAG, "Periodic location updates started!");

        } else {
            // Changing the button text
            btnStartLocationUpdates
                    .setText("Start Location Updates");

            mRequestingLocationUpdates = false;

            // Stopping the location updates
            stopLocationUpdates();

            Log.d(TAG, "Periodic location updates stopped!");
        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            /*if (googleApiAvailability.isUserRecoverableError(resultCode)) {
                googleApiAvailability.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }*/
            return false;
        }
        return true;
    }

    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        Toast.makeText(getApplicationContext(), "Location changed! " + String.valueOf(latitude ) + "  " + String.valueOf(longitude),
                Toast.LENGTH_SHORT).show();
        new UpdateCoordinatesOfBus().execute(Config.URL_UPDATE_BUS_COORDINATES);
        // Displaying the new location on UI
        displayLocation();
    }

    class UpdateCoordinatesOfBus extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... params) {
            HashMap<String,String> data = new HashMap<>();
            data.put("bus_lat",String.valueOf(latitude));
            data.put("bus_long",String.valueOf(longitude));
            data.put("id",String.valueOf(modelBusData.id));
            try{
                JSONObject jsonObject = jsonParser.makeHttpRequest(Config.URL_UPDATE_BUS_COORDINATES,"GET",data);
                if(jsonObject.getInt("success") == 1){
                    System.out.println("Successfully updated " + i);
                    i++;
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
            return null;
        }


    }

    class UpdateOccupancyLow extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... params) {
            HashMap<String,String> data = new HashMap<>();
            data.put("id",String.valueOf(modelBusData.id));
            try{
                jsonParser.makeHttpRequest(Config.URL_UPDATE_OCCUPANCY_LOW,"POST",data);
            } catch (JSONException e){
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            bus_occup_level.setText("Bus Occupancy: low");
        }
    }

    class UpdateOccupancyMedium extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... params) {
            HashMap<String,String> data = new HashMap<>();
            data.put("id",String.valueOf(modelBusData.id));
            try{
                jsonParser.makeHttpRequest(Config.URL_UPDATE_OCCUPANCY_MEDIUM,"POST",data);
            } catch (JSONException e){
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            bus_occup_level.setText("Bus Occupancy: Medium");
        }
    }

    class UpdateOccupancyHigh extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... params) {
            HashMap<String,String> data = new HashMap<>();
            data.put("id",String.valueOf(modelBusData.id));
            try{
                jsonParser.makeHttpRequest(Config.URL_UPDATE_OCCUPANCY_HIGH,"POST",data);
            } catch (JSONException e){
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            bus_occup_level.setText("Bus Occupancy: High");
        }
    }

    class BusLogout extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... params) {
            HashMap<String,String> data = new HashMap<>();
            data.put("id",String.valueOf(modelBusData.id));
            try{
                jsonParser.makeHttpRequest(Config.URL_BUS_LOGOUT,"POST",data);
            } catch (JSONException e){
                e.printStackTrace();
            }
            return null;
        }
    }

}
