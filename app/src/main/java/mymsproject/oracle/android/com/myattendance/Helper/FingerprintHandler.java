package mymsproject.oracle.android.com.myattendance.Helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;

import mymsproject.oracle.android.com.myattendance.Activity.FingerprintActivity;
import mymsproject.oracle.android.com.myattendance.Activity.HomeScreen;
import mymsproject.oracle.android.com.myattendance.R;

/**
 * Created by parasjos on 05-03-2018.
 */

@RequiresApi (api = Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private Context context;
    CancellationSignal cancellationSignal;
    private IntentIntegrator qrScan;

    private int failedBiometricCount=0;

    void logFailedBiometricScan(){
        failedBiometricCount++;
    }
    int getFailedBiometricScan(){
        return failedBiometricCount;
    }

    // Constructor
    public FingerprintHandler(Context mContext) {
        context = mContext;
        qrScan = new IntentIntegrator((Activity)context);
    }


    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
        cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }


    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        this.update("ERROR", context.getString(R.string.fingerprint_auth_error) + "\n" + errString, false);
        cancelAuth();
    }


    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        this.update("HELP",context.getString(R.string.fingerprint_auth_help)+"\n" + helpString, false);
    }


    @Override
    public void onAuthenticationFailed() {
        logFailedBiometricScan();
        this.update("FAILED", context.getString(R.string.fingerprint_auth_failed), false);
    }


    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        this.update("SUCCESS",context.getString(R.string.fingerprint_auth_success), true);
    }


    public void update(String type, String e, Boolean success){
        System.out.println("parasjos: In update, "+type+" & "+success);
        final TextView textView = (TextView) ((Activity)context).findViewById(R.id.errorText);
        final TextView infoView = (TextView) ((Activity)context).findViewById(R.id.desc);
        final ImageView iconView = (ImageView) ((Activity)context).findViewById(R.id.icon);
        final TextView bactToLogin = (TextView) ((Activity)context).findViewById(R.id.backToLogin);

        textView.setText(e);
        System.out.println("Error ; "+e);
        if(success){
            textView.setTextColor(ContextCompat.getColor(context, R.color.zxing_transparent));
            bactToLogin.setVisibility(View.GONE);
            qrScan.initiateScan();

            // startActivityClearStack(HomeScreen.class,false);
        }else{
            final String message = new String();
            boolean redirect = false;

            if(type.equalsIgnoreCase("ERROR")){
                failedBiometricCount++;
            }
            if(failedBiometricCount>1){
                textView.setText(context.getString(R.string.bio_scan_limit_exceeded)+context.getString(R.string.redirect_login));
                redirect=true;
            }
            if(redirect) {
                cancelAuth();
                infoView.setEnabled(false);
                iconView.setEnabled(false);
                startActivityClearStack(HomeScreen.class,true);
            }
        }
        System.out.println("Error set ; "+textView.getText());

    }

    private void startActivityClearStack(final Class activity, final boolean authStatus){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                Intent intent = new Intent(context, activity);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("exceededBiometricScans", authStatus);
                context.startActivity(intent);
                ((Activity) context).finish();
            }
        }, 3000);
    }

    //Cancel authentication

    public void cancelAuth(){
        if(cancellationSignal!=null) {
            cancellationSignal.cancel();
        }
    }
}
