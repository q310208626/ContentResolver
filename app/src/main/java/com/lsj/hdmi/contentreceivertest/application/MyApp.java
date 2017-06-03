package com.lsj.hdmi.contentreceivertest.application;

import android.app.Application;

import org.xutils.x;

/**
 * Created by hdmi on 17-6-3.
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(true);
    }
}
