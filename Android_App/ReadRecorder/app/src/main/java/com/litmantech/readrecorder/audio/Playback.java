package com.litmantech.readrecorder.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jeff_Dev_PC on 5/19/2016.
 */
public class Playback {

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
        audioTrack.play();
    }


    public void Play(short[] audioData){
        audioTrack.write(audioData,0,audioData.length);
    }

    public LinkedBlockingQueue<short[]> Play(){


        if(playbackThread !=null)
            return audioQueue;

        audioQueue = new LinkedBlockingQueue<>();
        playbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                stopPlayBack = false;
                AsyncPlay(audioQueue);
                playbackThread = null;
            }
        });
        playbackThread.start();
        return audioQueue;
    }

    private void AsyncPlay(LinkedBlockingQueue<short[]> audioQueue) {
        while (!stopPlayBack){
            try {
                short[] audioData =  audioQueue.take();
                Play(audioData);

            } catch (InterruptedException e) {
                //TODO
            }
        }
    }

    public void Stop() {
        stopPlayBack = true;
        if(playbackThread != null){
            try {
                playbackThread.join();
            } catch (InterruptedException e) {
                //TODO
            }
        }
    }
}
