package mymsproject.oracle.android.com.myattendance.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mymsproject.oracle.android.com.myattendance.R;

public class GridAdapter extends BaseAdapter {

  private List<String> mainList = new ArrayList();

  private Context context;

  private int layoutHeight;

  public GridAdapter(Context context, int height) {
    this.layoutHeight = height;
    this.context = context;
    mainList.clear();
    mainList.add(0, "QR-Code Scanner");
    mainList.add(1, "Attendance Report");
    mainList.add(2, "Marks Report");
    mainList.add(3, "My-Statistics");
  }

  @Override
  public int getCount() {
    return mainList.size();
  }

  @Override
  public Object getItem(int position) {
    return mainList.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      final LayoutInflater layoutInflater = LayoutInflater.from(context);
      convertView = layoutInflater.inflate(R.layout.grid_layout_main, null);
    }
    ImageView image = convertView.findViewById(R.id.grid_image_view);
    TextView text = convertView.findViewById(R.id.grid_text_view);
    image.setImageResource(R.drawable.menu_profile);
    text.setText(getItem(position).toString());
    convertView.setMinimumHeight(layoutHeight / 2);
    return convertView;
  }
}
