package mymsproject.oracle.android.com.myattendance.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mymsproject.oracle.android.com.myattendance.Adapter.ListAdapter;
import mymsproject.oracle.android.com.myattendance.R;

public class HomeScreen extends AppCompatActivity {

  private DrawerLayout mDrawerLayout;

  private IntentIntegrator qrScan;

  private List<String> mainList = new ArrayList();

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

    qrScan = new IntentIntegrator(this);

    ListView listView = findViewById(R.id.listview);
    ListAdapter gridAdapter = new ListAdapter(this, mainList);
    listView.setAdapter(gridAdapter);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
          qrScan.initiateScan();
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
            Intent intent = new Intent(HomeScreen.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            break;
          case R.id.nav_myprofile:
            startActivity(new Intent(HomeScreen.this, ProfileActivity.class));
            break;
          case R.id.nav_version:
            AlertDialog.Builder builder =  new AlertDialog.Builder(HomeScreen.this, android.R.style.Theme_Material_Dialog_Alert);
            builder.setTitle("Presenta Version")
              .setMessage("Version : 1.0.0")
              .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                  // continue with delete
                }
              }).show();
            break;
          case R.id.nav_changepassword:
            startActivity(new Intent(HomeScreen.this, ChangePasswordActivity.class));
            break;
        }
        menuItem.setChecked(true);
        mDrawerLayout.closeDrawers();
        return true;
      }
    });
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
    IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
    if (result != null) {
      if (result.getContents() == null) {
        Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
      } else {
        try {
          JSONObject obj = new JSONObject(result.getContents());
          Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
          e.printStackTrace();
          Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
        }
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }
}
