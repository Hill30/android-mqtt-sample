package com.hill30.android.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by azavarin on 1/29/14.
 */
public class NetworkStatusReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NetworkStatusReceiver", "Network status changed");

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        Log.d("NetworkStatusReceiver", "Network status is " + (activeNetwork != null && activeNetwork.isConnected() ? " CONNECTED " : " DISCONNECTED"));
        if(activeNetwork != null && activeNetwork.isConnected()){
            context.sendBroadcast(
                    new Intent(Constants.INTENT_NETWORK_CONNECTED)
            );
        } else {
            Log.d("NetworkStatusReceiver", "No network");
            context.sendBroadcast(
                new Intent(Constants.INTENT_NETWORK_DISCONNECTED)
            );

        }
    }
}
