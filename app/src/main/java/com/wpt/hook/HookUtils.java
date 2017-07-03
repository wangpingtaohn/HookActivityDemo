package com.wpt.hook;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.wpt.hook.activity.MainActivity;
import com.wpt.hook.invocation.PackageManagerInvocationHandler;
import com.wpt.hook.invocation.StartActivityInvocationHandler;

import java.lang.reflect.Method;

/**
 * /**
 * Created by wpt on 17/6/26.
 */

public class HookUtils {


    /**
     * * 这里我们通过反射获取到AMS的代理本地代理对象
     * Hook以后动态串改Intent为已注册的来躲避检测
     *
     * @param targetActivity      未被注册的Activity
     * @param registeredActivity 已注册的Activity
     */
    public static void registerTargetActivity(Class<?> targetActivity, Class<?> registeredActivity) {
        try {
            Class<?> amnCls = Class.forName("android.app.ActivityManagerNative");
            Field defaultField = amnCls.getDeclaredField("gDefault");
            defaultField.setAccessible(true);
            Object gDefaultObj = defaultField.get(null); //所有静态对象的反射可以通过传null获取。如果是实列必须传实例
            Class<?> singletonClazz = Class.forName("android.util.Singleton");
            Field amsField = singletonClazz.getDeclaredField("mInstance");
            amsField.setAccessible(true);
            Object amsObj = amsField.get(gDefaultObj);
            amsObj = Proxy.newProxyInstance(MainActivity.class.getClassLoader(),
                    amsObj.getClass().getInterfaces(),
                    new StartActivityInvocationHandler(amsObj, targetActivity, registeredActivity));
            amsField.set(gDefaultObj, amsObj);
            hookLaunchActivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * hook ActivityThread  handle 在这里我们需要替换我们未被注册的Activity Intent
     *
     * @throws Exception
     */
    public static void hookLaunchActivity() throws Exception {
        Class<?> activityThreadClazz = Class.forName("android.app.ActivityThread");
        Field sCurrentActivityThreadField = activityThreadClazz.getDeclaredField("sCurrentActivityThread");
        sCurrentActivityThreadField.setAccessible(true);
        Object sCurrentActivityThreadObj = sCurrentActivityThreadField.get(null);
        Field mHField = activityThreadClazz.getDeclaredField("mH");
        mHField.setAccessible(true);
        Handler mH = (Handler) mHField.get(sCurrentActivityThreadObj);
        Field callBackField = Handler.class.getDeclaredField("mCallback");
        callBackField.setAccessible(true);
        callBackField.set(mH, new ActivityThreadHandlerCallBack());
    }

    private static class ActivityThreadHandlerCallBack implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            int LAUNCH_ACTIVITY = 0;
            try {
                Class<?> clazz = Class.forName("android.app.ActivityThread$H");
                Field field = clazz.getField("LAUNCH_ACTIVITY");
                LAUNCH_ACTIVITY = field.getInt(null);
            } catch (Exception e) {
            }
            if (msg.what == LAUNCH_ACTIVITY) {
                handleLaunchActivity(msg);
            }
            return false;
        }
    }

    private static void handleLaunchActivity(Message msg) {
        try {
            Object obj = msg.obj;
            Field intentField = obj.getClass().getDeclaredField("intent");
            intentField.setAccessible(true);
            Intent proxyIntent = (Intent) intentField.get(obj);
            //拿到之前真实要被启动的Intent 然后把Intent换掉
            Intent targetIntent = proxyIntent.getParcelableExtra("targetIntent");
            proxyIntent.setComponent(targetIntent.getComponent());

            handleAppCompaActivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 兼容AppCompatActivity
     */
    private static void handleAppCompaActivity(){
        try {
            Class<?> forName = Class.forName("android.app.ActivityThread");
            Field field = forName.getDeclaredField("sCurrentActivityThread");
            field.setAccessible(true);
            Object activityThread = field.get(null);
            Method getPackageManager = activityThread.getClass().getDeclaredMethod("getPackageManager");
            Object iPackageManager = getPackageManager.invoke(activityThread);
            PackageManagerInvocationHandler handler = new PackageManagerInvocationHandler(iPackageManager);
            Class<?> iPackageManagerIntercept = Class.forName("android.content.pm.IPackageManager");
            Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class<?>[]{iPackageManagerIntercept}, handler);
            // 获取 sPackageManager 属性
            Field iPackageManagerField = activityThread.getClass().getDeclaredField("sPackageManager");
            iPackageManagerField.setAccessible(true);
            iPackageManagerField.set(activityThread, proxy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
