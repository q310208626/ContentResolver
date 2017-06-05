package com.lsj.hdmi.contentreceivertest.model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

import com.lsj.hdmi.contentreceivertest.application.MyApp;
import com.lsj.hdmi.contentreceivertest.bean.MediaItem;

import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by hdmi on 17-5-3.
 */
public class MyAudioPlayer   {
    public static String TAG="MyAudiaPlayer";
    //广播信息
    public static String ACTION_CHANGE_MUSIC="com.lsj.contentreceivertest.myaudioplayer.musicchange";   //音乐切换广播
    public static String ACTION_CHANGE_DURATION="com.lsj.contentreceivertest.myaudioplayer.durationchange"; //当前音乐播放的时间广播

    private MediaPlayer mediaPlayer;
    public Context mContext;
    private boolean isPlaying;      //播放器状态
    private boolean isComplete;     //播放是否完毕
    private int currentMusicIndex;  //目前播放音乐的序号
    private List<MediaItem> musicList=new ArrayList<MediaItem>();
    private MediaItem currentMediaItem;     //目前播放的音乐


    private DbManager dbManager;


    //查询本地音乐参数
    public static int BEFORE_QUERY=0;
    public static int AFTER_QUERY=1;

    //播放选项
    public int playType=SINGLE_ONCE;
    public int currentPlayTypeIndex=0;
    public static int SINGLE_ONCE=100;      //单曲一次
    public static int SINGLE_CIRCLE=101;    //单曲循环
    public static int SINGLE_NEXT=110;      //单曲下一曲
    public static int RANDOM_NEXT=111;      //随机下一曲
    public int[] playTypes=new int[]{SINGLE_ONCE,SINGLE_CIRCLE,SINGLE_NEXT,RANDOM_NEXT};

    public MyAudioPlayer(Context mContext) {
        this.mContext = mContext;
        isPlaying=false;
        currentMusicIndex=0;
        playType=SINGLE_ONCE;



    }

    //停止播放
    public  void stop(){
        if (mediaPlayer!=null){

            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
            isPlaying=false;
        }
    }

    //暂停或者开始,由isPlaying做判断
    public void pauseOrPlay(){
        if (mediaPlayer!=null){
            if (isComplete){
                setMediaPlayerData(mContext,getCurrentMediaItem());
                isComplete=false;
            }
            else if (isPlaying){
                mediaPlayer.pause();
                isPlaying=false;
            }else {
                mediaPlayer.start();
                sendCurrentPosition();
                isPlaying=true;
            }

        }
    }

    //播放音乐
    public void play(int position) throws IOException{//跑出Exception给上层图形界面处理，文件可能损坏
        stop();
        if (mediaPlayer==null){
            currentMediaItem =musicList.get(position);
            mediaPlayer=new MediaPlayer();
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

    //播放完毕监听器,根据播放选项自动播放下一首或者停播
    private class MyOnCompletionListener implements MediaPlayer.OnCompletionListener{
        int musicSize=musicList.size();
        MediaItem nextMediaItem;
        @Override
        public void onCompletion(MediaPlayer mp){
            isPlaying=false;
            //单曲一次
            if (playType==SINGLE_ONCE){
                mediaPlayer.stop();
                isComplete=true;
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
            sendChangeMusicBroadcast();
        }
    }

    //发送音乐切换广播
    public void sendChangeMusicBroadcast(){
        Intent intent = new Intent(ACTION_CHANGE_MUSIC);
        long totalDuration=mediaPlayer.getDuration();
        intent.putExtra("totalDuration",totalDuration);
        mContext.sendBroadcast(intent);
        Log.d(TAG, "sendChangeMusicBroadcast: --------------------------");
    }

    //下一首
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

    //上一首
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

    //设置播放的音乐信息
    private void setMediaPlayerData(Context context, MediaItem nextMediaItem){
        if (mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.reset();
            try {
                Uri musicUri=Uri.parse(nextMediaItem.getMusicUri());
                mediaPlayer.setDataSource(mContext,musicUri);
                mediaPlayer.prepare();
                mediaPlayer.start();
                isPlaying=true;
                sendCurrentPosition();
            } catch (IOException e) {
                e.printStackTrace();
                isPlaying=false;
                mediaPlayer.release();
                mediaPlayer=new MediaPlayer();
            }

        }
    }



    //本地查找音乐
    public void queryMusic(final Handler handler){
        stop();
        Message msg=new Message();
        msg.what=BEFORE_QUERY;
        handler.sendMessage(msg);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // List<MediaItem> mediaItemsList=new ArrayList<MediaItem>();
                musicList.clear();
                String where = MediaStore.Audio.Media.DATA + " like \"%music%\"";
                String selection = MediaStore.Audio.Media.DATA;
                String[] searchKey = new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Albums.ALBUM_ID,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DURATION
                };
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                ContentResolver contentResolver = null;
                contentResolver = mContext.getContentResolver();
                Cursor cursor = contentResolver.query(uri, searchKey, where, null, null);
                dbManager=x.getDb(MyApp.daoConfig);
                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                    String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    Uri musicUri = Uri.withAppendedPath(uri, id);
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                    int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID));
//               Uri albumUri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId);
                    Uri albumUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
                    MediaItem mediaItem = new MediaItem(Integer.valueOf(id),musicUri.toString(), albumUri.toString(), name, artist,duration);
                    Log.d(TAG, "run: --------query------" + name);
                    musicList.add(mediaItem);
                }
                cursor.close();
                Message msg = new Message();
                msg.what = AFTER_QUERY;
                handler.sendMessage(msg);

                //临时变量,避免重复扫描musicList指向空
                ArrayList<MediaItem> tempMusicList = new ArrayList<MediaItem>();
                tempMusicList.addAll(musicList);

                //添加进数据库
                Observable.from(tempMusicList)
                        .map(new Func1<MediaItem, Void>() {
                            @Override
                            public Void call(MediaItem mediaItem) {
                                try {
                                    dbManager.save(mediaItem);
                                } catch (DbException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        }).subscribeOn(Schedulers.newThread())
                        .subscribe(new Subscriber<Void>() {
                            @Override
                            public void onStart() {
                                try {
                                    dbManager.dropTable(MediaItem.class);
                                } catch (DbException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onCompleted() {
                                Log.d(TAG, "onCompleted: -----------------save complete--");
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(Void aVoid) {

                            }
                        });

                //新线程查找专辑图片
                Observable.from(tempMusicList)
                        .map(new Func1<MediaItem, Object>() {
                            @Override
                            public Object call(MediaItem mediaItem) {
                                Bitmap bitmap = null;
                                try {
                                    bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), Uri.parse(mediaItem.getAlbumUri()));
                                    mediaItem.setAlbumBitmap(bitmap);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        }).subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Object>() {
                            @Override
                            public void onCompleted() {
                                Log.d(TAG, "onCompleted: --------------------bitmap query complete----");
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(Object o) {

                            }
                        });
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
        List<MediaItem> newList=new ArrayList<MediaItem>();
        newList=musicList;
        musicList.addAll(newList);
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
                    long currentDuration;
                    try{
                        currentDuration=mediaPlayer.getCurrentPosition();
                        Intent intent=new Intent(ACTION_CHANGE_DURATION);
                        intent.putExtra("currentDuration",currentDuration);
                        mContext.sendBroadcast(intent);
                    }catch (IllegalStateException e){
                        Log.d(TAG, "run: ------------------------illgalException");
                    }

                    try {
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    public long getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }


    //时间滑动到position
    public void seekTo(int position){
        mediaPlayer.seekTo(position);
        mediaPlayer.start();
        isPlaying=true;
        sendCurrentPosition();
    }

    //改变播放的类型
    public int changePlayType(){
        currentPlayTypeIndex=(currentPlayTypeIndex+1)%playTypes.length;
        playType=playTypes[currentPlayTypeIndex];
        return playTypes[currentPlayTypeIndex];
    }

}
