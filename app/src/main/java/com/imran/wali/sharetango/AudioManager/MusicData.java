package com.imran.wali.sharetango.AudioManager;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.net.URI;

/**
 * Created by Wali on 19-Jul-15.
 */
public class MusicData implements Parcelable{

    public String title;
    public String artist;
    public String path;
    public String albumArtURIString;
    public String duration;

    public MusicData() {};

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(path);
        dest.writeString(albumArtURIString);
        dest.writeString(duration);
    }

    public MusicData(Parcel in){
        title = in.readString();
        artist = in.readString();
        path = in.readString();
        albumArtURIString = in.readString();
        duration = in.readString();
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    @Override
    public int describeContents() {  return 0;}

    public static final Parcelable.Creator<MusicData> CREATOR = new Creator<MusicData>(){
        @Override
        public MusicData[] newArray(int size) {
            return new MusicData[size];
        }

        @Override
        public MusicData createFromParcel(Parcel in) {
            return new MusicData(in);
        }
    };

    public Uri getAlbumArtURI() {
        return Uri.parse(albumArtURIString);
    }

}

