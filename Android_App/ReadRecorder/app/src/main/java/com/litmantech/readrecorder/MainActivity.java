package com.litmantech.readrecorder;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.litmantech.readrecorder.audio.Playback;
import com.litmantech.readrecorder.audio.Recorder;
import com.litmantech.readrecorder.fileexplore.FileBrowser;
import com.litmantech.readrecorder.fileexplore.SessionFile;
import com.litmantech.readrecorder.homescreen.HomeFragment;
import com.litmantech.readrecorder.read.NewSessionDialog;
import com.litmantech.readrecorder.read.OpenSessionDialog;
import com.litmantech.readrecorder.read.Session;
import com.litmantech.readrecorder.read.SessionExistsException;
import com.litmantech.readrecorder.read.line.Entry;
import com.litmantech.readrecorder.read.line.EntryListAdapter;
import com.litmantech.readrecorder.utilities.OnStopListener;
import com.litmantech.readrecorder.utilities.UiUtil;
import com.litmantech.readrecorder.viewsessionscreen.SessionFragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FragmentManager fragmentManager;
    private int fragmentID = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//don't let phone sleep


        fragmentManager = getSupportFragmentManager();
        fragmentID = R.id.fragment_layout;

        ShowHomeScreen();
    }

    public void ShowHomeScreen() {

        if(fragmentManager == null || fragmentID == -1){
            Log.e(TAG,"unable to show screen please wait for android to call onCreate before you call ShowHomeScreen");
            return;
        }

        FragmentTransaction ft = fragmentManager.beginTransaction();
        HomeFragment homeFragment = new HomeFragment();
        ft.replace(fragmentID, homeFragment,HomeFragment.TAG);
        ft.commit();
    }

    /**
     *
     * @param homeFragment - the fragment that called create session screen. we need this to update the fragment list
     */
    public void CreateSessionScreen(final HomeFragment homeFragment){
        if(fragmentManager == null || fragmentID == -1){
            Log.e(TAG,"unable to show  screen please wait for android to call onCreate before you call CreateSessionScreen");
            return;
        }

        File lastGoodSessionDir = null;

        //it is safe and ok to pass in null for lastGoodSessionDir
        final NewSessionDialog sessionDialog = new NewSessionDialog(this,lastGoodSessionDir);
        final Activity activity = this;
        sessionDialog.setOnFileSelectedListener(new NewSessionDialog.OnFileSelectedListener() {
            /**
             * onNewSession will dismiss and close the dialog box
             * @param session
             */
            @Override
            public void onNewSession(final Session session) {
                sessionDialog.setOnFileSelectedListener(null);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(homeFragment == null)
                            ShowHomeScreen();
                        homeFragment.AddSession(session);//add a session to the session list
                        homeFragment.UpdateUI();
                        //ShowSessionScreen(session);
                    }
                });
            }

            @Override
            public void onDismissed() {
                sessionDialog.setOnFileSelectedListener(null);
            }
        });

    }

    public void ShowSessionScreen(Session session){
        if(fragmentManager == null || fragmentID == -1){
            Log.e(TAG,"unable to show screen please wait for android to call onCreate before you call ShowSessionScreen");
            return;
        }


        FragmentTransaction ft = fragmentManager.beginTransaction();
        SessionFragment sessionFragment = new SessionFragment();
        sessionFragment.setSession(session);
        ft.addToBackStack(SessionFragment.TAG);//allow the back button to pop the fragment stack
        ft.add(fragmentID, sessionFragment,SessionFragment.TAG);
        ft.commit();


    }

    public void ShowEntryScreen(Session session){
        if(fragmentManager == null || fragmentID == -1){
            Log.e(TAG,"unable to show screen please wait for android to call onCreate before you call ShowEntryScreen");
            return;
        }
    }

}
