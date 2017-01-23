package com.imran.wali.sharetango;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.imran.wali.sharetango.UI.Fragments.PagerAdapterTabFragment;
import com.imran.wali.sharetango.UI.Fragments.SongFragment;
import com.imran.wali.sharetango.Wifi.WiFiDirectBroadcastReceiver;
import com.imran.wali.sharetango.Wifi.WifiMusicListProvider;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Context mContext;

    /* WIFI Direct Variables */
    WifiP2pManager mWifiDirectManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    Future WifiMusicListProviderService;
    WifiP2pManager.PeerListListener peerListListener;
    Timer timer;
    TimerTask timerTask;

    /* Dashboard UI Variables */
    private ViewPager viewPager;

    /* Dashboard UI Support Variables */
    private ScreenSlidePagerAdapter slidePagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        mContext = this;

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

        /* Init WIFI Direct */
        mWifiDirectManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiDirectManager.initialize(this, getMainLooper(), null);
        peerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {

            }
        };
        mReceiver = new WiFiDirectBroadcastReceiver(mWifiDirectManager, mChannel, this, peerListListener);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() { new DiscoverPeerAsyncTask().execute(); }
        };
        //Schedule Timer At Create Time
        // TODO Decide whether to pause this
        timer.schedule(timerTask, 1000, 10000);
        registerReceiver(mReceiver, mIntentFilter);

        ExecutorService executor = Executors.newFixedThreadPool(1);
        WifiMusicListProviderService = executor.submit(new WifiMusicListProvider());
    }

    /* Setting up the View Pager And Fragments */
    private SongFragment mRemoteSongFragment = (SongFragment) PagerAdapterTabFragment.newInstance(PagerAdapterTabFragment.PageType.SONG);
    private SongFragment mLocalSongFragment = (SongFragment) PagerAdapterTabFragment.newInstance(PagerAdapterTabFragment.PageType.SONG);
//    private ArtistFragment mArtistFragment = (ArtistFragment) PagerAdapterTabFragment.newInstance(PagerAdapterTabFragment.PageType.ARTIST);
//    private AlbumFragment mAlbumFragment = (AlbumFragment) PagerAdapterTabFragment.newInstance(PagerAdapterTabFragment.PageType.ALBUMS);
//    private GenreFragment mGenreFragment = (GenreFragment) PagerAdapterTabFragment.newInstance(PagerAdapterTabFragment.PageType.GENRE);

    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter{
        final int PAGE_COUNT = 2;
        private String tabTitles[] = new String[] { "Remote Songs", "Local Songs"};
        private ArrayList<Fragment> fragmentList;

        ScreenSlidePagerAdapter(FragmentManager fm){
            super(fm);
            fragmentList = new ArrayList<>();
            /* Adding All Fragments Here */
            fragmentList.add(mRemoteSongFragment);
            fragmentList.add(mLocalSongFragment);
//            fragmentList.add(mArtistFragment);
//            fragmentList.add(mAlbumFragment);
//            fragmentList.add(mGenreFragment);
        }

        @Override
        public int getCount() {return PAGE_COUNT;}

        @Override
        public Fragment getItem(int position) {return fragmentList.get(position);}

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }

    /* Wifi Direct Controls */

    //Discover Peer Async
    private class DiscoverPeerAsyncTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            mWifiDirectManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int reasonCode) { Toast.makeText(mContext,"Error",Toast.LENGTH_SHORT).show(); }
            });
            return null;
        }
    }


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
    protected void onStop(){
        super.onStop();
        /* unregister the wifi direct broadcast receiver */
        unregisterReceiver(mReceiver);
        /* cancel timer if pausing activity */
        timer.cancel();
        /* close the music list providing Thread */
        WifiMusicListProviderService.cancel(true);
    }
}
