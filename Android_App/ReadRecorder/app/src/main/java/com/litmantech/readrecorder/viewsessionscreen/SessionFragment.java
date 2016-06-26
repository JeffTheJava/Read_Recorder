package com.litmantech.readrecorder.viewsessionscreen;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.litmantech.readrecorder.R;
import com.litmantech.readrecorder.read.Session;

/**
 * Created by Jeff_Dev_PC on 6/13/2016.
 */
public class SessionFragment extends Fragment {

    public static final String TAG = "SessionFragment";
    private Context context;
    private Session session;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = this.getContext();

        View view = inflater.inflate(R.layout.session_screen_layout, container, false);

        return view;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
