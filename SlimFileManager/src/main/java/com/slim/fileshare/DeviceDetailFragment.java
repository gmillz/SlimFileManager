package com.slim.fileshare;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.slim.slimfilemanager.R;
import com.slim.slimfilemanager.settings.SettingsProvider;

import trikita.log.Log;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;

    public static final String CLIENT_IP = "wiFi_client_ip";
    public static final String SERVER = "server_device";
    public static final String GROUP_OWNER = "group_owner_address";

    private View mContentView = null;
    private WifiP2pDevice mDevice;
    private WifiP2pInfo mInfo;

    private static ProgressDialog mProgressDialog;

    public static String sWiFiClientIp = "";
    static boolean sClientCheck = false;
    public static String mGroupOwnerAddress = "";
    static long mFileLength = 0;
    static int sPercentage = 0;
    public static String sFolderName = "WiFiDirectDemo";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = View.inflate(getActivity(), R.layout.device_detail, null);

        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = mDevice.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                mProgressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + mDevice.deviceAddress, true, true
//                        new DialogInterface.OnCancelListener() {
//
//                            @Override
//                            public void onCancel(DialogInterface dialog) {
//                                ((DeviceActionListener) getActivity()).cancelDisconnect();
//                            }
//                        }
                );
                ((DeviceListFragment.DeviceActionListener) getActivity()).connect(config);

            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceListFragment.DeviceActionListener) getActivity()).disconnect();
                    }
                });

        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Allow user to pick an image from Gallery or other
                        // registered apps
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                    }
                });

        return mContentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
        if(resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String selectedFilePath = null;
            try {
                selectedFilePath = CommonMethods.getPath(uri,
                        getActivity());

                Log.e("Original Selected File Path-> ", selectedFilePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String Extension = "";
            if(selectedFilePath != null) {
                File f = new File(selectedFilePath);
                System.out.println("file name is   ::" + f.getName());
                mFileLength = f.length();
                try {
                    Extension = f.getName();
                    Log.e("Name of File-> ", "" + Extension);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                CommonMethods.e("", "path is null");
                return;
            }


            TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
            String status = "Sending: " + uri;
            statusText.setText(status);
            Log.d("Intent----------- " + uri);
            Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
    	        /*
    	         * Choose on which device file has to send weather its server or client
    	         */
            String Ip = SettingsProvider.getString(getActivity(), CLIENT_IP, null);
            String OwnerIp = SettingsProvider.getString(
                    getActivity(), GROUP_OWNER, null);
            Log.d("TEST", "Ip=" + Ip + " : OWNER=" + OwnerIp);
            if (OwnerIp != null && OwnerIp.length() > 0) {
                CommonMethods.e("", "inside the check -- >");
                // if(!info.groupOwnerAddress.getHostAddress().equals(LocalIp)){
                String host=null;
                int  sub_port =-1;
                
                if (SettingsProvider.getBoolean(getActivity(), SERVER, false)) {

                    //-----------------------------
                    if (Ip != null && !Ip.equals("")) {
                        CommonMethods.e(
                                "in if condition",
                                "Sending data to " + Ip);
                        // Get Client Ip Address and send data
                        host=Ip;
                        sub_port=FileTransferService.PORT;
                        serviceIntent
                                .putExtra(
                                        FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                                        Ip);
//    							serviceIntent
//    									.putExtra(
//    											WiFiFileTransferService.EXTRAS_GROUP_OWNER_PORT1,
//    											WiFiFileTransferService.CLIENTPORT);

                    }


                } else {
                    CommonMethods.e(
                            "in else condition",
                            "Sending data to " + OwnerIp);

                    FileTransferService.PORT = 8888;

                    host=OwnerIp;
                    sub_port=FileTransferService.PORT;
                    serviceIntent
                            .putExtra(
                                    FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                                    OwnerIp);

//    					serviceIntent
//    							.putExtra(
//    									WiFiFileTransferService.EXTRAS_GROUP_OWNER_PORT1,
//    									WiFiFileTransferService.PORT);
                    // anuj


                }


                serviceIntent.putExtra(FileTransferService.Extension, Extension);
                serviceIntent.putExtra(FileTransferService.Filelength, mFileLength);
                serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT,
                        FileTransferService.PORT);
                Log.d("TEST", "host=" + host + " : sub_port=" + sub_port);
                if(host !=null && sub_port!=-1) {
                    CommonMethods.e("Going to intiate service", "service intent for initiating transfer");
                    showprogress("Sending...");
                    getActivity().startService(serviceIntent);
                } else {
                    CommonMethods.DisplayToast(getActivity(),
                            "Host Address not found, Please Re-Connect");
                    dismissProgressDialog();
                }
            } else {
                dismissProgressDialog();
                CommonMethods.DisplayToast(getActivity(),
                        "Host Address not found, Please Re-Connect");
            }
        } else {
            CommonMethods.DisplayToast(getActivity(), "Cancelled Request");
        }
//        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
//                info.groupOwnerAddress.getHostAddress());

//        getActivity().startService(serviceIntent);
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mInfo = info;
        mContentView.setVisibility(View.VISIBLE);

        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        String groupOwner = getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner) ? getResources().getString(android.R.string.yes)
                : getResources().getString(R.string.no));
        view.setText(groupOwner);

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        if(info.groupOwnerAddress.getHostAddress() != null) {
            String addr = "Group Owner IP - " + info.groupOwnerAddress.getHostAddress();
            view.setText(addr);
        } else {
            CommonMethods.DisplayToast(getActivity(), "Host Address not found");
        }
        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        try {
            String GroupOwner = info.groupOwnerAddress.getHostAddress();
            if (GroupOwner!=null && !GroupOwner.equals("")) {
                SettingsProvider.putString(getActivity(), GROUP_OWNER, GroupOwner);
            }

            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);

            if (info.groupFormed && info.isGroupOwner) {
        	/*
        	 * set shaerdprefrence which remember that device is server.
        	 */
                SettingsProvider.putBoolean(getActivity(),
                        SERVER, true);

            /*new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text))
                    .execute();*/
                FileServerAsyncTask FileServerobj = new FileServerAsyncTask(
                        getActivity(), FileTransferService.PORT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    FileServerobj.executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR,
                            new String[] { null });
                    // FileServerobj.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,Void);
                } else {
                        FileServerobj.execute();
                }
            }
            else  {
                // The other device acts as the client. In this case, we enable the
                // get file button.
//            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
//            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
//                    .getString(R.string.client_text));
                if (!sClientCheck) {
                    firstConnectionMessage firstObj = new firstConnectionMessage(
                            mGroupOwnerAddress);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        firstObj.executeOnExecutor(
                                AsyncTask.THREAD_POOL_EXECUTOR, new String[] { null });
                    } else {
                        firstObj.execute();
                    }
                }

                FileServerAsyncTask FileServerobj = new FileServerAsyncTask(
                        getActivity(), FileTransferService.PORT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    FileServerobj.executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR, new String[] { null });
                } else {
                    FileServerobj.execute();
                }

            }
        } catch(Exception e) {
            // ignore
        }




    }


    /**
     * Updates the UI with device data
     *
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        mDevice = device;
        mContentView.setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText("");
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("");
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText("");
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText("");
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        mContentView.setVisibility(View.GONE);
        /*
         * Remove All the prefrences here
         */
        SettingsProvider.remove(getActivity(), GROUP_OWNER);
        SettingsProvider.remove(getActivity(), SERVER);
        SettingsProvider.remove(getActivity(), CLIENT_IP);
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    static Handler handler;
    public static class FileServerAsyncTask extends AsyncTask<String, String, String> {

        //        private TextView statusText;
        private Context mFilecontext;
        private long ReceivedFileLength;
        private int PORT;

        public FileServerAsyncTask(Context context, int port) {
            this.mFilecontext = context;
//            this.statusText = (TextView) statusText;
            handler = new Handler();
            this.PORT = port;
//			myTask = new FileServerAsyncTask();
            if (mProgressDialog == null)
                mProgressDialog = new ProgressDialog(mFilecontext);
        }


        @Override
        protected String doInBackground(String... params) {
            try {
                CommonMethods.e("File Async task port", "File Async task port-> " + PORT);
                // init handler for progressdialog
                ServerSocket serverSocket = new ServerSocket(PORT);

                Log.d(CommonMethods.Tag, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d("Client's InetAddresssss  ", "" + client.getInetAddress());

                sWiFiClientIp = client.getInetAddress().getHostAddress();

                ObjectInputStream ois = new ObjectInputStream(
                        client.getInputStream());
                WiFiTransferModal obj = null;
                // obj = (WiFiTransferModal) ois.readObject();
                String InetAddress;
                try {
                    obj = (WiFiTransferModal) ois.readObject();
                    InetAddress = obj.getInetAddress();
                    if (InetAddress != null
                            && InetAddress
                            .equalsIgnoreCase(FileTransferService.inetaddress)) {
                        CommonMethods.e("File Async Group Client Ip", "port-> "
                                + sWiFiClientIp);
                        SettingsProvider.putString(mFilecontext, CLIENT_IP, sWiFiClientIp);
                        CommonMethods
                                .e("File Async Group Client Ip from SHAREDPrefrence",
                                        "port-> "
                                                + SettingsProvider.getString(
                                                        mFilecontext, CLIENT_IP, ""));
                        //set boolean true which identifiy that this device will act as server.
                        SettingsProvider.putBoolean(mFilecontext, SERVER, true);
                        ois.close(); // close the ObjectOutputStream object
                        // after saving
                        serverSocket.close();

                        return "Demo";
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                final Runnable r = new Runnable() {

                    public void run() {
                        mProgressDialog.setMessage("Receiving...");
                        mProgressDialog.setIndeterminate(false);
                        mProgressDialog.setMax(100);
                        mProgressDialog.setProgress(0);
                        mProgressDialog.setProgressNumberFormat(null);
//						mProgressDialog.setCancelable(false);
                        mProgressDialog
                                .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        mProgressDialog.show();
                    }
                };
                handler.post(r);

                if (obj == null) return null;
                final File f = new File(
                        Environment.getExternalStorageDirectory() + "/"
                                + sFolderName + "/"
                                + obj.getFileName());

                File dirs = new File(f.getParent());
                if (!dirs.exists()) {
                    if (!dirs.mkdirs()) {
                        Log.e("Unable to create " + dirs.getAbsolutePath());
                    }
                }
                if (f.createNewFile()) {
                    Log.e("Unable to create " + f.getAbsolutePath());
                }

				/*
				 * Recieve file length and copy after it
				 */
                this.ReceivedFileLength = obj.getFileLength();

                InputStream inputstream = client.getInputStream();


                copyRecievedFile(inputstream, new FileOutputStream(f),
                        ReceivedFileLength);
                ois.close(); // close the ObjectOutputStream object after saving
                // file to storage.
                serverSocket.close();

                return f.getAbsolutePath();
            } catch (IOException e) {
                Log.e(e.getMessage());
                return null;
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                if(!result.equalsIgnoreCase("Demo")) {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                    mFilecontext.startActivity(intent);
                } else {
            		/*
					 * To initiate socket again we are intiating async task
					 * in this condition.
					 */
                    FileServerAsyncTask FileServerobj = new
                            FileServerAsyncTask(mFilecontext,FileTransferService.PORT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        FileServerobj.executeOnExecutor(
                                AsyncTask.THREAD_POOL_EXECUTOR, new String[] { null });
                    } else {
                        FileServerobj.execute();
                    }
                }
//                statusText.setText("File copied - " + result);

            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(mFilecontext);
            }
        }

    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        long total = 0;
        byte buf[] = new byte[FileTransferService.ByteSize];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
                try {
                    total += len;
                    if (mFileLength > 0) {
                        sPercentage = (int) ((total * 100) / mFileLength);
                    }
                    mProgressDialog.setProgress(sPercentage);
                } catch (Exception e) {
                    e.printStackTrace();
                    sPercentage = 0;
                    mFileLength = 0;
                }
            }
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }

            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(e.toString());
            return false;
        }
        return true;
    }

    public static boolean copyRecievedFile(InputStream inputStream,
                                           OutputStream out, Long length) {

        byte buf[] = new byte[FileTransferService.ByteSize];
        int len;
        long total = 0;
        int progresspercentage = 0;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                try {
                    out.write(buf, 0, len);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                try {
                    total += len;
                    if (length > 0) {
                        progresspercentage = (int) ((total * 100) / length);
                    }
                    mProgressDialog.setProgress(progresspercentage);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (mProgressDialog != null) {
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                    }
                }
            }
            // dismiss progress after sending
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(e.toString());
            return false;
        }
        return true;
    }

    public void showprogress(final String task) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
        }
        Handler handle = new Handler();
        final Runnable send = new Runnable() {

            public void run() {
                mProgressDialog.setMessage(task);
                // mProgressDialog.setProgressNumberFormat(null);
                // mProgressDialog.setProgressPercentFormat(null);
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setMax(100);
//				mProgressDialog.setCancelable(false);
                mProgressDialog.setProgressNumberFormat(null);
                mProgressDialog
                        .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.show();
            }
        };
        handle.post(send);
    }

    public static void dismissProgressDialog() {
        try {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*
     * Async class that has to be called when connection establish first time. Its main motive is to send blank message
     * to server so that server knows the IP address of client to send files Bi-Directional.
     */
    class firstConnectionMessage extends AsyncTask<String, Void, String> {

        String GroupOwnerAddress = "";

        public firstConnectionMessage(String owner) {
            this.GroupOwnerAddress = owner;
        }

        @Override
        protected String doInBackground(String... params) {
            CommonMethods.e("On first Connect", "On first Connect");

            Intent serviceIntent = new Intent(getActivity(),
                    WiFiClientIPTransferService.class);

            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);

            if (mInfo.groupOwnerAddress.getHostAddress() != null) {
                serviceIntent.putExtra(
                        FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                        mInfo.groupOwnerAddress.getHostAddress());

                serviceIntent.putExtra(
                        FileTransferService.EXTRAS_GROUP_OWNER_PORT,
                        FileTransferService.PORT);
                serviceIntent.putExtra(FileTransferService.inetaddress,
                        FileTransferService.inetaddress);

            }

            getActivity().startService(serviceIntent);

            return "success";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result!=null){
                if(result.equalsIgnoreCase("success")){
                    CommonMethods.e("On first Connect",
                            "On first Connect sent to asynctask");
                    sClientCheck = true;
                }
            }

        }

    }
}