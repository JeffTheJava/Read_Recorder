package com.litmantech.readrecorder.homescreen;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.litmantech.readrecorder.MainActivity;
import com.litmantech.readrecorder.R;
import com.litmantech.readrecorder.fileexplore.SessionFile;
import com.litmantech.readrecorder.fileexplore.SessionListAdapter;
import com.litmantech.readrecorder.read.Session;
import com.litmantech.readrecorder.utilities.UiUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Jeff_Dev_PC on 6/13/2016.
 */
public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";
    private Context context;
    private Button goRouteScreen;
    private Thread openSessions;
    private ListView sessionListView;
    private Button newSessionBTN;
    private ArrayList<Session> sessionArray;
    private SessionListAdapter sessionListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = this.getContext();

        View view = inflater.inflate(R.layout.home_screen_layout, container, false);
        sessionListView = (ListView) view.findViewById(R.id.session_list);
        newSessionBTN = (Button) view.findViewById(R.id.new_session_btn);


        newSessionBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateSessionClicked();
            }
        });
        sessionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Session session = sessionArray.get(position);
                onOpenSessionClicked(session);
            }
        });

        sessionArray = new ArrayList<Session>();

        sessionListAdapter = new SessionListAdapter(context,sessionArray);
        sessionListView.setAdapter(sessionListAdapter);

        if(openSessions == null)
            OpenSession();

        return view;
    }



    private void OpenSession() {
        final ProgressDialog progress = new ProgressDialog(context);
        progress.setMessage("loading...");
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);
        progress.show();

        openSessions = new Thread(new Runnable() {
            @Override
            public void run() {
                OpenMostRecentSessions(50);
                ((Activity)context).runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        UpdateUI();
                    }
                });
                openSessions=null;
                progress.dismiss();
            }
        }, "Open Session Threads");

        openSessions.start();


    }

    public void UpdateUI() {
        SortByDate(sessionArray);

        sessionListView.post(new Runnable() {
            @Override
            public void run() {

                sessionListAdapter.notifyDataSetChanged();
                sessionListView.setSelection(0);
                sessionListView.smoothScrollToPosition(0);//scroll back to the top
            }
        });
    }

    private void SortByDate(ArrayList<Session> sessionArray) {
        Collections.sort(sessionArray,new Comparator<Session>(){
            public int compare(Session file1, Session file2) {
                return file1.getSessionDir().getAbsolutePath().compareToIgnoreCase(file2.getSessionDir().getAbsolutePath());
            }
        });
    }

    public void OpenMostRecentSessions(int maxSessions) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sessionArray.clear();
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.getName().startsWith(".")) {
                    if (file.isDirectory()) {
                        if (isSessionFolder(file)) {
                            try {
                                Session session = new Session(context, file);
                                AddSession(session);
                            }catch (IOException e){
                                //TODO
                            }

                        }
                    }
                }
            }
        }





    }

    private int GetRecordingsCount(File sessionDir) {
        String[] listFiles = sessionDir.list();
        int count = 0;

        if(listFiles == null) return count;

        for(String eachFile: listFiles){
            String[] filenameArray = eachFile.split("\\.");
            if(filenameArray!=null){
                String extension = filenameArray[filenameArray.length-1];
                if(extension.toLowerCase().trim().contains("raw".toLowerCase())){
                    count++;
                }

            }
        }
        return count;
    }

    private String[] GetSentences(File sessionDir) {
        File textDoc = new File(sessionDir.getAbsolutePath(), Session.REF_TEXT_FILE_NAME); ;

        try {
            String[] linesInTextDoc = UiUtil.OpenTxtDocLineByLine(textDoc);
            return linesInTextDoc;
        } catch (IOException e) {
            return null;
        }
    }

    private boolean isSessionFolder(File file) {
        if(file == null) return false;

        String[] fileList = file.list();
        if(fileList == null) return false;


        for(String fileName: fileList){
            boolean hasHiddenFile =  fileName.trim().toLowerCase().contains(Session.HIDDEN_FILE_NAME.trim().toLowerCase());
            if(hasHiddenFile) return true;
        }

        return false;
    }

    private void onCreateSessionClicked() {
        ((MainActivity)context).CreateSessionScreen(this);
    }

    private void onOpenSessionClicked(Session session) {

        ((MainActivity)context).ShowSessionScreen(session);


    }

    public void AddSession(Session session) {
        sessionArray.add(session);// add if directory
    }
}
