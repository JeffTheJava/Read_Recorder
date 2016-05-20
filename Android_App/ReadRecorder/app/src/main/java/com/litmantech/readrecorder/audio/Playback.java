package com.litmantech.readrecorder.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jeff_Dev_PC on 5/19/2016.
 */
public class Playback {

    private static final String TAG = "Playback";
    private final int sampleRate;
    private final int minBufSize;
    private final AudioTrack audioTrack;
    private Thread playbackThread;
    private boolean stopPlayBack;
    private LinkedBlockingQueue<short[]> audioQueue;

    /**
     * Playback will create a new Audio Track and call play.
     */
    public Playback(){
        sampleRate = 16000;
        minBufSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufSize, AudioTrack.MODE_STREAM);
    }

    /**
     * asynchronously play audio data.
     * @return a LinkedBlockingQueue that you will continuously put your audio into.
     */
    public LinkedBlockingQueue<short[]> Play(){

        Log.d(TAG,"Start called");

        if(playbackThread !=null)
            return audioQueue;

        audioTrack.play();
        audioQueue = new LinkedBlockingQueue<>();
        playbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                stopPlayBack = false;
                AsyncPlay(audioQueue);
                audioTrack.stop();
                playbackThread = null;
            }
        });
        playbackThread.start();
        return audioQueue;
    }

    private void AsyncPlay(LinkedBlockingQueue<short[]> audioQueue) {
        while (!stopPlayBack){
            try {
                short[] audioData =  audioQueue.take();//this will block, will need to call interrupt on this thread
                WriteAudio(audioData);

            } catch (InterruptedException e) {
               // Don't care is the thread is Interrupted. Its ok we'll do nothing...
            }
        }
    }


    private void WriteAudio(short[] audioData){
        audioTrack.write(audioData,0,audioData.length);
    }


    /**
     * Will block your thread until stop is done. if you get locked and want to get out just call interrupt(); on the thread
     */
    public void Stop() {
        Log.d(TAG,"Stop called");

        stopPlayBack = true;
        if(playbackThread != null){
            try {
                playbackThread.interrupt();
                playbackThread.join();
            } catch (InterruptedException e) {/*we will do nothing here. dont care if another thread interrupts this thread*/}

        }
    }
}
