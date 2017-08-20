package com.imran.wali.sharetango.UI.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.imran.wali.sharetango.DashboardActivity;
import com.imran.wali.sharetango.R;
import com.imran.wali.sharetango.Services.NetworkStatus;
import com.imran.wali.sharetango.UI.Elements.IndexableListView.IndexableListAdapter;
import com.imran.wali.sharetango.UI.Elements.IndexableListView.IndexableListView;
import com.imran.wali.sharetango.UI.Elements.IndexableListView.PullToRefreshIndexableListView;
import com.imran.wali.sharetango.audiomanager.MusicData;
import com.imran.wali.sharetango.datarepository.MusicDataRepository;
import com.karumi.dexter.PermissionToken;

/**
 * Created by wali on 07/02/17.
 */

public class AvailableSongsFragment extends PagerAdapterTabFragment {

    private PagerAdapterTabFragment.PageType pageType;
    private DashboardActivity mContext;
    private PullToRefreshIndexableListView pullToRefreshIndexableListView;
    private IndexableListView listView;
    private IndexableListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (DashboardActivity) getActivity();
        adapter = new IndexableListAdapter(mContext, PagerAdapterTabFragment.PageType.Available);
        //fetchSongs();
        MusicDataRepository.getInstance().registerAvilableMusicDataFragment(adapter);
    }

    private PullToRefreshBase.OnRefreshListener refreshListener = new PullToRefreshBase.OnRefreshListener() {
        @Override
        public void onRefresh(PullToRefreshBase refreshView) {
            NetworkStatus networkStatus = mContext.getNetworkStatus();
            if (networkStatus.equals(NetworkStatus.NO_CONNECTION)) {
                adapter.clear();
                Toast.makeText(mContext, getResources().getString(R.string.join_network), Toast.LENGTH_LONG).show();
            }
            updateLastUpdateLabel();
            pullToRefreshIndexableListView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pullToRefreshIndexableListView.onRefreshComplete();
                }
            }, 1000);
        }
    };

    private void updateLastUpdateLabel() {
        String label = DateUtils.formatDateTime(mContext,
                System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
        ILoadingLayout layout = pullToRefreshIndexableListView.getLoadingLayoutProxy();
        layout.setLastUpdatedLabel(getResources().getString(R.string.last_update) + " " + label);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.indexable_songs_fragment, container, false);
        pullToRefreshIndexableListView =  (PullToRefreshIndexableListView) view.findViewById(R.id.pull_to_refresh_lv);
        pullToRefreshIndexableListView.setOnRefreshListener(refreshListener);
        updateLastUpdateLabel();
        listView = pullToRefreshIndexableListView.getRefreshableView();
        listView.setAdapter(adapter);
        listView.setFastScrollEnabled(true);
        listView.setOnItemClickListener(songClickListener);
        return view;
    }

    private AdapterView.OnItemClickListener songClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MusicData song = adapter.getItem(position);
            ((DashboardActivity)getActivity()).getSalutService().request(song);
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

    public void clearSongs() {
        adapter.clear();
    }
}