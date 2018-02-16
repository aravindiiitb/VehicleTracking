package com.example.perseus14.vehicletracking;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class AccountSettings extends AppCompatActivity {

    EditText edtFname,  edtEmail, edtPass, edtBusRadius, edtMobileNum;

    String first_name, email, password, bus_radius, mobile_num;
    Button btnUpdate;
    UserLocalStore userLocalStore;
    JSONParser jsonParser = new JSONParser();
    ProgressDialog pDialog;
    ModelUser modelUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtFname = (EditText) findViewById(R.id.username);
        edtMobileNum = (EditText) findViewById(R.id.mobileNum);
        edtEmail = (EditText) findViewById(R.id.email_id);
        edtPass = (EditText) findViewById(R.id.password);
        edtBusRadius = (EditText) findViewById(R.id.busRadius);

        userLocalStore = new UserLocalStore(this);
        modelUser = userLocalStore.getLoggedInUser();

        edtBusRadius.setText(String.valueOf(modelUser.bus_radius));
        edtPass.setText(modelUser.password);
        edtEmail.setText(modelUser.email);
        edtFname.setText(modelUser.name);
        edtMobileNum.setText(modelUser.mobile_num);

        btnUpdate = (Button) findViewById(R.id.update);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UpdateUserDetails().execute(Config.URL_UPDATE_USER_DETAILS);
            }
        });
    }

    class UpdateUserDetails extends AsyncTask<String,String,String>{
        Boolean flag = false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AccountSettings.this);
            pDialog.setMessage("Updating your details ..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

            first_name = edtFname.getText().toString();
            password = edtPass.getText().toString();
            email = edtEmail.getText().toString();
            bus_radius = edtBusRadius.getText().toString();
            mobile_num = edtMobileNum.getText().toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(flag){
                Toast.makeText(getApplicationContext(), "For security reasons you have been logged out please login again", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> data = new HashMap<>();
            data.put("name",first_name);
            data.put("email", email);
            data.put("password",password);
            data.put("bus_radius",bus_radius);
            data.put("mobile_num", mobile_num);
            data.put("user_id", String.valueOf(modelUser.id));

            try {
                JSONObject jsonObject = jsonParser.makeHttpRequest(Config.URL_UPDATE_USER_DETAILS,"POST",data);
                int success = jsonObject.getInt("success");
                if(success == 1){
                    flag = true;
                    Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(intent);
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
            return null;
        }
    }

}
