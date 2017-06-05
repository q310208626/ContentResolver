package com.lsj.hdmi.contentreceivertest.model;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.lsj.hdmi.contentreceivertest.bean.MediaItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hdmi on 17-5-3.
 */
public class MusicService extends Service {
    private String TAG="MusicService";
    private IBinder binder;
    private MyAudioPlayer myAudioPlayer;
    private List<MediaItem> musicList=new ArrayList<MediaItem>();

    @Override
    public void onCreate() {
        super.onCreate();
        binder=new MyBinder();
        myAudioPlayer=new MyAudioPlayer(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void stopMusic(){
        myAudioPlayer.stop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ---------------------------");
        super.onDestroy();
    }

    public class MyBinder extends Binder{
        public Service getService(){
            return  MusicService.this;
        }
    }

    public void playStart(int position) throws IOException{
        myAudioPlayer.play(position);
    }

    public void pleyPause(){
        if (myAudioPlayer!=null){
            myAudioPlayer.pauseOrPlay();
        }
    }

    public int getCurrentMusicIndex(){
        return myAudioPlayer.getCurrentMusicIndex();
    }

    public void queryMusic(final QueryMusicInterface queryMusicInterface){
        myAudioPlayer.queryMusic(new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 0:queryMusicInterface.beforeQuery(); break;
                    case 1:queryMusicInterface.afterQuery(); break;
                }
            }
        });
    }



    public List<MediaItem> getMusicList(){
        return myAudioPlayer.getMusicList();

    }

    public void setMusicList(List<MediaItem> musicList){
        myAudioPlayer.setMusicList(musicList);

    }

    public void setPlayType(int playType){
        myAudioPlayer.setPlayType(playType);
    }

    public int getPlayType(){
        return myAudioPlayer.getPlayType();
    }

    public void nextPlay(){
        myAudioPlayer.nextPlay();
    }
    public void backPlay(){
        myAudioPlayer.backPlay();
    }

    public boolean isPlaying(){
        return myAudioPlayer.isPlaying();
    }

    public interface QueryMusicInterface{
        void beforeQuery();
        void afterQuery();
    }



    public MediaItem getCurrentMediaItem(){
        return myAudioPlayer.getCurrentMediaItem();
    }

    public long getCurrentPosition(){
        return myAudioPlayer.getCurrentPosition();
    }

    public void seekTo(int position){
        myAudioPlayer.seekTo(position);
    }

    public int changPlayType(){
        return myAudioPlayer.changePlayType();
    }
}
