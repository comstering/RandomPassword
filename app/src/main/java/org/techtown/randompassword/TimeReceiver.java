package org.techtown.randompassword;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Random;

public class TimeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent pwdIntent = new Intent(context, ServiceReceiver.class);
        PendingIntent pwdPendIntent = PendingIntent.getBroadcast(context, 0, pwdIntent, 0);
        Random random = new SecureRandom();
        int addTime = (random.nextInt(5) + 1);
        //  변환 시간 추출
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE) + addTime, 0);
        Log.d("Security Seed", addTime + "");

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)    //  API 19이상 23미만
                alarmManager.setExact(AlarmManager.RTC, calendar.getTimeInMillis(), pwdPendIntent);
            else    //  API 19미만
                alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pwdPendIntent);
        } else    //  API 23 이상
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, calendar.getTimeInMillis(), pwdPendIntent);
    }
}
