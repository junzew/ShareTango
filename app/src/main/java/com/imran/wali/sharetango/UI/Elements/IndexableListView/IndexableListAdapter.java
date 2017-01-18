package com.imran.wali.sharetango.UI.Elements.IndexableListView;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.imran.wali.sharetango.AudioManager.MusicData;
import com.imran.wali.sharetango.AudioManager.PlaybackController;
import com.imran.wali.sharetango.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Wali on 18-Jul-15.
 */
public class IndexableListAdapter extends BaseAdapter implements SectionIndexer{

    private HashMap<String,Integer> indexerHashMap;
    private Cursor resultCursor;
    private Context mContext;
    private LayoutInflater inflater;
    private String[] mSections = {"#","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};//"#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private List<MusicData> mMusicDataList = new ArrayList<>();

    public IndexableListAdapter(Context context){
        mContext = context;
        inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        indexerHashMap = new HashMap<String, Integer>();
    }

    @Override
    public int getCount() {
        return resultCursor == null ? 0 : resultCursor.getCount();
    }

    @Override
    public MusicData getItem(int position) {
        if(resultCursor != null){
            resultCursor.moveToPosition(position);
            MusicData musicData = new MusicData();
            musicData.title = resultCursor.getString(resultCursor.getColumnIndex("title"));
            musicData.artist= resultCursor.getString(resultCursor.getColumnIndex("artist"));
            musicData.path = resultCursor.getString(resultCursor.getColumnIndex("_data"));
            int songId = resultCursor.getInt(resultCursor.getColumnIndex("_id"));
            musicData.albumArtURIString = "content://media/external/audio/media/" + songId + "/albumart";
            musicData.duration = " ";
            musicData.id = songId;
            musicData.albumId = resultCursor.getLong(resultCursor.getColumnIndex("album_id"));
            //duration
            return musicData;
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if( convertView == null){
            //This is the first time we are seeing this
            convertView = inflater.inflate(R.layout.listview_item,parent,false);
            holder = new ViewHolder();
            holder.tracktitle = (TextView) convertView.findViewById(R.id.item_trackname);
            holder.artist = (TextView) convertView.findViewById(R.id.item_artistname);
            holder.duration = (TextView) convertView.findViewById(R.id.item_duration);
            holder.albumart = (ImageView) convertView.findViewById(R.id.item_albumart);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }
        MusicData itemMusicData = getItem(position);
        if( itemMusicData != null) {
            holder.tracktitle.setText(itemMusicData.getTitle());
            holder.artist.setText(itemMusicData.getArtist());
            //holder.duration.setText(itemMusicData);
            Picasso.with(mContext).load(itemMusicData.getAlbumArtURI()).placeholder(R.drawable.track_ablumart_placeholder).into(holder.albumart);
        }
        return convertView;
    }

    @Override
    public Object[] getSections() {
        return mSections;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        for(int i = sectionIndex; i >= 0; i--){
            if (indexerHashMap.containsKey(mSections[i])) {
                //if the musicTitle matches the section of
                return indexerHashMap.get(mSections[i]);
            }
        }
        return 0;// Couldnt find anything;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    public class ViewHolder{
        TextView tracktitle;
        TextView artist;
        TextView duration;
        ImageView albumart;
    }

    /*
    Async Task to get Songs
    */

    public void fetchSongListAsync(){
        new GetSongsTask().execute();
    }

    private void setDataFromCursor(Cursor cursor){
        resultCursor = cursor;
        indexerHashMap.clear();
        for (int i = 0; i < resultCursor.getCount(); i++) {
            resultCursor.moveToPosition(i);
            String stringResult = resultCursor.getString(resultCursor.getColumnIndex("title"));
            if (stringResult.startsWith("a ") || stringResult.startsWith("A ")) {
                stringResult = stringResult.substring(2);
            }
            if (stringResult.startsWith("an ") || stringResult.startsWith("An ")) {
                stringResult = stringResult.substring(3);
            }
            if (stringResult.startsWith("the ") || stringResult.startsWith("The ")) {
                stringResult = stringResult.substring(4);
            }
            String musicTitleFirstLetter = stringResult.substring(0,1).toUpperCase();
            if (!indexerHashMap.containsKey(musicTitleFirstLetter)) {
                indexerHashMap.put(musicTitleFirstLetter, i);
            }
        }

        addToQueue(cursor);
    }


    private void addToQueue(Cursor cursor) {
        if (cursor != null && cursor.getCount() != 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                String songTitle = cursor.getString(cursor.getColumnIndex("title"));
                long albumId = cursor.getLong(cursor.getColumnIndex("album_id"));
                long songId = cursor.getLong(cursor.getColumnIndex("_ID"));
                MusicData data = new MusicData();
                data.title = songTitle;
                data.id = songId;
                data.albumId = albumId;
                data.artist= resultCursor.getString(resultCursor.getColumnIndex("artist"));
                data.path = resultCursor.getString(resultCursor.getColumnIndex("_data"));
                mMusicDataList.add(data);
                PlaybackController.getInstance().enqueue(data);
            }
        }
    }

    private class GetSongsTask extends AsyncTask <Object,Object,Object>{

        boolean updateListFlag;

        @Override
        protected Object doInBackground(Object[] params) {
            updateListFlag = false;
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
            try{
                Cursor cursor = musicResolver.query(musicUri, columns, selection, null,MediaStore.Audio.Media.TITLE);
                if (cursor != null && cursor.getCount() != 0){
                    resultCursor = cursor;
                    updateListFlag = true;
                    setDataFromCursor(resultCursor);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if(updateListFlag){ notifyDataSetChanged(); }
        }
    }
}
