package com.litmantech.readrecorder.fileexplore;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.litmantech.readrecorder.R;

import java.util.ArrayList;

/**
 * Created by Jeff_Dev_PC on 6/11/2016.
 */
public class SessionListAdapter extends ArrayAdapter<BrowserFile> {
    private final Context context;
    private final ArrayList<BrowserFile> values;
    private int selectedPosition = -1;

    public SessionListAdapter(Context context, ArrayList<BrowserFile> values) {
        super(context, R.layout.entryrowlayout, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.entryrowlayout, parent, false);

        BrowserFile file = values.get(position);
        TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
        textView.setText(file.fileName);

        if(position == selectedPosition){
            rowView.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
        }else{
            rowView.setBackgroundColor(Color.TRANSPARENT);
        }

        return rowView;
    }



    /**
     * Set the highlighted position. This does Not mean the highlighted position is the same as the current position. Its up to you to check for that.
     * @param position
     */
    public void setSelection(int position){
        this.selectedPosition = position;
    }





}
