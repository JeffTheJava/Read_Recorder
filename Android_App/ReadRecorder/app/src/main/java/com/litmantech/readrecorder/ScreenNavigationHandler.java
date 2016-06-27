package com.litmantech.readrecorder;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

import com.litmantech.readrecorder.homescreen.HomeFragment;
import com.litmantech.readrecorder.permissionsscreen.NoPermissionFragment;
import com.litmantech.readrecorder.read.Session;
import com.litmantech.readrecorder.viewsessionscreen.SessionFragment;

/**
 * Created by Jeff_Dev_PC on 6/26/2016.
 */
public class ScreenNavigationHandler {
    public static final String TAG = "ScreenNavigationHandler";


    private final FragmentManager fragmentManager;
    private final int fragmentID;

    public ScreenNavigationHandler(@NonNull FragmentManager fragmentManager, @NonNull int fragmentID){
        this.fragmentManager = fragmentManager;
        this.fragmentID = fragmentID;
    }


    public void ShowNoPermissionScreen(View.OnClickListener onRequestClick) {

        NoPermissionFragment permissionFragment = new NoPermissionFragment();
        permissionFragment.setOnRequestClick(onRequestClick);

        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(fragmentID, permissionFragment,NoPermissionFragment.TAG);
        ft.commitAllowingStateLoss();// we do not use .commit(); because onSaveInstanceState might be called do to the permissions popup screen.
    }

    public void ShowHomeScreen() {

        FragmentTransaction ft = fragmentManager.beginTransaction();
        HomeFragment homeFragment = new HomeFragment();
        ft.replace(fragmentID, homeFragment,HomeFragment.TAG);
        ft.commitAllowingStateLoss();// we do not use .commit(); because onSaveInstanceState might be called do to the permissions popup screen.
    }

    public void ShowSessionScreen(Session session) {

        FragmentTransaction ft = fragmentManager.beginTransaction();
        SessionFragment sessionFragment = new SessionFragment();
        sessionFragment.setSession(session);
        ft.addToBackStack(SessionFragment.TAG);//allow the back button to pop the fragment stack
        ft.add(fragmentID, sessionFragment,SessionFragment.TAG);
        ft.commit();
    }

    public void ShowEntryScreen() {
        if(fragmentManager == null || fragmentID == -1){
            Log.e(TAG,"unable to show screen please wait for android to call onCreate before you call ShowEntryScreen");
            return;
        }


    }
}
