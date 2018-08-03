package mymsproject.oracle.android.com.myattendance.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import mymsproject.oracle.android.com.myattendance.R;

public class StatsActivity extends AppCompatActivity {

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.stats_layout_main);
    int pos = getIntent().getIntExtra("position", 1);
    ImageView imageView = findViewById(R.id.stats_image_view);
    if (pos == 2) {
      imageView.setImageResource(R.drawable.marks);
    } else if (pos == 3) {
      imageView.setImageResource(R.drawable.stats);
    } else {
      imageView.setImageResource(R.drawable.attendance);
    }
  }

  public void registerClick(View v) {
    Intent intent = new Intent(this, LoginActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
  }
}