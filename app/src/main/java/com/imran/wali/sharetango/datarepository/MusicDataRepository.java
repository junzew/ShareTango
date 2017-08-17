package com.imran.wali.sharetango.datarepository;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.imran.wali.sharetango.R;
import com.imran.wali.sharetango.UI.Elements.IndexableListView.IndexableListAdapter;
import com.imran.wali.sharetango.Utility.Base64Utils;
import com.imran.wali.sharetango.audiomanager.MusicData;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wali on 31/01/17.
 */

public class MusicDataRepository {

    public interface MusicDataChangeListener {

        void onRefreshStart();

        void onProgressChanged(int progress);

        void onComplete();
    }

    private static MusicDataRepository INSTANCE;

    public static MusicDataRepository getInstance() {
        return INSTANCE;
    }

    public static MusicDataRepository init(Context mContext) {
        INSTANCE = new MusicDataRepository(mContext);
        return INSTANCE;
    }

    private Context mContext;
    private ArrayList<MusicData> localMusicDataList;
    private final Object lock;
    private List<MusicDataChangeListener> subscribers;
    private GetSongListAsyncTask getSongListAsyncTask;

    private MusicDataRepository(Context c) {
        mContext = c;
        localMusicDataList = new ArrayList<>();
        lock = new Object();
        subscribers = new ArrayList<>();
    }

    public void addSubscriber(MusicDataChangeListener subscriber) {
        if (!subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    public void removeSubscriber(MusicDataChangeListener subscriber) {
        if (subscribers.contains(subscriber)) {
            subscribers.remove(subscriber);
        }
    }

    public void refreshList() {
        if (getSongListAsyncTask == null || getSongListAsyncTask.getStatus() != AsyncTask.Status.RUNNING) {
            getSongListAsyncTask = new GetSongListAsyncTask(lock);
            getSongListAsyncTask.execute();
        }
    }

    public ArrayList<MusicData> getLocalSongList() {
        synchronized (lock) {
            return localMusicDataList;
        }
    }

    private class GetSongListAsyncTask extends AsyncTask<Void, Integer, ArrayList<MusicData>> {

        private final Object lock;

        GetSongListAsyncTask(Object lock) {
            this.lock = lock;
        }

        @Override
        protected void onPreExecute() {
            for (MusicDataChangeListener subscriber : subscribers) {
                if (subscriber != null)
                    subscriber.onRefreshStart();
            }
        }

        @Override
        protected ArrayList<MusicData> doInBackground(Void... voids) {
            ArrayList<MusicData> data = new ArrayList<>();

            //Create Query
            ContentResolver musicResolver = mContext.getContentResolver();
            Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String[] columns = new String[]{
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.ALBUM_ID
            };
            String selection = MediaStore.Audio.Media.TITLE + " != '' AND " + MediaStore.Audio.Media.IS_MUSIC + "=1";

            //Execute Query
            Cursor cursor;
            try {
                cursor = musicResolver.query(musicUri, columns, selection, null, MediaStore.Audio.Media.TITLE);
            } catch (Exception e) {
                e.printStackTrace();
                return data;
            }
            if (cursor == null || cursor.getCount() == 0) {
                return data;
            }
            cursor.moveToFirst();
            publishProgress(50);

            // Build List
            int numberOfSongs = cursor.getCount();
            double progressInterval = numberOfSongs / 50;
            int currentProgress = 50;
            for (int i = 1; i < numberOfSongs + 1; i++) {
                cursor.moveToPosition(i - 1);
                MusicData musicData = new MusicData();
                musicData.title = cursor.getString(cursor.getColumnIndex("title"));
                musicData.artist = cursor.getString(cursor.getColumnIndex("artist"));
                musicData.path = cursor.getString(cursor.getColumnIndex("_data"));
                int songId = cursor.getInt(cursor.getColumnIndex("_id"));
//                musicData.albumArtURIString = "content://media/external/audio/media/" + songId + "/albumart";
                musicData.duration = cursor.getString(cursor.getColumnIndex("duration"));;
                musicData.id = songId;
                musicData.albumId = cursor.getLong(cursor.getColumnIndex("album_id"));

                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri uri = ContentUris.withAppendedId(sArtworkUri, musicData.albumId);
                musicData.albumArtURIString = uri.toString();
                Bitmap bitmap;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
                    musicData.encodedBitmapString = Base64Utils.encodeBitmap(bitmap);
                } catch(FileNotFoundException fnfe) {
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                            R.drawable.default_album_art);
                    musicData.encodedBitmapString = Base64Utils.encodeBitmap(bitmap);
                    fnfe.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                data.add(musicData);
                if (i % progressInterval == 0) {
                    publishProgress(currentProgress++);
                }
            }

            cursor.close();
            synchronized (lock) {
                localMusicDataList.clear();
                localMusicDataList.addAll(data);
            }
            return data;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            for (MusicDataChangeListener subscriber : subscribers) {
                if (subscriber != null)
                    subscriber.onProgressChanged(values[0]);
            }
        }

        @Override
        protected void onPostExecute(ArrayList<MusicData> list) {
            for (MusicDataChangeListener subscriber : subscribers) {
                if (subscriber != null)
                    subscriber.onComplete();
            }
        }
    }


    public void addAvailableMusicData(MusicData data) {
        availableFragmentAdapter.add(data);
    }

    public void addAllAvailableMusicData(List<MusicData> songs) {
        availableFragmentAdapter.addAll(songs);
    }

    public List<MusicData> getAvailableSongs() {
        return availableFragmentAdapter.getAvailableSongs();
    }

    public void clearAvailableSongs() {
        availableFragmentAdapter.clear();
    }
    private IndexableListAdapter availableFragmentAdapter;
    public void registerAvilableMusicDataFragment(IndexableListAdapter adapter) {
        availableFragmentAdapter = adapter;
    }
}
