package mymsproject.oracle.android.com.myattendance.Activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.GridView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import mymsproject.oracle.android.com.myattendance.Adapter.GridAdapter;
import mymsproject.oracle.android.com.myattendance.Adapter.ListAdapter;
import mymsproject.oracle.android.com.myattendance.R;

public class StatsActivity extends AppCompatActivity {

  public final static String Subject = "Subject";
  public final static String Total_Classes = "Total Classes";
  public final static String Class_Attended = "Classes Attended";

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.stats_layout_main);

    JSONArray jsonArray = new JSONArray();
    JSONObject jsonObject1 = new JSONObject();
    JSONObject jsonObject2 = new JSONObject();
    JSONObject jsonObject3 = new JSONObject();
    JSONObject jsonObject4 = new JSONObject();

    try {
      jsonObject1.put(Subject, "Physics");
      jsonObject1.put(Class_Attended, 29);
      jsonObject1.put(Total_Classes, 35);
      jsonArray.put(jsonObject1);

      jsonObject2.put(Subject, "Chemistry");
      jsonObject2.put(Class_Attended, 30);
      jsonObject2.put(Total_Classes, 35);
      jsonArray.put(jsonObject2);

      jsonObject3.put(Subject, "Zoology");
      jsonObject3.put(Class_Attended, 26);
      jsonObject3.put(Total_Classes, 35);
      jsonArray.put(jsonObject3);

      jsonObject4.put(Subject, "Botany");
      jsonObject4.put(Class_Attended, 33);
      jsonObject4.put(Total_Classes, 35);
      jsonArray.put(jsonObject4);

      Log.i("sharath","jsonarray - "+jsonArray.toString());

    GridView listView = findViewById(R.id.gridviewstats);
    GridAdapter gridAdapter = new GridAdapter(this, jsonArray);
    listView.setAdapter(gridAdapter);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
}