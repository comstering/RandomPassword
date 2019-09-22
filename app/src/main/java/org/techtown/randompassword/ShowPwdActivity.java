package org.techtown.randompassword;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ShowPwdActivity extends AppCompatActivity {
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_pwd);

        textView = findViewById(R.id.textView);

        Intent pwdIntent = getIntent();

        String pwd = pwdIntent.getStringExtra("pwd");
        textView.setText(pwd);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String pwd = intent.getStringExtra("pwd");
        textView.setText(pwd);
        super.onNewIntent(intent);
    }
}
