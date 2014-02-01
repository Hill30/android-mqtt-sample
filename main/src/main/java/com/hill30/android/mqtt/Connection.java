package com.hill30.android.mqtt;

import android.os.Looper;
import android.os.Message;

import com.hill30.android.net.Constants;

import org.fusesource.mqtt.client.CallbackConnection;

/**
 * Created by michaelfeingold on 2/1/14.
 */
public abstract class Connection extends android.os.Handler {

    public final static String TAG = "MQTTConnection";
    public static final String LISTENER_TOPIC_SUFFIX = "Inbound";
    public static final String SENDER_TOPIC_SUFFIX = "Outbound";

    private final static String userName = "";
    private final static String password = "";
    private final static String clientID = "user1";
    private final static String rootTopic = "ServiceTracker";

    private CallbackConnection connection;

    public Connection(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        //Connect("tcp://10.0.2.2:1883", "", "", "user");
//        Connect("tcp://217.119.26.211:1883", "", "", "user");
//        Connect("tcp://192.168.1.67:1883", "", "", "user");
        Connect("tcp://" + Constants.ACTIVE_MQ_IP + ":1883", userName, password, clientID, rootTopic);
    }

    protected abstract void Connect(String brokerUrl, String userName, String password, String clientID, String topic);

    protected abstract void send(String stringExtra);

    public abstract void onMessageRecieved(String message);

}
