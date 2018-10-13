package mymsproject.oracle.android.com.myattendance.Activity;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;
import mymsproject.oracle.android.com.myattendance.Helper.FingerprintHandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import mymsproject.oracle.android.com.myattendance.R;

import static mymsproject.oracle.android.com.myattendance.Activity.LoginActivity.Email;
import static mymsproject.oracle.android.com.myattendance.Activity.LoginActivity.MyPREFERENCES;
import static mymsproject.oracle.android.com.myattendance.Activity.LoginActivity.Password;

public class FingerprintActivity extends AppCompatActivity {
    private KeyStore keyStore;
    // Variable used for storing the key in the Android Keystore container
    private static final String KEY_NAME = "androidHive";
    private Cipher cipher;
    private TextView textView;
    Context context;
    FingerprintHandler helper;
    double longitude ;
    double latitude ;
    GPSTracker gps;
    ProgressDialog prgDialog;

    @RequiresApi (api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);
        context = getApplicationContext();

        gps = new GPSTracker(FingerprintActivity.this);
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

        // Initializing Android Keyguard Manager to verify the device lock screen details
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        // Initializing Android Keyguard Manager and Fingerprint Manager
        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
        textView = (TextView) findViewById(R.id.errorText);

        // Does the device have at least one Fingerprint sensor.
        if(!fingerprintManager.isHardwareDetected()){
            textView.setText(getString(R.string.fingerprint_scanner_not_found));
        }else {
            // Checks whether fingerprint permission exists
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                textView.setText(getString(R.string.fingerprint_permission_not_enabled));
            }else{
                // Check whether at least one fingerprint is registered
                if (!fingerprintManager.hasEnrolledFingerprints()) {
                    textView.setText(getString(R.string.atleast_one_fingerprint_needed));
                }else{
                    // Checks whether lock screen security is enabled or not
                    if (!keyguardManager.isKeyguardSecure()) {
                        textView.setText(getString(R.string.lock_screen_not_enabled));
                    }else{
                        //generate the key to use for authentication
                        generateKey();
                        if (cipherInit()) {
                            FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                            helper = new FingerprintHandler(this);
                            helper.startAuth(fingerprintManager, cryptoObject);
                        }
                    }
                }
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }

        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get KeyGenerator instance", e);
        }


        try {
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }


        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    @RequiresApi (api = Build.VERSION_CODES.M)
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        helper.cancelAuth();
    }

    @RequiresApi (api = Build.VERSION_CODES.M)
    @Override
    protected void onStop() {
        super.onStop();
        helper.cancelAuth();
    }

    @RequiresApi (api = Build.VERSION_CODES.M)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        helper.cancelAuth();
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
                        jsonParams.put("location_lat", 22.4);
                        jsonParams.put("location_long", 22.4);
                        jsonParams.put("biometric", "{}");
                        Log.i("Supreeth", "JSON Array : " + jsonParams);

                        // Make RESTful webservice call using AsyncHttpClient object
                        AsyncHttpClient client = new AsyncHttpClient();
                        //String userName = "cheth@gmail.com";
                        //String passWord = "nice";
                        SharedPreferences pref = getSharedPreferences(MyPREFERENCES,MODE_PRIVATE);
                        String userName = pref.getString(Email, null);
                        Log.i("Supreeth", "username : " + userName);
                        String passWord = pref.getString(Password, null);
                        Log.i("Supreeth", "Password : " + passWord);

                        if (userName != null && passWord != null) {
                            byte[] base64bytes = Base64.encode((userName + ":" + passWord).getBytes(), Base64.DEFAULT);
                            String credentials = new String(base64bytes);
                            //headers.add(new BasicHeader("Authorization", "basic" + " " + credentials));
                            client.addHeader("Authorization", "Basic" + " " + credentials);
                            Log.i("Supreeth", "Crdentials : " + credentials);
                        }
                        //  client.setBasicAuth(userName,passWord);
                        //client.addHeader("Content-type", "application/json");
                        //client.addHeader("Accept", "text/plain");
                        //client.addHeader("Cache-control", "no-cache");

                        StringEntity entity = new StringEntity(jsonParams.toString());
                        Log.i("Supreeth", "Entity:" + entity);

                        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                        Log.i("Supreeth", "New Entity:" + entity);
                        Log.i("Supreeth", "New Entity Content:" + entity.getContent().toString());

                        client.post(this, url, entity, "application/json", new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                try {
                                    // JSON Object
                                    JSONObject obj = new JSONObject(new String(responseBody));

                                    // When the JSON response has status boolean value assigned with true
                                    if (statusCode == 200) {
                                        // Display successfully registered message using Toast
                                        Toast.makeText(getApplicationContext(), "Successfully marked the attendance", Toast.LENGTH_LONG).show();
                                    }

                                    // Else display error message
                                    else {
                                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                try {
                                    // When Http response code is '404'
                                    if (statusCode == 404) {
                                        Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                                    }

                                    // When Http response code is '500'
                                    else if (statusCode == 500) {
                                        Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                                    }
                                    else if (statusCode == 400) {
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
                    } else {
                        Toast.makeText(this, "The QR code is empty. Please don't scan after college hours. Please contact Admin.", Toast.LENGTH_LONG).show();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        finish();
        }

    /**
     *
     * @return yyyy-MM-dd HH:mm:ss formate date as string
     */
    public static String getCurrentTimeStamp(){
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            dateFormat.setTimeZone(TimeZone.getTimeZone("gmt"));
            String currentDateTime = dateFormat.format(new Date());// Find todays date
            Log.i("Supreeth", "Time now :"+ currentDateTime);
            //currentDateTime.replace(' ', 'T');
            Log.i("Supreeth", "Time now after replacing:"+ currentDateTime);
            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
