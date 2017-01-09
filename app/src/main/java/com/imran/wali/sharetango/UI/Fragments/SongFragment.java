package com.imran.wali.sharetango.UI.Fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.imran.wali.sharetango.AudioManager.MusicData;
import com.imran.wali.sharetango.DashboardActivity;
import com.imran.wali.sharetango.R;
import com.imran.wali.sharetango.UI.Elements.IndexableListView.IndexableListAdapter;
import com.imran.wali.sharetango.UI.Elements.IndexableListView.IndexableListView;
import com.imran.wali.sharetango.UI.activity.PlayActivity;
import com.imran.wali.sharetango.service.PlayService;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

/**
 * Created by wali on 07/04/16.
 */
public class SongFragment extends PagerAdapterTabFragment {

    private DashboardActivity mContext;
    private IndexableListView listView;
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

            Dexter.checkPermission(new PermissionListener() {

                @Override
                public void onPermissionGranted(PermissionGrantedResponse response) {
                    fetchSongs();
                }

                @Override
                public void onPermissionDenied(PermissionDeniedResponse response) {
                    Toast.makeText(mContext, "Permission Denied", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                    createDialog(token);
                }
            }, Manifest.permission.READ_EXTERNAL_STORAGE);
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
        listView =  (IndexableListView) view.findViewById(R.id.song_list);
        listView.setAdapter(adapter);
        listView.setFastScrollEnabled(true);
        listView.setOnItemClickListener(songClickListener);
        return view;
    }

    private AdapterView.OnItemClickListener songClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MusicData song = adapter.getItem(position);

            Intent intent = new Intent(getActivity(), PlayService.class);
            intent.putExtra("id", song.getId());
            getActivity().startService(intent);

            Intent i = new Intent(getActivity(), PlayActivity.class);
            i.putExtra("albumId", song.getAlbumId());
            i.putExtra("title", song.getTitle());
            startActivity(i);

        }
    };

    // create dialog showing users why the permission is needed
    private void createDialog(final PermissionToken token) {
        new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.permission_dialog_title))
                .setMessage(getString(R.string.permission_dialog_message))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        token.continuePermissionRequest();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        token.cancelPermissionRequest();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
