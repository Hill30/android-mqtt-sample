package com.hill30.android.mqtt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;

import org.fusesource.mqtt.client.CallbackConnection;

public class Service extends android.app.Service {
    public final static String BROKER_URL = "com.hill30.android.mqtt.BROKER_URL";
    public final static String BROKER_TOPIC = "com.hill30.android.mqtt.BROKER_TOPIC";
    public final static String USER_NAME = "com.hill30.android.mqtt.USER_NAME";
    public final static String USER_PSWD = "com.hill30.android.mqtt.USER_PSWD";
    public final static String CLIENT_ID = "com.hill30.android.mqtt.CLIENT_ID";
    public final static String MESSAGE_RECEIVED = "com.hill30.android.mqtt.MESSAGE_RECEIVED";
    public final static String MESSAGE_PAYLOAD = "com.hill30.android.mqtt.MESSAGE_PAYLOAD";
    public final static String SEND_MESSAGE = "com.hill30.android.mqtt.SEND_MESSAGE";
    private String rootTopic = "ServiceTracker";
    private Listener listener;
    private Sender sender;
    private Connection connection;

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread connectionThread = new HandlerThread("mqttConnection", Process.THREAD_PRIORITY_BACKGROUND);
        connectionThread.start();

        connection = new Connection(connectionThread.getLooper()) {

            @Override
            public void onConnected(CallbackConnection connection) {
                listener = new Listener(connection, rootTopic){

                    @Override
                    public void onMessageReceived(String message) {
                        // Broadcasts the Intent to receivers in this app.
                        sendBroadcast(
                                new Intent(Service.MESSAGE_RECEIVED).putExtra(Service.MESSAGE_PAYLOAD, message)
                        );


                    }
                };

                sender = new Sender(getApplicationContext().getFilesDir().getPath(), this, rootTopic);
            }

        };

        registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                sender.send(connection, intent.getStringExtra(Service.MESSAGE_PAYLOAD));
            }

        }, new IntentFilter(Service.SEND_MESSAGE));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Message msg = connection.obtainMessage();
        // This is what initiates the connection
        // Connection parameters will have to go into msg
        connection.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

}
