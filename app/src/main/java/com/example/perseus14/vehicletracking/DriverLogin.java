package com.example.perseus14.vehicletracking;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class DriverLogin extends AppCompatActivity {

    EditText bus_reg_num;
    EditText password;
    Button btnLogin;
    String value_busRegNum;
    String value_password;

    JSONParser jsonParser = new JSONParser();
    ProgressDialog pDialog;

    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        bus_reg_num = (EditText) findViewById(R.id.busNum);
        password = (EditText) findViewById(R.id.password);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        userLocalStore = new UserLocalStore(this);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CheckDriverLogin().execute(Config.URL_CHECK_BUS);
            }
        });
    }

    class CheckDriverLogin extends AsyncTask<String,String,String> {
        Boolean val = false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(DriverLogin.this);
            pDialog.setMessage("Verifying your details ..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

            value_busRegNum = bus_reg_num.getText().toString();
            value_password = password.getText().toString();

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
            if(val){
                Toast.makeText(getApplicationContext(),"Your password/Bus num is wrong",Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            HashMap<String,String> data = new HashMap<>();
            data.put("bus_reg_num",value_busRegNum);
            data.put("password",value_password);

            try{
                JSONObject jsonObject = jsonParser.makeHttpRequest(Config.URL_CHECK_BUS,"GET",data);
                if (jsonObject.getInt("success") == 1){
                    ModelBusData modelBusData = new ModelBusData();
                    JSONArray jsonArray = jsonObject.getJSONArray("bus_data");
                    JSONObject bus_info = jsonArray.getJSONObject(0);
                    modelBusData.id = bus_info.getInt("id");
                    modelBusData.bus_lat = bus_info.getDouble("bus_lat");
                    modelBusData.bus_long = bus_info.getDouble("bus_long");
                    modelBusData.bus_occupancy_level = bus_info.getInt("bus_occupancy_level");
                    modelBusData.bus_service_num = bus_info.getString("bus_service_num");
                    modelBusData.password = bus_info.getString("password");
                    modelBusData.bus_reg_num = bus_info.getString("bus_reg_num");

                    userLocalStore.storeBusData(modelBusData);
                    userLocalStore.setBusLoggedIn(true);

                    Intent intent = new Intent(DriverLogin.this,DriverProfile.class);
                    startActivity(intent);
                }
                else{
                    val = true;
                }
            } catch (JSONException e){
                e.printStackTrace();
            }

            return null;
        }
    }
}
