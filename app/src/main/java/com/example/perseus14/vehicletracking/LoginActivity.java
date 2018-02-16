package com.example.perseus14.vehicletracking;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    EditText email;
    EditText password;

    Button btnLogin;
    ProgressDialog pDialog;

    String value_email,value_password;
    UserLocalStore userLocalStore;
    JSONParser jsonParser = new JSONParser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        userLocalStore = new UserLocalStore(this);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Login().execute(Config.URL_LOGIN_USER);
            }
        });

    }

    class Login extends AsyncTask<String,String,String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Verifying your details ..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

            value_email = email.getText().toString();
            value_password = password.getText().toString();
        }

        @Override
        protected String doInBackground(String... params) {

            if(value_email.length() > 0 && value_password.length() > 0){
                HashMap<String,String> data = new HashMap<>();
                data.put("value_email", value_email);
                try {
                    JSONObject jsonObject = jsonParser.makeHttpRequest(Config.URL_LOGIN_USER, "GET", data);
                    int success = jsonObject.getInt("success");
                    if(success == 1){
                        JSONArray userObj = jsonObject.getJSONArray("user"); // JSON Array
                        JSONObject user = userObj.getJSONObject(0);
                        if(user != null) {
                            String rcvd_email = user.getString("email");
                            String rcvd_password = user.getString("password");

                            if (rcvd_password.equals(value_password) && rcvd_email.equals(value_email)) {
                                ModelUser returnedUser = new ModelUser();
                                returnedUser.id = user.getInt("id");
                                returnedUser.name = user.getString("name");
                                returnedUser.password = user.getString("password");
                                returnedUser.email = user.getString("email");
                                returnedUser.bus_radius = user.getInt("bus_radius");
                                returnedUser.mobile_num = user.getString("mobile_num");

                                userLocalStore.storeUserData(returnedUser);
                                userLocalStore.setUserLoggedIn(true);

                                Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                                startActivity(i);
                                // closing this screen
                                finish();

                            } else {
                                // failed to Authenticate
                               // noUser = true;
                            }
                        }

                    }
                    else {

                    }

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
        }
    }

}
