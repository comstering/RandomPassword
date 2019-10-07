package org.techtown.randompassword;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RandomPasswordService extends Service {
    public static Intent serviceIntent = null;

    static RequestQueue requestQueue;

    public RandomPasswordService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceIntent = intent;

        final Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        String pwd;
        while(true) {
            pwd = getRandomPassword(9);
            Pattern pattern = Pattern.compile("((?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%=+]).{9,})");    //  정규식

            Matcher matcher1 = pattern.matcher(pwd);
            Matcher matcher2 = Pattern.compile("(.)\1\1\1").matcher(pwd);    //  같은 문자 4개

            if(matcher1.matches() && !matcher2.matches() && !pwd.contains(" "))
                break;
        }
        Log.d("pwd", pwd);


        makeRequest(pwd);    //  Volley 서버 통신

        mainIntent.putExtra("pwd", pwd);
        sendNotification(pwd);
        stopSelf();

        return START_NOT_STICKY;
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

    private static String CHANNEL_ID = "Random";
    private static String CHANNEL_NAME = "Password";

    private void sendNotification(String pwd) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationManager noManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder noBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            noManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT));
        }


        noBuilder.setSmallIcon(R.mipmap.ic_launcher);
        noBuilder.setContentTitle("RandomPassword");
        noBuilder.setContentText("비밀번호 생성 중 : " + pwd);
        noBuilder.setDefaults(Notification.DEFAULT_SOUND);
        noBuilder.setAutoCancel(true);
        noBuilder.setPriority(Notification.PRIORITY_HIGH);
        noBuilder.setContentIntent(pendingIntent);

        noManager.notify((int)(System.currentTimeMillis())/1000, noBuilder.build());
    }

    public void makeRequest(final String pwd) {

        String url = "http://192.168.35.74:8080/AndroidTEST/TEST.jsp";

        StringRequest request = new StringRequest(Request.Method.POST, url,
        new Response.Listener<String>() {
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
                params.put("id", "gildon");
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
