package com.hill30.android.mqtt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Sender extends Connection {

    private static final String MSG_FILE_PREFIX = "msg_to_send_";
    private static final String MSG_FILE_EXT = ".txt";
    private static final String SENDER_TOPIC_SUFFIX = "Outbound";
    private static final String LISTENER_TOPIC_SUFFIX = "Inbound";
    private String msg_folder_path;
    private String topicName;
    private Service service;

    private List<Pair<String,String>> lstMsgToSend = new ArrayList<Pair<String, String>>();

    public Sender(Service service, Looper looper) {
        super(looper);
        this.service = service;
    }

    @Override
    protected String suffix() {
        return "sender";
    }

    @Override
    public void onConnected(CallbackConnection connection) {

        msg_folder_path = service.getApplicationContext().getFilesDir().getPath();
        topicName = getTopicName() + "." + SENDER_TOPIC_SUFFIX;

        File folder = new File(msg_folder_path);
        Log.d(TAG, String.format("Message directory: %s", msg_folder_path));
        if (folder.isDirectory()) {
            for (File fileEntry : folder.listFiles()) {
                if(fileEntry.getName().contains(MSG_FILE_PREFIX)) {
                    String msgRead = readMessageFile(fileEntry.getName());
                    Log.d(TAG, String.format("Message to send. FileName: %s, MsgBody: %s.", fileEntry.getName(), msgRead));

                    publish(msgRead, fileEntry.getName());
                }
            }
        }

        service.registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra(Service.MESSAGE_PAYLOAD);
                Log.e(TAG, "Received message notification: " + message);

                // create message file name. Format: msg_to_send_ddMMyyyhhmmss.txt
                SimpleDateFormat s = new SimpleDateFormat("yyyyMMddHHmmss");
                String timeStamp = s.format(new Date());
                String msgFileName = MSG_FILE_PREFIX + timeStamp + MSG_FILE_EXT;

                writeMessageToFile(message, msgFileName);
                 publish(message, msgFileName);
            }

        }, new IntentFilter(Service.SEND_MESSAGE));

        final String topic_name = getTopicName() + "." + LISTENER_TOPIC_SUFFIX + ".User";
        Topic[] topics = {new Topic(topic_name, QoS.AT_LEAST_ONCE)};

        connection.listener(new org.fusesource.mqtt.client.Listener() {
            long count = 0;

            public void onConnected() { }

            public void onDisconnected() { }

            public void onFailure(Throwable value) {
                Log.d(TAG, String.format("Listener failure. Message: %s.", value.getMessage()));
                value.printStackTrace();
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

        connection.subscribe(topics, new org.fusesource.mqtt.client.Callback<byte[]>() {
            public void onSuccess(byte[] qoses) {
                Log.d(Connection.TAG, String.format("Subscribed to topic: %s.", topic_name));
            }

            public void onFailure(Throwable value) {
                Log.d(TAG, String.format("Subscribe failure. Message: %s.", value.getMessage()));
                value.printStackTrace();
            }
        });

    }

    private void publish(final String message, final String fileNameToClear) {
        publish(topicName, message.getBytes(), QoS.AT_LEAST_ONCE, true,
            new org.fusesource.mqtt.client.Callback<Void>() {

                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, String.format("Acked published message id: %s, destination: %s.", fileNameToClear, message));
                    deleteStoredMessage(fileNameToClear);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    Log.e(TAG,"Error sending message: " + throwable.getMessage());
                }
            });

    }

    private String readMessageFile(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(new File(msg_folder_path, fileName)));
            while ((line = in.readLine()) != null)
                stringBuilder.append(line);

        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return stringBuilder.toString();
    }

    public void writeMessageToFile(final String fileContents, String fileName) {
        try {
            FileWriter out = new FileWriter(new File(msg_folder_path, fileName));
            out.write(fileContents);
            out.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void deleteStoredMessage(final String fileName) {
        File fileToDelete = new File(msg_folder_path, fileName);
        fileToDelete.delete();
        Log.d(TAG, String.format("Deleted file path: %s, name: %s. ", fileToDelete.getPath(), fileToDelete.getName()));
    }

}
