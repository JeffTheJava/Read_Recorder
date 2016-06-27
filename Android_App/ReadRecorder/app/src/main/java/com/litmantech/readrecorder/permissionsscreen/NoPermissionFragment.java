package com.litmantech.readrecorder.permissionsscreen;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.litmantech.readrecorder.R;

/**
 * Created by Jeff_Dev_PC on 6/26/2016.
 */
public class NoPermissionFragment extends Fragment {

    public static final String TAG = "NoPermissionFragment";
    private Context context;
    private View.OnClickListener onRequestClick;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = this.getContext();

        View view = inflater.inflate(R.layout.no_permission_screen_layout, container, false);

        Button requestBTN = (Button) view.findViewById(R.id.request_btn);
        requestBTN.setOnClickListener(onRequestClick);
        return view;
    }

    public void setOnRequestClick(View.OnClickListener onRequestClick) {
        this.onRequestClick = onRequestClick;
    }
}
