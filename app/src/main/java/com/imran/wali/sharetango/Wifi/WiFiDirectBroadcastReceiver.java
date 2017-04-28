package com.imran.wali.sharetango.Wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

import com.imran.wali.sharetango.DashboardActivity;

/**
 * Created by Wali on 1/22/2017.
 */

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private DashboardActivity mDashboardActivity;
    private WifiP2pManager.PeerListListener mPeerListListener;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       DashboardActivity activity, WifiP2pManager.PeerListListener peerListListener) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mDashboardActivity = activity;
        this.mPeerListListener = peerListListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            mManager.requestPeers(mChannel, mPeerListListener);
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            //mManager.requestConnectionInfo(mChannel, mDashboardActivity);

            if (networkInfo.isConnected()) {
                System.out.println("Connection");
                // we are connected with the other device, request connection
                // info to find group owner IP

//                DeviceDetailFragment fragment = (DeviceDetailFragment) activity
//                        .getFragmentManager().findFragmentById(R.id.frag_detail);
                //mManager.requestConnectionInfo(mChannel, mDashboardActivity);
            } else {
                // It's a disconnect
                System.out.println("THIS IS A DISCONNECTION");
            }


        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
            WifiP2pDevice a = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            System.out.println(a.toString());
        }
    }
}
