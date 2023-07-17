package com.example.automatic_phone_silencer;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.widget.Toast;

public class silentbroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        long currentTime = System.currentTimeMillis();
        long startMillis = intent.getLongExtra("startMillis", -1);
        long stopMillis = intent.getLongExtra("stopMillis", -1);
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);


        if (action != null && action.equals("com.example.automatic_phone_silencer.SILENCE_PHONE")) {

            if (currentTime >= startMillis && currentTime < stopMillis) {
                // Set the phone to silent mode
                if (audioManager != null) {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    Toast.makeText(context, "Silent Mode Active", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Set the phone to normal mode
                if (audioManager != null) {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    Toast.makeText(context, "Ringer Mode Active", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (action != null && action.equals("com.example.automatic_phone_silencer.RESTORE_AUDIO")){
            Log.d("TAG", "Restore audio action received");
            Log.d("TAG", "Current time: " + currentTime);
            Log.d("TAG", "Stop time: " + stopMillis);
            if(currentTime>=stopMillis) {
                Log.d("TAG", "Restore audio action received");
                // Set the phone to normal mode
                if (audioManager != null) {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    Toast.makeText(context, "Ringer Mode Active", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}

