package com.lsj.hdmi.contentreceivertest.model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

import com.lsj.hdmi.contentreceivertest.bean.MediaItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by hdmi on 17-5-3.
 */
public class MyAudioPlayer   {
    public static String TAG="MyAudiaPlayer";
    public static String ACTION_CHANGE_MUSIC="com.lsj.contentreceivertest.myaudioplayer.musicchange";
    public static String ACTION_CHANGE_DURATION="com.lsj.contentreceivertest.myaudioplayer.durationchange";

    private MediaPlayer mediaPlayer;
    public Context mContext;
    private boolean isPlaying;
    private int currentMusicIndex;
    private List<MediaItem> musicList=new ArrayList<MediaItem>();
    private MediaItem currentMediaItem;


    public static int BEFORE_QUERY=0;
    public static int AFTER_QUERY=1;

    //播放选项
    public int playType=SINGLE_ONCE;
    public int currentPlayTypeIndex=0;
    public static int SINGLE_ONCE=100;
    public static int SINGLE_CIRCLE=101;
    public static int SINGLE_NEXT=110;
    public static int RANDOM_NEXT=111;
    public int[] playTypes=new int[]{SINGLE_ONCE,SINGLE_CIRCLE,SINGLE_NEXT,RANDOM_NEXT};

    public MyAudioPlayer(Context mContext) {
        this.mContext = mContext;
        isPlaying=false;
        currentMusicIndex=0;
        playType=SINGLE_ONCE;
    }

    public  void stop(){
        Log.d(TAG, "stop: -------------------mediaIsNull");
        if (mediaPlayer!=null){
            mediaPlayer.release();
            mediaPlayer=null;
            isPlaying=false;
            Log.d(TAG, "stop: -------------------");
        }
    }

    public void pauseOrPlay(){
        if (mediaPlayer!=null){
            if (isPlaying){
                mediaPlayer.pause();
                isPlaying=false;
            }else {
                mediaPlayer.start();
                sendCurrentPosition();
                isPlaying=true;
            }
        }
    }


    public void play(int position) throws IOException{//跑出Exception给上层图形界面处理，文件可能损坏
        stop();
        if (mediaPlayer==null){
           // mediaPlayer.create(mContext,uri);  //创建时为空？？
            currentMediaItem =musicList.get(position);
            mediaPlayer=new MediaPlayer();
//            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mediaPlayer.setDataSource(mContext, currentMediaItem.getMusicUri());
//            mediaPlayer.prepare();
//            mediaPlayer.start();
            Log.d(TAG, "play: -----------------------context"+mContext);
            Log.d(TAG, "play: -----------------------uri"+ currentMediaItem.getMusicUri());
            Log.d(TAG, "play: -----------------------mediaPlayer"+mediaPlayer);

        }
        currentMusicIndex=position;
        setMediaPlayerData(mContext,currentMediaItem);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
        isPlaying=true;
    }

    private class MyOnCompletionListener implements MediaPlayer.OnCompletionListener{
        int musicSize=musicList.size();
        MediaItem nextMediaItem;
        @Override
        public void onCompletion(MediaPlayer mp){
            isPlaying=false;
            //单曲一次
            if (playType==SINGLE_ONCE){
                mediaPlayer.stop();
                isPlaying=false;
            }
            //单曲循环
            else  if (playType==SINGLE_CIRCLE){
                setMediaPlayerData(mContext,getCurrentMediaItem());
                isPlaying=true;
            }
            //单曲渐进
            else if (playType==SINGLE_NEXT||playType==RANDOM_NEXT){
                nextPlay();
            }
         else {
//                setPlayType(SINGLE_ONCE);
            }
            sendChangeMusicBroadcast();
        }
    }

    public void sendChangeMusicBroadcast(){
        Intent intent = new Intent(ACTION_CHANGE_MUSIC);
        long totalDuration=mediaPlayer.getDuration();
        intent.putExtra("totalDuration",totalDuration);
        mContext.sendBroadcast(intent);
        Log.d(TAG, "sendChangeMusicBroadcast: --------------------------");
    }

    public void nextPlay(){
        int musicSize=musicList.size();
        MediaItem nextMediaItem;
        isPlaying=false;
        Log.d(TAG, "nextPlay: -----------playtype========"+playType);
     if (playType==SINGLE_NEXT){

            currentMusicIndex=(currentMusicIndex+1)%musicSize;
            nextMediaItem=musicList.get(currentMusicIndex);
            setMediaPlayerData(mContext,nextMediaItem);

        }
        //随机播放
        else if (playType==RANDOM_NEXT){
            Random random=new Random();
            int randomIndex;
            do {
                randomIndex=random.nextInt(musicSize);
            }while (randomIndex==currentMusicIndex);
            currentMusicIndex=randomIndex;
            nextMediaItem=musicList.get(currentMusicIndex);

                setMediaPlayerData(mContext,nextMediaItem);


        }else {
//            setPlayType(SINGLE_ONCE);
         currentMusicIndex=(currentMusicIndex+1)%musicSize;
         nextMediaItem=musicList.get(currentMusicIndex);

             setMediaPlayerData(mContext,nextMediaItem);
             isPlaying=true;

        }
        sendChangeMusicBroadcast();
    }


    public void backPlay(){
        int musicSize=musicList.size();
        MediaItem nextMediaItem;
        isPlaying=false;
      if (playType==SINGLE_NEXT){

            currentMusicIndex=currentMusicIndex-1;
            if (currentMusicIndex<0){
                currentMusicIndex=musicSize-1;
            }
            nextMediaItem=musicList.get(currentMusicIndex);
            setMediaPlayerData(mContext,nextMediaItem);
        }
        //随机播放
        else if (playType==RANDOM_NEXT){
            Random random=new Random();
            int randomIndex;
            do {
                randomIndex=random.nextInt(musicSize);
            }while (randomIndex==currentMusicIndex);
            currentMusicIndex=randomIndex;
            nextMediaItem=musicList.get(currentMusicIndex);
            setMediaPlayerData(mContext,nextMediaItem);

        }else {
//            setPlayType(SINGLE_ONCE);
          currentMusicIndex=currentMusicIndex-1;
          if (currentMusicIndex<0){
              currentMusicIndex=musicSize-1;
          }
          nextMediaItem=musicList.get(currentMusicIndex);
          setMediaPlayerData(mContext,nextMediaItem);
        }
        sendChangeMusicBroadcast();
    }

    private void setMediaPlayerData(Context context, MediaItem nextMediaItem){
        if (mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(mContext,nextMediaItem.getMusicUri());
                mediaPlayer.prepare();
                mediaPlayer.start();
                isPlaying=true;
                sendCurrentPosition();
            } catch (IOException e) {
                e.printStackTrace();
                isPlaying=false;
                mediaPlayer.release();
            }

        }
    }



    //本地查找音乐
    public void queryMusic(final Handler handler){
        Message msg=new Message();
        msg.what=BEFORE_QUERY;
        handler.sendMessage(msg);
        new Thread(new Runnable() {
            @Override
            public void run() {
               // List<MediaItem> mediaItemsList=new ArrayList<MediaItem>();
                musicList.clear();
                String where = MediaStore.Audio.Media.DATA + " like \"%music%\"";
                String selection=MediaStore.Audio.Media.DATA;
                String[] searchKey = new String[] {
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Albums.ALBUM_ID,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DURATION
                };
                Uri uri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                ContentResolver contentResolver= null;
                contentResolver = mContext.getContentResolver();
                Cursor cursor=contentResolver.query(uri,searchKey,where,null,null);
                while(cursor.moveToNext()){
                    String path=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                    String artist=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    Uri musicUri = Uri.withAppendedPath(uri, id);
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                    int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID));
//            Uri albumUri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId);
                    Uri albumUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
                    MediaItem mediaItem=new MediaItem(mContext,musicUri,albumUri,name,duration,artist);
                    Log.d(TAG, "run: --------query------"+name);
                    musicList.add(mediaItem);
                }
                cursor.close();
                Message msg=new Message();
                msg.what=AFTER_QUERY;
                handler.sendMessage(msg);
            }
        }).start();

    }

    public int getCurrentMusicIndex() {
        return currentMusicIndex;
    }

    public void setCurrentMusicIndex(int currentMusicIndex) {
        this.currentMusicIndex = currentMusicIndex;
    }

    public int getPlayType() {
        return playType;
    }

    public void setPlayType(int playType) {
        this.playType = playType;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public List<MediaItem> getMusicList() {
        return musicList;
    }

    public void setMusicList(List<MediaItem> musicList) {
        this.musicList = musicList;
    }


    public MediaItem getCurrentMediaItem() {
        return musicList.get(currentMusicIndex);
    }

    //发送当前时间
    public void sendCurrentPosition(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isPlaying){
                    Log.d(TAG, "run: -----------------send-------------currentDuration"+Thread.currentThread().getName());
                    long currentDuration=mediaPlayer.getCurrentPosition();
                    Intent intent=new Intent(ACTION_CHANGE_DURATION);
                    intent.putExtra("currentDuration",currentDuration);
                    mContext.sendBroadcast(intent);
                    try {
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }



//    public void setCurrentPosition(){
//
//    }

    public long getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }

    public void seekTo(int position){
        mediaPlayer.seekTo(position);
        mediaPlayer.start();
    }

    public int changePlayType(){
        currentPlayTypeIndex=(currentPlayTypeIndex+1)%playTypes.length;
        playType=playTypes[currentPlayTypeIndex];
        return playTypes[currentPlayTypeIndex];
    }

}
