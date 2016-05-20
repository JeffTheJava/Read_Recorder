package com.litmantech.readrecorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.litmantech.readrecorder.audio.Playback;
import com.litmantech.readrecorder.audio.Recorder;

import java.util.concurrent.LinkedBlockingQueue;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Recorder recorder;
    private Playback playback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    55);
        }


    }

    @Override
    protected void onResume(){
        super.onResume();
        RunTest();
    }

    private void RunTest() {

        if(playback == null)
            playback = new Playback();
        if(recorder == null)
            recorder = new Recorder();

        LinkedBlockingQueue audioListener = playback.Play();

        recorder.setAudioListener(audioListener);
        recorder.Start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        recorder.Stop();
        playback.Stop();
    }
}
