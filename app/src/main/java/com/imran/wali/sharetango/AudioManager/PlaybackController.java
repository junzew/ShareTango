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
    int size =0;
    boolean isRandom = false;

    public void start(Context context, MusicData song) {
        index = playbackQueue.indexOf(song);
        size = playbackQueue.size()-1;

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

    public MusicData shuffle() {
        index = (int) (Math.random()*size);
        return playbackQueue.get(index);
    }

    public MusicData next() {
        if(index == size){
            // restart from the top of the list
            index = 0;
        } else {
            index++;
            //index = index % size;
        }
        return playbackQueue.get(index);
    }

    public MusicData previous() {
        index--;
        if(index <= 0){
            index = playbackQueue.size()-1;
        }else{
            index = index % playbackQueue.size();
        }
        return playbackQueue.get(index);
    }


    public void clear() {
        playbackQueue.clear();
    }



}
