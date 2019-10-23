package org.techtown.randompassword;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText idEditText;    //  id입력창
    private EditText pwEditText;    //  pw입력창
    private TextView textView;    //  pw 보이는 textView

    private Button loginButton, joinButton, findPwButton;

    private static RequestQueue RequestQueue;    //  Volley 리퀘스트

    private void inIt() {
        idEditText = findViewById(R.id.idEditText);
        pwEditText = findViewById(R.id.pwEditText);
        textView = findViewById(R.id.textView);
        loginButton = findViewById(R.id.loginButton);
        joinButton = findViewById(R.id.joinButton);
        findPwButton = findViewById(R.id.findPwButton);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.loginButton:    //  로그인 버튼
                String login_id = idEditText.getText().toString();
                String login_pwd = pwEditText.getText().toString();
                loginRequest(login_id, login_pwd);
                break;
            case R.id.joinButton:    //  회원가입 버튼
                Intent joinIntent = new Intent(getApplicationContext(), JoinActivity.class);
                startActivity(joinIntent);
                finish();
                break;
            case R.id.findPwButton:    //  비밀번호 get 버튼
                SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                String find_id = sharedPreferences.getString("id", "");
                findPWRequest(find_id);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inIt();    //  초기화

        loginButton.setOnClickListener(this);
        joinButton.setOnClickListener(this);
        findPwButton.setOnClickListener(this);
    }

    private void loginRequest(final String id, final String pwd) {    //  로그인 리퀘스트
        String url = "http://222.236.93.13:8080/RandomPassword/PhoneConnection.jsp";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("로그인", response);

                if(response.equals("true")) {
                    Intent loginIntent = new Intent(getApplicationContext(),LoginActivity.class);
                    loginIntent.putExtra("id", id);
                    startActivity(loginIntent);
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

        if(RequestQueue == null)
            RequestQueue = Volley.newRequestQueue(getApplicationContext());

        request.setShouldCache(false);
        RequestQueue.add(request);
    }

    private void findPWRequest(final String id) {    //  비밀번호 get 리퀘스트
        String url = "http://222.236.93.13:8080/RandomPassword/PhoneConnection.jsp";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equals("noID")){
                    Toast.makeText(getApplicationContext(), "존재하지 않는 아이디 입니다.", Toast.LENGTH_LONG).show();
                } else if(response.equals("error")) {
                    Toast.makeText(getApplicationContext(), "에러", Toast.LENGTH_LONG).show();
                } else {
                    pwEditText.setText(response);
                    textView.setText(response);
                    Toast.makeText(getApplicationContext(), "비밀번호를 가져왔습니다.", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("find", error.getMessage());
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

        if(RequestQueue == null)
            RequestQueue = Volley.newRequestQueue(getApplicationContext());

        request.setShouldCache(false);
        RequestQueue.add(request);
    }
}
