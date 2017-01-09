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
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.imran.wali.sharetango.R;
import com.imran.wali.sharetango.service.PlayService;
import com.squareup.picasso.Picasso;

import static com.imran.wali.sharetango.UI.Fragments.AlbumFragment.ARTWORK_URI;

public class PlayActivity extends AppCompatActivity {

    private PlayService mService = null;
    private boolean mBound = false;
    private boolean isPlaying = false;
    ImageView mPlayImage;
    ImageView mAlbumArtImage;
    TextView mAlbumTitle;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            PlayService.PlayBinder binder = (PlayService.PlayBinder) service;
            mService = (PlayService) binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

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
