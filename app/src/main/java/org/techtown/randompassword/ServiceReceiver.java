package org.techtown.randompassword;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
            context.startService(new Intent(context, RandomPasswordService.class));
    }
}
