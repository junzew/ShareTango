package com.imran.wali.sharetango.UI.activity;

/**
 * Created by junze on 2017-01-08.
 */

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.media.AudioManager;

import com.imran.wali.sharetango.R;
import com.imran.wali.sharetango.service.PlayService;
import com.squareup.picasso.Picasso;

import static com.imran.wali.sharetango.UI.Fragments.AlbumFragment.ARTWORK_URI;

public class PlayActivity extends AppCompatActivity {

    private PlayService mService = null;
    // for volume adjustment
    private AudioManager mAudioManager = null;
    private boolean mBound = false;
    private boolean isPlaying = false;
    private boolean isMute = false;
    private float currVolume = 0;

    ImageView mPlayImage;
    ImageView mAlbumArtImage;
    ImageView mVolumeImage;
    TextView mAlbumTitle;
    SeekBar mSeekBar;
    SeekBar mVolumeBar;
    boolean isSeeking = false;
    boolean isSeekingV = false;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            PlayService.PlayBinder binder = (PlayService.PlayBinder) service;
            mService = (PlayService) binder.getService();
            mBound = true;
            new DelayTask().execute();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public class DelayTask extends AsyncTask<Void, Integer, String> {

        @Override
        protected void onPreExecute() {
            mSeekBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {

            while (mBound && mService.currentPosition() < mService.getDuration()) {
                if (!mService.isPlaying()) {
                    SystemClock.sleep(500);
                } else {
                    int p = (int) ((double)mService.currentPosition()/ (double)mService.getDuration() * 100);
                    if (!isSeeking) {
                        publishProgress(p);
                    }
                }
            }
            return "Complete";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mSeekBar.setProgress(values[0]);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        initViews();
        bindPlayService();
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
        // TODO: Nex/Prev button
        // TODO: shuffle and repeat and favorite

        // Audio handler
        // mute
        mVolumeImage = (ImageView) findViewById(R.id.volume);
        mVolumeBar = (SeekBar) findViewById(R.id.volume_bar);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // Set default/max volume to the current system volume
        mVolumeBar.setMax(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        // change system volume
        // mVolumeBar.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        // change volume
        mVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // progress is the user input
                if(fromUser) {
                    // if user is updating the volume, then change mProgress
                    // mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                    float volume = (float) (1 - (Math.log(100 - progress) / Math.log(100)));
                    mService.volume(volume, volume);
                    currVolume = volume;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        mVolumeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isMute) {
                    isMute = false;
                    mService.volume(currVolume,currVolume);
                    mPlayImage.setImageResource(R.drawable.volume);
                    Log.i("PlayActivity", "normal volume");
                } else {
                    mService.volume(0,0);
                    Log.i("PlayActivity", "mute");
                    isMute = true;
                    mVolumeImage.setImageResource(R.drawable.mute);
                }
            }
        });


        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int mProgress = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    mProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSeekBar.setProgress(mProgress);
                isSeeking = false;
                int position = (int) ((double) mProgress / 100 * mService.getDuration());
                mService.seekTo(position);
            }
        });


        Intent i = getIntent();
        long albumId = i.getLongExtra("albumId", 0);
        Uri uri = ContentUris.withAppendedId(ARTWORK_URI, albumId);
        Picasso.with(PlayActivity.this)
                .load(uri)
                .placeholder(R.drawable.track_ablumart_placeholder)
                .into(mAlbumArtImage);
        String songTitle = i.getStringExtra("title");
        mAlbumTitle.setText(songTitle);
        mPlayImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying) {
                    mService.pause();
                    Log.i("PlayActivity", "pause");
                    isPlaying = false;
                    mPlayImage.setImageResource(R.drawable.play);
                } else {
                    isPlaying = true;
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
            mService.stopSelf();
            unbindService(mConnection);
            mBound = false;
        }
    }
}
