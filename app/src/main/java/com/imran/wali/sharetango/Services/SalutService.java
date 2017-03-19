package com.imran.wali.sharetango.Services;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.bluelinelabs.logansquare.LoganSquare;
import com.imran.wali.sharetango.DataRepository.Message;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutDevice;
import com.peak.salut.SalutServiceData;

import java.io.IOException;

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

    @Override
    public void onDataReceived(Object data) {
        Log.d(TAG, "Received network data.");
        try
        {
            Message newMessage = LoganSquare.parse((String)data, Message.class);
            Log.d(TAG, newMessage.lol);  //See you on the other side!
            //Do other stuff with data.
        }
        catch (IOException ex)
        {
            Log.e(TAG, "Failed to parse network data.");
        }
    }

    public void setupNetwork() {
        if(!network.isRunningAsHost)
        {
            network.startNetworkService(new SalutDeviceCallback() {
                @Override
                public void call(SalutDevice salutDevice) {
                    Log.d(TAG, "Host = someone connected");
                    Toast.makeText(getApplicationContext(), "Device: " + salutDevice.instanceName + " connected.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {
            network.stopNetworkService(false);
        }
    }

    public void isHostServiceAvailable() {
        if(!network.isRunningAsHost && !network.isDiscovering)
        {
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
                            Message message = new Message();
                            message.lol = "Wali";
                            network.sendToHost(message, new SalutCallback() {
                                @Override
                                public void call() {
                                    Log.e("SHARETANGO", "Oh no! The data failed to send.");
                                }
                            });
                        }
                    };

                    SalutCallback onRegisterFaliure = new SalutCallback(){
                        @Override
                        public void call() {
                            System.out.println("WE FUCKED UP");
                        }
                    };
                    network.registerWithHost(network.foundDevices.get(0),onRegisterSuccess, onRegisterFaliure );
                }
            };
            SalutCallback ifHostIsNotFound = new SalutCallback(){
                @Override
                public void call() {
                    setupNetwork();
                    System.out.println("Host not found, starting service");
                }
            };

            network.discoverWithTimeout(ifHostIsFound, ifHostIsNotFound, 10000);
        }
        else
        {
            network.stopServiceDiscovery(true);
        }
    }

    // Communicate to activity through this interface
    public interface ISalutCallback {
        void updateClient();
    }
}
