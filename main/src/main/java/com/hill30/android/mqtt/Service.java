package com.hill30.android.mqtt;

import android.app.IntentService;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import android.widget.Toast;

public class Service extends android.app.Service {
    public final static String BROKER_URL = "com.hill30.android.mqtt.BROKER_URL";
    public final static String BROKER_TOPIC = "com.hill30.android.mqtt.BROKER_TOPIC";
    public final static String USER_NAME = "com.hill30.android.mqtt.USER_NAME";
    public final static String USER_PSWD = "com.hill30.android.mqtt.USER_PSWD";
    public final static String CLIENT_ID = "com.hill30.android.mqtt.CLIENT_ID";
    public final static String MESSAGE_RECEIVED = "com.hill30.android.mqtt.MESSAGE_RECEIVED";
    public final static String MESSAGE_PAYLOAD = "com.hill30.android.mqtt.MESSAGE_PAYLOAD";
    public final static String SEND_MESSAGE = "com.hill30.android.mqtt.SEND_MESSAGE";
    private Listener listener;
    private Sender sender;

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread listenerThread = new HandlerThread("mqttListener", Process.THREAD_PRIORITY_BACKGROUND);
        listenerThread.start();
        listener = new Listener(this, listenerThread.getLooper());
        HandlerThread senderThread = new HandlerThread("mqttSender", Process.THREAD_PRIORITY_BACKGROUND);
        senderThread.start();
        sender = new Sender(this, senderThread.getLooper());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        Message msg = listener.obtainMessage();
//        listener.sendMessage(msg);

        Message msg = sender.obtainMessage();
        sender.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

}
