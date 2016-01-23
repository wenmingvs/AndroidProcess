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
import android.widget.RelativeLayout;

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

    private final float UPDATA_INTERVAL = 0.5f;//in seconds
    private String status;
    private Context mContext;
    private ArrayList<String> mContentList;
    private Notification notification;
    private AlarmManager manager;
    private PendingIntent pendingIntent;
    private RelativeLayout mClickLayout;
    private NotificationManager notificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        initContentData();
    }


    private void initContentData() {
        mContentList = new ArrayList<String>();
        mContentList.add("通过getRunningTask判断");
        mContentList.add("通过getRunningAppProcess判断");
        mContentList.add("通过ActivityLifecycleCallbacks判断");
        mContentList.add("通过UsageStatsManager判断");
        mContentList.add("通过LinuxCoreInfo判断");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!Features.stopForeground) {
            status = getAppStatus() ? "前台" : "后台";
            intent = new Intent(mContext, MainActivity.class);
            pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.drawable.largeicon)
               //     .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.largeicon))
                    .setContentText(mContentList.get(Features.BGK_METHOD))
                    .setContentTitle("App处于" + status)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);
            notification = mBuilder.build();
            startForeground(1, notification);
            manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            //这里是定时的,这里设置的是每隔1秒打印一次时间
            int anHour = (int) UPDATA_INTERVAL * 1000;
            long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
            Intent i = new Intent(mContext, MyReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, i, 0);
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        } else {
            stopForeground(true);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Features.stopForeground = true;
        stopForeground(true);
        super.onDestroy();
    }

    private boolean getAppStatus() {
        return BackgroundUtil.isForeground(mContext, Features.BGK_METHOD, mContext.getPackageName());
    }


}
