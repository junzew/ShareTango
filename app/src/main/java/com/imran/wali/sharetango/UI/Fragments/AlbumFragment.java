package com.imran.wali.sharetango.UI.Fragments;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.imran.wali.sharetango.DashboardActivity;
import com.imran.wali.sharetango.R;
import com.imran.wali.sharetango.UI.activity.AlbumActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wali on 07/04/16.
 */
public class AlbumFragment extends PagerAdapterTabFragment {

    public static final Uri ARTWORK_URI = Uri.parse("content://media/external/audio/albumart");

    DashboardActivity mDashboardActivity;
    ListView mListView;
    AlbumListAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDashboardActivity = (DashboardActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mListView = (ListView) view.findViewById(R.id.container);
        mAdapter = new AlbumListAdapter();
        mListView.setAdapter(mAdapter);
        getAlbums();
    }

    private void getAlbums() {
        new GetAlbumsTask().execute();
    }

    private class GetAlbumsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            ContentResolver musicResolver = mDashboardActivity.getContentResolver();
            Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String[] columns = new String[]{"DISTINCT " + MediaStore.Audio.Media.ALBUM , MediaStore.Audio.Media.ALBUM_ID};
            String selection = MediaStore.Audio.Media.TITLE + " != '' AND " + MediaStore.Audio.Media.IS_MUSIC + "=1";
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
                    String albumName = cursor.getString(cursor.getColumnIndex("album"));
                    long albumId = cursor.getLong(cursor.getColumnIndex("album_id"));
                    mAdapter.addAlbum(new Album(albumId, albumName));
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mAdapter.notifyDataSetChanged();
        }
    }

    private class Album {
        String albumName;
        long albumId;

        public Album(long albumId, String albumName) {
            this.albumId = albumId;
            this.albumName = albumName;
        }
    }

    private class AlbumListAdapter extends BaseAdapter {
        List<Album> mAlbumList = new ArrayList<>();
        @Override
        public int getCount() {
            return mAlbumList == null ? 0 : mAlbumList.size();
        }

        @Override
        public Object getItem(int position) {
            return mAlbumList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.listview_item, parent,false);
            }
            final Album album = mAlbumList.get(position);
            final TextView albumName = (TextView) convertView.findViewById(R.id.item_trackname);
            ImageView albumArt = (ImageView) convertView.findViewById(R.id.item_albumart);
            albumName.setText(album.albumName);
            Uri uri = ContentUris.withAppendedId(ARTWORK_URI, album.albumId);
            Picasso.with(getActivity())
                    .load(uri)
                    .placeholder(R.drawable.default_album_art)
                    .into(albumArt);
            convertView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mDashboardActivity, AlbumActivity.class);
                    intent.putExtra("AlbumName", album.albumName);
                    intent.putExtra("AlbumId", album.albumId);
                    startActivity(intent);
                }
            });
            return convertView;
        }

        public void addAlbum(Album album) {
            mAlbumList.add(album);
        }
    }
}
