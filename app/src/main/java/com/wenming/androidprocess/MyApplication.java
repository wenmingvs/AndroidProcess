package com.wenming.androidprocess;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by wenmingvs on 2016/1/13.
 */
public class MyApplication extends Application {
    private int appCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                Log.d("wenming", "onActivityCreated");
            }

            @Override
            public void onActivityStarted(Activity activity) {
                Log.d("wenming", "onActivityStarted");
                appCount++;
            }

            @Override
            public void onActivityResumed(Activity activity) {
                Log.d("wenming", "onActivityResumed");
            }

            @Override
            public void onActivityPaused(Activity activity) {
                Log.d("wenming", "onActivityPaused");
            }

            @Override
            public void onActivityStopped(Activity activity) {
                Log.d("wenming", "onActivityStopped");
                appCount--;
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                Log.d("wenming", "onActivitySaveInstanceState");
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Log.d("wenming", "onActivityDestroyed");
            }
        });
    }

    public int getAppCount() {
        return appCount;
    }

    public void setAppCount(int appCount) {
        this.appCount = appCount;
    }
}
