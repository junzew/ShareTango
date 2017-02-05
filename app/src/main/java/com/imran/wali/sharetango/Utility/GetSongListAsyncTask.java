//package com.imran.wali.sharetango.Utility;
//
//import android.content.ContentResolver;
//import android.content.Context;
//import android.database.Cursor;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.provider.MediaStore;
//import android.widget.Adapter;
//import android.widget.ProgressBar;
//
//import com.imran.wali.sharetango.AudioManager.MusicData;
//
//import java.lang.ref.WeakReference;
//import java.util.ArrayList;
//
///**
// * Created by wali on 31/01/17.
// */
//
//public class GetSongListAsyncTask extends AsyncTask<Object,Object,ArrayList<MusicData>> {
//
//    private Context mContext;
//
//    // If List
//    private WeakReference<Adapter> listAdapter;
//
//    // If Progress Bar
//    private WeakReference<ProgressBar> progressBar;
//
//    public GetSongListAsyncTask(Context c, Adapter listAdapter){
//        this.listAdapter = new WeakReference<>(listAdapter);
//        mContext = c;
//    }
//
//    public GetSongListAsyncTask(Context c,ProgressBar progressBar){
//        this.progressBar = new WeakReference<>(progressBar);
//        mContext = c;
//    }
//
//    public GetSongListAsyncTask(Context c){
//        mContext = c;
//    }
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
//}