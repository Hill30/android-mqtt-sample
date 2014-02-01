package com.hill30.android.mqtt.paho;

import android.os.Looper;

import com.hill30.android.mqtt.Connection;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.internal.MemoryPersistence;

/**
 * Created by michaelfeingold on 2/1/14.
 */
public abstract class PahoConnection extends Connection implements MqttCallback
{
    private MqttClient mqttClient;

    public PahoConnection(Looper looper) {
        super(looper);

    }

    @Override
    protected void Connect(String brokerUrl, String userName, String password, String clientID, String topic) {
        try {
            final String topic_name = topic + "." + LISTENER_TOPIC_SUFFIX + ".User";
            mqttClient = new MqttClient(brokerUrl, clientID, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            mqttClient.connect(options);
            mqttClient.subscribe(topic_name, 2 /*QoS = EXACTLY_ONCE*/);
            mqttClient.setCallback(this);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void send(String stringExtra) {

    }

    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(MqttTopic mqttTopic, MqttMessage mqttMessage) throws Exception {
        onMessageRecieved(mqttMessage.toString());
    }

    @Override
    public void deliveryComplete(MqttDeliveryToken mqttDeliveryToken) {

    }
}
