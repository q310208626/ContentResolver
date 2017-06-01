package com.lsj.hdmi.contentreceivertest.bean;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.IdRes;

import org.xutils.db.annotation.Table;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;

/**
 * Created by hdmi on 17-5-2.
 */
@Table(name="mediaitem")
public class MediaItem implements Parcelable{



    private int id;
    private Uri musicUri;
    private Uri albumUri;
    private String musicName;
    private String artistName;
    private long duration;




    public MediaItem(Uri musicUri, Uri albumUri, String musicName, long duration, String artistName) {

        this.musicUri = musicUri;
        this.albumUri = albumUri;
        this.musicName = musicName;
        this.duration=duration;
        this.artistName=artistName;
    }



    public Uri getMusicUri() {
        return musicUri;
    }


    public Uri getAlbumUri() {
        return albumUri;
    }


    public String getMusicName() {
        return musicName;
    }



    public long getDuration() {
        return duration;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setMusicUri(Uri musicUri) {
        this.musicUri = musicUri;
    }

    public void setAlbumUri(Uri albumUri) {
        this.albumUri = albumUri;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(musicUri,flags);
        dest.writeParcelable(albumUri,flags);
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

    private MediaItem() {

    }

    private MediaItem(Parcel in){

        musicUri=  in.readParcelable(Uri.class.getClassLoader());
        albumUri=  in.readParcelable(Uri.class.getClassLoader());
        musicName=in.readString();
        duration=in.readLong();
        artistName=in.readString();
    }
}
