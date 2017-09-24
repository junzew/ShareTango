package com.imran.wali.sharetango.audiomanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.imran.wali.sharetango.R;
import com.imran.wali.sharetango.Utility.Base64Utils;

import java.io.FileNotFoundException;

/**
 * Created by Wali on 19-Jul-15.
 * Edited by junzew
 */
@JsonObject
public class MusicData implements Parcelable{

    @JsonField public String owner;
    @JsonField public String title;
    @JsonField public String artist;
    @JsonField public String path;
    @JsonField public String albumArtURIString;
    @JsonField public String duration;
    @JsonField public long id;
    @JsonField public long albumId;
    // Base 64 encoded bitmap for cover art, only set before transferring data
    @JsonField public String encodedBitmapString;

    public MusicData() {};

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(path);
        dest.writeString(albumArtURIString);
        dest.writeString(duration);
        dest.writeLong(id);
        dest.writeLong(albumId);
        dest.writeString(encodedBitmapString);
    }

    public MusicData(Parcel in){
        title = in.readString();
        artist = in.readString();
        path = in.readString();
        albumArtURIString = in.readString();
        duration = in.readString();
        id = in.readLong();
        albumId = in.readLong();
        encodedBitmapString = in.readString();
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

    public long getId() {
        return id;
    }

    public long getAlbumId() {
        return albumId;
    }

    public String getDuration() {
        return duration;
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

    @Override
    public String toString() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MusicData)) return false;

        MusicData musicData = (MusicData) o;

        return id == musicData.id || (title.equals(musicData.title) && artist.equals(musicData.artist));

    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + artist.hashCode();
        return result;
    }

    public void prepareCoverArtForSending(Context context) {
        Bitmap bitmap;
        try {
            Uri uri = Uri.parse(albumArtURIString);
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            this.encodedBitmapString = Base64Utils.encodeBitmap(bitmap);
        } catch(FileNotFoundException fnfe) {
            bitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.default_album_art);
            this.encodedBitmapString = Base64Utils.encodeBitmap(bitmap);
            fnfe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

