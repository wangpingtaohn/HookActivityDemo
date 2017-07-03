package com.wpt.hook.invocation;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;

import com.wpt.hook.activity.RegisteredActivity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by wpt on 17/6/29.
 */

public class PackageManagerInvocationHandler implements InvocationHandler {

    private Object mActivityManagerObject;

    public PackageManagerInvocationHandler(Object activityManagerObject) {
        this.mActivityManagerObject = activityManagerObject;

    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("getActivityInfo")) {
            if (args[0] instanceof ComponentName){
                String packageName = ((ComponentName) args[0]).getPackageName();
                ComponentName componentName = new ComponentName(packageName, RegisteredActivity.class.getName());
                args[0] = componentName;
            }
        }
        return method.invoke(mActivityManagerObject, args);
    }
}
