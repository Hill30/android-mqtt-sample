package com.hill30.android.mqttSample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.hill30.android.paho.MQTTService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by azavarin on 1/31/14.
 */
public class PahoClientActivity  extends Activity {

    public final static String MESSAGE_RECEIVED = "com.hill30.android.paho.PahoClientActivity.MESSAGE_RECEIVED";
    public static final String MESSAGE_PAYLOAD = "com.hill30.android.paho.PushCallback.MESSAGE_PAYLOAD";

    private ListView messages;
    private ArrayAdapter<String> messagesAdapter;
    private List<String> messagesList = new ArrayList<String>();

    private BroadcastReceiver messageReciever = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getExtras().getString(MESSAGE_PAYLOAD);
            messagesList.add(msg);
            messagesAdapter.notifyDataSetChanged();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paho_client);

        messages = (ListView) findViewById(R.id.messages);
        messagesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, PahoClientActivity.this.messagesList);
        messages.setAdapter(messagesAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        final Intent intent = new Intent(this, MQTTService.class);
        startService(intent);

        registerReceiver(messageReciever, new IntentFilter(MESSAGE_RECEIVED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(messageReciever);
    }
}
