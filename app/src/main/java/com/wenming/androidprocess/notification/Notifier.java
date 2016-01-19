/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wenming.androidprocess.notification;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.wenming.androidprocess.activity.MainActivity;
import com.wenming.androidprocess.util.BackgroundUtil;

import java.util.HashSet;
import java.util.List;

/**
 * <p/>
 * Created by Jay on 2015/9/14.
 */
public class Notifier {
    private static final String TAG = Notifier.class.getSimpleName();
    private static final int UNIQUE_ID_FLASH = 0x1001;
    private static final int UNIQUE_ID_COMMON_MESSAGE = 0x1002;
    private static final int UNIQUE_ID_INVITATION = 0x1003;
    private static final int UNIQUE_ID_SEND_MESSAGE_FAIL = 0x1004;
    private static Notifier mInstance;
    private Context mContext;
    private NotificationManager mNotificationManager;
    private KeyguardManager mKeyguardManager;
    private long fromId = -1;
    private HashSet<Long> fromMessageUsers = new HashSet<Long>();
    private int notifyNum = 0;
    private int notifyInvitationNum = 0;
    private int notifyMessageNum = 0;
    private long lastNotifiyTime;
    //发送消息失败
    private long toIdSendMsgFail = -1;
    private int notifyNumSendMsgFail = 0;
    private boolean isVoiceOn = true;
    private boolean isVibrateOn = true;

    private Notifier() {
    }

    public static Notifier getInstance() {
        if (mInstance == null) {
            synchronized (Notifier.class) {
                if (mInstance == null) {
                    mInstance = new Notifier();
                }
            }
        }
        return mInstance;
    }

    public static boolean isRunningForeground(Context context) {


        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
            String currentPackageName = cn.getPackageName();
            return !TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(context.getPackageName());
        } else {
            return BackgroundUtil.isForeground(context, BackgroundUtil.BKGMETHOD_GETAPPLICATION_VALUE, context.getPackageName());
        }
    }

    public static boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public void prepare(Context context) {
        this.mContext = context;
        this.mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
    }

    public void reset() {
        this.fromId = -1;
        this.toIdSendMsgFail = -1;
        resetNotificationCount();
        cancelAll();
    }

    private void resetNotificationCount() {
        notifyNum = 0;
        notifyMessageNum = 0;
        notifyNumSendMsgFail = 0;
        fromMessageUsers.clear();
    }

    public void cancelAll() {
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
    }

    private void cancelNotificaton(int notifyID) {
        if (mNotificationManager != null) {
            mNotificationManager.cancel(notifyID);
        }
    }

    private ComponentName getTopActivity(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName topActivity = am.getRunningTasks(1).get(0).topActivity;
        return topActivity;
    }

    private boolean isMainActivityAtTop(Context context) {
        ComponentName topActivity = getTopActivity(context);
        if (topActivity.compareTo(new ComponentName(context, MainActivity.class)) == 0) {
            return true;
        }
        return false;
    }

    private boolean isChatActivityAtTop(Context context) {
        ComponentName topActivity = getTopActivity(context);
        if (topActivity.compareTo(new ComponentName(context, MainActivity.class)) == 0) {
            return true;
        }
        return false;
    }

    public void notify(Context context, String tag, int id, PendingIntent contentIntent, String contextTitle, String contextText, String tickerText, boolean isFlash, boolean soundOnly) {
        //获取电源管理器对象
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
        //点亮屏幕
        wl.acquire();
        //释放
        wl.release();

        cancelNotificaton(id);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        if (!soundOnly) {
            builder.setContentIntent(contentIntent);
            builder.setSmallIcon(context.getApplicationInfo().icon);
            builder.setContentTitle(contextTitle).setContentText(contextText).setTicker(tickerText);
        }
        final String resPrefix = "android.resource://" + context.getPackageName() + "/";
        if (System.currentTimeMillis() - lastNotifiyTime > 2000) {
            // received new messages within 2 seconds, skip play ringtone
            lastNotifiyTime = System.currentTimeMillis();
            if (isVoiceOn) {
                builder.setSound(Uri.parse((isRunningForeground(mContext) &&
                        !mKeyguardManager.inKeyguardRestrictedInputMode()) ? (resPrefix + "raw/push2") : (resPrefix + "raw/push1")), AudioManager.STREAM_SYSTEM);
            }

            if (isVibrateOn) {
                builder.setVibrate(new long[]{100, 200, 230, 250});
            }
        }
        builder.setAutoCancel(true);
        Notification notification = builder.build();
        if (contentIntent == null) {
            mNotificationManager.notify(id, notification);
        } else {
            if (notifyNum > 99)
                notifyNum = 99;
            if (Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")) {
                /*ShortcutBadger.with(mContext)
                        .setNotification(notification)
                        .setNotifyId(id)
                        .count(notifyNum == 99 ? 99 : notifyNum);*/
            } else {
                mNotificationManager.notify(id, notification);
                /*ShortcutBadger.with(mContext).count(notifyNum);*/
            }
        }
    }
}
