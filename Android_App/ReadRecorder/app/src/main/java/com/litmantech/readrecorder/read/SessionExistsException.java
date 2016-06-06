package com.litmantech.readrecorder.read;

import java.io.File;

/**
 * Created by Jeff_Dev_PC on 6/1/2016.
 */
public class SessionExistsException  extends InstantiationException{
    private final File session;
    public SessionExistsException(String message, File session){
        super(message);
        /**
         * To prevent any memory leaks we need to make a copy of the session File variable
         *
         */
        this.session = new File(session.getAbsolutePath());
    }

    /**
     * get the File session that cause the exception to happen. if you want you can us this to open a session;
     * or us it to get the path that cause the exception.
     * @return the file dir that holds the session that already exist that caused the exception;
     */
    public File getSession(){
        return session;
    }
}
