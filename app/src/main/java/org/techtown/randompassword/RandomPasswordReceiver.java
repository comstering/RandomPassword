package org.techtown.randompassword;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.security.SecureRandom;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RandomPasswordReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {String pwd;
        while(true) {
            pwd = getRandomPassword(9);
            Pattern pattern = Pattern.compile("((?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%=+]).{9,})");    //  정규식

            Matcher matcher1 = pattern.matcher(pwd);
            Matcher matcher2 = Pattern.compile("(.)\1\1\1").matcher(pwd);    //  같은 문자 4개

            if(matcher1.matches() && !matcher2.matches() && !pwd.contains(" "))
                break;
        }
        Log.d("pwd", pwd);

        Intent showIntent = new Intent(context, ShowPwdActivity.class);
        showIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        showIntent.putExtra("pwd", pwd);
        context.startActivity(showIntent);

        Intent timeIntent = new Intent(context, TimeReceiver.class);
        PendingIntent timePendIntent = PendingIntent.getBroadcast(context, 0, timeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)    //  API 19이상 23미만
                am.setExact(AlarmManager.RTC, 0, timePendIntent);
            else    //  API 19미만
                am.set(AlarmManager.RTC, 0, timePendIntent);
        } else
            am.setExactAndAllowWhileIdle(AlarmManager.RTC, 0, timePendIntent);
    }

    public String getRandomPassword(int length) {
        char[] charaters = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r',
                's','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','C','K','L',
                'M','O','P','Q','R','S','T','U','V','W','X','Y','Z','0','1','2','3','4','5','6','7','8','9','!','@','#','$','%','=','+'};
        StringBuilder sb = new StringBuilder("");
        Random rn = new SecureRandom();
        for(int i = 0; i < length; i++) {
            sb.append(charaters[rn.nextInt(charaters.length)]);
        }
        return sb.toString();
    }
}
