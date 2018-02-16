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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    EditText username;
    EditText email;
    EditText password;
    EditText re_password;
    EditText mobile_num;

    Button btnRegister;
    ProgressDialog pDialog;

    String value_username;
    String value_email;
    String value_password;
    String value_re_password;
    String value_mobilenum;

    private static String TAG_SUCCESS = "success";
    JSONParser jsonParser= new JSONParser();
    Boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
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

        username = (EditText) findViewById(R.id.uname);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        re_password = (EditText) findViewById(R.id.re_password);
        mobile_num = (EditText) findViewById(R.id.mobileNum);

        btnRegister = (Button) findViewById(R.id.btnSignup);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SignUp().execute(Config.URL_CREATE_USER);
            }
        });

    }

    class SignUp extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SignUpActivity.this);
            pDialog.setMessage("Creating your Account ..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

            value_username = username.getText().toString();
            value_email = email.getText().toString();
            value_password = password.getText().toString();
            value_re_password = re_password.getText().toString();
            value_mobilenum = mobile_num.getText().toString();
        }

        @Override
        protected String doInBackground(String... params) {

            if(value_password.equals(value_re_password)) {
                HashMap<String, String> data = new HashMap<>();
                data.put("value_username", value_username);
                data.put("value_email", value_email);
                data.put("value_password", value_password);
                data.put("value_re_password", value_re_password);
                data.put("value_mobilenum",value_mobilenum);

                try {
                    JSONObject json = jsonParser.makeHttpRequest(Config.URL_CREATE_USER, "POST", data);

                    // check log cat fro response
                    Log.d("Create Response", json.toString());

                    int success = json.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        // successfully created product
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(i);

                        // closing this screen
                        finish();
                    } else {
                        // failed to create product
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SignUpActivity.this);
                        dialogBuilder.setMessage("Something went wrong :( ");
                        dialogBuilder.setPositiveButton("Ok", null);
                        dialogBuilder.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                flag = true;
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
            if (flag)
                Toast.makeText(SignUpActivity.this,"Password is not matching",Toast.LENGTH_LONG).show();
        }
    }
}
