package pt.ist.cmu.wifip2p;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.provider.SyncStateContract;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.widget.TextView;

import com.orhanobut.hawk.Hawk;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import okio.Utf8;
import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.Channel;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.PeerListListener;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.GroupInfoListener;
import pt.ist.cmu.helpers.Constants;
import pt.ist.cmu.models.User;

public class

P2PConnectionManager implements PeerListListener, GroupInfoListener {

    private static HashMap<String, String> ipTable = new HashMap<String, String>();
    private static SimWifiP2pSocket CliSocket = null;
    private static SimWifiP2pDeviceList peerList;
    private static boolean finished;
    private static Context context;



    private static SimWifiP2pManager mManager = null;
    private static SimWifiP2pManager.Channel mChannel = null;
    private static Messenger mService = null;
    private static P2PBroadcastReceiver mReceiver;


    public P2PConnectionManager(){
    }

    public static void init(Context context_arg){

        context = context_arg;

        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        mReceiver = new P2PBroadcastReceiver();
        context.registerReceiver(mReceiver, filter);

        //initialize wifi direct
        Intent intent = new Intent(context, SimWifiP2pService.class);
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        // initialize the WDSim API
       SimWifiP2pSocketManager.Init(context);

       new IncomingCommTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void destroy(Context context){
        context.unregisterReceiver(mReceiver);
    }

    private static ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Looper mainLooper = Looper.getMainLooper(); //wtf is a looper

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


    public static HashMap  getIPs (){

        ipTable.clear(); //reset IP Table

        for( SimWifiP2pDevice device : peerList.getDeviceList()){  //for each device call SendCatalog
            new getUserNamesTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR,
                    device.getVirtIp());
        }

        while (!finished){
        }

        finished = false;
        return ipTable;
    }


    public static class getUserNamesTask extends AsyncTask<String, Void, String> { //get all usernames and associate to IP addresses

        @Override
        protected String doInBackground(String... params) {
            byte message [] = ("GET_USERNAME \n").getBytes();

            try {
                SimWifiP2pSocket CliSocket = new SimWifiP2pSocket(params[0],
                        Constants.PORT_NUMBER);

                CliSocket.getOutputStream().write(message);

                InputStream inputStream = CliSocket.getInputStream();

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
            finished = true;
        }


    }

    public static void getImages (String uri, String ip){

            new getPicturesTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR,
                    uri,
                    ip);
    }

    public static class getPicturesTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {

            byte message [] = ("GET CATALOG \n" + params[0] + "\n").getBytes(Charset.forName("UTF-8"));

            try {
                SimWifiP2pSocket CliSocket = new SimWifiP2pSocket(params[1],
                                Constants.PORT_NUMBER);

                CliSocket.getOutputStream().write(message);


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

    public static class IncomingCommTask extends AsyncTask<Void, String, Void> {


        @Override
        protected Void doInBackground(Void... params) {       //TODO implement two operations: return username and return pictures


            try {
                    SimWifiP2pSocketServer  mSrvSocket = new SimWifiP2pSocketServer(
                                        Constants.PORT_NUMBER);

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        SimWifiP2pSocket sock = mSrvSocket.accept();
                        try {
                            BufferedReader sockIn = new BufferedReader(
                                    new InputStreamReader(sock.getInputStream()));
                            String st = sockIn.readLine();

                            if(st == "GET_USERNAME"){
                                //TODO get username and send it

                               User user = Hawk.get(Constants.CURRENT_USER_KEY);
                               String username = user.getUsername();

                               sock.getOutputStream().write(username.getBytes(Charset.forName("UTF-8")));

                            }

                            else if ( st == "GET CATALOG" ){

                                String catalogURI = sockIn.readLine();
                                //handle picture request
                            }

                        } catch (IOException e) {
                            Log.d("Error reading socket:", e.getMessage());
                        } finally {
                            sock.close();
                        }
                    } catch (IOException e) {
                        Log.d("Error socket:", e.getMessage());
                        break;
                        //e.printStackTrace();
                    }
                }

            } catch (IOException e) {
                Log.d("Error creating socket:", e.getMessage());
            }
            return null;

        }
    }
    /*
     * Listeners associated to Termite
     */


    public void updateGroupInfo(){
        mManager.requestGroupInfo(mChannel, this);
    }


    //TODO requestPeers to get this callback

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList peers) {

       // peerList = peers; //update peer list

    }

    //TODO requestGroupInfo to get this callback

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices,
                                     SimWifiP2pInfo groupInfo) {

         peerList = devices; //update peer list
    }
}
