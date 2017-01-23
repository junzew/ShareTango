package com.imran.wali.sharetango.AudioManager;

import android.content.Context;
import android.content.Intent;

import com.imran.wali.sharetango.UI.activity.PlayActivity;
import com.imran.wali.sharetango.Service.PlayService;

/**
 * Created by junze on 2017-01-08.
 */

public class PlaybackController {

    public static void start(Context context, MusicData song) {
        Intent intent = new Intent(context, PlayService.class);
        intent.putExtra("id", song.getId());
        context.startService(intent);

        Intent i = new Intent(context, PlayActivity.class);
        i.putExtra("albumId", song.getAlbumId());
        i.putExtra("title", song.getTitle());
        context.startActivity(i);
    }

}
