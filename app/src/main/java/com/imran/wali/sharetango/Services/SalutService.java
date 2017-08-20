package com.imran.wali.sharetango.Services;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.bluelinelabs.logansquare.LoganSquare;
import com.imran.wali.sharetango.DashboardActivity;
import com.imran.wali.sharetango.Utility.Base64Utils;
import com.imran.wali.sharetango.audiomanager.MusicData;
import com.imran.wali.sharetango.datarepository.MusicDataRepository;
import com.imran.wali.sharetango.datarepository.Packet;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutDevice;
import com.peak.salut.SalutServiceData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.imran.wali.sharetango.Services.NetworkStatus.CLIENT;
import static com.imran.wali.sharetango.Services.NetworkStatus.DISCOVERING;
import static com.imran.wali.sharetango.Services.NetworkStatus.HOST;
import static com.imran.wali.sharetango.Services.NetworkStatus.NO_CONNECTION;

/**
 * Created by junze on 2017-03-18.
 */

public class SalutService extends Service implements SalutDataCallback {

    public static final int PORT_NUMBER = 4321;
    public static final int TIME_OUT = 7000; // 7 seconds

    private static final String TAG = "SalutService";

    public class SalutBinder extends Binder {
        public Service getService() {
            return SalutService.this;
        }
    }

    IBinder salutBinder = new SalutBinder();

    public SalutDataReceiver salutDataReceiver;
    public SalutServiceData serviceData;
    public Salut network;

    Activity mActivity; // the activity the service is bound to

    public void setBoundActivity(Activity activity) {
        this.mActivity = activity;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return salutBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        /* Salut Init */
        salutDataReceiver = new SalutDataReceiver(mActivity, this);

        serviceData = new SalutServiceData("TestService", PORT_NUMBER, "HostDevice");

        network = new Salut(salutDataReceiver, serviceData, new SalutCallback() {
            @Override
            public void call() {
                Log.d(TAG, "YOUR DEVICE SUCKS");
                Log.d(TAG, "Device not supported");
            }
        });
         /* Discover hosts*/
//        isHostServiceAvailable();
        return super.onStartCommand(intent, flags, startId);
    }

    // map songs to device names
    private Map<MusicData, String> map = new HashMap<>();

    @Override
    public void onDataReceived(Object data) {
        Log.d(TAG, "Received network data.");
        Log.d(TAG, data.toString());
        try {
            Packet pkt = LoganSquare.parse((String) data, Packet.class);
            // discard packet if destination device name doesn't match
//            if (pkt.getDstDevice().deviceName.equals(network.thisDevice.deviceName)) {
                switch (pkt.getTransactionType()) {
                    case REQUEST_SONG:  // got request for actual song
                        onReceiveRequestForSong(pkt);
                        break;
                    case SEND_SONG:  // received new actual playable song
                        onSongReceived(pkt);
                        break;
                    case SEND_SONG_LIST:
                        // new available song information
                        onNewSongListReceived(pkt);
                        break;
                    case DISCONNECT:
                        onClientDisconnect(pkt);
                    default:
                        break;
                }
//            }
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e(TAG, "Failed to parse network data.");
        }
    }

    /**
     * A client has left the network
     * */
    private void onClientDisconnect(Packet pkt) {
        if (!network.isRunningAsHost) return; // must be host
        SalutDevice client = pkt.getSrcDevice();
        // remove songs from map owned by client
        for(Iterator<Map.Entry<MusicData, String>> it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<MusicData, String> entry = it.next();
            if(entry.getValue().equals(client.deviceName)) {
                it.remove();
            }
        }
        broadcastAvailableSongs();
        // update host's available song list
        List<MusicData> available = MusicDataRepository.getInstance().getAvailableSongs();
        List<MusicData> updatedAvailableSongs = new ArrayList<>();
        for (MusicData song : available) {
            if (map.containsKey(song)) { // someonw owns the song
                updatedAvailableSongs.add(song);
            }
        }
        MusicDataRepository.getInstance().clearAvailableSongs();
        MusicDataRepository.getInstance().addAllAvailableMusicData(updatedAvailableSongs);
    }

    private void broadcastAvailableSongs() {
        Packet packet = new Packet();
        packet.setTransactionType(Packet.MessageType.SEND_SONG_LIST);
        packet.setSrcDevice(network.thisDevice);
        List<MusicData> availableSongs = new ArrayList<>();
        availableSongs.addAll(map.keySet());
        packet.setSongList(availableSongs);
        // send all available songs information from host to all devices
        network.sendToAllDevices(packet, new SalutCallback() {
            @Override
            public void call() {
                Log.e(TAG, "sending song list to ALL devices failed");
            }
        });
    }

    /**
     * An actual song has been received
     */
    private void onSongReceived(Packet pkt) {
        Log.d(TAG, "onSongReceived");
        MusicData song = pkt.getmMusicData();
        Log.d(TAG, song.toString());
        Toast.makeText(getApplicationContext(), "Received "+song, Toast.LENGTH_SHORT).show();
        String base64 = pkt.getBase64string();
        try {
            // TODO: should be stored in a specific folder and deleted later
            // String secondary_storage = System.getenv("SECONDARY_STORAGE");
            String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/ShareTango/";
            Log.d(TAG, path);
            String songName = song.getTitle() + ".mp3";
            Base64Utils.decode(base64, path, songName);
            Toast.makeText(getApplicationContext(), song+" saved", Toast.LENGTH_SHORT).show();

            // Tell the media scanner about the new file so that it is immediately available to the user.
            MediaScannerConnection.scanFile(this,
                    new String[] { path+songName}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                            MusicDataRepository.getInstance().refreshList();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * handler when song owner receives a request for a song
     */
    private void onReceiveRequestForSong(Packet pkt) throws IOException {
        Log.d(TAG, "onReceiveRequestForSong");
        LogPacket(pkt);

        if (network.isRunningAsHost) {
            Log.d(TAG, "Registered clients:" + network.registeredClients.toString());
            MusicData songToRequest = pkt.getMusicData();
            String songOwnerDeviceName = map.get(songToRequest);
            if (songOwnerDeviceName == null) {
                Log.e(TAG, "Request failed");
                return;
            }
            if (!songOwnerDeviceName.equals(network.thisDevice.deviceName)) {
                // host is not the song owner then forward request
                SalutDevice songOwnerDevice = getDeviceFromName(songOwnerDeviceName);
                pkt.setDstDevice(songOwnerDevice);
                fixServiceAddress(pkt.getSrcDevice());
                network.sendToDevice(songOwnerDevice, pkt, new SalutCallback() {
                    @Override
                    public void call() {
                        Log.e(TAG, "Forward request failed");
                    }
                });
                return;
            }
        }
        // otherwise this is the song owner who received the request for a song
        MusicData songToRequest = pkt.getMusicData();
        Log.d(TAG, songToRequest.toString());

        Uri uri = Uri.parse("content://media/external/audio/media/" + songToRequest.getId());
        File file;
        try {
            file = new File(getRealPathFromURI(mActivity, uri));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Request failed");
            return;
        }
        String encoded = Base64Utils.toBase64(file);
        SalutDevice clientDevice = pkt.getSrcDevice();

        Log.d(TAG, "client device " + clientDevice);
        fixServiceAddress(clientDevice);

        Packet response = new Packet();
        response.setMusicData(songToRequest);
        response.setTransactionType(Packet.MessageType.SEND_SONG);
        response.setBase64string(encoded);
        response.setSrcDevice(network.thisDevice);
//        Log.e(TAG, "encoded="+encoded);
        Log.e(TAG, "after set base 64 string, sending to Device");
        Log.e(TAG, "serviceName= "+clientDevice.serviceName);
        // TODO get rid of deviceName
        if (!network.isRunningAsHost && clientDevice.deviceName.equals(network.registeredHost.deviceName)) {
            response.setDstDevice(network.registeredHost);
            // host does not have serviceAddress (is null)
            network.sendToHost(response, new SalutCallback() {
                @Override
                public void call() {
                    Log.d(TAG, "send song to host failed");
                }
            });
        } else {
            response.setDstDevice(clientDevice);
            network.sendToDevice(clientDevice, response, new SalutCallback() {
                        @Override
                        public void call() {
                            Log.d(TAG, "send song to device failed");
                        }
                    }
            );
        }
    }

    private void LogPacket(Packet pkt) {
        Log.d(TAG, "Packet information:");
        if (pkt.getSrcDevice() != null) {
            Log.d(TAG, "    Src device                = "+pkt.getSrcDevice().deviceName);
            Log.d(TAG, "    Src device serviceAddress = "+pkt.getSrcDevice().serviceAddress);
        }
        if (pkt.getDstDevice() != null) {
            Log.d(TAG, "    Dst device                = "+pkt.getDstDevice().deviceName);
            Log.d(TAG, "    Dst device serviceAddress = "+pkt.getDstDevice().serviceAddress);
        }
        Log.d(TAG, "    Transaction type           =  " + pkt.getTransactionType());
    }

    private void fixServiceAddress(SalutDevice clientDevice) {
        if (clientDevice.serviceAddress == null) {
            if (network.isRunningAsHost) {
                for (SalutDevice d: network.registeredClients) {
                    if (clientDevice.deviceName.equals(d.deviceName)) {
                        clientDevice.serviceAddress = d.serviceAddress;
                        break;
                    }
                }
            } else {
                for (SalutDevice d: network.foundDevices) {
                    if (clientDevice.deviceName.equals(d.deviceName)) {
                        clientDevice.serviceAddress = d.serviceAddress;
                        break;
                    }
                }

            }
        }
    }

    /**
     * handler when a new list of MusicData is received
     */
    private void onNewSongListReceived(Packet pkt) {
        Log.d(TAG, "onNewSongListReceived");
        LogPacket(pkt);
        List<MusicData> receivedSongList = pkt.getSongList();
        if (network.isRunningAsHost) {
            // Add local songs to map
            for (MusicData s : MusicDataRepository.getInstance().getLocalSongList()) {
                if (!map.containsKey(s)) {
                    map.put(s, network.thisDevice.deviceName);
                }
            }
            // Add new songs to map
            List<MusicData> localSongs = MusicDataRepository.getInstance().getLocalSongList();
            String srcDeviceName = pkt.getSrcDevice().deviceName;
            for (MusicData s : receivedSongList) {
                if (!localSongs.contains(s)) {
                    MusicDataRepository.getInstance().addAvailableMusicData(s);
                }
                if (!map.containsKey(s)) {
                    map.put(s, srcDeviceName); // store songs' owners info
                }
            }
            broadcastAvailableSongs();
        } else {
            // if client receives a song list, just update listed available songs
            List<MusicData> localSongs = MusicDataRepository.getInstance().getLocalSongList();
            for (MusicData s : receivedSongList) {
                if (!localSongs.contains(s)) { // do not display local songs in available fragment
                    MusicDataRepository.getInstance().addAvailableMusicData(s);
                }
            }
        }
    }

    public void setupNetwork() {
        Log.d(TAG, "setting up network...");
        if (!network.isRunningAsHost) {
            // stop host
            forceStopNetwork();
            network.startNetworkService(new SalutDeviceCallback() {
                @Override
                public void call(SalutDevice salutDevice) {
                    Log.d(TAG, "Host: " + salutDevice.deviceName + " is connected");
                    Log.e(TAG, network.thisDevice.toString());
                    Toast.makeText(getApplicationContext(), "Device: " + salutDevice.deviceName + " connected.", Toast.LENGTH_SHORT).show();
                }
            }, new SalutCallback() /* on success */ {
                @Override
                public void call() {
                    notifyActivityNetworkStatusChanged();
                    Toast.makeText(getApplicationContext(), "Network created", Toast.LENGTH_SHORT).show();
                }
            }, new SalutCallback() /* on failure*/ {
                @Override
                public void call() {
                    notifyActivityNetworkStatusChanged();
                    Toast.makeText(getApplicationContext(), "Failed to create network", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            network.stopNetworkService(false);
        }
        notifyActivityNetworkStatusChanged();
    }

    public void isHostServiceAvailable() {
        Log.d(TAG, "Is Host Service Available?");
        Toast.makeText(getApplicationContext(), "Discovering networks...", Toast.LENGTH_SHORT).show();
        if (!network.isRunningAsHost && !network.isDiscovering) {
            SalutCallback ifHostIsFound = new SalutCallback() {
                @Override
                public void call() {
                    notifyActivityNetworkStatusChanged();
                    Log.d(TAG, "Found Host, Make Connection with Host.");
                    // DEVICE MAINTAINABLE AREA
                    Toast.makeText(getApplicationContext(),
                            "Found host " + network.foundDevices.get(0).deviceName,
                            Toast.LENGTH_SHORT).show();
                    SalutCallback onRegisterSuccess = new SalutCallback() {
                        @Override
                        public void call() {
                            notifyActivityNetworkStatusChanged();
                            Log.d(TAG, "REGISTER SUCCESS... SENDING song list to host");
                            Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                            // TODO send message
                            /** send client's local song list to the host */
                            sendSongListToHost();
                        }
                    };

                    SalutCallback onRegisterFaliure = new SalutCallback() {
                        @Override
                        public void call() {
                            notifyActivityNetworkStatusChanged();
                            Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "WE FUCKED UP");
                        }
                    };
                    network.registerWithHost(network.foundDevices.get(0), onRegisterSuccess, onRegisterFaliure);
                }
            };
            SalutCallback ifHostIsNotFound = new SalutCallback() {
                @Override
                public void call() {
                    notifyActivityNetworkStatusChanged();
                    Log.d(TAG, "Host not found, starting service");
                    Toast.makeText(getApplicationContext(), "No host found, setting up network...", Toast.LENGTH_SHORT).show();
                    setupNetwork();
                }
            };

            network.discoverWithTimeout(ifHostIsFound, ifHostIsNotFound, TIME_OUT);
        } else {
            network.stopServiceDiscovery(true);
        }
        notifyActivityNetworkStatusChanged();
    }

    private void sendSongListToHost() {
        List<MusicData> localSongList = MusicDataRepository.getInstance().getLocalSongList();
        Log.d(TAG, localSongList.toString());
        Packet pkt = new Packet();
//        // put a 'stamp' on the song title
//        for (MusicData data : localSongList) {
//            if (!isStamped(data))
//                stamp(data);
//        }
        pkt.setSongList(localSongList);
        pkt.setDstDevice(network.registeredHost);
        pkt.setSrcDevice(network.thisDevice);
        pkt.setTransactionType(Packet.MessageType.SEND_SONG_LIST); // offering song list
        network.sendToHost(pkt, new SalutCallback() {
            @Override
            public void call() {
                Log.e("SHARETANGO", "Oh no! The data failed to send.");
            }
        });
    }

//    private final String STAMP = "- By ShareTango";
//    private void stamp(MusicData data) {
//        data.title += STAMP;
//    }
//    private boolean isStamped(MusicData data) {
//        return data.title.endsWith(STAMP);
//    }
//    private String unstamp(MusicData data) {
//        String songTitleWithNoStamp = data.getTitle();
//        if (isStamped(data)) {
//            songTitleWithNoStamp = data.title.replace(STAMP, "");
//        }
//        return songTitleWithNoStamp;
//    }

    // Communicate to activity through this interface
    public interface ISalutCallback {
        void update(NetworkStatus networkStatus);
    }

    public void request(MusicData song) {
        Toast.makeText(getApplicationContext(), "Downloading...", Toast.LENGTH_LONG).show();
        if (network.isRunningAsHost) {
            SalutDevice songOwner = getDeviceFromName(map.get(song));
            if (songOwner == null) {
                Toast.makeText(getApplicationContext(), "Request Failed", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d(TAG, "songOwner of "+song.toString() +" is " + songOwner.toString());
            Packet packet = new Packet();
            packet.setTransactionType(Packet.MessageType.REQUEST_SONG); // indicate request for actual song
            packet.setMusicData(song);// the song to request
            packet.setSrcDevice(network.thisDevice);
            packet.setDstDevice(songOwner);
            Log.d("SalutService", "requesting" + song.toString());
            network.sendToDevice(songOwner, packet, new SalutCallback() {
                @Override
                public void call() {
                    Log.d(TAG, "sending request to device failed");
                    Toast.makeText(getApplicationContext(), "Request song failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // let host resolve the request
            Packet pkt = new Packet();
            pkt.setDstDevice(network.registeredHost);
            pkt.setSrcDevice(network.thisDevice);
            pkt.setTransactionType(Packet.MessageType.REQUEST_SONG);
            pkt.setMusicData(song); // the song to request
            network.sendToHost(pkt, new SalutCallback() {
                @Override
                public void call() {
                    Log.d(TAG, "request song failed");
                }
            });
        }
    }

    // helper to return the device with a specific device name
    private SalutDevice getDeviceFromName(String destDeviceName) {
        SalutDevice destinationDevice = null;
        ArrayList<SalutDevice> devices;
        if (network.isRunningAsHost) {
            devices = network.registeredClients;
            devices.add(network.thisDevice);
        } else {
            devices = network.foundDevices;
        }
        for (SalutDevice device : devices) {
            if (device.deviceName.equals(destDeviceName)) {
                destinationDevice = device;
                break;
            }
        }
        return destinationDevice;
    }


    // get absolute file path
    // https://stackoverflow.com/questions/3401579/get-filename-and-path-from-uri-from-mediastore
    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void stopNetwork() {
        if (network.isRunningAsHost) {
            Toast.makeText(getApplicationContext(), "Stopping host...", Toast.LENGTH_SHORT).show();
            network.stopNetworkService(false, new SalutCallback() {
                @Override
                public void call() {
                    Toast.makeText(getApplicationContext(), "Host stopped", Toast.LENGTH_SHORT).show();
                    notifyActivityNetworkStatusChanged(NO_CONNECTION);
                }
            }, new SalutCallback() {
                @Override
                public void call() {
                    Toast.makeText(getApplicationContext(), "Failed to stop host; Force stopping...", Toast.LENGTH_SHORT).show();
                    forceStopNetwork();
                    notifyActivityNetworkStatusChanged();
                }
            });
        } else { // Client or Not connected
            if (network.isConnectedToAnotherDevice) {
                // send DISCONNECT signal to host
                Packet pkt = new Packet();
                pkt.setSrcDevice(network.thisDevice);
                pkt.setDstDevice(network.registeredHost);
                pkt.setTransactionType(Packet.MessageType.DISCONNECT);
                network.sendToHost(pkt, new SalutCallback() {
                    @Override
                    public void call() {
                        Log.e(TAG, "Client Disconnect signal failed to send");
                    }
                });
                Toast.makeText(getApplicationContext(), "Leaving Network...", Toast.LENGTH_SHORT).show();
                network.unregisterClient(new SalutCallback() {
                    @Override
                    public void call() {
                        Toast.makeText(getApplicationContext(), "Left Network", Toast.LENGTH_SHORT).show();
                        notifyActivityNetworkStatusChanged(NO_CONNECTION);
                    }
                }, new SalutCallback() {
                    @Override
                    public void call() {
                        Toast.makeText(getApplicationContext(), "Failed to disconnect; Force stopping...", Toast.LENGTH_SHORT).show();
                        forceStopNetwork();
                        notifyActivityNetworkStatusChanged();
                    }
                }, false);
            }
        }
    }

    public List<SalutDevice> getConnectedDivices() {
        List<SalutDevice> result = null;
        if (isRunningAsHost()) {
            result = network.registeredClients;
        }
        return result;
    }

    public boolean isRunningAsHost() {
        if (network != null) {
            return network.isRunningAsHost;
        } else {
            return false;
        }
    }

    public NetworkStatus networkStatus() {
        if (network.isDiscovering) {
            return DISCOVERING;
        }
        if (network.isRunningAsHost) {
            return HOST;
        } else if (network.isConnectedToAnotherDevice){
            return CLIENT;
        }
        return NO_CONNECTION;
    }

    private void notifyActivityNetworkStatusChanged() {
        ((DashboardActivity)mActivity).update(networkStatus());
    }

    private void notifyActivityNetworkStatusChanged(NetworkStatus status) {
        ((DashboardActivity)mActivity).update(status);
    }


    public void forceStopNetwork() {
        Log.d(TAG, "forceStopNetwork");
        network.disconnectFromDevice();
        network.forceDisconnect();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SalutService onDestroy");
        stopNetwork();
    }
}
