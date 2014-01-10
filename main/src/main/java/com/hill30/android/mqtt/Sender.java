package com.hill30.android.mqtt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.util.Pair;

import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.QoS;

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
    private final Service service;
    private final String msg_folder_path;

    private List<Pair<String,String>> lstMsgToSend = new ArrayList<Pair<String, String>>();

    public Sender(Service service, Intent intent) {
        super(intent);
        this.service = service;

        msg_folder_path = service.getApplicationContext().getFilesDir().getPath();

        readStoredMessagesToBeSend();

        service.registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra(Service.MESSAGE_PAYLOAD);
                Log.e(TAG, "Received message notification: " + message);
                synchronized (Sender.class) {
                    // create message file name. Format: msg_to_send_ddMMyyyhhmmss.txt
                    SimpleDateFormat s = new SimpleDateFormat("yyyyMMddHHmmss");
                    String timeStamp = s.format(new Date());
                    String msgFileName = MSG_FILE_PREFIX + timeStamp + MSG_FILE_EXT;

                    lstMsgToSend.add(new Pair(message, msgFileName));

                    writeMessageToFile(message, msgFileName);
                }
            }

        }, new IntentFilter(Service.SEND_MESSAGE));

        String topicName = getTopicName() + "." + SENDER_TOPIC_SUFFIX;

        while(true) {
            if(lstMsgToSend.isEmpty()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else {
                synchronized (Sender.class) {
                    Pair toPublish = lstMsgToSend.get(0);
                    final String message = (String)toPublish.first;
                    final String fileNameToClear = (String)toPublish.second;

                    // publish message
                    publish(topicName, message.getBytes(), QoS.AT_LEAST_ONCE, true,
                        new Callback<Void>() {

                            @Override
                            public void onSuccess(Void aVoid) {

                                Log.d(TAG, String.format("Acked published message id: %s, destination: %s.", fileNameToClear, message));
                                deleteStoredMessage(fileNameToClear); //TODO: move to non-blocking cleaning.
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                Log.e(TAG,"Error sending message: " + throwable.getMessage());
                            }
                        });

                    lstMsgToSend.remove(0);
                }
            }
        }
    }

    private void readStoredMessagesToBeSend() {
        final File folder = new File(msg_folder_path);
        Log.d(TAG, String.format("Message directory: %s", msg_folder_path));
        if (folder.isDirectory()) {
            for (final File fileEntry : folder.listFiles()) {
                if(fileEntry.getName().contains(MSG_FILE_PREFIX)) {
                    String msgRead = readMessageFile(fileEntry.getName());
                    Log.d(TAG, String.format("Message to send. FileName: %s, MsgBody: %s.", fileEntry.getName(), msgRead));

                    lstMsgToSend.add(new Pair( msgRead, fileEntry.getName() ));
                }
            }
        }
        return;
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

    private boolean deleteStoredMessage(final String fileName) {
        File fileToDelete = new File(msg_folder_path, fileName);
        fileToDelete.delete();
        Log.d(TAG, String.format("Deleted file path: %s, name: %s. ", fileToDelete.getPath(), fileToDelete.getName()));
        return true;
    }

}
