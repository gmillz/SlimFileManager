package com.slim.slimfilemanager.activity;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slim.slimfilemanager.R;
import com.slim.slimfilemanager.ThemeActivity;
import com.slim.slimfilemanager.utils.WiFiFileShare;
import com.slim.slimfilemanager.widget.DividerItemDecoration;

import java.util.ArrayList;

import trikita.log.Log;

public class WiFiFileShareActivity extends ThemeActivity implements WiFiFileShare.Callback {

    private WifiP2pDevice mMyDevice;
    private WiFiFileShare mFileShare;
    private ItemAdapter mAdapter;

    private ArrayList<WifiP2pDevice> mDevices = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.file_share_activity);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.device_list);
        mAdapter = new ItemAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
        DividerItemDecoration decor = new DividerItemDecoration(this, null);
        decor.setShowFirstDivider(false);
        decor.setShowLastDivider(false);
        recyclerView.addItemDecoration(decor);

        mFileShare = new WiFiFileShare(this);
        mFileShare.setCallback(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mFileShare.register();
        mFileShare.discoverPeers();
    }

    @Override
    public void onPause() {
        super.onPause();
        mFileShare.unRegister();
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList devices) {
        mDevices.clear();
        if (mMyDevice != null) {
            mDevices.add(0, mMyDevice);
        }
        for (WifiP2pDevice device : devices.getDeviceList()) {
            mDevices.add(device);
            Log.d("Device=" + device.deviceName);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onUpdateThisDevice(WifiP2pDevice thisDevice) {
        mMyDevice = thisDevice;
        mDevices.add(0, mMyDevice);
        mAdapter.notifyDataSetChanged();
    }

    public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView name;
            TextView status;

            public ViewHolder(View v) {
                super(v);
                name = (TextView) v.findViewById(R.id.device_name);
                status = (TextView) v.findViewById(R.id.device_details);
            }
        }

        @Override
        public ItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(WiFiFileShareActivity.this, R.layout.row_devices, null);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ItemAdapter.ViewHolder holder, final int position) {
            holder.name.setText(mDevices.get(position).deviceName);
            holder.status.setText(mFileShare.getStatus(mDevices.get(position).status));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mFileShare.connect(mDevices.get(position));
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDevices.size();
        }
    }
}
