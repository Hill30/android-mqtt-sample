package com.hill30.android.mqtt.paho;

import android.os.Looper;
import android.util.Log;

import com.hill30.android.mqtt.Connection;
import com.hill30.android.mqtt.Service;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

/**
 * Created by michaelfeingold on 2/1/14.
 */
public abstract class PahoConnection extends Connection
{
    private final String persistenceFolder;
    private MqttAsyncClient mqttClient;
    private String topic;

    public PahoConnection(Looper looper, String persistenceFolder) {
        super(looper);

        this.persistenceFolder = persistenceFolder;
    }

    @Override
    protected void Connect(String brokerUrl, String userName, String password, String clientID, String topic) {
        try {
            this.topic = topic;
            final String topic_name = topic + "." + LISTENER_TOPIC_SUFFIX + ".User";
            if (mqttClient != null && mqttClient.isConnected())
                mqttClient.disconnect(0, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d(TAG, "disconnected - success");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.d(TAG, "disconnected - failure " + exception.getMessage());
                    }
                });
            mqttClient = new MqttAsyncClient(brokerUrl, clientID, new MqttDefaultFilePersistence(persistenceFolder));
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d(TAG, "connection lost cause: " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    onMessageRecieved(message.toString());
                    Log.d(TAG, "message " + message + " received");
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "message delivery complete");
                }
            });
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(false);
            mqttClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    try {
                        Log.d(TAG, "connection - success");
                        mqttClient.subscribe(topic_name, 2 /*QoS = EXACTLY_ONCE*/);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "connection failure: " + exception.getMessage());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void send(final String message) {
        final String topic_name = topic + "." + SENDER_TOPIC_SUFFIX;
        try {
            mqttClient.publish(topic_name, message.getBytes(), 2, true, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "send message " + message + " - success");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "send message " + message + " - failure " + exception.getMessage());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
