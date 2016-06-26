package com.litmantech.readrecorder.fileexplore;


import java.io.File;

/**
 * Created by Jeff_Dev_PC on 6/13/2016.
 */
public class SessionFile {

    public final File fileDir;
    public final String fileName;
    public final long lastModDate;
    public final String[] sentencesList;
    public final int recCount;

    public SessionFile(File fileDir, long lastModDate, String[] sentencesList, int recCount){
        this.fileDir = fileDir;
        this.fileName = fileDir.getName();
        this.lastModDate = lastModDate;
        this.sentencesList = sentencesList;
        this.recCount = recCount;
    }
}
