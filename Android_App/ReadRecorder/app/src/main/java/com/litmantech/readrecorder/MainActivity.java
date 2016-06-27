package com.litmantech.readrecorder;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.litmantech.readrecorder.permissionsscreen.PermissionsHandler;
import com.litmantech.readrecorder.read.Session;

public class MainActivity extends AppCompatActivity implements PermissionsHandler.OnPermissionsListener {

    private static final String TAG = "MainActivity";
    private PermissionsHandler pmHandler;
    private ScreenNavigationHandler navHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//don't let phone sleep


        FragmentManager fragmentManager = getSupportFragmentManager();
        int fragmentID = R.id.fragment_layout;

        navHandler = new ScreenNavigationHandler(fragmentManager, fragmentID);


        pmHandler = new PermissionsHandler();
        pmHandler.RequestAndroidPermissions(this, this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        pmHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //if we get access to all permissions
    public void OnPermissionsSuccessful(){
        ShowHomeScreen();
    }

    /**
     * Run Only On UI Thread
     */
    public void OnPermissionsFail() {
        ShowNoPermissionScreen();
    }


    private void ShowNoPermissionScreen() {

        navHandler.ShowNoPermissionScreen(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pmHandler.RequestAndroidPermissions(MainActivity.this,MainActivity.this);
            }
        });
    }

    public void ShowHomeScreen() {
        navHandler.ShowHomeScreen();
    }


    public void ShowSessionScreen(Session session){
        navHandler.ShowSessionScreen(session);
    }

    public void ShowEntryScreen(Session session){
        navHandler.ShowEntryScreen();
    }

}
