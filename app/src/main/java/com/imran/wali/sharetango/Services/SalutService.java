package com.imran.wali.sharetango.Services;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
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
        /* Salut Init */
        salutDataReceiver = new SalutDataReceiver(mActivity, this);

        serviceData = new SalutServiceData("TestService", 9000, "HostDevice");

        network = new Salut(salutDataReceiver, serviceData, new SalutCallback() {
            @Override
            public void call() {
                System.out.println("YOUR DEVICE SUCKS");
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
            Log.e(TAG, "Failed to parse network data.");
        }
    }

    /**
     * An actual song has been received
     */
    private void onSongReceived(Packet pkt) {
        String base64 = pkt.getBase64string();
        try {
            // TODO: should be stored in a specific folder and deleted later
            // String secondary_storage = System.getenv("SECONDARY_STORAGE");
            Base64Utils.decode(base64, Environment.getExternalStorageState());
            MusicDataRepository.getInstance().refreshList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * handler when song owner receives a request for a song
     */
    private void onReceiveRequestForSong(Packet pkt) throws IOException {
        // this is the song owner
        MusicData songToRequest = pkt.getMusicData();
        // TODO find songToRequest locally and encode to Base 64
        // use Cursor/ContentResolver to get the song

        Uri uri = Uri.parse("content://media/external/audio/media/" + songToRequest.getId());
        File file = new File(uri.getPath());
        String encoded = Base64Utils.toBase64(file);
        String client = pkt.getSourceDviceName();
        SalutDevice clientDevice = findDestinationDevice(client);
        Packet response = new Packet();
        response.setTransactionType(Packet.MessageType.SEND_SONG);
        response.setBase64string(encoded);
        network.sendToDevice(clientDevice, response, new SalutCallback() {
                    @Override
                    public void call() {
                        Log.d(TAG, "send song to device failed");
                    }
                }
        );
    }

    /**
     * handler when a new list of MusicData is received
     */
    private void onNewSongListReceived(Packet pkt) {
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
        if (!network.isRunningAsHost) {
            network.startNetworkService(new SalutDeviceCallback() {
                @Override
                public void call(SalutDevice salutDevice) {
                    Log.d(TAG, "Host = someone connected");
                    Toast.makeText(getApplicationContext(), "Device: " + salutDevice.instanceName + " connected.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            network.stopNetworkService(false);
        }
    }

    public void isHostServiceAvailable() {
        if (!network.isRunningAsHost && !network.isDiscovering) {
            SalutCallback ifHostIsFound = new SalutCallback() {
                @Override
                public void call() {
                    System.out.println("Make Connection with host cos you found HOST.");
                    // DEVICE MAINTAINABLE AREA

                    SalutCallback onRegisterSuccess = new SalutCallback() {
                        @Override
                        public void call() {
                            System.out.println("REGISTER SUCCESSS... SENDING MESSAGE");

                            // TODO send message
//                            Message message = new Message();
//                            message.lol = "Wali";
                            /** send client's local song list to the host */
                            List<MusicData> localSongList = MusicDataRepository.getInstance().getList();
                            Packet pkt = new Packet();
                            // put a 'stamp' on the song title
                            for (MusicData data : localSongList) {
                                data.title += "- By ShareTango";
                            }
                            pkt.setSongList(localSongList);
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
                            System.out.println("WE FUCKED UP");
                        }
                    };
                    network.registerWithHost(network.foundDevices.get(0), onRegisterSuccess, onRegisterFaliure);
                }
            };
            SalutCallback ifHostIsNotFound = new SalutCallback() {
                @Override
                public void call() {
                    setupNetwork();
                    System.out.println("Host not found, starting service");
                }
            };

            network.discoverWithTimeout(ifHostIsFound, ifHostIsNotFound, 10000);
        } else {
            network.stopServiceDiscovery(true);
        }
    }

    // Communicate to activity through this interface
    public interface ISalutCallback {
        void updateClient();
    }

    /**
     * Request a song
     */
    public void request(MusicData song) {
        if (network.isRunningAsHost) {
            SalutDevice songOwner = findDestinationDevice(map.get(song));
            Packet packet = new Packet();
            packet.setTransactionType(Packet.MessageType.REQUEST_SONG); // indicate request for actual song
            packet.setMusicData(song);// the song to request
            packet.setSourceDviceName(network.thisDevice.deviceName);
            packet.setDestinationDviceName(map.get(song));
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
        for (SalutDevice device : network.foundDevices) {
            if (device.deviceName.equals(destDeviceName)) {
                destinationDevice = device;
                break;
            }
        }
        return destinationDevice;
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
