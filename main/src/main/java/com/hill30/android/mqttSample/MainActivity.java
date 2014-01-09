package com.hill30.android.mqttSample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.hill30.android.MQTTService;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private EditText address;

    class Visit {

        private final String id;

        public Visit(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return id;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        address = (EditText)findViewById(R.id.address);

        findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mServiceIntent = new Intent(MainActivity.this, MQTTService.class);
                mServiceIntent.putExtra(MQTTService.BROKER_URL, "tcp://10.0.2.2:1883");
                mServiceIntent.putExtra(MQTTService.BROKER_TOPIC, "TestOne");
                mServiceIntent.putExtra(MQTTService.CLIENT_ID, "android_svc");

                startService(mServiceIntent);
            }
        });

        ArrayList<Visit> visitObjects = new ArrayList<Visit>();
        visitObjects.add(new Visit("line 1"));
        visitObjects.add(new Visit("line 2"));

        ListView visits = (ListView) findViewById(R.id.visits);
        visits.setAdapter(new ArrayAdapter<Visit>(this, android.R.layout.simple_list_item_1, visitObjects));

        visits.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final Visit item = (Visit) parent.getItemAtPosition(position);
            }

        });

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                address.setText(intent.getCharSequenceExtra(MQTTService.BROADCAST_MSG));
            }
        }, new IntentFilter(MQTTService.BROADCAST_ACTION));

    }

}
