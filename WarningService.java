package com.example.thesender;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.List;

public class WarningService extends Service {
    private boolean appCloseAllowed = true;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = (level / (float) scale) * 100;

                if(batteryPct <= 20){
                    Toast.makeText(context, "Battery is low! If the phone turns off, the SMS will not be sent!", Toast.LENGTH_SHORT).show();
                }
            }
        };
        registerReceiver(batteryReceiver, filter);

        IntentFilter closeDialogFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        BroadcastReceiver closeSystemDialogsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String reason = intent.getStringExtra("reason");
                if (reason != null && reason.equals("recentapps")) {
                    if (!appCloseAllowed) {
                        Toast.makeText(context, "If you close the TheSender app the SMS will not be send!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        registerReceiver(closeSystemDialogsReceiver, closeDialogFilter);

        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(1);
        if (!runningTasks.isEmpty()){
            String packageName = runningTasks.get(0).topActivity.getPackageName();
            appCloseAllowed = !packageName.equals("com.example.thesender");
        }

        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
