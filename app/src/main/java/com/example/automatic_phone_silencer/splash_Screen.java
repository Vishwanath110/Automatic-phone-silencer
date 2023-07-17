package com.example.automatic_phone_silencer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.VideoView;

public class splash_Screen extends AppCompatActivity {
    private VideoView videoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        videoView = findViewById(R.id.videoView);
        videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.splash_video);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Video playback has ended, start the next activity
                startNextActivity();
            }
        });

        // Start playing the video
        videoView.start();
    }
    private void startNextActivity() {
    // Start the next activity (e.g., MainActivity)
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
    finish(); // Finish the splash screen activity to prevent going back to it
}
}