package com.imran.wali.sharetango.datarepository;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.typeconverters.IntBasedTypeConverter;
import com.imran.wali.sharetango.audiomanager.MusicData;
import com.peak.salut.SalutDevice;

import java.util.List;


/**
 * Created by junze on 2017-04-30.
 */
@JsonObject
public class Packet {
    @JsonField public MusicData mMusicData;
    @JsonField public String base64string;
    @JsonField public List<MusicData> songList;
    @JsonField(typeConverter = MessageTypeConverter.class) public MessageType transactionType;
    @JsonField public SalutDevice srcDevice;
    @JsonField public SalutDevice dstDevice;

    public MusicData getmMusicData() {
        return mMusicData;
    }

    public void setmMusicData(MusicData mMusicData) {
        this.mMusicData = mMusicData;
    }

    public SalutDevice getSrcDevice() {
        return srcDevice;
    }

    public void setSrcDevice(SalutDevice srcDevice) {
        this.srcDevice = srcDevice;
    }

    public SalutDevice getDstDevice() {
        return dstDevice;
    }

    public void setDstDevice(SalutDevice dstDevice) {
        this.dstDevice = dstDevice;
    }

    public enum MessageType {
        SEND_SONG, SEND_SONG_LIST, REQUEST_SONG
    }

    static class MessageTypeConverter extends IntBasedTypeConverter<MessageType> {
        @Override
        public MessageType getFromInt(int i) {
            return MessageType.values()[i];
        }

        public int convertToInt(MessageType object) {
            int result;
            switch (object) {
                case SEND_SONG:
                    result = 0;
                    break;
                case SEND_SONG_LIST:
                    result = 1;
                    break;
                case REQUEST_SONG:
                    result = 2;
                    break;
                default:
                    result = -1;
                    break;
            }
            return result;
        }

    }

    public List<MusicData> getSongList() {
        return songList;
    }

    public void setSongList(List<MusicData> songList) {
        this.songList = songList;
    }

    public String getBase64string() {
        return base64string;
    }

    public void setBase64string(String base64string) {
        this.base64string = base64string;
    }

    public MusicData getMusicData() {
        return mMusicData;
    }

    public void setMusicData(MusicData musicData) {
        mMusicData = musicData;
    }

    public MessageType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(MessageType transactionType) {
        this.transactionType = transactionType;
    }
}
