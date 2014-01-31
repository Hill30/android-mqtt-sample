package com.hill30.android.mqtt;

import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.hill30.android.net.Constants;

import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Tracer;

import java.net.URISyntaxException;

public abstract class Connection extends android.os.Handler {

    public final static String TAG = "MQTTConnection";

    private CallbackConnection connection;

    public Connection(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        //Connect("tcp://10.0.2.2:1883", "", "", "user");
        Connect("tcp://"+ Constants.ACTIVE_MQ_IP +":1883", "", "", "user");
    }

    private void Connect(String brokerAddress, String userName, String password, String clientId) {

        MQTT mqtt = new MQTT();
        mqtt.setTracer(new Tracer(){
            @Override
            public void debug(String message, Object... args) {
                Log.d(TAG, "*** " + String.format(message, args));
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

        connection.connect(new org.fusesource.mqtt.client.Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                Log.d(TAG, "Sender received onSuccess() in connect to broker.");
                onConnected(connection);
            }

            @Override
            public void onFailure(Throwable value) {
                Log.e(TAG, "Sender exiting. Received onFailure in connect(). Message " + value.getMessage());
                value.printStackTrace();
            }
        });

    }

    public void restart() {
        connection.disconnect(new org.fusesource.mqtt.client.Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                //Connect("tcp://10.0.2.2:1883", "", "", "user");
                Connect("tcp://"+ Constants.ACTIVE_MQ_IP +":1883", "", "", "user");
                Log.e(TAG, "Connection restarted ");
            }

            @Override
            public void onFailure(Throwable value) { }
        });
    }

    public abstract void onConnected(CallbackConnection connection);

    public void publish(String topic, byte[] payload, QoS qos, boolean flag, org.fusesource.mqtt.client.Callback<Void> callback) {
        connection.publish(topic, payload, qos, flag, callback);
    }

}
