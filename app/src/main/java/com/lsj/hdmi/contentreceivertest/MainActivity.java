package com.lsj.hdmi.contentreceivertest;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;

import com.lsj.hdmi.contentreceivertest.bean.MediaItem;
import com.lsj.hdmi.contentreceivertest.view.MainFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "MainActivity";
    private Fragment fragment;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();


        android.app.FragmentManager fm=getFragmentManager();
        if (fragment==null){
            fragment=new MainFragment();
        }
        fm.beginTransaction().add(R.id.container,fragment).commit();

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void init(){
        drawerLayout= (DrawerLayout) findViewById(R.id.main_drawerlayout);
        toolbar= (Toolbar) findViewById(R.id.main_toolbar);
        this.setSupportActionBar(toolbar);
        ActionBarDrawerToggle actionBarDrawerToggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }
}
