package org.techtown.randompassword;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
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

import java.util.HashMap;
import java.util.Map;

public class JoinActivity extends AppCompatActivity {
    EditText editText;
    EditText editText2;

    static RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        editText = findViewById(R.id.editText);
        editText2 = findViewById(R.id.editText2);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = editText.getText().toString();
                String pwd = editText2.getText().toString();
                makeRequest(id, pwd);
            }
        });
    }

    public void makeRequest(final String id, final String pwd) {
        String url = "http://192.168.35.74:8080/AndroidTEST/TEST.jsp";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("회원가입", response);
                if(response.equals("ok")) {
                    SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("id", id);
                    editor.commit();
                    Toast.makeText(getApplicationContext(), "회원가입이 완료되었습니다.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "이미 존재하는 아이디입니다.", Toast.LENGTH_LONG).show();
                }

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

    private void showMessage(String response) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setTitle("안내");
        if(response.equals("ok")) {
            builder.setMessage("회원가입 완료");
        }
        else if(response.equals("fail"))
            builder.setMessage("회원가입 실패: 아이디가 존재합니다.");
        else
            builder.setMessage("회원가입 실패: 에러");
    }
}
