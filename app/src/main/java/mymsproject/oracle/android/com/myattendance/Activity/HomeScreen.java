package mymsproject.oracle.android.com.myattendance.Activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;
import mymsproject.oracle.android.com.myattendance.Adapter.ListAdapter;
import mymsproject.oracle.android.com.myattendance.R;

public class HomeScreen extends AppCompatActivity{

  private DrawerLayout mDrawerLayout;

  private IntentIntegrator qrScan;

  private List<String> mainList = new ArrayList();

  // Declaring a Location Manager
  private LocationManager locationManager;
  double longitude ;
  double latitude ;
  GPSTracker gps;
  ProgressDialog prgDialog;
  public static final String MyPREFERENCES = "MyPrefs" ;
  public static final String Email = "email_id";
  public static final String LoggedStatus = "isLoggedIn";
  SharedPreferences sharedpreferences;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home_screen);

    mDrawerLayout = findViewById(R.id.drawer_layout);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ActionBar actionbar = getSupportActionBar();
    actionbar.setDisplayHomeAsUpEnabled(true);
    actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
    mainList.clear();
    mainList.add(0, "QR-Code Scanner");
    mainList.add(1, "Attendance Report");

    sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

    qrScan = new IntentIntegrator(this);

    ListView listView = findViewById(R.id.listview);
    ListAdapter gridAdapter = new ListAdapter(this, mainList);
    listView.setAdapter(gridAdapter);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
         // qrScan.initiateScan();
          Intent intent = new Intent(HomeScreen.this, FingerprintActivity.class);
          startActivity(intent);
        } else {
          Intent intent = new Intent(HomeScreen.this, StatsActivity.class);
          intent.putExtra("position", position);
          intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
          startActivity(intent);
        }
      }
    });

    NavigationView navigationView = findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
      @Override
      public boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
          case R.id.nav_signout:
            if(sharedpreferences.getBoolean(LoggedStatus, false)){
              SharedPreferences.Editor editor = sharedpreferences.edit();
              editor.putBoolean(LoggedStatus, false);
              editor.commit();
            }
            Intent intent = new Intent(HomeScreen.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            break;
          case R.id.nav_myprofile:
            startActivity(new Intent(HomeScreen.this, ProfileActivity.class));
            break;
          case R.id.nav_version:
            AlertDialog.Builder builder =  new AlertDialog.Builder(HomeScreen.this, android.R.style.Theme_Material_Dialog_Alert);
            builder.setTitle("Prezenta Version")
              .setMessage("Version : 1.0.0")
              .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                  // continue with delete
                }
              }).show();
            break;
        }
        menuItem.setChecked(true);
        mDrawerLayout.closeDrawers();
        return true;
      }
    });

    gps = new GPSTracker(HomeScreen.this);
    // check if GPS enabled
    if(gps.canGetLocation()){
      latitude = gps.getLatitude();
      longitude = gps.getLongitude();
      // \n is for new line
      //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
    }else{
      // can't get location
      // GPS or Network is not enabled
      // Ask user to enable GPS/network in settings
      gps.showSettingsAlert();
    }

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        mDrawerLayout.openDrawer(GravityCompat.START);
        return true;
      case R.id.nav_signout:
        startActivity(new Intent(HomeScreen.this, LoginActivity.class));
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    try {
      IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
      if (result != null) {
        Log.i("Supreeth", "Result : " + result);
        if (result.getContents() == null) {
          Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
        } else {
          Log.i("Supreeth", "JSON RESULT CONTENTS : " + result.getContents());
          if (result.getContents().length() != 0) {
            String url = result.getContents();
            Log.d("The scanned URL = ", "url" + url);
            //prgDialog.show();
            //JSONObject obj = new JSONObject(result.getContents());
            //Log.i("Supreeth", "JSON RESULT CONTENTS : " + obj);
            Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            Log.i("Supreeth", "Location : " + latitude + "Long :" + longitude);

            JSONObject jsonParams = new JSONObject();
            jsonParams.put("timestamp", getCurrentTimeStamp());
            jsonParams.put("location_lat", latitude);
            jsonParams.put("location_long", longitude);
            jsonParams.put("biometric", "{}");
            Log.i("Supreeth", "JSON Array : " + jsonParams);

            // Make RESTful webservice call using AsyncHttpClient object
            AsyncHttpClient client = new AsyncHttpClient();
            String userName = "cheth@gmail.com";
            String passWord = "nice";
            if (userName != null && passWord != null) {
              byte[] base64bytes = Base64.encode((userName + ":" + passWord).getBytes(), Base64.DEFAULT);
              String credentials = new String(base64bytes);
              //headers.add(new BasicHeader("Authorization", "basic" + " " + credentials));
              client.addHeader("Authorization", "basic" + " " + credentials);
            }
            //  client.setBasicAuth(userName,passWord);
            client.addHeader("Content-type", "application/json");
            client.addHeader("Accept", "text/plain");
            client.addHeader("Cache-control", "no-cache");

            StringEntity entity = new StringEntity(jsonParams.toString());
            Log.i("Supreeth", "Entity:" + entity);
            client.post(this, url, entity, "application/json", new AsyncHttpResponseHandler() {
              @Override
              public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

              }

              @Override
              public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

              }
            });
          } else {
            Toast.makeText(this, "The QR code is empty. Please dont scan after college hours. Please contact Admin.", Toast.LENGTH_LONG).show();
          }
        }
      } else {
        super.onActivityResult(requestCode, resultCode, data);
      }
    } catch (UnsupportedEncodingException e)
    {
      e.printStackTrace();
    } catch (JSONException e)
    {
      e.printStackTrace();
      //Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  /**
   *
   * @return yyyy-MM-dd HH:mm:ss formate date as string
   */
  public static String  getCurrentTimeStamp(){
    try {

      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      String currentDateTimeinUTC = dateFormat.format(new Date()); // Find todays date
      return currentDateTimeinUTC;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /*void getLocation() {
    try {
      locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
    }
    catch(SecurityException e) {
      e.printStackTrace();
    }
  }*/


  /*public Location getLocation() {
    try {
      locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
      // getting GPS status
      isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

      // getting network status
      isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
      if (!isGPSEnabled && !isNetworkEnabled) {
        // no network provider is enabled
      } else {
        this.canGetLocation = true;
        if (isNetworkEnabled) {
          locationManager.requestLocationUpdates(
                  LocationManager.NETWORK_PROVIDER,
                  MIN_TIME_BW_UPDATES,
                  MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
          Log.d("Network", "Network");
          if (locationManager != null) {
            location = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
              latitude = location.getLatitude();
              longitude = location.getLongitude();
            }
          }
        }
        // if GPS Enabled get lat/long using GPS Services
        if (isGPSEnabled) {
          if (location == null) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            Log.d("GPS Enabled", "GPS Enabled");
            if (locationManager != null) {
              location = locationManager
                      .getLastKnownLocation(LocationManager.GPS_PROVIDER);
              if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
              }
            }
          }
        }
      }

    } catch(SecurityException e) {
      e.printStackTrace();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return location;
  }

  *//**
   * Stop using GPS listener
   * Calling this function will stop using GPS in your app
   * *//*
  public void stopUsingGPS(){
    if(locationManager != null){
      locationManager.removeUpdates(this);
    }
  }

  *//**
   * Function to get latitude
   * *//*
  public double getLatitude(){
    if(location != null){
      latitude = location.getLatitude();
    }

    // return latitude
    return latitude;
  }

  *//**
   * Function to get longitude
   * *//*
  public double getLongitude(){
    if(location != null){
      longitude = location.getLongitude();
    }

    // return longitude
    return longitude;
  }

  *//**
   * Function to check GPS/wifi enabled
   * @return boolean
   * *//*
  public boolean canGetLocation() {
    return this.canGetLocation;
  }*/


}
