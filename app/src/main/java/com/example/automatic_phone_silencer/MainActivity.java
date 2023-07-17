package com.example.automatic_phone_silencer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_VIEW_COUNT = "viewCount";
    private LinearLayout contentLayout;
    private FloatingActionButton fab;
    private int viewCount;
    private static final int REQUEST_CODE_DETAILS = 1;
    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !nm.isNotificationPolicyAccessGranted()) {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
            Toast.makeText(this, "Please give the access to continue", Toast.LENGTH_SHORT).show();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        contentLayout = findViewById(R.id.contentLayout);
        fab = findViewById(R.id.fab);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the details_page activity
                Intent intent = new Intent(MainActivity.this, details_page.class);
                startActivityForResult(intent, REQUEST_CODE_DETAILS);
            }
        });

        restoreViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_DETAILS && resultCode == RESULT_OK && data != null) {
            // Retrieve the data from the result intent
            String ename = data.getStringExtra("eventn");
            String startt = data.getStringExtra("startt");
            String stopt = data.getStringExtra("stopt");

            // Use the data to populate the dynamically created layout
            addLayout(ename, startt, stopt);
        }
    }

    public void addLayout(String ename, String startt, String stopt) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dynamo, contentLayout, false);
        ImageButton deleteButton = view.findViewById(R.id.delete);
        Switch swt = view.findViewById(R.id.switch1);

        TextView eventname = view.findViewById(R.id.Eventname);
        eventname.setText(ename);
        TextView tstarttime1 = view.findViewById(R.id.starttime1);
        tstarttime1.setText(startt);
        TextView stoptime1 = view.findViewById(R.id.stoptime1);
        stoptime1.setText(stopt);

        swt.setChecked(true);


        contentLayout.addView(view);
        saveViews();


        long startMillis = convertTimeToMillis(startt);
        long stopMillis = convertTimeToMillis(stopt);


        // Create an Intent for the broadcast
        scheduleSilenceBroadcast(startMillis, stopMillis);
        // Set an OnClickListener to the delete button/icon
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove the view from the parent layout
                contentLayout.removeView(view);
                cancelSilenceBroadcast(startMillis,stopMillis);
                saveViews();
            }
        });

        swt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Enable the event (set the silent mode)
                    scheduleSilenceBroadcast(startMillis, stopMillis);
                } else {
                    // Disable the event (cancel the scheduled silent mode)
                    cancelSilenceBroadcast(startMillis, stopMillis);
                }
                saveViews();
            }
        });
    }

    private void restoreViews() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        viewCount = sharedPreferences.getInt(KEY_VIEW_COUNT, 0);

        contentLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < viewCount; i++) {
            View view = inflater.inflate(R.layout.dynamo, contentLayout, false);
            ImageButton deleteButton = view.findViewById(R.id.delete);
            Switch swt = view.findViewById(R.id.switch1);

            TextView eventname = view.findViewById(R.id.Eventname);
            TextView tstarttime1 = view.findViewById(R.id.starttime1);
            TextView stoptime1 = view.findViewById(R.id.stoptime1);

            String ename = sharedPreferences.getString("eventn" + i, "");
            String startt = sharedPreferences.getString("startt" + i, "");
            String stopt = sharedPreferences.getString("stopt" + i, "");
            Boolean sw = sharedPreferences.getBoolean("swt" + i, true);

            eventname.setText(ename);
            tstarttime1.setText(startt);
            stoptime1.setText(stopt);
            swt.setChecked(sw);

            long startMillis = convertTimeToMillis(startt);
            long stopMillis = convertTimeToMillis(stopt);

            if(sw){
                scheduleSilenceBroadcast(startMillis, stopMillis);
            }

            final View finalView = view; // Create a final reference to the inflated view
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Remove the view from the parent layout
                    contentLayout.removeView(finalView);
                    saveViews();
                }
            });

            swt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        // Enable the event (set the silent mode)

                        scheduleSilenceBroadcast(startMillis, stopMillis);
                    } else {
                        // Disable the event (cancel the scheduled silent mode)
                        cancelSilenceBroadcast(startMillis, stopMillis);
                    }
                    saveViews();
                }
            });

            contentLayout.addView(view);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveViews();
    }

    private void saveViews() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_VIEW_COUNT, contentLayout.getChildCount());

        for (int i = 0; i < contentLayout.getChildCount(); i++) {
            View view = contentLayout.getChildAt(i);
            TextView eventname = view.findViewById(R.id.Eventname);
            TextView tstarttime1 = view.findViewById(R.id.starttime1);
            TextView stoptime1 = view.findViewById(R.id.stoptime1);
            Switch swt = view.findViewById(R.id.switch1);

            editor.putString("eventn" + i, eventname.getText().toString());
            editor.putString("startt" + i, tstarttime1.getText().toString());
            editor.putString("stopt" + i, stoptime1.getText().toString());
            editor.putBoolean("swt" + i, swt.isChecked());
        }

        editor.apply();
    }

    private void scheduleSilenceBroadcast(long startMillis, long stopMillis) {

        // Create an Intent for the broadcast
        Intent broadcastIntent = new Intent(this, silentbroadcast.class);
        broadcastIntent.setAction("com.example.automatic_phone_silencer.SILENCE_PHONE");

// Set the startMillis and stopMillis as extras
        broadcastIntent.putExtra("startMillis", startMillis);
        broadcastIntent.putExtra("stopMillis", stopMillis);

        // PendingIntent to be fired when the alarm triggers
        PendingIntent silencePendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                broadcastIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        // Create an Intent for the broadcast to restore the audio mode
        Intent restoreIntent = new Intent(this, silentbroadcast.class);
        restoreIntent.setAction("com.example.automatic_phone_silencer.RESTORE_AUDIO");
        restoreIntent.putExtra("startMillis", startMillis);
       restoreIntent.putExtra("stopMillis", stopMillis);

        // PendingIntent to be fired when the alarm triggers to restore the audio mode
        PendingIntent restorePendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                restoreIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );



        // Schedule the alarm to start at the specified start time
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, startMillis, silencePendingIntent);

        // Schedule the alarm to stop at the specified stop time
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, stopMillis, restorePendingIntent);
    }




    private void cancelSilenceBroadcast(long startMillis, long stopMillis) {

        // Create an Intent for the broadcast
        Intent broadcastIntent = new Intent(this, silentbroadcast.class);
        broadcastIntent.setAction("com.example.automatic_phone_silencer.SILENCE_PHONE");

// Set the startMillis and stopMillis as extras
        broadcastIntent.putExtra("startMillis", startMillis);
        broadcastIntent.putExtra("stopMillis", stopMillis);

        // PendingIntent to be fired when the alarm triggers
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                broadcastIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        // Create an Intent for the broadcast to restore the audio mode
        Intent restoreIntent = new Intent(this, silentbroadcast.class);
        restoreIntent.setAction("com.example.automatic_phone_silencer.RESTORE_AUDIO");
        restoreIntent.putExtra("startMillis", startMillis);
        restoreIntent.putExtra("stopMillis", stopMillis);

        // PendingIntent to be fired when the alarm triggers to restore the audio mode
        PendingIntent restorePendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                restoreIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );



        // Schedule the alarm to start at the specified start time
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(cancelPendingIntent);

        // Schedule the alarm to stop at the specified stop time
        alarmManager.cancel(restorePendingIntent);
    }






    private long convertTimeToMillis(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);

        long millis = convertISTtoMillis(hours, minutes);
        return  millis;
    }
    public static long convertISTtoMillis(int hours, int minutes) {
        // Create a Calendar instance and set the time zone to IST
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Kolkata");
        calendar.setTimeZone(timeZone);

        // Set the hours and minutes
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Get the time in milliseconds
        return calendar.getTimeInMillis();
    }



}
