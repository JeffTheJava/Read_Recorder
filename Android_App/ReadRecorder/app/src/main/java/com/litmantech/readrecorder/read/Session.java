package com.litmantech.readrecorder.read;

import android.util.Log;

import com.litmantech.readrecorder.audio.Recorder;
import com.litmantech.readrecorder.read.line.Entry;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jeff_Dev_PC on 5/20/2016.
 */
public class Session {
    private static final String TAG = "Session";

    private final ArrayList<Entry> entries;
    private Entry currentEntry;
    private Thread audioCollectionThread = null;
    private boolean stopCollectingAudio = false;

    /**
     * A session will hold all the data about the current data collection test.
     * @param sentences each sentences that was in the text document. make sure you clean in it and make it nice a pretty before passing in.
     */
    public Session(String[] sentences) throws NullPointerException{

        if(sentences == null || sentences.length ==0)
            throw new NullPointerException("Your sentences are NULL!!!!");

        entries = new  ArrayList<Entry>();
        for(String sentence : sentences){
            int uniqueID = entries.size();//so this is a cool trick. I don't want to i++ each loop (that's borrowing). Take a look at the loop. when it first starts, what is entries.size()? 0 right. then next loop what is it? 1. and so on..
            Entry entry = new Entry(sentence, uniqueID);
            entries.add(entry);
        }

        audioCollectionThread = null;

        NextEntry();// go to the first Entry;
    }

    /**
     * Will go to the next Entry if it can. you might already be at the last entry.
     * This will NOT loop around. Its a hard stop at the last entry.
     * @return will return false if (for any reason) could not go to the next entry(like there is no "NEXT" entry). will return true is successful
     */
    public boolean NextEntry(){

        if(currentEntry == null) {
            currentEntry = entries.get(0);//we can hard code 0 because this class would have never been created if there were o entries
            return true;
        }

        int currentPosition = entries.lastIndexOf(currentEntry);
        int nextPosition = currentPosition + 1;

        if(nextPosition >= entries.size())
            return false;//we are currently at the end. cant go past

        currentEntry = entries.get(nextPosition);
        return true;
    }

    //TODO
    /**
     *
     * @return
     */
    public boolean PreviousEntry(){

        if(currentEntry == null) {
            currentEntry = entries.get(0);//we can hard code 0 because this class would have never been created if there were o entries
            return true;
        }

        int currentPosition = entries.lastIndexOf(currentEntry);
        int previousPosition = currentPosition - 1;

        if(previousPosition <= -1)
            return false;//we are at the first entry already cant go back any more

        currentEntry = entries.get(previousPosition);
        return true;
    }

    /**
     * Get the current active entry.
     * There will always be an active entry. never will return null;
     * @see #NextEntry()
     * @see #PreviousEntry()
     */
    public Entry getCurrentEntry(){
        return currentEntry;
    }

    public boolean isRecording() {
        return audioCollectionThread !=null;// is the audio collector is running the we must be recording.
    }

    /**
     * will start collect audio and giving it to the current entry. if it is already running nothing will happen
     *StartRecording is not a blocking call it will return right away. that does not mean you have started yet.
     * check isRecording to see if you have started yet.
     * @param recorder the global recorder that will open and collect mic audio for us.
     * @see #getCurrentEntry()
     */
    public void StartRecording(final Recorder recorder) {
        if(audioCollectionThread != null)
            return;

        audioCollectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                stopCollectingAudio = false;
                LinkedBlockingQueue<short[]> audioHolder = new LinkedBlockingQueue<short[]>();
                recorder.setAudioListener(audioHolder);
                CollectAudio(audioHolder);//this will block until you call #StopRecording()
                audioCollectionThread = null;// if we got here then that mean we are ready to stop. null the thread so we can make a new one
            }
        }, "Audio Collection Thread in Session.java");

        audioCollectionThread.start();
    }

    private void CollectAudio(LinkedBlockingQueue<short[]> audioHolder) {
        while (!stopCollectingAudio){
            try {
                short[] audioBuff = audioHolder.take();
                currentEntry.SaveAudio(audioBuff);

            } catch (InterruptedException e) {
                StopRecording();//if we got interrupted then we need to stop!!!
            }
        }
    }

    /**
     * stop collect audio for the current event
     * this call will lock your thread until stop is 100% done
     */
    public void StopRecording(){
        stopCollectingAudio = true;
        if(audioCollectionThread!=null){
            audioCollectionThread.interrupt();
            try {
                audioCollectionThread.join();
            } catch (InterruptedException e) {/*its ok for this thread to be interrupted i will do nothing here.*/}
        }

    }
}
