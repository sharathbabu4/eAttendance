package mymsproject.oracle.android.com.myattendance.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mymsproject.oracle.android.com.myattendance.R;

public class GridAdapter extends BaseAdapter {

  private JSONArray mainList = new JSONArray();

  private Context context;

  public final static String Subject = "Subject";
  public final static String Total_Classes = "Total Classes";
  public final static String Class_Attended = "Classes Attended";

  public GridAdapter(Context context, JSONArray list) {
    this.context = context;
    this.mainList = list;
  }

  @Override
  public int getCount() {
    return mainList.length();
  }

  @Override
  public Object getItem(int position) {
    try {
      return mainList.get(position);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      final LayoutInflater layoutInflater = LayoutInflater.from(context);
      convertView = layoutInflater.inflate(R.layout.grid_layout_main_stats, null);
    }
    TextView text = convertView.findViewById(R.id.grid_text_view);
    TextView text1 = convertView.findViewById(R.id.grid_text_view1);
    TextView text2 = convertView.findViewById(R.id.grid_text_view2);
    TextView text3 = convertView.findViewById(R.id.grid_text_view3);

    try {
      JSONObject jsonObject = (JSONObject)getItem(position);
      int classatteneded =  jsonObject.getInt(Class_Attended);
      int totalclasses = jsonObject.getInt(Total_Classes);
      text.setText("Subject : " + jsonObject.getString(Subject));
      text1.setText(Class_Attended + " : "+classatteneded);
      text2.setText(Total_Classes + " : " + totalclasses);
      text3.setText("Percentage : " + ((classatteneded*100)/totalclasses));

    } catch (JSONException e) {
      e.printStackTrace();
    }
    return convertView;
  }
}
