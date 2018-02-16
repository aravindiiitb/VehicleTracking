package com.example.perseus14.vehicletracking;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class FbActivity extends AppCompatActivity {

    String fb_name,fb_email;
    ProgressDialog pDialog;
    UserLocalStore userLocalStore;
    private static String TAG_SUCCESS = "success";
    JSONParser jsonParser= new JSONParser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fb);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        fb_name = intent.getStringExtra("fb_name");
        fb_email = intent.getStringExtra("fb_email");
        userLocalStore = new UserLocalStore(this);
        new FbLogin().execute(Config.URL_FB_LOGIN);
    }

    class FbLogin extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(FbActivity.this);
            pDialog.setMessage("Verifying ..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {


            HashMap<String, String> data = new HashMap<>();
            data.put("value_username", fb_name);
            data.put("value_email", fb_email);

            try {
                JSONObject jsonObject = jsonParser.makeHttpRequest(Config.URL_FB_LOGIN, "GET", data);
                try {
                    Log.v("tag", jsonObject.toString());
                    int success = jsonObject.getInt("success");
                    if (success == 1) {
                        JSONArray userObj = jsonObject.getJSONArray("user"); // JSON Array
                        JSONObject user = userObj.getJSONObject(0);
                        if (user != null) {
                            String rcvd_email = user.getString("email");
                            String rcvd_password = user.getString("password");
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
                        }

                    } else {
                        Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                        startActivity(i);
                        Log.v("Tag", "Signed up but not logged in");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
                catch (Exception e) {
                    e.printStackTrace();
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

