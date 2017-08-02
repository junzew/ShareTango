package com.imran.wali.sharetango.Services;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.bluelinelabs.logansquare.LoganSquare;
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
import java.util.List;
import java.util.Map;

/**
 * Created by junze on 2017-03-18.
 */

public class SalutService extends Service implements SalutDataCallback {

    public static final int PORT_NUMBER = 4321;

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
        isHostServiceAvailable();
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
            if (pkt.getDestinationDviceName().equals(network.thisDevice.deviceName)) {
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
                    default:
                        break;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e(TAG, "Failed to parse network data.");
        }
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
            String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/ShareTango";
            Log.d(TAG, path);
            Base64Utils.decode(base64, path, song.getTitle()+".mp3");
            Toast.makeText(getApplicationContext(), song+" saved", Toast.LENGTH_SHORT).show();
            MusicDataRepository.getInstance().refreshList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * handler when song owner receives a request for a song
     */
    private void onReceiveRequestForSong(Packet pkt) throws IOException {
        Log.d(TAG, "onReceiveRequestForSong");
        if (network.isRunningAsHost) {
            // forward request
            request(pkt.getMusicData(), pkt.getSourceDviceName());
            return;
        }
        // otherwise this is the song owner who received the request for a song
        MusicData songToRequest = pkt.getMusicData();
        // TODO find songToRequest locally and encode to Base 64
        // use Cursor/ContentResolver to get the song
        Log.d(TAG, songToRequest.toString());
        Log.d(TAG, pkt.getDestinationDviceName());
        Uri uri = Uri.parse("content://media/external/audio/media/" + songToRequest.getId());
        File file = new File(getRealPathFromURI(mActivity, uri));
        String encoded = Base64Utils.toBase64(file);
        String client = pkt.getSourceDviceName();
        //SalutDevice clientDevice = findDestinationDevice(client);
        SalutDevice clientDevice = pkt.getSrcDevice();

        Log.d(TAG, "client device " + clientDevice);
        Packet response = new Packet();
        response.setMusicData(songToRequest);
        response.setTransactionType(Packet.MessageType.SEND_SONG);
        response.setBase64string(encoded);
        response.setSrcDevice(network.thisDevice);
        response.setDestinationDviceName(clientDevice.deviceName);
        response.setSourceDviceName(network.thisDevice.deviceName);
//        Log.e(TAG, "encoded="+encoded);
        Log.e(TAG, "after set base 64 string, sending to Device");
        Log.e(TAG, "serviceName= "+clientDevice.serviceName);
        // TODO get rid of deviceName
        if (clientDevice.deviceName.equals(network.registeredHost.deviceName)) {
            response.setDstDevice(network.registeredHost);
            // host does not have serviceAddress (is null)
            network.sendToHost(response, new SalutCallback() {
                @Override
                public void call() {
                    Log.d(TAG, "send song to host failed");
                    Toast.makeText(getApplicationContext(), "Send song failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            response.setDstDevice(clientDevice);
            network.sendToDevice(clientDevice, response, new SalutCallback() {
                        @Override
                        public void call() {
                            Log.d(TAG, "send song to device failed");
                            Toast.makeText(getApplicationContext(), "Send song failed", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
    }

    /**
     * handler when a new list of MusicData is received
     */
    private void onNewSongListReceived(Packet pkt) {
        Log.d(TAG, "onNewSongListReceived");
        List<MusicData> songList = pkt.getSongList();
        if (network.isRunningAsHost) {
            for (MusicData s : songList) {
                MusicDataRepository.getInstance().addAvailableMusicData(s);
                map.put(s, pkt.getSourceDviceName()); // store songs' owners info
            }
            SalutDevice destinationDevice = findDestinationDevice(pkt.getSourceDviceName());
            Packet packet = new Packet();
            packet.setTransactionType(Packet.MessageType.SEND_SONG_LIST);
            packet.setSourceDviceName(network.thisDevice.deviceName);
            packet.setDestinationDviceName(pkt.getSourceDviceName());
            List<MusicData> availableSongs = new ArrayList<>();
            availableSongs.addAll(map.keySet());
            packet.setSongList(availableSongs);
            // send all available songs information from host to client
            network.sendToDevice(destinationDevice, packet, new SalutCallback() {
                @Override
                public void call() {
                    Log.d(TAG, "sending song list to device failed");
                }
            });
        } else {
            // if client receives a song list, just update listed available songs
            for (MusicData s : songList) {
                MusicDataRepository.getInstance().addAvailableMusicData(s);
            }
        }
    }

    public void setupNetwork() {
        Log.d(TAG, "setting up network...");
        if (!network.isRunningAsHost) {
            network.startNetworkService(new SalutDeviceCallback() {
                @Override
                public void call(SalutDevice salutDevice) {
                    Log.d(TAG, "Host: "+salutDevice.deviceName+" is connected");
                    Log.e(TAG, network.thisDevice.toString());
                    Toast.makeText(getApplicationContext(), "Device: " + salutDevice.instanceName + " connected.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            network.stopNetworkService(false);
        }
    }

    public void isHostServiceAvailable() {
        Log.d(TAG, "Is Host Service Available?");
        Toast.makeText(getApplicationContext(), "Discovering networks...", Toast.LENGTH_SHORT).show();
        if (!network.isRunningAsHost && !network.isDiscovering) {
            SalutCallback ifHostIsFound = new SalutCallback() {
                @Override
                public void call() {
                    Log.d(TAG, "Make Connection with host cos you found HOST.");
                    // DEVICE MAINTAINABLE AREA
                    Toast.makeText(getApplicationContext(),
                            "Found host " + network.foundDevices.get(0).deviceName,
                            Toast.LENGTH_SHORT).show();
                    SalutCallback onRegisterSuccess = new SalutCallback() {
                        @Override
                        public void call() {
                            Log.d(TAG, "REGISTER SUCCESSS... SENDING song list to host");
                            Toast.makeText(getApplicationContext(), "Registration success", Toast.LENGTH_SHORT).show();
                            // TODO send message
//                            Message message = new Message();
//                            message.lol = "Wali";
                            /** send client's local song list to the host */
                            List<MusicData> localSongList = MusicDataRepository.getInstance().getList();
                            Log.d(TAG, localSongList.toString());
                            Packet pkt = new Packet();
                            // put a 'stamp' on the song title
                            for (MusicData data : localSongList) {
                                data.title += "- By ShareTango";
                            }
                            pkt.setSongList(localSongList);
                            pkt.setDstDevice(network.registeredHost);
                            pkt.setSrcDevice(network.thisDevice);
                            pkt.setDestinationDviceName(network.registeredHost.deviceName);
                            pkt.setSourceDviceName(network.thisDevice.deviceName);
                            pkt.setTransactionType(Packet.MessageType.SEND_SONG_LIST); // offering song list
                            network.sendToHost(pkt, new SalutCallback() {
                                @Override
                                public void call() {
                                    Log.e("SHARETANGO", "Oh no! The data failed to send.");
                                }
                            });
                        }
                    };

                    SalutCallback onRegisterFaliure = new SalutCallback() {
                        @Override
                        public void call() {
                            Toast.makeText(getApplicationContext(), "Registration failed", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "WE FUCKED UP");
                        }
                    };
                    network.registerWithHost(network.foundDevices.get(0), onRegisterSuccess, onRegisterFaliure);
                }
            };
            SalutCallback ifHostIsNotFound = new SalutCallback() {
                @Override
                public void call() {
                    Log.d(TAG, "Host not found, starting service");
                    Toast.makeText(getApplicationContext(), "No host found, setting up network...", Toast.LENGTH_SHORT).show();
                    setupNetwork();
                }
            };

            network.discoverWithTimeout(ifHostIsFound, ifHostIsNotFound, 7000);
        } else {
            network.stopServiceDiscovery(true);
        }
    }

    // Communicate to activity through this interface
    public interface ISalutCallback {
        void updateClient();
    }

    public void request(MusicData song) {
        request(song, null);
    }
    /**
     * Request a song
     */
    public void request(MusicData song, String fromDeviceName) {
        Log.d(TAG, "request a song");
        if (network.isRunningAsHost) {
            SalutDevice songOwner = findDestinationDevice(map.get(song));
            Log.d(TAG, "songOwner of "+song.toString() +" is " + songOwner.toString());
            Packet packet = new Packet();
            packet.setTransactionType(Packet.MessageType.REQUEST_SONG); // indicate request for actual song
            packet.setMusicData(song);// the song to request
            if (fromDeviceName == null) {
                packet.setSourceDviceName(network.thisDevice.deviceName);
                packet.setSrcDevice(network.thisDevice);
            } else {
                packet.setSourceDviceName(fromDeviceName);
                packet.setSrcDevice(findDestinationDevice(fromDeviceName));
            }
            packet.setDestinationDviceName(map.get(song));
            packet.setDstDevice(songOwner);
            Log.d("SalutService", "requesting" + song.toString());

            network.sendToDevice(songOwner, packet, new SalutCallback() {
                @Override
                public void call() {
                    Log.d(TAG, "sending request to device failed");
                }
            });
        } else {
            // let host resolve the request
            Packet pkt = new Packet();
            pkt.setDstDevice(network.registeredHost);
            pkt.setSrcDevice(network.thisDevice);
            pkt.setDestinationDviceName(network.registeredHost.deviceName);
            pkt.setSourceDviceName(network.thisDevice.deviceName);
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
    private SalutDevice findDestinationDevice(String destDeviceName) {
        SalutDevice destinationDevice = null;
        ArrayList<SalutDevice> devices;
        if (network.isRunningAsHost) {
            devices = network.registeredClients;
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SalutService onDestroy");
        if (network.isRunningAsHost)
            network.stopNetworkService(true);
        else
            network.unregisterClient(true);

    }
}
