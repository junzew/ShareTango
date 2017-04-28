package com.imran.wali.sharetango;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.imran.wali.sharetango.AudioManager.MusicData;
import com.imran.wali.sharetango.AudioManager.PlaybackController;
import com.imran.wali.sharetango.DataRepository.MusicDataRepository;
import com.imran.wali.sharetango.Services.PlayService;
import com.imran.wali.sharetango.Services.SalutService;
import com.imran.wali.sharetango.UI.Fragments.AvailableSongsFragment;
import com.imran.wali.sharetango.UI.Fragments.DownloadedSongsFragment;
import com.imran.wali.sharetango.UI.Fragments.LocalSongsFragment;
import com.imran.wali.sharetango.UI.Fragments.PagerAdapterTabFragment;
import com.imran.wali.sharetango.UI.Fragments.PlayerFragment;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.imran.wali.sharetango.UI.Fragments.AlbumFragment.ARTWORK_URI;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SalutService.ISalutCallback, SlidingUpPanelLayout.PanelSlideListener {

    Context mContext;
    private SalutService mSalutService;
    private boolean mSalutServiceBound = false;
    private boolean isServiceBound = false;
    private ServiceConnection mSalutServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SalutService.SalutBinder binder = (SalutService.SalutBinder) service;
            mSalutService = (SalutService) binder.getService();
            isServiceBound = true;
            mSalutService.setBoundActivity(DashboardActivity.this);
            startSalutService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isServiceBound = false;
        }
    };

    Timer timer;
    TimerTask timerTask;

    /* Dashboard UI Variables */
    private ViewPager viewPager;

    /* Dashboard UI Support Variables */
    private ScreenSlidePagerAdapter slidePagerAdapter;

    /* Floating Player */
    private SlidingUpPanelLayout mSlidingUpPanelLayout;
    private LinearLayout mFloatingPlayer;
    private ImageView mAlbumArtImage;
    private TextView mSongTitle;
    private ImageButton mPlayButton;
    private ImageButton mNextButton;

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

        mSlidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mSlidingUpPanelLayout.addPanelSlideListener(this);
        mFloatingPlayer = (LinearLayout) findViewById(R.id.floating_player);

        mAlbumArtImage = (ImageView) findViewById(R.id.floating_player_album_art);
        mSongTitle = (TextView) findViewById(R.id.floating_player_song_name);
        mPlayButton = (ImageButton) findViewById(R.id.floating_player_play_button);
        mNextButton = (ImageButton) findViewById(R.id.floating_player_next_button);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mService.isPlaying()) {
                    mService.pause();
                    mPlayButton.setImageResource(R.drawable.play);
                } else {
                    mService.resume();
                    mPlayButton.setImageResource(R.drawable.pause_button);
                }
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.playNextOrStop(true);
            }
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                MusicData data = intent.getParcelableExtra(PlayService.BROADCAST_FILTER);
                mPlayerFragment.updateMusicData(data);
            }
        };

        PlaybackController.getInstance().addListener(new PlaybackController.IMusicStartListener() {
            @Override
            public void startMusic(MusicData music, boolean isFromUser) {
                // Update the floating player's UI
                Picasso.with(DashboardActivity.this)
                        .load(ContentUris.withAppendedId(ARTWORK_URI, music.albumId))
                        .placeholder(R.drawable.track_ablumart_placeholder)
                        .into(mAlbumArtImage);
                mSongTitle.setText(music.getTitle());
                mPlayButton.setImageResource(R.drawable.pause_button);
            }
        });

        Log.d("DashboardActivity", "starting PlayService");
        bindPlayService();

        bindSalutService();
        Log.d("DASHBOARD", "bind service salut");
    }

    private void startSalutService() {
        Intent intent = new Intent(this, SalutService.class);
        startService(intent);
    }
    private void bindSalutService() {
        Intent intent = new Intent(this, SalutService.class);
        bindService(intent, mSalutServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private PlayerFragment mPlayerFragment;
    private BroadcastReceiver receiver;

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("DashboardActivity", "onStart");
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(PlayService.BROADCAST_FILTER));
    }

    @Override
    protected void onStop() {
        Log.d("DashboardActivity", "onStop");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    private boolean mPlayServiceBound = false;
    private PlayService mService = null;

    public boolean isPlayServiceBound() {
        return mPlayServiceBound;
    }

    public PlayService getPlayService() {
        return mService;
    }

    private void bindPlayService() {
        Intent intent = new Intent(this, PlayService.class);
        bindService(intent, mPlayServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mPlayServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            PlayService.PlayBinder binder = (PlayService.PlayBinder) service;
            mService = (PlayService) binder.getService();
            mPlayServiceBound = true;

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            mPlayerFragment = PlayerFragment.newInstance();
            ft.replace(R.id.player_fragment_container, mPlayerFragment);
            Log.d("DashboardActivity", "starting PlayerFragment");
            ft.commit();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mPlayServiceBound = false;
        }
    };


//    private SongFragment mSongFragment = (SongFragment) PagerAdapterTabFragment.newInstance(PagerAdapterTabFragment.PageType.SONG);

    @Override
    public void updateClient() {
        // receive data from SalutService
        // TODO
    }
    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        Log.d("panel", "slide");
        mFloatingPlayer.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        switch (newState) {
            case EXPANDED:
                Log.d("panel", "expanded");
                mFloatingPlayer.setVisibility(View.INVISIBLE);
                break;
            case COLLAPSED:
                Log.d("panel", "collapsed");
                mFloatingPlayer.setVisibility(View.VISIBLE);
                break;
            case ANCHORED:
                Log.d("panel", "anchord");
                break;
            case HIDDEN:
                Log.d("panel", "hidden");
                break;
            case DRAGGING:
                Log.d("panel", "draggin");
                break;
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
       // timer.cancel();
        /* close the music list providing Thread */
        //WifiMusicListProviderService.cancel(true);
        if (mPlayServiceBound) {
            mPlayServiceBound = false;
            mService.stopSelf();
            unbindService(mPlayServiceConnection);
        }
        PlaybackController.getInstance().clearListeners();
        unbindService(mSalutServiceConnection);
        super.onDestroy();
    }

}
