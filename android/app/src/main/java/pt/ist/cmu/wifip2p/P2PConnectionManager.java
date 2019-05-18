package pt.ist.cmu.wifip2p;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.util.Log;

import com.orhanobut.hawk.Hawk;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.GroupInfoListener;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.PeerListListener;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ist.cmu.helpers.Constants;
import pt.ist.cmu.models.User;

public class P2PConnectionManager implements PeerListListener, GroupInfoListener {

    private static P2PConnectionManager instance;

    private HashMap<String, String> ipTable = new HashMap<>();
    private SimWifiP2pDeviceList peerList;
    private Context context;

    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    private P2PBroadcastReceiver mReceiver;

    private P2PConnectionManager() {
    }

    // singleton
    public static P2PConnectionManager getInstance() {
        if (instance == null) {
            instance = new P2PConnectionManager();
        }
        return instance;
    }

    public void init(Context context_arg) {
        context = context_arg;

        // initialize the WDSim API
        SimWifiP2pSocketManager.Init(context);

        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);

        mReceiver = new P2PBroadcastReceiver();
        context.registerReceiver(mReceiver, filter);

        // initialize wi-fi direct
        Intent intent = new Intent(context, SimWifiP2pService.class);
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        new IncomingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void destroy() {
        instance = null;
    }

    public HashMap<String, String> getIpTable() {
        return ipTable;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Looper mainLooper = Looper.getMainLooper();

            mService = new Messenger(service);
            mManager = new SimWifiP2pManager(mService);
            mChannel = mManager.initialize(context, mainLooper, null);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mManager = null;
            mChannel = null;
        }
    };


    public void findIPs() {
        // reset IP table
        ipTable.clear();

        mManager.requestGroupInfo(mChannel, this);

        // for each device call SendCatalog
        for (SimWifiP2pDevice device : peerList.getDeviceList()) {
            new getUsernamesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, device.getVirtIp());
        }
    }

    // get all usernames and associate to IP addresses
    public class getUsernamesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            byte message[] = ("USERNAME\n").getBytes();

            try {
                SimWifiP2pSocket clientSocket = new SimWifiP2pSocket(params[0], Constants.PORT_NUMBER);
                clientSocket.getOutputStream().write(message);

                InputStream inputStream = clientSocket.getInputStream();

                StringWriter writer = new StringWriter();
                IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8.name());
                String username = writer.toString();

                ipTable.put(username, params[0]);

            } catch (UnknownHostException e) {
                return "Unknown Host:" + e.getMessage();
            } catch (IOException e) {
                return "IO error:" + e.getMessage();
            }
            return null;

        }

        @Override
        protected void onPostExecute(String result) {
        }
    }

    public void getImages(String uri, String ip) {
        new getPicturesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uri, ip);
    }

    public class getPicturesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            byte message[] = ("CATALOG\n" + params[0] + "\n").getBytes(Charset.forName("UTF-8"));

            try {
                // write to client socket
                SimWifiP2pSocket clientSocket = new SimWifiP2pSocket(params[1], Constants.PORT_NUMBER);
                clientSocket.getOutputStream().write(message);

                // receive pictures
                // save them

            } catch (UnknownHostException e) {
                return "Unknown Host:" + e.getMessage();
            } catch (IOException e) {
                return "IO error:" + e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    public class IncomingCommTask extends AsyncTask<Void, String, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                SimWifiP2pSocketServer mSrvSocket = new SimWifiP2pSocketServer(Constants.PORT_NUMBER);

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        SimWifiP2pSocket sock = mSrvSocket.accept();
                        try {
                            BufferedReader sockIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                            String str = sockIn.readLine();

                            if (str.equals("USERNAME")) {
                                // get username and send it

                                User user = Hawk.get(Constants.CURRENT_USER_KEY);
                                String username = user.getUsername();

                                sock.getOutputStream().write(username.getBytes(Charset.forName("UTF-8")));

                            } else if (str.equals("CATALOG")) {
                                String catalogURI = sockIn.readLine();
                                //handle picture request
                            }

                        } catch (IOException e) {
                            System.out.println("Error reading socket: " + e.getMessage());
                        } finally {
                            sock.close();
                        }
                    } catch (IOException e) {
                        System.out.println("Error socket: " + e.getMessage());
                        break;
                    }
                }

            } catch (IOException e) {
                System.out.println("Error creating socket: " + e.getMessage());
            }

            return null;
        }
    }

    /*
     * Listeners associated to Termite
     */

    public void updateGroupInfo() {
        mManager.requestGroupInfo(mChannel, this);
    }

    public void updatePeers() {
        mManager.requestPeers(mChannel, this);
    }

    // requestPeers callback
    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList peers) {
        peerList = peers;
    }

    // requestGroupInfo callback
    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices, SimWifiP2pInfo groupInfo) {
        peerList = devices;
    }
}
