package org.techtown.randompassword;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Intent serviceIntent;

    EditText editText;
    EditText editText2;
    TextView textView;

    static RequestQueue loginRequestQueue;    //  로그인 리퀘스트
    static RequestQueue findPWRequestQueue;    //  패스워드 확인 리퀘스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
        boolean isWhiteListing = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            isWhiteListing = pm.isIgnoringBatteryOptimizations(getApplicationContext().getPackageName());
        }
        if (!isWhiteListing) {
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
            startActivity(intent);
        }

        if (RandomPasswordService.serviceIntent==null) {
            serviceIntent = new Intent(this, RandomPasswordService.class);
            startService(serviceIntent);
        } else
            serviceIntent = RandomPasswordService.serviceIntent;

        editText = findViewById(R.id.editText);
        editText2 = findViewById(R.id.editText2);
        textView = findViewById(R.id.textView);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = editText.getText().toString();
                String pwd = editText2.getText().toString();
                loginRequest(id, pwd);
            }
        });

        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent joinIntent = new Intent(getApplicationContext(), JoinActivity.class);
                startActivityForResult(joinIntent, 101);
            }
        });

        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = editText.getText().toString();
                findPWRequest(id);
            }
        });
    }

    public void loginRequest(final String id, final String pwd) {    //  로그인 리퀘스트
        String url = "http://192.168.35.74:8080/AndroidTEST/TEST.jsp";

        final Intent loginIntent = new Intent(getApplicationContext(),LoginActivity.class);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("로그인", response);

                if(response.equals("true")) {
                    loginIntent.putExtra("id", id);
                    startActivityForResult(loginIntent, 101);
                }
                else if(response.equals("false"))
                    Toast.makeText(getApplicationContext(),"패스워드가 틀렸습니다.", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), "아이디가 존재하지 않습니다.", Toast.LENGTH_LONG).show();


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("로그인", error.getMessage());
                startActivityForResult(loginIntent, 101);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("pwd", pwd);
                params.put("type", "login");

                return params;
            }
        };

        if(loginRequestQueue == null)
            loginRequestQueue = Volley.newRequestQueue(getApplicationContext());

        request.setShouldCache(false);
        loginRequestQueue.add(request);
    }

    public void findPWRequest(final String id) {    //  비밀번호 get 리퀘스트
        String url = "http://192.168.35.74:8080/AndroidTEST/TEST.jsp";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equals("noID")){
                    Toast.makeText(getApplicationContext(), "존재하지 않는 아이디 입니다.", Toast.LENGTH_LONG).show();
                } else if(response.equals("error")) {
                    Toast.makeText(getApplicationContext(), "에러", Toast.LENGTH_LONG).show();
                } else {
                    editText2.setText(response);
                    textView.setText(response);
                    Toast.makeText(getApplicationContext(), "비밀번호를 가져왔습니다.", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("type", "find");

                return params;
            }
        };

        if(findPWRequestQueue == null)
            findPWRequestQueue = Volley.newRequestQueue(getApplicationContext());

        request.setShouldCache(false);
        findPWRequestQueue.add(request);
    }
}
