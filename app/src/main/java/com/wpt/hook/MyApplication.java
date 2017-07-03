package com.wpt.hook;

import android.app.Application;

import com.wpt.hook.activity.RegisteredActivity;
import com.wpt.hook.activity.TargetActvitity;

/**
 * Created by wpt on 17/6/26.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HookUtils.registerTargetActivity(TargetActvitity.class,RegisteredActivity.class);
    }
}
