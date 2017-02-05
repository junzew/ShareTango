package com.imran.wali.sharetango.UI.Elements.IndexableListView;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.imran.wali.sharetango.AudioManager.MusicData;
import com.imran.wali.sharetango.DataRepository.MusicDataRepository;
import com.imran.wali.sharetango.R;
import com.imran.wali.sharetango.Utility.MusicDataChangeListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Wali on 18-Jul-15.
 */
public class IndexableListAdapter extends BaseAdapter implements SectionIndexer{

    private ArrayList<MusicData> dataHolder;
    private HashMap<String, Integer> indexerHashMap;
    private Context mContext;
    private LayoutInflater inflater;
    private String[] mSections = {"#", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};//"#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public IndexableListAdapter(Context context, Cursor cursor) {
        mContext = context;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        indexerHashMap = new HashMap<String, Integer>();
        dataHolder = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return MusicDataRepository.getInstance().getList().size();
    }

    @Override
    public MusicData getItem(int position) {
        return dataHolder.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        TextView tracktitle;
        TextView artist;
        TextView duration;
        ImageView albumart;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            //This is the first time we are seeing this
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
            holder = new ViewHolder();
            holder.tracktitle = (TextView) convertView.findViewById(R.id.item_trackname);
            holder.artist = (TextView) convertView.findViewById(R.id.item_artistname);
            holder.duration = (TextView) convertView.findViewById(R.id.item_duration);
            holder.albumart = (ImageView) convertView.findViewById(R.id.item_albumart);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        MusicData itemMusicData = getItem(position);
        if (itemMusicData != null) {
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
        for (int i = sectionIndex; i >= 0; i--) {
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

    @Override
    public void onProgressChanged(int progress) {
    }

    @Override
    public void onComplete() {
        dataHolder = MusicDataRepository.getInstance().getList();
        indexerHashMap.clear();
        for (int i = 0; i < dataHolder.size(); i++) {
            String stringResult = dataHolder.get(i).getTitle();
            if (stringResult.startsWith("a ") || stringResult.startsWith("A ")) {
                stringResult = stringResult.substring(2);
            }
            if (stringResult.startsWith("an ") || stringResult.startsWith("An ")) {
                stringResult = stringResult.substring(3);
            }
            if (stringResult.startsWith("the ") || stringResult.startsWith("The ")) {
                stringResult = stringResult.substring(4);
            }
            String musicTitleFirstLetter = stringResult.substring(0, 1).toUpperCase();
            if (!indexerHashMap.containsKey(musicTitleFirstLetter)) {
                indexerHashMap.put(musicTitleFirstLetter, i);
            }
            notifyDataSetChanged();
        }

    }
}
