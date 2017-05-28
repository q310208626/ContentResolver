package com.lsj.hdmi.contentreceivertest.view;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lsj.hdmi.contentreceivertest.MediaItemAdapter;
import com.lsj.hdmi.contentreceivertest.R;
import com.lsj.hdmi.contentreceivertest.bean.MediaItem;
import com.lsj.hdmi.contentreceivertest.model.MusicService;
import com.lsj.hdmi.contentreceivertest.model.MyAudioPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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


    private MusicService musicService;
    private Toast toast;
    private ProgressDialog progressDialog;

    private RelativeLayout bottomRelativeLayout;

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

        //listView点击事件
        mediaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaItem onclickMediaItem= (MediaItem) mediaItemAdapter.getItem(position);
                if (toast==null) {
                    toast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
                }
                try {
                    bottomBarConfiguration(onclickMediaItem);
                        musicService.playStart(position);
                    } catch (IOException e) {
                        toast.setText("音乐文件可能损坏");
                        toast.show();
                        e.printStackTrace();
                    }
                toast.setText(onclickMediaItem.getMusicName());
                toast.show();

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
                Bundle bundle=new Bundle();
                bundle.putParcelable("currentMusic",currentMusic);
                detailFragment.setArguments(bundle);
                getActivity().getFragmentManager().beginTransaction().addToBackStack("").replace(R.id.container,detailFragment).commit();
                }
            }
        });

    }


    @Override
    public void onAttach(Context context) {
        //绑定service
        Log.d(TAG, "onAttach: ------------------");
        Intent musicServiceIntent=new Intent(context,MusicService.class);
        getActivity().startService(musicServiceIntent);
        getActivity().bindService(musicServiceIntent,sc,context.BIND_AUTO_CREATE);
        progressDialog=new ProgressDialog(context);
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
       // getActivity().unbindService(sc);
        super.onDestroyView();
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
                Log.d(TAG, "onOptionsItemSelected: ------------query");
                musicService.queryMusic(new MusicService.QueryMusicInterface() {
                    @Override
                    public void beforeQuery() {
                        Log.d(TAG, "beforeQuery: ------------------");
                        showProgressDialog();
                    }

                    @Override
                    public void afterQuery() {
                        Log.d(TAG, "afterQuery: -----------------------");
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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService=null;
        }
    };

    private void bottomBarConfiguration(MediaItem mediaItem){
        Bitmap albumBitmap=mediaItem.getAlbumBitmap();
        if (albumBitmap!=null){
            bottomAlbumImageView.setImageBitmap(albumBitmap);
            bottomMusicNameTextView.setText(mediaItem.getMusicName());
        }
    }

    public void showProgressDialog(){
        progressDialog.setMessage("音乐扫描中");
        progressDialog.setTitle("扫描");
        progressDialog.show();
    }

    public void dismisProgressDialog(){
        progressDialog.dismiss();
    }

    @Override
    public void onDestroy() {
        getActivity().unbindService(sc);
        super.onDestroy();
    }

//    Handler handler=new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what){
//                case 1:
//                    musicList.addAll((Collection<? extends MediaItem>) msg.obj) ;
//                    mediaItemAdapter.notifyDataSetChanged();
//                    dismisProgressDialog();
//                    break;
//            }
//
//        }
//    };

}
