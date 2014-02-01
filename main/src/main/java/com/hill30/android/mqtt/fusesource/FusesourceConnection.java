package com.hill30.android.mqtt.fusesource;

import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.hill30.android.mqtt.Connection;
import com.hill30.android.net.Constants;

import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Tracer;
import org.fusesource.mqtt.codec.MQTTFrame;

import java.net.URISyntaxException;

public abstract class FusesourceConnection extends Connection {

    private CallbackConnection connection;
    private Listener listener;
    private Sender sender;

    public FusesourceConnection(Looper looper) {
        super(looper);
    }

    @Override
    protected void Connect(String brokerAddress, String userName, String password, String clientId, String topic) {

        MQTT mqtt = new MQTT();
        mqtt.setTracer(new Tracer(){
            @Override
            public void debug(String message, Object... args) {
                Log.d(TAG, "*** " + String.format(message, args));
            }

            @Override
            public void onSend(MQTTFrame frame) {
                Log.d(TAG, "Sending " + frame.toString());
            }

            @Override
            public void onReceive(MQTTFrame frame) {
                Log.d(TAG, "Received " + frame.toString());
            }
        });
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

        listener = new Listener(connection, topic) {
            @Override
            public void onMessageReceived(String message) {
                FusesourceConnection.this.onMessageRecieved(message);
            }
        };

        sender = new Sender("path", this, topic);

        connection.connect(new org.fusesource.mqtt.client.Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                Log.d(TAG, "Sender received onSuccess() in connect to broker.");
                sender.onConnected(connection);
            }

            @Override
            public void onFailure(Throwable value) {
                Log.e(TAG, "Sender exiting. Received onFailure in connect(). Message " + value.getMessage());
                value.printStackTrace();
            }
        });

    }

    @Override
    protected void send(String message) {
        sender.send(connection, message);
    }
}
