package com.lsj.hdmi.contentreceivertest.application;

import android.app.Application;
import android.os.Environment;

import org.xutils.DbManager;
import org.xutils.x;

import java.io.File;

/**
 * Created by hdmi on 17-6-3.
 */
public class MyApp extends Application {
    //本地数据库
    private String localDBName="myMusicDB.db";
    private File localDBDir=new File(Environment.getExternalStorageDirectory().getPath());
    private int localDBVersion=1;
    public static DbManager.DaoConfig daoConfig;
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(true);

        daoConfig=new DbManager.DaoConfig()
                .setDbName(localDBName)
                .setDbDir(localDBDir)
                .setDbVersion(localDBVersion)
                .setAllowTransaction(true);
    }
}
