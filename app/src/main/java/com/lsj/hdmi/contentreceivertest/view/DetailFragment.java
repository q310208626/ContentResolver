package com.lsj.hdmi.contentreceivertest.view;


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lsj.hdmi.contentreceivertest.R;
import com.lsj.hdmi.contentreceivertest.bean.MediaItem;
import com.lsj.hdmi.contentreceivertest.model.MusicService;
import com.lsj.hdmi.contentreceivertest.model.MyAudioPlayer;

import java.text.SimpleDateFormat;

/**
 * Created by hdmi on 17-5-25.
 */
public class DetailFragment extends Fragment {
    public static String TAG="DetablFragment";
    private MediaItem currentMusic;
    private MusicService musicService;

    private RelativeLayout botomBarLayout;
    private TextView nameTextView;
    private TextView durationTextView;//时间
    private ImageView detailImageView;//专辑图
    private ImageButton backButton;
    private ImageButton playButton;
    private ImageButton aheadButton;
    private SeekBar durationSeekBar;    //时间seekbar
    private CardView bottomBarCardView;
    private ImageButton playTypeButton;
    private TextView currentDurationTextView;

    private SimpleDateFormat simpleDateFormat;




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.detail_fragment,null);
        currentMusic=getArguments().getParcelable("currentMusic");
        boolean isPlaying=getArguments().getBoolean("isPlaying");
        Log.d(TAG, "onCreateView: ---------------create--currentMusic-----------"+currentMusic);
        init(view);
        if (!isPlaying){
            playButton.setBackgroundResource(R.drawable.mediastop);
        }else {
            playButton.setBackgroundResource(R.drawable.mediastart);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach: ----------------------");
        Intent intent=new Intent(context,MusicService.class);
        context.startService(intent);
        context.bindService(intent,serviceConnection,context.BIND_AUTO_CREATE);

        IntentFilter musicChangeIntentFilter = new IntentFilter();
        musicChangeIntentFilter.addAction(MyAudioPlayer.ACTION_CHANGE_MUSIC); //为BroadcastReceiver指定action，即要监听的消息名字。
        context.registerReceiver(myBroadcastReceiver,musicChangeIntentFilter); //注册监听

        IntentFilter durationChangeIntentFilter = new IntentFilter();
        durationChangeIntentFilter.addAction(MyAudioPlayer.ACTION_CHANGE_DURATION);
        context.registerReceiver(myBroadcastReceiver,durationChangeIntentFilter);
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
        getActivity().unbindService(serviceConnection);
        getActivity().unregisterReceiver(myBroadcastReceiver);
        super.onDestroyView();
    }

    private void init(View view){
        nameTextView= (TextView) view.findViewById(R.id.detail_textview_detailshow);
        durationSeekBar= (SeekBar) view.findViewById(R.id.detail_seekbar_duration);
        durationTextView= (TextView) view.findViewById(R.id.detail_textview_duration);
        backButton= (ImageButton) view.findViewById(R.id.detail_button_back);
        aheadButton= (ImageButton) view.findViewById(R.id.detail_button_ahead);
        playButton= (ImageButton) view.findViewById(R.id.detail_button_play);
        detailImageView= (ImageView) view.findViewById(R.id.detail_imageview_show);
        botomBarLayout= (RelativeLayout) view.findViewById(R.id.detail_bottombar);
        playTypeButton= (ImageButton) view.findViewById(R.id.detail_button_playtype);
        bottomBarCardView= (CardView) view.findViewById(R.id.detail_bottombar_cardview);
        currentDurationTextView= (TextView) view.findViewById(R.id.detail_textview_currentduration);
        Log.d(TAG, "init: ---------------"+currentMusic.getMusicName()+"\n"+currentMusic.getDuration()+"\n"+currentMusic.getArtistName()+"\n");

        //重绘视图
        reMeasureImage(view);

        if (currentMusic!=null){
            setMusicData(currentMusic);
        }

        //初始化监听器
        initListener();
    }

    //初始化监听器
    private void initListener(){
        playTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int playType=musicService.changPlayType();
                if (playType==MyAudioPlayer.SINGLE_ONCE){
                    playTypeButton.setBackgroundResource(R.drawable.mediaplaytype_justonce);
                }else if(playType==MyAudioPlayer.SINGLE_CIRCLE){
                    playTypeButton.setBackgroundResource(R.drawable.mediaplaytype_oncecircle);
                }else if (playType==MyAudioPlayer.SINGLE_NEXT){
                    playTypeButton.setBackgroundResource(R.drawable.mediaplaytype_oncenext);
                }else if (playType==MyAudioPlayer.RANDOM_NEXT){
                    playTypeButton.setBackgroundResource(R.drawable.mediaplaytype_ramdon);
                }
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.pleyPause();
                setBottomPlayButtonImage();
            }
        });

        aheadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.nextPlay();
                playButton.setBackgroundResource(R.drawable.mediastart);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.backPlay();
                playButton.setBackgroundResource(R.drawable.mediastart);
            }
        });

        durationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
               musicService.seekTo(seekBar.getProgress());
                setBottomPlayButtonImage();
                currentDurationTextView.setText(simpleDateFormat.format(seekBar.getProgress()));
            }
        });
    }

    //重绘图形,使其长宽相等
    private void reMeasureImage(final View view){
        ViewTreeObserver vto=view.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                //专辑图


                int detailImageViewWidth=detailImageView.getMeasuredWidth();
                ViewGroup.LayoutParams params=detailImageView.getLayoutParams();
                params.height=detailImageViewWidth;
                detailImageView.setLayoutParams(params);


                //按钮
                int bottombarHeight=bottomBarCardView.getMeasuredHeight();

                ViewGroup.LayoutParams params1=backButton.getLayoutParams();
                params1.width= (int) (bottombarHeight*0.5);
                params1.height=(int) (bottombarHeight*0.5);
                backButton.setLayoutParams(params1);

                ViewGroup.LayoutParams params2=aheadButton.getLayoutParams();
                params2.width=(int) (bottombarHeight*0.5);
                params2.height=(int) (bottombarHeight*0.5);
                aheadButton.setLayoutParams(params2);


                ViewGroup.LayoutParams params3=playButton.getLayoutParams();
                params3.width=(int) (bottombarHeight*0.75);
                params3.height=(int) (bottombarHeight*0.75);
                playButton.setLayoutParams(params3);

                ViewGroup.LayoutParams params4=playTypeButton.getLayoutParams();
                params4.width=(int) (bottombarHeight*0.3);
                params4.height=(int) (bottombarHeight*0.3);
                playTypeButton.setLayoutParams(params4);
                return  true;
            }
        });
    }

    public void setBottomPlayButtonImage(){
        if (musicService!=null){
            if (musicService.isPlaying()){
                playButton.setBackgroundResource(R.drawable.mediastart);
            }else {
                playButton.setBackgroundResource(R.drawable.mediastop);
            }
        }else {
            Log.d(TAG, "setBottomPlayButtonImage: ------------Service is null------------");
        }
    }

    private void setMusicData(MediaItem mediaItem){
        detailImageView.setImageBitmap(currentMusic.getAlbumBitmap());
        nameTextView.setText(currentMusic.getMusicName());
        simpleDateFormat=new SimpleDateFormat("mm:ss");
        long totalDuration=currentMusic.getDuration();
        durationSeekBar.setMax((int) totalDuration);
        String duration=simpleDateFormat.format(totalDuration);
        durationTextView.setText(duration);
        currentDurationTextView.setText("00:00");

    }

    private ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicService=(MusicService) ((MusicService.MyBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };



    BroadcastReceiver myBroadcastReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if (action.equals(MyAudioPlayer.ACTION_CHANGE_MUSIC)){
                currentMusic=musicService.getCurrentMediaItem();
                long totalDuration=intent.getLongExtra("totalDuration",0);
                durationSeekBar.setMax((int) totalDuration);
                setMusicData(currentMusic);
                setBottomPlayButtonImage();
            }
            else if(action.equals(MyAudioPlayer.ACTION_CHANGE_DURATION)){

                long currentDuration=intent.getLongExtra("currentDuration",0);
                durationSeekBar.setProgress((int) currentDuration);
                String currentDurationFormat=simpleDateFormat.format(currentDuration);
                currentDurationTextView.setText(currentDurationFormat);
            }
        }
    };
}
