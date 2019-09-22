package org.techtown.randompassword;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Button", "push");
                Intent intent = new Intent(getApplicationContext(), RandomPasswordReceiver.class);
                AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
                PendingIntent pwdIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)    //  API 19이상 23미만
                        am.setExact(AlarmManager.RTC, 0, pwdIntent);
                    else    //  API 19미만
                        am.set(AlarmManager.RTC, 0, pwdIntent);
                } else
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC, 0, pwdIntent);
            }
        });
    }
}
