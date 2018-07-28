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

  public GridAdapter(Context context, List list) {
    this.context = context;
this.mainList = list;
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
    image.setImageResource(getImage(position));
    text.setText(getItem(position).toString());
    return convertView;
  }

  public int getImage(int position){
    int resource = R.drawable.menu_profile;
    switch(position){
      case 0:
        resource = R.drawable.ic_photo_scan_black;
        break;
      case 1:
        resource = R.drawable.ic_people_attendance;
        break;
      case 2:
        resource = R.drawable.ic_marks_black;
        break;
      case 3:
        resource = R.drawable.ic_statistics;
        break;
    }
    return resource;
  }
}
