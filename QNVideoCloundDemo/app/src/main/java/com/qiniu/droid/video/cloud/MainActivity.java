package com.qiniu.droid.video.cloud;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.qiniu.droid.video.cloud.player.PlayerAddressActivity;
import com.qiniu.droid.video.cloud.rtc.activity.RTCActivity;
import com.qiniu.droid.video.cloud.streaming.activity.StreamingAddressActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(R.id.lv);

        String[] data = new String[]{"推流", "连麦", "播放"};
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        startActivity(new Intent(MainActivity.this, StreamingAddressActivity.class));
                        break;
                    case 1:
                        startActivity(new Intent(MainActivity.this, RTCActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(MainActivity.this, PlayerAddressActivity.class));
                        break;
                }
            }
        });
    }
}