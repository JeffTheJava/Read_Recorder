package com.litmantech.readrecorder.read.line;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.litmantech.readrecorder.R;

import java.util.ArrayList;

/**
 * Created by Jeff_Dev_PC on 6/7/2016.
 */
public class EntryListAdapter extends ArrayAdapter<Entry> {
    private final Context context;
    private final ArrayList<Entry> values;
    private int selectedPosition = -1;

    public EntryListAdapter(Context context, ArrayList<Entry> values) {
        super(context, R.layout.entryrowlayout, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.entryrowlayout, parent, false);

        TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
        textView.setText(values.get(position).getSentence());

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
