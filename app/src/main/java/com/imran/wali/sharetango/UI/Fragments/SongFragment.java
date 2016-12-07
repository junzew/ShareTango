package com.imran.wali.sharetango.UI.Fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.imran.wali.sharetango.DashboardActivity;
import com.imran.wali.sharetango.R;
import com.imran.wali.sharetango.UI.Elements.IndexableListView.IndexableListAdapter;

import static com.imran.wali.sharetango.DashboardActivity.PERMISSION_REQUEST_READ_STORAGE;

/**
 * Created by wali on 07/04/16.
 */
public class SongFragment extends PagerAdapterTabFragment {

    private DashboardActivity mContext;
    private ListView listView;
    private IndexableListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (DashboardActivity) getActivity();
        adapter = new IndexableListAdapter(mContext);
        getPermissionAndFetch();
    }

    // deal with Android 6 runtime permissions and fetch songs
    private void getPermissionAndFetch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!haveReadAccessToExternalStorage()) {
                requestReadStoragePermission();
            } else {
                fetchSongs();
            }
        } else {
           fetchSongs();
        }
    }

    // get a list of songs on phone
    public void fetchSongs() {
        adapter.fetchSongListAsync();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.songs_fragment, container, false);
        listView =  (ListView) view.findViewById(R.id.song_list);
        listView.setAdapter(adapter);
        listView.setFastScrollEnabled(true);
        listView.setOnItemClickListener(songClickListener);
        return view;
    }

    private AdapterView.OnItemClickListener songClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(mContext,"YO!",Toast.LENGTH_LONG).show();
        }
    };

    private void requestReadStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(mContext,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            createDialog();
        } else {
            try {
                ActivityCompat.requestPermissions(mContext,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_READ_STORAGE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // returns true if can read files on external storage
    private boolean haveReadAccessToExternalStorage() {
        return ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    // create dialog showing users why the permission is needed
    private void createDialog() {
        new AlertDialog.Builder(mContext)
                .setTitle("Permission needed")
                .setMessage("ShareTango needs read permission in order to play songs on your phone")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        ActivityCompat.requestPermissions(mContext,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                PERMISSION_REQUEST_READ_STORAGE);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
