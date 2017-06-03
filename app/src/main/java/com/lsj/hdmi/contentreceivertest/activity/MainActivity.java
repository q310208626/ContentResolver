package com.lsj.hdmi.contentreceivertest.activity;

import android.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.lsj.hdmi.contentreceivertest.R;
import com.lsj.hdmi.contentreceivertest.view.MainFragment;

import org.xutils.view.annotation.ContentView;
import org.xutils.x;

@ContentView(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    public static String TAG = "MainActivity";
    private Fragment fragment;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;

    //drawLayout Context
    private TextView logOutTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        x.view().inject(this);
        init();

        android.app.FragmentManager fm=getFragmentManager();
        if (fragment==null){
            fragment=new MainFragment();
        }
        fm.beginTransaction().add(R.id.container,fragment).commit();
    }

    private void init(){
        drawerLayout= (DrawerLayout) findViewById(R.id.main_drawerlayout);
        logOutTextView= (TextView) drawerLayout.findViewById(R.id.logout_layout);
        toolbar= (Toolbar) findViewById(R.id.main_toolbar);
        this.setSupportActionBar(toolbar);
        ActionBarDrawerToggle actionBarDrawerToggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        initDrawerListener();
    }


    private void initDrawerListener(){
        logOutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ------------------------");
                MainActivity.this.finish();
            }
        });
    }

    @Override
    public boolean moveTaskToBack(boolean nonRoot) {
        return super.moveTaskToBack(nonRoot);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
            Fragment fragment=getFragmentManager().findFragmentById(R.id.container);
            if (fragment.getClass().equals(MainFragment.class)){
                Log.d(TAG, "onKeyDown: --------------------down to back-----------");
                moveTaskToBack(false);
                return true;
            }
            Log.d(TAG, "onKeyDown: --------------------down not to back-----------");
        }
        return super.onKeyDown(keyCode, event);
    }
}
