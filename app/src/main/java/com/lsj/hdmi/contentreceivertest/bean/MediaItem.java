package com.lsj.hdmi.contentreceivertest.bean;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by hdmi on 17-5-2.
 */
@Table(name="mediaitem")
public class MediaItem implements Parcelable{


    @Column(name ="id",isId = true)
    private int id;
    @Column(name = "musicUri")
    private String musicUri;
    @Column(name = "albumUri")
    private String albumUri;
    @Column(name = "musicName")
    private String musicName;
    @Column(name = "artistName")
    private String artistName;
    @Column(name = "duration")
    private long duration;

    private Bitmap albumBitmap;



    public  MediaItem() {

    }

    public MediaItem(int id, String musicUri, String albumUri, String musicName, String artistName, long duration) {
        this.id = id;
        this.musicUri = musicUri;
        this.albumUri = albumUri;
        this.musicName = musicName;
        this.artistName = artistName;
        this.duration = duration;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMusicUri() {
        return musicUri;
    }

    public void setMusicUri(String musicUri) {
        this.musicUri = musicUri;
    }

    public String getAlbumUri() {
        return albumUri;
    }

    public void setAlbumUri(String albumUri) {
        this.albumUri = albumUri;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Bitmap getAlbumBitmap() {
        return albumBitmap;
    }

    public void setAlbumBitmap(Bitmap albumBitmap) {
        this.albumBitmap = albumBitmap;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(musicUri);
        dest.writeString(albumUri);
        dest.writeString(musicName);
        dest.writeLong(duration);
        dest.writeString(artistName);

    }

    public static final Parcelable.Creator<MediaItem> CREATOR=new Parcelable.Creator<MediaItem>(){
        @Override
        public MediaItem createFromParcel(Parcel source) {
            return new MediaItem(source);
        }

        @Override
        public MediaItem[] newArray(int size) {
            return new MediaItem[size];
        }
    };



    private MediaItem(Parcel in){

        musicUri=  in.readParcelable(Uri.class.getClassLoader());
        albumUri=  in.readParcelable(Uri.class.getClassLoader());
        musicName=in.readString();
        duration=in.readLong();
        artistName=in.readString();
    }
}
