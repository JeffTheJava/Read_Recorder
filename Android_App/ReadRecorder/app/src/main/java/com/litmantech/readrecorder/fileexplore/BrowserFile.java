package com.litmantech.readrecorder.fileexplore;

/**
 * Created by Jeff_Dev_PC on 6/11/2016.
 */
public class BrowserFile{
    public final String fileName;
    public final String fileAbsolutePath;
    public final long timeCreated;
    public final boolean isDirectory;

    public BrowserFile(String fileName, String fileAbsolutePath, long timeCreated, boolean isDirectory) {
        this.fileName = fileName;
        this.fileAbsolutePath = fileAbsolutePath;
        this.timeCreated = timeCreated;
        this.isDirectory = isDirectory;
    }
}