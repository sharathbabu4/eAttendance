package mymsproject.oracle.android.com.myattendance.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.loopj.android.http.*;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import mymsproject.oracle.android.com.myattendance.R;

public class LoginActivity extends Activity {

    public static final String REQUEST_METHOD = "GET";
    public static final int READ_TIMEOUT = 15000;
    public static final int CONNECTION_TIMEOUT = 15000;

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String Email = "email_id";
    public static final String Password = "password";
    public static final String LoggedStatus = "isLoggedIn";
    SharedPreferences sharedpreferences;

    // Progress Dialog Object
    ProgressDialog prgDialog;
    // Error Msg TextView Object
    TextView errorMsg;
    // Email Edit View Object
    EditText emailET;
    // Password Edit View Object
    EditText pwdET;
    String email = null ;
    String password = null;

    @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_login_screen);

        // Find Error Msg Text View control by ID
        errorMsg = (TextView)findViewById(R.id.login_error);

        // Find Email Edit View control by ID
        emailET = (EditText)findViewById(R.id.loginEmail);

        // Find Password Edit View control by ID
        pwdET = (EditText)findViewById(R.id.loginPassword);

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);

        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");

        // Set Cancelable as False
        prgDialog.setCancelable(false);

      sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

      if(sharedpreferences.getBoolean(LoggedStatus, false)){
        navigatetoHomeActivity();
      }
    }

    /**
    Method gets triggered when Login button is clicked
    *
    * @param view
    */
    public void loginUser(View view){
        // Get Email Edit View Value
        email =  emailET.getText().toString();

        // Get Password Edit View Value
        password = pwdET.getText().toString();

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(Email, email);
        editor.putString(Password, password);
        editor.commit();

      // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();

        // When Email Edit View and Password Edit View have values other than Null
        if(Utility.isNotNull(email) && Utility.isNotNull(password)){
            // When Email entered is Valid
            if(Utility.validate(email)){
                // Put Http parameter username with value of Email Edit View control
                params.put("username", email);

                // Put Http parameter password with value of Password Edit Value control
                params.put("password", password);

                // Invoke RESTful Web Service with Http parameters
                invokeWS(params);
            }
            // When Email is invalid
            else{
                Toast.makeText(getApplicationContext(), "Please enter valid email", Toast.LENGTH_LONG).show();
            }
        } else{
            Toast.makeText(getApplicationContext(), "Please fill the form, don't leave any field blank", Toast.LENGTH_LONG).show();
        }
    }

    /*public void loginClick(View v) {
    Intent intent = new Intent(this, HomeScreen.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      startActivity(intent);
    }*/

    public void invokeWS(RequestParams params) {
        // Show Progress Dialog
        prgDialog.show();

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();

        String url = "http://13.232.137.59:2000/students/email_id/" + email;
        Log.d("ApiUrl = ", "url" + url);
        client.get(url, new AsyncHttpResponseHandler() {

            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // Hide Progress Dialog
                prgDialog.hide();
                try {
                    // JSON Object
                    //JSONObject obj = new JSONObject(new String(responseBody));
                    JSONArray jsonarray = new JSONArray(new String(responseBody));
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        //store your variable
                        String emailIdInResponse = obj.getString("email_id");
                        String passwordInResponse = obj.getString("password");

                        // When the JSON response has status boolean value assigned with true
                        if (statusCode == 200 && emailIdInResponse.equals(email) && passwordInResponse.equals(password)) {
                            Toast.makeText(getApplicationContext(), "You are successfully logged in!", Toast.LENGTH_LONG).show();
                            // Navigate to Home screen
                          SharedPreferences.Editor editor = sharedpreferences.edit();
                          editor.putBoolean(LoggedStatus, true);
                          editor.commit();
                            navigatetoHomeActivity();
                        } else if (!passwordInResponse.equals(password)) {
                            Toast.makeText(getApplicationContext(), "Password is incorrect.", Toast.LENGTH_LONG).show();
                        }

                        // Else display error message
                        else {
                            errorMsg.setText(obj.getString("error_msg"));
                            Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                try {
                    // Hide Progress Dialog
                    prgDialog.hide();

                    // When Http response code is '404'
                    if (statusCode == 404) {
                        Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                    }

                    // When Http response code is '500'
                    else if (statusCode == 500) {
                        Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                    } else if (statusCode == 400) {
                        JSONObject jsonObject = new JSONObject(new String(responseBody));
                        jsonObject.getString("message");
                        Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                    }

                    // When Http response code other than 404, 500
                    else {
                        Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Method which navigates from Login Activity to Home Activity
     */
    public void navigatetoHomeActivity(){
        Intent homeIntent = new Intent(getApplicationContext(),HomeScreen.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
    }

  public void registerClick(View v) {
    Intent intent = new Intent(this, RegisterActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
  }
}