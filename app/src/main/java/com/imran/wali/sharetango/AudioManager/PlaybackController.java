package com.imran.wali.sharetango.AudioManager;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
    public interface IMusicStartListener {
        void startMusic(MusicData music);
    }

    private List<IMusicStartListener> listeners = new ArrayList<>();

    public void addListener(IMusicStartListener l) {
        Log.d("PlaybackController", "addListener");
        this.listeners.add(l);
    }

    private LinkedList<MusicData> playbackQueue = new LinkedList<>();
    private int index = 0;

    public void start(Context context, MusicData song) {
        index = playbackQueue.indexOf(song);

//        Intent intent = new Intent(context, PlayService.class);
//        intent.putExtra("id", song.getId());
//        context.startService(intent);

//        Intent i = new Intent(context, PlayActivity.class);
//        i.putExtra("albumId", song.getAlbumId());
//        i.putExtra("title", song.getTitle());
//        context.startActivity(i);

        for (IMusicStartListener listener: listeners) {
            listener.startMusic(song);
        }
    }

    public void enqueue(MusicData data) {
        playbackQueue.add(data);
    }

    public MusicData next() {
        index++;
        index = mod(index, playbackQueue.size());
        return playbackQueue.get(index);
    }

    public MusicData previous() {
        index--;
        index = mod(index, playbackQueue.size());
        return playbackQueue.get(index);
    }

    public void clear() {
        playbackQueue.clear();
    }


    // Java uses % for remainder, but we want to use modulo
    private int mod(int x, int y) {
        int result = x % y;
        return result < 0? result + y : result;
    }

}
