package com.imran.wali.sharetango.UI.activity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.imran.wali.sharetango.AudioManager.MusicData;
import com.imran.wali.sharetango.AudioManager.PlaybackController;
import com.imran.wali.sharetango.R;
import com.squareup.picasso.Picasso;

import static com.imran.wali.sharetango.UI.Fragments.AlbumFragment.ARTWORK_URI;

/**
 * Activity for showing albums
 */
public class AlbumActivity extends AppCompatActivity {

    TextView mAlbumName;
    ImageView mAlbumCover;
    ListView mListView;
    ArrayAdapter<MusicData> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        initViews();
        long albumID = getIntent().getLongExtra("AlbumId", 0);
        getSongs(albumID);
    }

    private void initViews() {
        mAlbumName = (TextView) findViewById(R.id.album_name);
        mAlbumCover = (ImageView) findViewById(R.id.album_cover);
        mListView = (ListView) findViewById(R.id.lv_album);
        mAdapter = new AlbumSongListAdapter(this, android.R.layout.simple_list_item_1);
        mListView.setAdapter(mAdapter);
        Intent i = getIntent();
        String albumName = i.getStringExtra("AlbumName");
        long albumID = i.getLongExtra("AlbumId", 0);
        mAlbumName.setText(albumName);
        Uri uri = ContentUris.withAppendedId(ARTWORK_URI, albumID);
        Picasso.with(this)
                .load(uri)
                .placeholder(R.drawable.track_ablumart_placeholder)
                .into(mAlbumCover);
    }

    private void getSongs(long albumID) {
        new GetSongsTask().execute(albumID);
    }

    private class GetSongsTask extends AsyncTask<Long, Void, Void> {

        @Override
        protected Void doInBackground(Long... params) {
            ContentResolver musicResolver = getContentResolver();
            Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String[] columns = new String[]{MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media._ID};
            String selection = MediaStore.Audio.Media.TITLE + " != '' AND " +
                    MediaStore.Audio.Media.IS_MUSIC + " = 1 " +
                    " AND " + MediaStore.Audio.Media.ALBUM_ID + "=" + params[0];
            try {
                Cursor cursor = musicResolver.query(musicUri, columns, selection, null, MediaStore.Audio.Media.TITLE);
                setResult(cursor);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private void setResult(Cursor cursor) {
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
                    mAdapter.add(data);
                    PlaybackController.getInstance().enqueue(data);
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mAdapter.notifyDataSetChanged();
        }
    }

    private class AlbumSongListAdapter extends ArrayAdapter<MusicData> {

        public AlbumSongListAdapter(Context context, int resource) {
            super(context, resource);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // start playing song
                    MusicData song = getItem(position);
                    PlaybackController.getInstance().start(song, true);
                }
            });
            return view;
        }
    }
}
