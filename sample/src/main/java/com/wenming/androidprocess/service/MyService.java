package com.wenming.androidprocess.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.wenming.andriodprocess.R;
import com.wenming.androidprocess.Features;
import com.wenming.androidprocess.activity.MainActivity;
import com.wenming.androidprocess.receiver.MyReceiver;
import com.wenming.library.BackgroundUtil;

import java.util.ArrayList;

/**
 * Created by wenmingvs on 2016/1/13.
 */
public class MyService extends Service {

    private static final float UPDATA_INTERVAL = 0.5f;//in seconds
    private String status;
    private Context mContext;
    private ArrayList<String> mContentList;
    private Notification notification;
    private AlarmManager manager;
    private PendingIntent pendingIntent;
    private NotificationCompat.Builder mBuilder;
    private Intent mIntent;
    private NotificationManager mNotificationManager;
    private static final int NOTICATION_ID = 0x1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        initContentData();
        startNotification();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Features.showForeground) {
            manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            int updateTime = (int) UPDATA_INTERVAL * 1000;
            long triggerAtTime = SystemClock.elapsedRealtime() + updateTime;
            Intent i = new Intent(mContext, MyReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, i, 0);
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
            updateNotification();

        } else {
            stopForeground(true);
            mNotificationManager.cancelAll();
            stopSelf();
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Features.showForeground = false;
        stopForeground(true);
        super.onDestroy();
    }

    private void startNotification() {
        status = getAppStatus() ? "前台" : "后台";
        mIntent = new Intent(mContext, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(mContext, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.largeicon)
                .setContentText(mContentList.get(Features.BGK_METHOD))
                .setContentTitle("App处于" + status)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        notification = mBuilder.build();
        startForeground(NOTICATION_ID, notification);
    }

    private void updateNotification() {
        status = getAppStatus() ? "前台" : "后台";
        mBuilder.setContentTitle("App处于" + status);
        mBuilder.setContentText(mContentList.get(Features.BGK_METHOD));
        if (Features.BGK_METHOD == BackgroundUtil.BKGMETHOD_GETACCESSIBILITYSERVICE) {
            mBuilder.setContentTitle("请到LogCat中观察前后台变化");
            Log.d("wenming", "**方法五** App处于" + status);
        }
        notification = mBuilder.build();
        mNotificationManager.notify(NOTICATION_ID, notification);
    }

    private void initContentData() {
        mContentList = new ArrayList<String>();
        mContentList.add("通过getRunningTask判断");
        mContentList.add("通过getRunningAppProcess判断");
        mContentList.add("通过ActivityLifecycleCallbacks判断");
        mContentList.add("通过UsageStatsManager判断");
        mContentList.add("通过AccessibilityService判断");
        mContentList.add("通过LinuxCoreInfo判断");
    }

    private boolean getAppStatus() {
        return BackgroundUtil.isForeground(mContext, Features.BGK_METHOD, mContext.getPackageName());
    }


}
