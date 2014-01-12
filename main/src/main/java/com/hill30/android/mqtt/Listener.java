package com.hill30.android.mqtt;

import android.util.Log;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

public abstract class Listener {

    private static final String LISTENER_TOPIC_SUFFIX = "Inbound";

    public Listener(CallbackConnection connection, String topic) {
        final String topic_name = topic + "." + LISTENER_TOPIC_SUFFIX + ".User";
        Topic[] topics = {new Topic(topic_name, QoS.AT_LEAST_ONCE)};

        connection.listener(new org.fusesource.mqtt.client.Listener() {
            long count = 0;

            public void onConnected() { }

            public void onDisconnected() { }

            public void onFailure(Throwable value) {
                Log.d(Connection.TAG, String.format("Listener failure. Message: %s.", value.getMessage()));
                value.printStackTrace();
            }
            public void onPublish(UTF8Buffer topic, Buffer msg, Runnable ack) {
                String body = msg.utf8().toString();
                String messagePayLoad = new String(msg.getData());
                Log.d(Connection.TAG, String.format("Received %d. Message: %s.", count, messagePayLoad));

                onMessageReceived(body);

                count ++;
                ack.run();
            }
        });

        connection.subscribe(topics, new Callback<byte[]>() {
            public void onSuccess(byte[] qoses) {
                Log.d(Connection.TAG, String.format("Subscribed to topic: %s.", topic_name));
            }

            public void onFailure(Throwable value) {
                Log.d(Connection.TAG, String.format("Subscribe failure. Message: %s.", value.getMessage()));
                value.printStackTrace();
            }
        });

    }

    public abstract void onMessageReceived(String message);

}
