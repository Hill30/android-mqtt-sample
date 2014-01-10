package com.hill30.android.mqtt;

import android.content.Intent;
import android.util.Log;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

public class Listener extends Connection {

    private final Service service;

    public Listener(Service service, Intent intent) {
        super(service, intent);
        this.service = service;
    }

    @Override
    protected void onConnected(CallbackConnection connection) {
        final String topic_name = getTopicName();
        Topic[] topics = {new Topic(topic_name, QoS.AT_LEAST_ONCE)};

        connection.listener(new org.fusesource.mqtt.client.Listener() {
            long count = 0;

            public void onConnected() { }

            public void onDisconnected() { }

            public void onFailure(Throwable value) {
                value.printStackTrace();
                System.exit(-2);
            }
            public void onPublish(UTF8Buffer topic, Buffer msg, Runnable ack) {
                String body = msg.utf8().toString();
                String messagePayLoad = new String(msg.getData());
                Log.d(TAG, String.format("Received %d. Message: %s.", count, messagePayLoad));

                // Broadcasts the Intent to receivers in this app.
                service.sendBroadcast(
                    new Intent(Service.MESSAGE_RECEIVED).putExtra(Service.MESSAGE_PAYLOAD, body)
                );

                count ++;
                ack.run();
            }
        });

        connection.subscribe(topics, new Callback<byte[]>() {
            public void onSuccess(byte[] qoses) {
                Log.d(Connection.TAG, String.format("Subscribed to topic: %s.", topic_name));
            }

            public void onFailure(Throwable value) {
                value.printStackTrace();
                System.exit(-2);
            }
        });

    }
}
