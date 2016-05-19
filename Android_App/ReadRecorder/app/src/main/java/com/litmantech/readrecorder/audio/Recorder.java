package com.litmantech.readrecorder.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jeff_Dev_PC on 5/19/2016.
 */
public class Recorder {

    private final AudioRecord audioRecord;
    private final int minBufSize;
    private final int sampleRate;
    private Thread collector = null;
    private boolean stopCollector;
    private LinkedBlockingQueue<short[]> audioListener;

    /**
     * Recorder is used to initialize the microphone then collect and buffer audio data.
     *
     */
    public Recorder(){
        sampleRate = 16000;
        minBufSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioRecord  = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufSize);
    }

    /**
     * Start collection audio data asynchronously on a new, high priority,  audio thread.
     * When you call start, that does not mean it will start right a way. It will take time to start but will not block your thread.
     * If start has already been called and it is already running then it will just keep running.
     */
    public void Start(){

        if(collector!=null)
            return;//we are already running

        audioRecord.startRecording();

        collector = new Thread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                stopCollector = false;
                CollectAudio();
            }
        });

        collector.start();
    }

    private void CollectAudio() {
        short[] audioBuffer = new short[minBufSize];

        while (!stopCollector){
            int bufferReadResult = audioRecord.read(audioBuffer, 0, minBufSize);

            short[] tempHolder = new short[bufferReadResult];

            System.arraycopy(audioBuffer,0,tempHolder,0,bufferReadResult);//this will help clean out any old audio and remove 0 0 0 from the back end if audioRecord.read could not fill it up
            audioListener.add(tempHolder);
        }
    }

    /**
     * Set the LinkedBlockingQueue of shorts that will receive the audio data. Remember, audioListener will be passed by reference (not by value).
     * So when this class manipulates the data you will see that change.
     * when you pass in an audioListener LinkedBlockingQueue object you can call audioListener.take() in your class.
     * your method will take ffrom the top of the que. If there is nothing .take() will BLOCK and lock your thread until some data is available.
     * If no data ever gets added for you to take then your thread will stay locked forever.
     * @param audioListener the holder for the audio data. the audio data will be an array of shorts. and audioListener will start to fill up. be carfull . if you never .take() (or clear) then this object will start to get full
     */
    public void setAudioListener(LinkedBlockingQueue audioListener){
        this.audioListener = audioListener;

    }
}
