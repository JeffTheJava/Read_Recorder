package com.litmantech.readrecorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.litmantech.readrecorder.audio.Playback;
import com.litmantech.readrecorder.audio.Recorder;
import com.litmantech.readrecorder.read.Session;
import com.litmantech.readrecorder.read.line.Entry;

import java.util.concurrent.LinkedBlockingQueue;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private Recorder recorder;
    private Playback playback;
    private boolean testAudioStuffON = false;
    private Button prevBTN;
    private Button nextBTN;
    private Button recBTN;
    private Session session;
    private TextView currentSentenceTXT;

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

        currentSentenceTXT = (TextView) findViewById(R.id.current_sentence);
        prevBTN = (Button) findViewById(R.id.prev_btn);
        recBTN  = (Button) findViewById(R.id.rec_btn);
        nextBTN = (Button) findViewById(R.id.next_btn);

        prevBTN.setOnClickListener(this);
        recBTN.setOnClickListener(this);
        nextBTN.setOnClickListener(this);

        String[] mTestArray = getResources().getStringArray(R.array.testArray);
        session = new Session(mTestArray);
        UpdateUI();
    }


    @Override
    protected void onResume(){
        super.onResume();
        if(testAudioStuffON)
            RunTest();

        if(recorder == null)
            recorder = new Recorder();

        recorder.Start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(testAudioStuffON){
            playback.Stop();
        }
        recorder.Stop();
    }

    private void RunTest() {

        if(playback == null)
            playback = new Playback();


        LinkedBlockingQueue audioListener = playback.Play();

        recorder.setAudioListener(audioListener);

    }


    @Override
    public void onClick(View v) {
        if(v == prevBTN){
            session.PreviousEntry();
        }else if(v == nextBTN){
            session.NextEntry();
        }else if(v == recBTN){
            if(session.isRecording())
                session.StopRecording();
            else
                session.StartRecording(recorder);
        }


        UpdateUI();
    }

    /**
     * ONLY call this on the UI thread.!!!!
     */
    private void UpdateUI() {
        Entry currentEntry = session.getCurrentEntry();
        if(session.isRecording()){
            prevBTN.setEnabled(false);
            nextBTN.setEnabled(false);
        }else{
            prevBTN.setEnabled(true);
            nextBTN.setEnabled(true);
        }

        currentSentenceTXT.setText(currentEntry.getSentence());
    }
}
