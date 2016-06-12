package com.litmantech.readrecorder.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;

import com.litmantech.readrecorder.read.line.Entry;
import com.litmantech.readrecorder.utilities.OnStopListener;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jeff_Dev_PC on 5/19/2016.
 */
public class Playback {

    private static final String TAG = "Playback";
    private final int sampleRate;
    private final int minBufSize;
    private final AudioTrack audioTrack;
    private Thread playbackLiveThread;
    private boolean stopPlayBack;
    private LinkedBlockingQueue<short[]> audioQueue;
    private Thread playbackEntryThread;
    private OnStopListener onStopListener;

    /**
     * Playback will create a new Audio Track and call play.
     */
    public Playback(){
        sampleRate = 16000;
        minBufSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufSize, AudioTrack.MODE_STREAM);
    }

    /**
     * asynchronously play live audio data.
     * after calling play you will receive a LinkedBlockingQueue just start filling it with shorts[] then it will auto play back.
     * @return a LinkedBlockingQueue that you will continuously put your audio into.
     */
    public LinkedBlockingQueue<short[]> PlayLive(OnStopListener onStopListener){
        this.onStopListener = onStopListener;

        Log.d(TAG,"Start called");

        if(playbackLiveThread !=null)
            return audioQueue;

        audioTrack.play();
        audioQueue = new LinkedBlockingQueue<>();
        playbackLiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                stopPlayBack = false;
                AsyncLivePlay(audioQueue);
                audioTrack.stop();
                playbackLiveThread = null;
                if(Playback.this.onStopListener != null)
                    Playback.this.onStopListener.onStop();
            }
        }, "Playback Thread Playback.java");
        playbackLiveThread.start();
        return audioQueue;
    }


    public void PlayFile(final Entry currentEntry, OnStopListener onStopListener){
        this.onStopListener = onStopListener;
        if(playbackEntryThread !=null)
            return;

        audioTrack.play();
        playbackEntryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                stopPlayBack = false;
                AsyncFilePlay(currentEntry);
                audioTrack.stop();
                currentEntry.close();
                playbackEntryThread = null;
                if(Playback.this.onStopListener != null)
                    Playback.this.onStopListener.onStop();
            }
        }, "Playback Thread in Playback.java");

        playbackEntryThread.start();
    }

    private void AsyncLivePlay(LinkedBlockingQueue<short[]> audioQueue) {
        while (!stopPlayBack){
            try {
                short[] audioData =  audioQueue.take();//this will block, will need to call interrupt on this thread
                WriteAudio(audioData);

            } catch (InterruptedException e) {
                Stop();
            }
        }
    }

    private void AsyncFilePlay(Entry currentEntry) {
        while (!stopPlayBack){
            try {
                byte[] audioData = currentEntry.ReadAudio(minBufSize);
                WriteAudio(audioData);//this will block until all the bytes are played

            } catch (IOException e) {
                Stop();
            }catch (BufferUnderflowException e) {
                Stop();
            }
        }
    }

    private void WriteAudio(short[] audioData){ audioTrack.write(audioData,0,audioData.length);}
    private void WriteAudio(byte[] audioData){ audioTrack.write(audioData,0,audioData.length);}


    /**
     * no matter if you are playing live audio or a file you can call this stop.
     * Will block your thread until stop is done. if you get locked and want to get out just call interrupt(); on the thread
     */
    public void Stop() {
        Log.d(TAG,"Stop called");
        //check to make sure the thread that called StopRecording() is not the same as the thread we are trying to stop (the audioCollectionThread)
        boolean onPlaybackThread = Thread.currentThread() == playbackLiveThread;

        stopPlayBack = true;

        if(playbackLiveThread != null && onPlaybackThread){
            try {
                playbackLiveThread.interrupt();
                playbackLiveThread.join();
            } catch (InterruptedException e) {/*we will do nothing here. dont care if another thread interrupts this thread*/}

        }

        //check to make sure the thread that called StopRecording() is not the same as the thread we are trying to stop (the audioCollectionThread)
        onPlaybackThread = Thread.currentThread() == playbackEntryThread;
        if(playbackEntryThread != null && onPlaybackThread){
            try {
                playbackEntryThread.interrupt();
                playbackEntryThread.join();
            } catch (InterruptedException e) {/*we will do nothing here. dont care if another thread interrupts this thread*/}

        }

    }

    public boolean isPlaying() {
        if(playbackLiveThread != null)
            return true;
        if(playbackEntryThread != null)
            return true;
        return false;
    }
}
