package com.lsj.hdmi.contentreceivertest.view;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lsj.hdmi.contentreceivertest.adapter.MediaItemAdapter;
import com.lsj.hdmi.contentreceivertest.R;
import com.lsj.hdmi.contentreceivertest.application.MyApp;
import com.lsj.hdmi.contentreceivertest.bean.MediaItem;
import com.lsj.hdmi.contentreceivertest.model.MusicService;
import com.lsj.hdmi.contentreceivertest.model.MyAudioPlayer;

import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by hdmi on 17-5-2.
 */
public class MainFragment extends Fragment {

    public static String TAG="MainFragment";

    List<MediaItem> musicList=new ArrayList<MediaItem>();
    private ListView mediaListView;
    private MediaItemAdapter mediaItemAdapter;
    private ImageView bottomAlbumImageView;
    private ImageButton bottomControlImageButton;
    private CardView bottomCardView;
    private TextView bottomMusicNameTextView;

    private  Intent musicServiceIntent;
    private MusicService musicService;
    private Boolean isBindService;

    private Toast toast;
    private ProgressDialog progressDialog;

    private RelativeLayout bottomRelativeLayout;

    private DbManager dbManager;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_main,null);
        init(view);
        return  view;
    }



    //初始化
    private void init(View view){
        mediaListView= (ListView) view.findViewById(R.id.media_listview);
        bottomAlbumImageView = (ImageView) view.findViewById(R.id.bottombar_music_album);
        bottomControlImageButton = (ImageButton) view.findViewById(R.id.bottombar_control);
        bottomCardView = (CardView) view.findViewById(R.id.bottombar_cardview);
        bottomMusicNameTextView= (TextView) view.findViewById(R.id.bottombar_music_name);

        mediaItemAdapter=new MediaItemAdapter(getActivity(),musicList);
        mediaListView.setAdapter(mediaItemAdapter);

        bottomRelativeLayout= (RelativeLayout) view.findViewById(R.id.bottombar_layout);

        preDraw(view);
        setHasOptionsMenu(true);
        initListener();

    }

    private void initListener(){
        //listView点击事件
        mediaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaItem onclickMediaItem= (MediaItem) mediaItemAdapter.getItem(position);
                if (toast==null) {
                    toast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
                }
                try {
                    musicService.playStart(position);
                    bottomBarConfiguration(onclickMediaItem);
                } catch (IOException e) {
                    toast.setText("音乐文件可能损坏");
                    toast.show();
                    e.printStackTrace();
                }
            }
        });

        //bottomBar控制按钮
        bottomControlImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService!=null){
                    musicService.pleyPause();
                    setBottomControlImageButton();
                }

            }
        });

        bottomRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicList.size()>0){
                    int currentIndex=musicService.getCurrentMusicIndex();
                    MediaItem currentMusic=musicList.get(currentIndex);
                    DetailFragment detailFragment=new DetailFragment();
                    Boolean isPlaying=musicService.isPlaying();
                    long currentPosition=musicService.getCurrentPosition();
                    Bundle bundle=new Bundle();
                    bundle.putParcelable("currentMusic",currentMusic);
                    bundle.putBoolean("isPlaying",isPlaying);
                    bundle.putLong("currentPosition",currentPosition);
                    detailFragment.setArguments(bundle);
                    getActivity().getFragmentManager().beginTransaction().addToBackStack("MainFragment").replace(R.id.container,detailFragment).commit();
//                getActivity().getFragmentManager().beginTransaction().add(R.id.container,detailFragment,"detail").hide(MainFragment.this).commit();
                }
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        //绑定service
        Log.d(TAG, "onAttach: ----------------registService---------");
        musicServiceIntent=new Intent(context,MusicService.class);
        getActivity().startService(musicServiceIntent);
        isBindService=getActivity().bindService(musicServiceIntent,sc,context.BIND_ADJUST_WITH_ACTIVITY);

        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(MyAudioPlayer.ACTION_CHANGE_MUSIC);
        getActivity().registerReceiver(mainBroadCastRrceiver,intentFilter);
        progressDialog=new ProgressDialog(context);
        super.onAttach(context);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.d(TAG, "onHiddenChanged: ------------------------"+hidden);
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ----------------unregisterRecevicer---------");
        getActivity().unregisterReceiver(mainBroadCastRrceiver);
        if (isBindService){
            Log.d(TAG, "onDestroy: ----------------------stopService");
            musicService.stopMusic();
            musicService.stopSelf();
            getActivity().unbindService(sc);
            isBindService=false;
        }
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_main,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch (id){
            case R.id.search_music:
                musicService.queryMusic(new MusicService.QueryMusicInterface() {
                    @Override
                    public void beforeQuery() {
                        showProgressDialog();
                    }

                    @Override
                    public void afterQuery() {
                        musicList.clear();
                        musicList.addAll(musicService.getMusicList());
                        mediaItemAdapter.notifyDataSetChanged();
                        dismisProgressDialog();
                    }
                });

                break;

        }
        return super.onOptionsItemSelected(item);
    }



    public void setBottomControlImageButton(){

        if (musicService!=null){
            if(musicList.size()>0){
                if (musicService.isPlaying()){

                    bottomControlImageButton.setBackgroundResource(R.drawable.mediastart);
                }else {

                    bottomControlImageButton.setBackgroundResource(R.drawable.mediastop);
                }
            }
        }
    }

    //重绘bottomBar 专辑图片以及控制按钮，使其长宽相等
    private void preDraw(final View view){
        ViewTreeObserver vto=view.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                int bottomCardViewHeight= bottomCardView.getMeasuredHeight();
                int bottomCardViewWidth= bottomCardView.getMeasuredWidth();

                float height= (float) (bottomCardViewHeight*0.9);
                float width= height;
                ViewGroup.LayoutParams parmas= bottomAlbumImageView.getLayoutParams();
                parmas.height= (int) height;
                parmas.width= (int) width;
                bottomAlbumImageView.setLayoutParams(parmas);

                ViewGroup.LayoutParams controlImageButtonparmas= bottomControlImageButton.getLayoutParams();
                controlImageButtonparmas.height= (int) height;
                controlImageButtonparmas.width= (int) width;
                bottomControlImageButton.setLayoutParams(controlImageButtonparmas);
                return true;
            }
        });
    }

    //扫面音乐并添加到musicList中


    private ServiceConnection sc=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicService= (MusicService) ((MusicService.MyBinder)service).getService();
            musicService.queryMusicFromDB(new MusicService.QueryMusicInterface() {
                @Override
                public void beforeQuery() {
                    showProgressDialog();
                }

                @Override
                public void afterQuery() {
                    musicList.addAll(musicService.getMusicList());
                    mediaItemAdapter.notifyDataSetChanged();
                    dismisProgressDialog();
                }
            });

            Log.d(TAG, "onServiceConnected: --------------service has connect-----------"+musicService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService=null;
        }
    };

    private void bottomBarConfiguration(final MediaItem mediaItem){
        Uri albumUri=Uri.parse(mediaItem.getAlbumUri());
        if (albumUri!=null){
            bottomAlbumImageView.setImageBitmap(mediaItem.getAlbumBitmap());
        }else {
            bottomAlbumImageView.setBackgroundResource(R.mipmap.ic_launcher);
        }
        bottomAlbumImageView.setImageBitmap(mediaItem.getAlbumBitmap());
        bottomMusicNameTextView.setText(mediaItem.getMusicName());
        if (musicService!=null){
            if (musicService.isPlaying()){
                bottomControlImageButton.setBackgroundResource(R.drawable.mediastart);
            }else {
                bottomControlImageButton.setBackgroundResource(R.drawable.mediastop);
            }
        }
    }

    public void showProgressDialog(){
        progressDialog.setMessage("音乐扫描中");
        progressDialog.setTitle("扫描");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void dismisProgressDialog(){
        progressDialog.dismiss();
    }

    public BroadcastReceiver mainBroadCastRrceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if (action.equals(MyAudioPlayer.ACTION_CHANGE_MUSIC)){
                MediaItem currentMediaItem=musicService.getCurrentMediaItem();
                bottomBarConfiguration(currentMediaItem);
            }
        }
    };



}
