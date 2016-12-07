package com.imran.wali.sharetango.UI.Fragments;

import android.support.v4.app.Fragment;
/**
 * Created by wali on 30/12/15.
 */

public abstract class PagerAdapterTabFragment extends Fragment {

    public enum PageType {
        SONG,
        ARTIST,
        GENRE,
        ALBUMS;
    }

    public static PagerAdapterTabFragment newInstance(PageType type) {
        //Bundle args = new Bundle();
        //args.putInt(ARG_PAGE, page);
        switch (type){
            case SONG:
                return new SongFragment();
            case ARTIST:
                return new ArtistFragment();
            case GENRE:
                return  new GenreFragment();
            case ALBUMS:
                return  new AlbumFragment();
        }
        //fragment.setArguments(args);
        return null;
    }
}
