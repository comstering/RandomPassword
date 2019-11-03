package org.techtown.randompassword;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class JoinActivity extends AppCompatActivity {

    private EditText idEditText;
    private EditText pwEditText;
    private Button button;

    private void inIt() {
        idEditText = findViewById(R.id.idEditText);
        pwEditText = findViewById(R.id.pwEditText);
        button = findViewById(R.id.button);
    }

    private static RequestQueue requestQueue;    //  Volley 리퀘스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        inIt();    //  초기화

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String join_id = idEditText.getText().toString();
                String join_pwd = pwEditText.getText().toString();
                if(join_id.length() == 0)    //  아이디를 입력하지 않았을 경우
                    Toast.makeText(getApplicationContext(), "아이디를 입력해주세요", Toast.LENGTH_LONG).show();
                else if(join_pwd.length() == 0)    //  아이디를 입력하였지만 비밀번호를 입력하지 않았을 경우
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해 주세요", Toast.LENGTH_LONG).show();
                else
                    makeRequest(join_id, join_pwd);
            }
        });
    }

    public void makeRequest(final String id, final String pwd) {
        String url = "http://222.236.93.13:8080/RandomPassword/PhoneConnection.jsp";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("회원가입", response);
                if(response.equals("ok")) {
                    SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("id", id);
                    editor.apply();

                    Toast.makeText(getApplicationContext(), "회원가입이 완료되었습니다.", Toast.LENGTH_LONG).show();

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.add(Calendar.SECOND, 1);

                    Intent intent = new Intent(getApplicationContext(), TimeReceiver.class);
                    PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), 0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)    //  API 19이상 23미만
                            alarmManager.setExact(AlarmManager.RTC, calendar.getTimeInMillis(), sender);
                        else    //  API 19미만
                            alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), sender);
                    } else    //  API 23 이상
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, calendar.getTimeInMillis(), sender);
                    finish();
                    //  회원가입 이후 변환 시간 셋팅
                } else
                    Toast.makeText(getApplicationContext(), "이미 존재하는 아이디입니다.", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("회원가입", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("pwd", pwd);
                params.put("type", "join");

                return params;
            }
        };

        if(requestQueue == null)
            requestQueue = Volley.newRequestQueue(getApplicationContext());

        request.setShouldCache(false);
        requestQueue.add(request);
    }
}
