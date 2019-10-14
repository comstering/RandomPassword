package org.techtown.randompassword;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import java.security.SecureRandom;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RandomPasswordService extends Service {
    public static Intent serviceIntent = null;

    static RequestQueue requestQueue;    //  비밀번호 변경 리퀘스트

    public RandomPasswordService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceIntent = intent;

        final Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        String pwd;
        while(true) {    //  비밀번호 만들기
            pwd = getRandomPassword(10);
            Pattern pattern = Pattern.compile("((?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%=+]).{10,})");    //  정규식

            Matcher matcher1 = pattern.matcher(pwd);
            Matcher matcher2 = Pattern.compile("(.)\1\1\1").matcher(pwd);    //  같은 문자 4개

            if(matcher1.matches() && !matcher2.matches() && !pwd.contains(" "))
                break;
        }
        Log.d("Rnadom Password", pwd);

        SharedPreferences sf = getSharedPreferences("user", MODE_PRIVATE);
        String id = sf.getString("id", "");

        makeRequest(id, pwd);    //  Volley 서버 통신

        mainIntent.putExtra("pwd", pwd);
        stopSelf();

        return START_NOT_STICKY;
    }

    public String getRandomPassword(int length) {    //  비밀번호 가능 문자열 만들기
        char[] charaters = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r',
                's','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J', 'K','L',
                'M', 'N', 'O','P','Q','R','S','T','U','V','W','X','Y','Z','0','1','2','3','4','5','6','7','8','9','!','@','#','$','%','=','+'};
        StringBuilder sb = new StringBuilder("");
        Random rn = new SecureRandom();
        for(int i = 0; i < length; i++) {
            sb.append(charaters[rn.nextInt(charaters.length)]);
        }
        return sb.toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 1);
        Intent intent = new Intent(this, TimeReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0,intent,0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), sender);

        Thread.currentThread().interrupt();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void makeRequest(final String id, final String pwd) {    //  서버 비밀번호 변경 리퀘스트
        String url = "http://192.168.35.74:8080/AndroidTEST/TEST.jsp";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("비밀번호", "변경" + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("에러", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("pwd", pwd);
                params.put("type", "update");

                return params;
            }
        };

        if(requestQueue == null)
            requestQueue = Volley.newRequestQueue(getApplicationContext());

        request.setShouldCache(false);
        requestQueue.add(request);
    }
}
