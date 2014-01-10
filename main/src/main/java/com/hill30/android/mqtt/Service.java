package com.hill30.android.mqtt;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by mfeingol on 1/10/14.
 */
public class Service extends IntentService {
    public final static String BROKER_URL = "com.hill30.android.mqtt.BROKER_URL";
    public final static String BROKER_TOPIC = "com.hill30.android.mqtt.BROKER_TOPIC";
    public final static String USER_NAME = "com.hill30.android.mqtt.USER_NAME";
    public final static String USER_PSWD = "com.hill30.android.mqtt.USER_PSWD";
    public final static String CLIENT_ID = "com.hill30.android.mqtt.CLIENT_ID";
    public final static String MESSAGE_RECEIVED = "com.hill30.android.mqtt.MESSAGE_RECEIVED";
    public final static String MESSAGE_PAYLOAD = "com.hill30.android.mqtt.MESSAGE_PAYLOAD";

    public Service() {
        super("MQTTMessaging");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Listener listener = new Listener(this, intent);
    }
}
