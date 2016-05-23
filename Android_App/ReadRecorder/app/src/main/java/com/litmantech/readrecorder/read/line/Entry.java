package com.litmantech.readrecorder.read.line;

/**
 * Created by Jeff_Dev_PC on 5/20/2016.
 */
public class Entry {

    private final String sentence;
    private final int uniqueID;
    private String audioFileName;
    private String audioFileLocation;

    public Entry(String sentence, int uniqueID) {
        this.sentence = sentence;
        this.uniqueID = uniqueID;
    }

    /**
     * Get the sentence that this entry was created based of.
     * @return
     */
    public String getSentence(){
        return sentence;
    }


    public void SaveAudio(short[] audioBuff) {

    }
}
