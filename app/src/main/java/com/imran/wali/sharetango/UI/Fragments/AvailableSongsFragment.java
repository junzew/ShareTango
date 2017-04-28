package com.imran.wali.sharetango.UI.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.imran.wali.sharetango.DashboardActivity;
import com.imran.wali.sharetango.R;
import com.imran.wali.sharetango.UI.Elements.IndexableListView.IndexableListAdapter;
import com.imran.wali.sharetango.UI.Elements.IndexableListView.PullToRefreshIndexableListView;
import com.karumi.dexter.PermissionToken;

/**
 * Created by wali on 07/02/17.
 */

public class AvailableSongsFragment extends PagerAdapterTabFragment {

    private PagerAdapterTabFragment.PageType pageType;
    private DashboardActivity mContext;
    private PullToRefreshIndexableListView listView;
    private ListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (DashboardActivity) getActivity();
        adapter = new IndexableListAdapter(mContext, PagerAdapterTabFragment.PageType.Available);
        //fetchSongs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.non_indexable_songs_fragment, container, false);
        listView =  (PullToRefreshIndexableListView) view.findViewById(R.id.pull_to_refresh_lv);
//        listView.setAdapter(adapter);
//        //listView.setFastScrollEnabled(true);
//        listView.setOnItemClickListener(songClickListener);
        return view;
    }

    private AdapterView.OnItemClickListener songClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //MusicData song = adapter.getItem(position);
            //PlaybackController.start(getActivity(), song);
        }
    };

    // create dialog showing users why the permission is needed
    private void createPermissionDialog(final PermissionToken token) {
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