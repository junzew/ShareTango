package com.imran.wali.sharetango.UI.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.imran.wali.sharetango.DashboardActivity;
import com.imran.wali.sharetango.R;
import com.imran.wali.sharetango.UI.Elements.IndexableListView.IndexableListAdapter;
import com.imran.wali.sharetango.UI.Elements.IndexableListView.IndexableListView;
import com.imran.wali.sharetango.UI.Elements.IndexableListView.PullToRefreshIndexableListView;
import com.imran.wali.sharetango.audiomanager.MusicData;
import com.imran.wali.sharetango.audiomanager.PlaybackController;
import com.imran.wali.sharetango.datarepository.MusicDataRepository;
import com.karumi.dexter.PermissionToken;

/**
 * Created by wali on 30/12/15.
 */

public class LocalSongsFragment extends PagerAdapterTabFragment {

    private PagerAdapterTabFragment.PageType pageType;
    private DashboardActivity mContext;

    private IndexableListAdapter adapter;
    private PullToRefreshIndexableListView pullToRefreshIndexableListView;
    IndexableListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (DashboardActivity) getActivity();
        adapter = new IndexableListAdapter(mContext, PagerAdapterTabFragment.PageType.Local);
        MusicDataRepository.getInstance().refreshList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.indexable_songs_fragment, container, false);

        pullToRefreshIndexableListView = (PullToRefreshIndexableListView) view.findViewById(R.id.pull_to_refresh_lv);
        pullToRefreshIndexableListView.setOnRefreshListener(refreshListener);
//        ILoadingLayout layout = pullToRefreshIndexableListView.getLoadingLayoutProxy();
        // set header texts here
//        layout.setPullLabel("1");
//        layout.setLastUpdatedLabel("2");
//        layout.setRefreshingLabel("3");
//        layout.setReleaseLabel("4");
        updateLastUpdateLabel();
        listView = pullToRefreshIndexableListView.getRefreshableView();

        listView.setFastScrollEnabled(true); // must come before setting adapter
        listView.setAdapter(adapter);
        listView.setFastScrollEnabled(true);
        listView.setOnItemClickListener(songClickListener);

        return view;
    }

    private AdapterView.OnItemClickListener songClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MusicData song = adapter.getItem(position);
            PlaybackController.getInstance().start(song, true);
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

    private PullToRefreshBase.OnRefreshListener refreshListener = new PullToRefreshBase.OnRefreshListener() {
        @Override
        public void onRefresh(PullToRefreshBase refreshView) {
            MusicDataRepository.getInstance().refreshList();
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
        layout.setLastUpdatedLabel(getResources().getString(R.string.last_update)+" "+ label);
    }

    @Override
    public void onResume() {
        super.onResume();
        MusicDataRepository.getInstance().addSubscriber(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        MusicDataRepository.getInstance().removeSubscriber(adapter);
    }
}

