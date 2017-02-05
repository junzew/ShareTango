package com.imran.wali.sharetango.DataRepository;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.imran.wali.sharetango.AudioManager.MusicData;

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

    public static MusicDataRepository getInstance() { return INSTANCE; }

    public static MusicDataRepository init(Context mContext) {
        INSTANCE = new MusicDataRepository(mContext);
        return INSTANCE;
    }

    private Context mContext;
    private ArrayList<MusicData> musicDataList;
    private List<MusicDataChangeListener> subscribers;
    private GetSongListAsyncTask getSongListAsyncTask;

    private MusicDataRepository(Context c) {
        mContext = c;
        musicDataList = new ArrayList<>();
        subscribers = new ArrayList<>();
        getSongListAsyncTask = new GetSongListAsyncTask();
        refreshList();
    }

    public void addSubscriber(MusicDataChangeListener subscriber){ subscribers.add(subscriber); }

    public void removeSubscriber(MusicDataChangeListener subscriber){ subscribers.remove(subscriber); }

    public void refreshList(){ getSongListAsyncTask.execute(); }

    public ArrayList<MusicData> getList(){ return new ArrayList<>(musicDataList);}

    private class GetSongListAsyncTask extends AsyncTask<Void, Integer, ArrayList<MusicData>>{

        @Override
        protected void onPreExecute() {
            for(MusicDataChangeListener subscriber : subscribers){
                if(subscriber!=null)
                    subscriber.onRefreshStart();
            }
        }

        @Override
        protected ArrayList<MusicData> doInBackground(Void... voids) {
            ArrayList<MusicData> data = new ArrayList<>();

            //Create Query
            ContentResolver musicResolver = mContext.getContentResolver();
            Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String[] columns = new String []{
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.ALBUM_ID
            };
            String selection = MediaStore.Audio.Media.TITLE + " != '' AND " +  MediaStore.Audio.Media.IS_MUSIC + "=1";

            //Execute Query
            Cursor cursor;
            try{cursor = musicResolver.query(musicUri, columns, selection, null,MediaStore.Audio.Media.TITLE);}
            catch (Exception e) {e.printStackTrace();return data;}
            if (cursor == null && cursor.getCount() != 0){ return data;}

            // Build List




            musicDataList.clear();
            musicDataList.addAll(data);
            return data;
        }


        @Override
        protected void onProgressUpdate(Integer... values){
            for(MusicDataChangeListener subscriber : subscribers){
                if(subscriber!=null)
                    subscriber.onProgressChanged(values[0]);
            }
        }


        @Override
        protected void onPostExecute(ArrayList<MusicData> list){
            for(MusicDataChangeListener subscriber : subscribers){
                if(subscriber!=null)
                    subscriber.onComplete();
            }
        }
    }





















//    @Override
//    protected ArrayList<MusicData> doInBackground(Object[] params) {
//        ArrayList<MusicData> data = new ArrayList<>();
//
//        ContentResolver musicResolver = mContext.getContentResolver();
//        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//        String[] columns = new String []{
//                MediaStore.Audio.Media.TITLE,
//                MediaStore.Audio.Media.ARTIST,
//                MediaStore.Audio.Media.DURATION,
//                MediaStore.Audio.Media.DATA,
//                MediaStore.Audio.Media._ID,
//                MediaStore.Audio.Media.ALBUM_ID
//        };
//        String selection = MediaStore.Audio.Media.TITLE + " != '' AND " +  MediaStore.Audio.Media.IS_MUSIC + "=1";
//        try{
//            Cursor cursor = musicResolver.query(musicUri, columns, selection, null,MediaStore.Audio.Media.TITLE);
//            if (cursor != null && cursor.getCount() != 0){
//                resultCursor = cursor;
//                updateListFlag = true;
//                setDataFromCursor(resultCursor);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return data;
//    }
//
//    @Override
//    protected void onPostExecute(Object o) {
//        if(updateListFlag){ notifyDataSetChanged(); }
//    }


}
