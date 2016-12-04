package com.imran.wali.sharetango.UI.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.imran.wali.sharetango.DashboardActivity;
import com.imran.wali.sharetango.R;
import com.imran.wali.sharetango.UI.Elements.IndexableListView.IndexableListAdapter;
import com.imran.wali.sharetango.UI.Elements.IndexableListView.IndexableListView;

/**
 * Created by wali on 07/04/16.
 */
public class SongFragment extends PagerAdapterTabFragment {

    private DashboardActivity mContext;
    ListView listView;
    IndexableListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (DashboardActivity) getActivity();
        adapter = new IndexableListAdapter(mContext);
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
            Toast.makeText(mContext,"YO!",Toast.LENGTH_LONG);
        }
    };
}
