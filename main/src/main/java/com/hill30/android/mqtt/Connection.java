package com.hill30.android.mqtt;

import android.content.Intent;
import android.util.Log;

import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

import java.net.URISyntaxException;

public abstract class Connection {

    public final static String TAG = "MQTTConnection";

    private MQTT mqtt;
    private CallbackConnection connection;
    private String rootTopicName;

    protected String getTopicName() {
        return rootTopicName;
    }

    private String getConnectionParameter(Intent intent, String name, String defaultValue) {
        String value = intent.getStringExtra(name);
        if(value == null || value.isEmpty())
            return defaultValue;
        else
            return value;
    }

    public Connection(Intent intent)
    {
        String brokerAddress = getConnectionParameter(intent, Service.BROKER_URL, "tcp://10.0.2.2:1883");

        rootTopicName = getConnectionParameter(intent, Service.BROKER_TOPIC, "ServiceTracker");

        String userName = getConnectionParameter(intent, Service.USER_NAME, "");

        String password = getConnectionParameter(intent, Service.USER_PSWD, "");

        String clientId = getConnectionParameter(intent, Service.CLIENT_ID, "user");

        mqtt = new MQTT();
        mqtt.setClientId(clientId);

        try {
            mqtt.setHost(brokerAddress);
            Log.d(TAG, "Address set: " + brokerAddress);

            if(!userName.isEmpty()) {
                mqtt.setUserName(userName);
                Log.d(TAG, "UserName set: [" + userName + "]");
            }

            if(!password.isEmpty()) {
                mqtt.setPassword(password);
                Log.d(TAG, "Password set: [" + password + "]");
            }

            mqtt.setCleanSession(false); ///AAB for durable topic
            connection = mqtt.callbackConnection();
        }
        catch(URISyntaxException urise) {
            Log.e(TAG, "URISyntaxException connecting to " + brokerAddress + " - " + urise);
        }
        catch(Exception exc) {
            Log.e(TAG, "Exception: " + exc.getMessage());
        }

        connection.connect(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                Log.d(TAG, "Sender received onSuccess() in connect to broker.");
                onConnected(connection);
            }

            @Override
            public void onFailure(Throwable value) {
                Log.e(TAG, "Sender exiting. Received onFailure in connect().");
                value.printStackTrace();
                System.exit(-2);
            }
        });
    }

    protected void onConnected(CallbackConnection connection) { }

    protected void publish(String topic, byte[] payload, QoS qos, boolean flag, Callback<Void> callback) {
        connection.publish(topic, payload, qos, flag, callback);
    }

}
