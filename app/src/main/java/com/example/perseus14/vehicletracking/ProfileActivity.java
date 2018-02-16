package com.example.perseus14.vehicletracking;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private GoogleMap mMap;
    EditText busNum;
    Button search;
    TextView name;
    TextView email;
    Fragment gmap;
    UserLocalStore userLocalStore;
    String bus_num;
    Handler mHandler;
    int i = 0;
    JSONParser jsonParser = new JSONParser();
    ArrayList<HashMap<String, String>> requestList = new ArrayList<>();

    JSONArray requests = new JSONArray();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ProfileActivity.this);
                dialogBuilder.setMessage("You will now receive a SMS notification if the bus comes closer");
                dialogBuilder.setPositiveButton("Ok", null);
                dialogBuilder.setTitle("SMS Notification");
                dialogBuilder.show();
            }
        });
        userLocalStore = new UserLocalStore(this);


        busNum = (EditText) findViewById(R.id.editText);
        search = (Button) findViewById(R.id.button);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        name = (TextView) header.findViewById(R.id.name);
        email = (TextView) header.findViewById(R.id.email_id);
        mHandler  = new Handler();

        startRepeatingTask();

        name.setText(userLocalStore.getLoggedInUser().name);
        email.setText(userLocalStore.getLoggedInUser().email);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ShowBuses().execute(Config.URL_SEARCH_BUS);
            }
        });
    }

    Runnable mHandlerTask = new Runnable() {
        @Override
        public void run() {
            new ShowBuses().execute(Config.URL_SEARCH_BUS);
            mHandler.postDelayed(mHandlerTask,5000);
        }
    };


    void startRepeatingTask()
    {
        mHandlerTask.run();
    }

    void stopRepeatingTask()
    {
        mHandler.removeCallbacks(mHandlerTask);
    }

    class ShowBuses extends AsyncTask<String,String,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bus_num = busNum.getText().toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            plotPointsOnMap();
        }

        @Override
        protected String doInBackground(String... params) {
            requestList.clear();
            HashMap<String,String> data = new HashMap<>();
            data.put("bus_num",bus_num);

            try{
                JSONObject json = jsonParser.makeHttpRequest(Config.URL_SEARCH_BUS,"GET",data);
                if(json.getInt("success") == 1){
                    if(json.length() != 0) {
                        requests = json.getJSONArray("bus_gps");
                        for (int i = 0; i < requests.length(); i++) {
                            JSONObject c = requests.getJSONObject(i);

                            // Storing each json item in variable
                            double bus_lat = c.getDouble("bus_lat");
                            double bus_long = c.getDouble("bus_long");
                            String bus_service_num = c.getString("bus_service_num");
                            String bus_reg_num = c.getString("bus_reg_num");
                            int bus_occupancy_level = c.getInt("bus_occupancy_level");

                            // creating new HashMap
                            HashMap<String, String> map = new HashMap<>();

                            // adding each child node to HashMap key => value
                            map.put("bus_lat",String.valueOf(bus_lat));
                            map.put("bus_long",String.valueOf(bus_long));
                            map.put("bus_service_num",bus_service_num);
                            map.put("bus_reg_num",bus_reg_num);
                            map.put("bus_occupancy_level",String.valueOf(bus_occupancy_level));

                            // adding HashList to ArrayList
                            requestList.add(map);
                        }

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {
            Intent intent = new Intent(this,AccountSettings.class);
            startActivity(intent);
        } else if (id == R.id.nav_send) {
            stopRepeatingTask();
            userLocalStore.clearUserData();
            userLocalStore.setUserLoggedIn(false);
            startActivity(new Intent(this,LoginActivity.class));
        }
        if(fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().add(R.id.frame_container, fragment).commit();
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        String locationProvider;
        Location location = null;
        // Add a marker in Sydney and move the camera
        try{
            mMap.setMyLocationEnabled(true);
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            locationProvider = LocationManager.NETWORK_PROVIDER;
            location = locationManager.getLastKnownLocation(locationProvider);
        }catch (Exception e){
            e.printStackTrace();
        }
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            //MarkerOptions marker = new MarkerOptions();

            /*marker.position(latLng);
            marker.draggable(true);
            marker.title("Latitude: " + String.valueOf(location.getLatitude()) + " longitude: " + String.valueOf(location.getLongitude()));
            mMap.addMarker(marker);*/
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)      // Sets the center of the map to location user
                    .zoom(16)                   // Sets the zoom
                    .bearing(0)                // Sets the orientation of the camera to east
                    .tilt(0)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            /*mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    LatLng markerLocation = marker.getPosition();
                    Toast.makeText(ProfileActivity.this, markerLocation.toString(), Toast.LENGTH_LONG).show();
                    Log.d("Marker", "finished");
                }
            });*/


        }
    }

    void plotPointsOnMap(){
        mMap.clear();
        for(int i=0;i<requestList.size();i++){
            LatLng latLng1 = new LatLng(Double.parseDouble(requestList.get(i).get("bus_lat")),Double.parseDouble(requestList.get(i).get("bus_long")));
            MarkerOptions marker = new MarkerOptions();
            marker.position(latLng1);

            int occupancyLevel = Integer.parseInt(requestList.get(i).get("bus_occupancy_level"));
            System.out.println(occupancyLevel);
            if(occupancyLevel == 0){
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                //marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.busstop));
                marker.title("Regis Num: " + requestList.get(i).get("bus_reg_num") + "Occupancy: Less");
            }
            else if(occupancyLevel == 1){
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                marker.title("Regis Num: " + requestList.get(i).get("bus_reg_num") + "Occupancy: Medium");
            }
            else {
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                marker.title("Regis Num: " + requestList.get(i).get("bus_reg_num") + "Occupancy: High");
            }
            mMap.addMarker(marker);

        }
    }
}
