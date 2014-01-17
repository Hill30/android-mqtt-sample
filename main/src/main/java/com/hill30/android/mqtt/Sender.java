package com.hill30.android.mqtt;

import android.util.Log;

import org.fusesource.mqtt.client.QoS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Sender {

    private static final String MSG_FILE_PREFIX = "msg_to_send_";
    private static final String MSG_FILE_EXT = ".txt";
    private static final String SENDER_TOPIC_SUFFIX = "Outbound";
    private String msg_folder_path;
    private String topicName;

    public Sender(String persistenceFolder, final Connection connection, String topic) {

        msg_folder_path = persistenceFolder;
        topicName = topic + "." + SENDER_TOPIC_SUFFIX;

        File folder = new File(msg_folder_path);
        Log.d(Connection.TAG, String.format("Message directory: %s", msg_folder_path));
        if (folder.isDirectory()) {
            if (folder.listFiles() != null)
                for (File fileEntry : folder.listFiles()) {
                    if(fileEntry.getName().contains(MSG_FILE_PREFIX)) {
                        String msgRead = readMessageFile(fileEntry.getName());
                        Log.d(Connection.TAG, String.format("Resending message. FileName: %s, MsgBody: %s.", fileEntry.getName(), msgRead));

                        publish(connection, msgRead, fileEntry.getName());
                    }
                }
        }

    }

    public void send(Connection connection, String message) {

        Log.e(Connection.TAG, "Sending message: " + message);
        // create message file name. Format: msg_to_send_ddMMyyyhhmmss.txt
        SimpleDateFormat s = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStamp = s.format(new Date());
        String msgFileName = MSG_FILE_PREFIX + timeStamp + MSG_FILE_EXT;

        writeMessageToFile(message, msgFileName);
        Log.e(Connection.TAG, "Message saved to : " + msgFileName);
        publish(connection, message, msgFileName);
    }

    private void publish(final Connection connection, final String message, final String fileNameToClear) {
        connection.publish(topicName, message.getBytes(), QoS.EXACTLY_ONCE, true,
            new org.fusesource.mqtt.client.Callback<Void>() {

                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(Connection.TAG, String.format("Acked published message id: %s, destination: %s.", fileNameToClear, message));
                    deleteStoredMessage(fileNameToClear);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    Log.e(Connection.TAG, "Error sending message: " + throwable.getMessage());
                    connection.restart();
                }
            });

    }

    private String readMessageFile(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in;

        try {
            in = new BufferedReader(new FileReader(new File(msg_folder_path, fileName)));
            while ((line = in.readLine()) != null)
                stringBuilder.append(line);

        } catch (FileNotFoundException e) {
            Log.e(Connection.TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(Connection.TAG, e.getMessage());
        }
        return stringBuilder.toString();
    }

    public void writeMessageToFile(final String fileContents, String fileName) {
        try {
            FileWriter out = new FileWriter(new File(msg_folder_path, fileName));
            out.write(fileContents);
            out.close();
        } catch (IOException e) {
            Log.e(Connection.TAG, e.getMessage());
        }
    }

    private void deleteStoredMessage(final String fileName) {
        File fileToDelete = new File(msg_folder_path, fileName);
        fileToDelete.delete();
        Log.d(Connection.TAG, String.format("Deleted file path: %s, name: %s. ", fileToDelete.getPath(), fileToDelete.getName()));
    }

}
