package com.hill30.android.mqttSample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.hill30.android.MQTTService;

public class MainActivity extends Activity {

    private EditText address;

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

    }

}
