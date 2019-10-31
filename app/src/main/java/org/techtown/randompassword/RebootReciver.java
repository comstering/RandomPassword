package org.techtown.randompassword;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public class RebootReciver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.SECOND, 1);
            Intent timeIntent = new Intent(context, TimeReceiver.class);
            PendingIntent sender = PendingIntent.getBroadcast(context, 0, timeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)    //  API 19이상 23미만
                    alarmManager.setExact(AlarmManager.RTC, calendar.getTimeInMillis(), sender);
                else    //  API 19미만
                    alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), sender);
            } else    //  API 23 이상
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, calendar.getTimeInMillis(), sender);
        }
    }
}
