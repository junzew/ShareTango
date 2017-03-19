package com.imran.wali.sharetango.UI.activity;

/**
 * Created by junze on 2017-01-08.
 */

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.imran.wali.sharetango.AudioManager.MusicData;
import com.imran.wali.sharetango.R;
import com.imran.wali.sharetango.service.PlayService;
import com.squareup.picasso.Picasso;

public class PlayActivity extends AppCompatActivity {

    private PlayService mService = null;
    private boolean mBound = false;
    private boolean isPlaying = false;
    ImageView mPlayImage;
    ImageView mAlbumArtImage;
    ImageView mVolumeImage;
    TextView mAlbumTitle;
    SeekBar mSeekBar;
    SeekBar mVolumeBar;
    ImageView mPreviousButton;
    ImageView mNextButton;
    ImageView mRepeatButton;
    float maxVolume;
    boolean isSeeking = false;
    boolean isMute = false;
    boolean isRepeat = false;
    boolean isShuffle = false;
    boolean isNormal = true;
    private UpdateSeekBarProgressTask task;
    private BroadcastReceiver receiver;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            PlayService.PlayBinder binder = (PlayService.PlayBinder) service;
            mService = (PlayService) binder.getService();
            mBound = true;
            task = new UpdateSeekBarProgressTask();
            task.execute();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public class UpdateSeekBarProgressTask extends AsyncTask<Void, Integer, String> {

        @Override
        protected void onPreExecute() {
            mSeekBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {

            while (mBound && !isCancelled()) {
                if (!mService.isPlaying()) {
                    SystemClock.sleep(200);
                } else {
                    int p = (int) ((double) mService.currentPosition() / (double) mService.getDuration() * 100);
                    if (!isSeeking) {
                        publishProgress(p);
                    }
                }
            }
            return "Complete";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int from = mSeekBar.getProgress();
            int to = values[0];
            if (Math.abs(from - to) <= 1)
                mSeekBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(mService, "Complete", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        bindPlayService();
        initViews();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                MusicData data = intent.getParcelableExtra(PlayService.BROADCAST_FILTER);
//                Uri uri = ContentUris.withAppendedId(ARTWORK_URI, data.albumId);
//                Picasso.with(PlayActivity.this)
//                        .load(uri)
//                        .placeholder(R.drawable.track_ablumart_placeholder)
//                        .into(mAlbumArtImage);
                mAlbumTitle.setText(data.title);
                mSeekBar.setProgress(0);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(PlayService.BROADCAST_FILTER));
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    private void bindPlayService() {
        Intent intent = new Intent(this, PlayService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        isPlaying = true;
    }

    private void initViews() {
        mAlbumArtImage = (ImageView) findViewById(R.id.album);
        mPlayImage = (ImageView) findViewById(R.id.play_button);
        mAlbumTitle = (TextView) findViewById(R.id.song_title);
        mSeekBar = (SeekBar) findViewById(R.id.progress);
        mPreviousButton = (ImageView) findViewById(R.id.skip_prev);
        mNextButton = (ImageView) findViewById(R.id.skip_next);
        mRepeatButton = (ImageView) findViewById(R.id.repeat);

        // TODO: shuffle and favorite
        // play mode
        mRepeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNormal) {
                    isRepeat = true;
                    isNormal = false;
                    isShuffle = false;
                    mRepeatButton.setImageResource(R.drawable.repeat_one);
                    Log.i("PlayActivity", "repeat one play");
                } else if (isRepeat) {
                    isRepeat = false;
                    isNormal = false;
                    isShuffle = true;
                    mRepeatButton.setImageResource(R.drawable.shuffle);
                    Log.i("PlayActivity", "shuffle play");
                } else {
                    isRepeat = false;
                    isNormal = true;
                    isShuffle = false;
                    mRepeatButton.setImageResource(R.drawable.repeat);
                    Log.i("PlayActivity", "normal play");
                }
            }
        });

        // Audio handler
        mVolumeImage = (ImageView) findViewById(R.id.volume);
        mVolumeBar = (SeekBar) findViewById(R.id.volume_bar);
        maxVolume = (float) mVolumeBar.getMax(); // default is 100

        // change volume
        mVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // progress is the user input
                if (fromUser) {
                    // if user is updating the volume, then change mProgress
                    // http://stackoverflow.com/questions/5215459/android-mediaplayer-setvolume-function
                    float volume = (float) (1 - (Math.log(100 - progress) / Math.log(100)));
                    if (!isMute) {
                        mService.volume(volume, volume);
                    }
                    mService.setCurrVolume(volume);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mVolumeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMute) {
                    isMute = false;
                    float vol = mService.getCurrVolume();
                    mService.volume(vol, vol);
                    mVolumeImage.setImageResource(R.drawable.volume);
                } else {
                    mService.volume(0, 0);
                    isMute = true;
                    mVolumeImage.setImageResource(R.drawable.mute);
                }
            }
        });

        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRepeat) {
                    mService.playPrevious(true);
                } else {
                    mService.restart();
                    mSeekBar.setProgress(0);
                }
                mPlayImage.setImageResource(R.drawable.pause_button);
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNormal) {
                    mService.playNextOrStop(true);
                } else if (isRepeat) {
                    mService.restart();
                    mSeekBar.setProgress(0);
                } else {
                    mService.shuffle(true);
                }
                mPlayImage.setImageResource(R.drawable.pause_button);
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int mProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeeking = false;
                int position = (int) ((double) mProgress / 100 * mService.getDuration());
                mService.seekTo(position);
            }
        });
        
        Intent i = getIntent();
        long albumId = i.getLongExtra("albumId", 0);
//        Uri uri = ContentUris.withAppendedId(ARTWORK_URI, albumId);
//        Picasso.with(PlayActivity.this)
//                .load(uri)
//                .placeholder(R.drawable.track_ablumart_placeholder)
//                .into(mAlbumArtImage);
        String songTitle = i.getStringExtra("title");
        mAlbumTitle.setText(songTitle);
        mPlayImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mService.isPlaying()) {
                    mService.pause();
                    Log.i("PlayActivity", "pause");
                    mPlayImage.setImageResource(R.drawable.play);
                } else {
                    mService.resume();
                    mPlayImage.setImageResource(R.drawable.pause_button);
                    Log.i("PlayActivity", "resume");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            mBound = false;
            task.cancel(true);
            mService.stopSelf();
            unbindService(mConnection);
            mBound = false;
        }
    }
}
