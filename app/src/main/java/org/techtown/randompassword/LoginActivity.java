package org.techtown.randompassword;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textView = findViewById(R.id.textView);
        Intent intent = getIntent();

        if(intent != null) {
            if(intent != null) {
                textView.setText(intent.getStringExtra("id") + "님 환영합니다.");
            }
        }

    }
}
