package com.imran.wali.sharetango.AudioManager;

import android.content.Context;
import android.content.Intent;

import com.imran.wali.sharetango.PlayActivity;
import com.imran.wali.sharetango.Services.PlayService;

import java.util.LinkedList;

/**
 * Created by junze on 2017-01-08.
 */

public class PlaybackController {

    private static PlaybackController INSTANCE = null;

    private PlaybackController() {}

    public static PlaybackController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlaybackController();
        }
        return INSTANCE;
    }

    private LinkedList<MusicData> playbackQueue = new LinkedList<>();
    private int index = 0;

    public static void start(Context context, MusicData song) {
       // index = playbackQueue.indexOf(song);

        Intent intent = new Intent(context, PlayService.class);
        intent.putExtra("id", song.getId());
        context.startService(intent);

        Intent i = new Intent(context, PlayActivity.class);
        i.putExtra("albumId", song.getAlbumId());
        i.putExtra("title", song.getTitle());
        context.startActivity(i);
    }

    public void enqueue(MusicData data) {
        playbackQueue.add(data);
    }

    public MusicData next() {
        index++;
        index = index % playbackQueue.size();
        return playbackQueue.get(index);
    }

    public MusicData previous() {
        index--;
        index = index % playbackQueue.size();
        return playbackQueue.get(index);
    }

    public void clear() {
        playbackQueue.clear();
    }



}
