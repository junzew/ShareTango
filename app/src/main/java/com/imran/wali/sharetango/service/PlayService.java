package com.imran.wali.sharetango.service;

/**
 * Created by junze on 2017-01-08.
 */
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.imran.wali.sharetango.AudioManager.MusicData;
import com.imran.wali.sharetango.AudioManager.PlaybackController;

import java.io.IOException;

public class PlayService extends Service implements MediaPlayer.OnPreparedListener {

    public static final String BROADCAST_FILTER = "com.imran.wali.sharetango.service";

    private MediaPlayer mMediaPlayer = null;
    private IBinder mBinder = new PlayBinder();
    private LocalBroadcastManager broadcaster;
    private boolean fromUser = false; // true if user pressed 'next'/'previous' vs. automatically advance to next

    public PlayService() {
    }

    public class PlayBinder extends Binder {
        public Service getService() {
            return PlayService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        broadcaster = LocalBroadcastManager.getInstance(this);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (!fromUser) {
                    playNextOrStop(false);
                }
                fromUser = false;
            }
        });
        long id = intent.getLongExtra("id", 0);
        play(id);
        return super.onStartCommand(intent, flags, startId);
    }

    public void playNextOrStop(boolean isFromUser) {
        fromUser = isFromUser;
        MusicData nextSong = PlaybackController.getInstance().next();
        if (nextSong == null) {
            stopSelf();
        } else {
            mMediaPlayer.reset();
            broadcast(nextSong);
            play(nextSong.id);
        }
    }

    public void playPrevious(boolean isFromUser) {
        fromUser = isFromUser;
        MusicData previousSong = PlaybackController.getInstance().previous();
        if (previousSong == null) {
            stopSelf();
        } else {
            mMediaPlayer.reset();
            broadcast(previousSong);
            play(previousSong.id);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    // play a song with id
    public void play(long id) {
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), contentUri);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        mMediaPlayer.pause();
    }
    public void resume() {
        mMediaPlayer.start();
    }
    public void stop() {
        mMediaPlayer.stop();
    }
    public int currentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }
    public void seekTo(int progress) {
        mMediaPlayer.seekTo(progress);
    }

    @Override
    public void onDestroy() {
        Log.i("PlayService", "onDestroy");
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
    }
    public void broadcast(MusicData message) {
        Intent intent = new Intent(BROADCAST_FILTER);
        if(message != null) {
            intent.putExtra(BROADCAST_FILTER, message);
        }
        broadcaster.sendBroadcast(intent);
    }
}