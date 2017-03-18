package com.imran.wali.sharetango;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bluelinelabs.logansquare.LoganSquare;
import com.imran.wali.sharetango.DataRepository.Message;
import com.imran.wali.sharetango.DataRepository.MusicDataRepository;
import com.imran.wali.sharetango.UI.Fragments.AvailableSongsFragment;
import com.imran.wali.sharetango.UI.Fragments.DownloadedSongsFragment;
import com.imran.wali.sharetango.UI.Fragments.LocalSongsFragment;
import com.imran.wali.sharetango.UI.Fragments.PagerAdapterTabFragment;
import com.imran.wali.sharetango.Wifi.WiFiDirectBroadcastReceiver;
import com.imran.wali.sharetango.DataRepository.WifiClientRepository;
import com.imran.wali.sharetango.Wifi.WifiMusicListProvider;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutDevice;
import com.peak.salut.SalutServiceData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SalutDataCallback {

    Context mContext;

    public SalutDataReceiver salutDataReceiver;
    public SalutServiceData serviceData;
    public Salut network;
    SalutDataCallback callback;

    /* WIFI Direct Variables */
//    WifiP2pManager mWifiDirectManager;
//    WifiP2pManager.Channel mChannel;
//    BroadcastReceiver mReceiver;
//    IntentFilter mIntentFilter;
//    Future WifiMusicListProviderService;
//    WifiP2pManager.PeerListListener peerListListener;
    Timer timer;
    TimerTask timerTask;
    WifiClientRepository wifiClientRepository;

    /* Dashboard UI Variables */
    private ViewPager viewPager;

    /* Dashboard UI Support Variables */
    private ScreenSlidePagerAdapter slidePagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        mContext = this;

        /* Ask For Permission */
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        }

        /* Init Singletons */
        MusicDataRepository.init(mContext);

        /* Init Toolbar */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* Init Drawer */
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /* Init Variables */
        viewPager = (ViewPager) findViewById(R.id.dashboard_viewpager);
        slidePagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(slidePagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.dashboard_viewpager_sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        /* Salut Init */
        salutDataReceiver = new SalutDataReceiver(this, this);

        serviceData = new SalutServiceData("TestService", 9000, "HostDevice");

        network = new Salut(salutDataReceiver, serviceData, new SalutCallback() {
            @Override
            public void call() {
                System.out.println("YOUR DEVICE SUCKS");
            }
        });

        isServiceAvailable();




//        /* Init WIFI Direct */
//        wifiClientRepository = WifiClientRepository.getInstance();
//        mWifiDirectManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
//        mChannel = mWifiDirectManager.initialize(this, getMainLooper(), null);
//        peerListListener = new WifiP2pManager.PeerListListener() {
//            @Override
//            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
//                new PeerHandlingAsyncTask().execute(wifiP2pDeviceList.getDeviceList());
//            }
//        };
//
//        mReceiver = new WiFiDirectBroadcastReceiver(mWifiDirectManager, mChannel, this, peerListListener);
//        mIntentFilter = new IntentFilter();
//        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
//        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
//        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
//        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
//        timer = new Timer();
//        timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                new DiscoverPeerAsyncTask().execute();
//            }
//        };
//        //Schedule Timer At Create Time
//        // TODO Decide whether to pause this
//        timer.schedule(timerTask, 1000, 30000);
//        registerReceiver(mReceiver, mIntentFilter);
//
//        ExecutorService executor = Executors.newFixedThreadPool(1);
//        WifiMusicListProviderService = executor.submit(new WifiMusicListProvider());


    }

    @Override
    public void onDataReceived(Object data) {
        Log.d("SHARETANGO", "Received network data.");
        try
        {
            Message newMessage = LoganSquare.parse((String)data, Message.class);
            Log.d("SHARETANGO", newMessage.lol);  //See you on the other side!
            //Do other stuff with data.
        }
        catch (IOException ex)
        {
            Log.e("SHARETANGO", "Failed to parse network data.");
        }
    }


    private void setupNetwork()
    {
        if(!network.isRunningAsHost)
        {
            network.startNetworkService(new SalutDeviceCallback() {
                @Override
                public void call(SalutDevice salutDevice) {
                    System.out.println("Host = someone connected");
                    Toast.makeText(getApplicationContext(), "Device: " + salutDevice.instanceName + " connected.", Toast.LENGTH_SHORT).show();
                }
            });

            //hostingBtn.setText("Stop Service");
            //discoverBtn.setAlpha(0.5f);
            //discoverBtn.setClickable(false);
        }
        else
        {
            network.stopNetworkService(false);
            //hostingBtn.setText("Start Service");
            //discoverBtn.setAlpha(1f);
            //discoverBtn.setClickable(true);
        }
    }

    private void isServiceAvailable()
    {
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
            //discoverBtn.setText("Stop Discovery");
            //hostingBtn.setAlpha(0.5f);
            //hostingBtn.setClickable(false);
        }
        else
        {
            network.stopServiceDiscovery(true);
            //discoverBtn.setText("Discover Services");
            //hostingBtn.setAlpha(1f);
            //hostingBtn.setClickable(false);
        }
    }

//    @Override
//    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
//        System.out.print(wifiP2pInfo.toString());
//    }

    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 3;
        private String tabTitles[] = new String[]{"Available", "Downloaded", "Local"}; // Fix this
        private ArrayList<Fragment> fragmentList;

        ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
            /* Create Fragments and their arguments */
            Bundle args = new Bundle();
            args.putSerializable("type", PagerAdapterTabFragment.PageType.Available);
            PagerAdapterTabFragment mAvailableSongFragment = new AvailableSongsFragment();
            mAvailableSongFragment.setArguments(args);
            args = new Bundle();
            args.putSerializable("type", PagerAdapterTabFragment.PageType.Downloaded);
            PagerAdapterTabFragment mDownloadedSongFragment = new DownloadedSongsFragment();
            mDownloadedSongFragment.setArguments(args);
            args = new Bundle();
            args.putSerializable("type", PagerAdapterTabFragment.PageType.Local);
            PagerAdapterTabFragment mLocalSongFragment = new LocalSongsFragment();
            mLocalSongFragment.setArguments(args);
            fragmentList = new ArrayList<>();

            /* Adding All Fragments Here To Adapter*/
            fragmentList.add(mAvailableSongFragment);
            fragmentList.add(mDownloadedSongFragment);
            fragmentList.add(mLocalSongFragment);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }

    /* Wifi Direct Controls */

    //Discover Peer Async
//    private class DiscoverPeerAsyncTask extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected Void doInBackground(Void... voids) {
//            mWifiDirectManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
//                @Override
//                public void onSuccess() {
//                    Toast.makeText(DashboardActivity.this, "Discovery Initiated", Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onFailure(int reasonCode) {
//                    Toast.makeText(DashboardActivity.this, "Discovery Failed : " + reasonCode, Toast.LENGTH_SHORT).show();
//                }
//            });
//            return null;
//        }
//    }

//    private class PeerHandlingAsyncTask extends AsyncTask<Object, Void, Void> {
//
//        @Override
//        protected Void doInBackground(Object... params) {
//            Collection<WifiP2pDevice> deviceList = (Collection<WifiP2pDevice>) params[0];
//            // Get Devices To Remove
//            ArrayList<WifiP2pDevice> devicesToRemove = wifiClientRepository.getListOfDevicesToRemove(deviceList);
//            // Get Devices to Add
//            //ArrayList<WifiP2pDevice> devicesToAdd = wifiClientRepository.getListOfDevicesToAdd(deviceList);
//            // Add new DeviceList to Repository
//            wifiClientRepository.setActiveList(deviceList);
//            // Remove Songs from Available for "devicesToRemove" peers
//            // TODO: Remove this!
//            // For each peer, ask for their music data
//            //for(WifiP2pDevice device : wifiClientRepository.getActiveList()){
//            if (!wifiClientRepository.getActiveList().isEmpty()) {
//                WifiP2pConfig config = new WifiP2pConfig();
//                config.deviceAddress = wifiClientRepository.getActiveList().get(0).deviceAddress;
//                config.wps.setup = WpsInfo.PBC;
//                mWifiDirectManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
//
//                    @Override
//                    public void onSuccess() {
//                        Toast.makeText(DashboardActivity.this, "Connect Success. ",
//                                Toast.LENGTH_SHORT).show();
//                        // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
//                    }
//
//                    @Override
//                    public void onFailure(int reason) {
//                        Toast.makeText(DashboardActivity.this, "Connect failed. Retry.",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//
//            //}
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void v) {
//
//        }
//    }


    /* Menu Control Overrides */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /* Activity Control Overrides */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        /* unregister the wifi direct broadcast receiver */
        //unregisterReceiver(mReceiver);
        /* cancel timer if pausing activity */
        timer.cancel();
        /* close the music list providing Thread */
        //WifiMusicListProviderService.cancel(true);
        super.onDestroy();
    }

}
