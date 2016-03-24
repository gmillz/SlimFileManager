package com.slim.slimfilemanager.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import trikita.log.Log;

public class WiFiFileShare {

    private Context mContext;

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pDeviceList mDevices;

    private Callback mCallback;

    private final IntentFilter mIntentFilter = new IntentFilter();

    public interface Callback {
        void onPeersAvailable(WifiP2pDeviceList devices);
        void onUpdateThisDevice(WifiP2pDevice thisDevice);
    }

    public WiFiFileShare(Context context) {
        mContext = context;
        mManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(mContext, mContext.getMainLooper(), null);

        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void register() {
        mContext.registerReceiver(mReceiver, mIntentFilter);
    }

    public void unRegister() {
        mContext.unregisterReceiver(mReceiver);
    }

    public String getStatus(int status) {
        switch (status) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }

    public void discoverPeers() {
        Log.d("discoverPeers");
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("onSuccess");
            }

            @Override
            public void onFailure(int i) {
                Log.d("onFailure(" + i + ")");
            }
        });
    }

    private void requestPeers() {
        mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                mDevices = wifiP2pDeviceList;
                if (mCallback != null) {
                    mCallback.onPeersAvailable(wifiP2pDeviceList);
                }
            }
        });
    }

    public void connect(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("Connection success");
            }

            @Override
            public void onFailure(int i) {
                Log.d("Connection failed");
            }
        });
    }

    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("onReceive - " + intent.getAction());
            String action = intent.getAction();
            if (action.equals(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {

                }
            } else if (action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)) {
                requestPeers();
            } else if (action.equals(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)) {
                if (mCallback != null) {
                    mCallback.onUpdateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                            WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
                }
            }
        }
    };

    private class FileTransferTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                ServerSocket serverSocket = new ServerSocket(8888);
                Socket client = serverSocket.accept();
                client.get
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
