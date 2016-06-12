package com.litmantech.readrecorder.read;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;

import com.litmantech.readrecorder.R;
import com.litmantech.readrecorder.audio.Recorder;
import com.litmantech.readrecorder.read.line.Entry;
import com.litmantech.readrecorder.utilities.OnStopListener;
import com.litmantech.readrecorder.utilities.UiUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jeff_Dev_PC on 5/20/2016.
 */
public class Session {
    private static final String TAG = "Session";
    public static final String HIDDEN_FILE_NAME =".ReadRecorder.info";// that first dot makes this file hidden on android file system.
    public static final String DEFAULT_DIR_NAME = "Session1";
    public static final String REF_TEXT_FILE_NAME = "RefText.txt";

    private Context context;
    private final ArrayList<Entry> entries;
    private final File sessionDir;
    private Entry currentEntry;
    private Thread audioCollectionThread = null;
    private boolean stopCollectingAudio = false;
    private OnStopListener onStopListener = null;

    /**
     * Create a new session using an String[] of sentences
     * if you want to use an ArrayList<String>  use:
     *  @see #Session(Context, String, ArrayList)
     *  A session will hold all the data about the current data collection test.
     *
     * @param context context of the active onscreen activity
     * @param sessionDirName the name of the dir we will save on the sd card. dont worry about if it exists or not. just pass in a string like "Jeff_folder"
     * @param sentences each sentences that was in the text document. make sure you clean in it and make it nice a pretty before passing in.
     * @throws NullPointerException
     * @throws SessionExistsException
     */
    public Session(Context context, String sessionDirName, String[] sentences) throws NullPointerException, SessionExistsException{
        this.context = context;
        if (sentences == null || sentences.length == 0)
            throw new NullPointerException("Your sentences are NULL!!!!");

        String dataDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        String currentSessionLocation = dataDirectory + "/" + sessionDirName;

        sessionDir = new File(currentSessionLocation);
        if (!sessionDir.exists()) {
            sessionDir.mkdirs();
        }else{
            String[] list = sessionDir.list();
            if (list != null) {
                for (String file : list) {
                    if(file.trim().toUpperCase().contains(HIDDEN_FILE_NAME.trim().toUpperCase())){
                        throw new SessionExistsException("Session Already exists Try a different name!", sessionDir);
                    }
                }
            }
        }
        
        CreateHiddenInfoFile(sessionDir);// create a hidden info file that we will use to know that this is a Read Recorder Dir. we will use this when the open session button is pressed

        SaveSentencesToCurrentLocation(sentences);

        //MakeDirVisibleOverUSB(sessionDir.toString());//having issues with one phone but works with 4 other phones i tested

        entries = BuildEntries(sentences);//because entries is finale we use a method that returns entries

        audioCollectionThread = null;

        NextEntry();// go to the first Entry;
    }

    /**
     * Create a new session using an ArrayList<String> of sentences
     * if you want to use a String[] use:
     *  @see #Session(Context, String, String[])
     *  A session will hold all the data about the current data collection test.
     *
     * @param context context of the active onscreen activity
     * @param sessionDirName the name of the dir we will save on the sd card. dont worry about if it exists or not. just pass in a string like "Jeff_folder"
     * @param sentences each sentences that was in the text document. make sure you clean in it and make it nice a pretty before passing in.
     * @throws NullPointerException
     * @throws SessionExistsException
     */
    public Session(Context context, String sessionDirName, ArrayList<String> sentences) throws NullPointerException, SessionExistsException {
        this(context, sessionDirName, sentences.toArray(new String[0]));//Originally this code used new String[list.size()]. However, i found a blogpost revealing that due to JVM optimizations, using new String[0] is better now. http://shipilev.net/blog/2016/arrays-wisdom-ancients/
    }

    /**
     * open a session that already exists on the hdd of the device
     */
    public Session(Context context, File alreadyExistingSession) throws IOException {
        this.context = context;
        File refTXT = new File(alreadyExistingSession,REF_TEXT_FILE_NAME);

        String[] sentences = UiUtil.OpenTxtDocLineByLine(refTXT);

        this.sessionDir = alreadyExistingSession;
        entries = BuildEntries(sentences);
        audioCollectionThread = null;

        NextEntry();// go to the first Entry;
    }

    private ArrayList<Entry> BuildEntries(String[] sentences) {
        ArrayList<Entry> tempEntries = new ArrayList<Entry>();
        for (String sentence : sentences) {
            int uniqueID = tempEntries.size();//so this is a cool trick. I don't want to i++ each loop (that's borrowing). Take a look at the loop. when it first starts, what is entries.size()? 0 right. then next loop what is it? 1. and so on..

            Entry entry = new Entry(sentence, (uniqueID+1)/*make sure the ID starts at 1 not 0*/, sessionDir);
            entry.setSession(this);
            tempEntries.add(entry);
        }
        return tempEntries;
    }

    private void SaveSentencesToCurrentLocation(String[] sentences) {

        File sessionTextFile = new File(sessionDir.getAbsolutePath(),REF_TEXT_FILE_NAME);

        StringBuilder builder = new StringBuilder();
        for(String s : sentences) {
            String endOfLineMarker = "\r\n";//This is because \n does not represent a full line break in Windows. Using \n is "the Unix" way of doing line breaks
            String newLine = builder.length() != 0?endOfLineMarker:"";

            builder.append(newLine);
            builder.append(s);
        }

        String message = builder.toString();
        try {
            UiUtil.SaveStringToFile(sessionTextFile,message);
            MakeDirVisibleOverUSB(sessionTextFile.getAbsolutePath());
        } catch (IOException e) {
            Log.d(TAG,"unable to save file:"+REF_TEXT_FILE_NAME);
        }
    }

    private void CreateHiddenInfoFile(File sessionDir) {

        String filename = HIDDEN_FILE_NAME;
        File hiddenFile = new File(sessionDir.getAbsolutePath(), filename);

        String message = "!!Do not delete or rename this file!!\n\nWill have more data in this file soon. For now we use this file to prove this is a Read Recorder directory. If you delete this file then this folder will not show up when you click open session button ";
        try {
            UiUtil.SaveStringToFile(hiddenFile,message);
        } catch (IOException e) {
            Log.d(TAG,"unable to save file:"+filename);
        }
    }


    /**
     * Will go to the next Entry if it can. you might already be at the last entry.
     * This will NOT loop around. Its a hard stop at the last entry and/or the first entry.
     *
     * @return will return false if (for any reason) could not go to the next entry(like there is no "NEXT" entry). will return true is successful
     */
    public boolean NextEntry() {

        if (currentEntry == null) {
            currentEntry = entries.get(0);//we can hard code 0 because this class would have never been created if there went at lest one
            return true;
        }

        int currentPosition = entries.lastIndexOf(currentEntry);
        int nextPosition = currentPosition + 1;

        if (nextPosition >= entries.size())
            return false;//we are currently at the end. cant go past

        return GoToEntry(nextPosition);
    }

    //TODO

    /**
     * @return
     */
    public boolean PreviousEntry() {

        if (currentEntry == null) {
            currentEntry = entries.get(0);//we can hard code 0 because this class would have never been created if there were o entries
            return true;
        }

        int currentPosition = entries.lastIndexOf(currentEntry);
        int previousPosition = currentPosition - 1;

        if (previousPosition <= -1)
            return false;//we are at the first entry already cant go back any more

        return GoToEntry(previousPosition);
    }

    /**
     * Set the current Entry to an entry in in the getEntries() arraylist
     * @param position the position in the getEntries() arraylist that you want to set the current entry to.
     * @return will return true if we can find a valid entry at this position. false is nothing happened.
     * @see #getEntries()
     */
    public boolean GoToEntry(int position){

        try {
            currentEntry = entries.get(position);
            return true;
        }catch (ArrayIndexOutOfBoundsException e){
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

    /**
     * Get the current active entry.
     * There will always be an active entry. never will return null;
     *
     * @see #NextEntry()
     * @see #PreviousEntry()
     */
    public Entry getCurrentEntry() {
        return currentEntry;
    }

    public boolean isRecording() {
        return audioCollectionThread != null;// is the audio collector is running the we must be recording.
    }

    /**
     * will start collect audio and giving it to the current entry. if it is already running nothing will happen
     * StartRecording is not a blocking call it will return right away. that does not mean you have started yet.
     * check isRecording to see if you have started yet.
     *
     * @param recorder the global recorder that will open and collect mic audio for us.
     * @param onStopListener the Rec audio thread might stop but you did not call stop. so you did'ent know it was stopped. you can use this to be notified. its ok to set this to null
     * @see #getCurrentEntry()
     */
    public void StartRecording(final Recorder recorder, OnStopListener onStopListener) {
        if (audioCollectionThread != null)
            return;

        this.onStopListener = onStopListener;

        audioCollectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                stopCollectingAudio = false;
                LinkedBlockingQueue<short[]> audioHolder = new LinkedBlockingQueue<short[]>();
                recorder.setAudioListener(audioHolder);
                CollectAudio(audioHolder);//this will block until you call #StopRecording()
                recorder.setAudioListener(null);//stop adding audio
                DrainAudio(audioHolder);
                currentEntry.close();//close the audio file on disk
                audioCollectionThread = null;// if we got here then that mean we are ready to stop. null the thread so we can make a new one
                if(Session.this.onStopListener != null)
                    Session.this.onStopListener.onStop();
            }
        }, "Audio Collection Thread in Session.java");

        audioCollectionThread.start();
    }

    private void CollectAudio(LinkedBlockingQueue<short[]> audioHolder) {
        while (!stopCollectingAudio) {
            try {
                short[] audioBuff = audioHolder.take();
                currentEntry.SaveAudio(audioBuff);

            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
                StopRecording();//if we got interrupted then we need to stop!!!
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                StopRecording();//if an error happen while recording!!!
            }
        }
    }

    /**
     * save the last little bits of audio that is left
     */
    private void DrainAudio(LinkedBlockingQueue<short[]> audioHolder) {
        if(!audioHolder.isEmpty()){
            short[] audioBuff;
            try {
                audioBuff = audioHolder.take();
                currentEntry.SaveAudio(audioBuff);
            } catch (InterruptedException e) {
                StopRecording();
            } catch (IOException e) {
                StopRecording();
            }
        }
    }

    /**
     * stop collect audio for the current event
     * this call will lock your thread until stop is 100% done
     */
    public void StopRecording() {
        stopCollectingAudio = true;

        //check to make sure the thread that called StopRecording() is not the same as the thread we are trying to stop (the audioCollectionThread)
        boolean onAudioCollectionThread = Thread.currentThread() == audioCollectionThread;

        //check if audioCollectionThread is done and we are not on the audioCollectionThread
        if (audioCollectionThread != null && !onAudioCollectionThread) {
            try {
                audioCollectionThread.join();// audioCollectionThread not done yet so lest wait and lock until its done
            } catch (InterruptedException e) {/*its ok for this thread to be interrupted i will do nothing here.*/}
        }

    }

    /**
     * Make a file or/and a dir visible over USB
     * A way to make sure, if the user plugs the phone into a computer, they can see the files over USB. This may or may not work in some cases.
     * We dont care if this works or not. If you do then you need to add a callback use:
     * @see #MakeDirVisibleOverUSB(String, MediaScannerConnection.OnScanCompletedListener)
     *
     * @param absolutePath full path and/or file name of the folder or file you want to user to see over usb
     */
    public void MakeDirVisibleOverUSB(String absolutePath) {
        if (context != null) {
            MakeDirVisibleOverUSB(absolutePath,null);
        }
    }

    /**
     * Make a file or/and a dir visible over USB
     * A way to make sure, if the user plugs the phone into a computer, they can see the files over USB.
     * If you dont need the call back then make onScanCompletedListener == null or call:
     *
     * @param absolutePath full path and/or file name of the folder or file you want to user to see over usb
     * @see #MakeDirVisibleOverUSB(String)
     */
    public void MakeDirVisibleOverUSB(String absolutePath, MediaScannerConnection.OnScanCompletedListener onScanCompletedListener) {
        if (context != null) {
            MediaScannerConnection.scanFile(context, new String[]{absolutePath}, null, onScanCompletedListener);//I dont care if this works or not. thats why i pass null for the call back. so what is the usb works. dont care
        }
    }

    public String getName() {
        return sessionDir.getName();
    }

    public File getSessionDir(){
        return sessionDir;
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }

}
