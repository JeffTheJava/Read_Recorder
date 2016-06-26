package com.litmantech.readrecorder.fileexplore;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.litmantech.readrecorder.R;
import com.litmantech.readrecorder.read.Session;

import java.util.ArrayList;

/**
 * Created by Jeff_Dev_PC on 6/11/2016.
 */
public class SessionListAdapter extends ArrayAdapter<Session> {
    private final Context context;
    private final ArrayList<Session> values;
    private int selectedPosition = -1;

    public SessionListAdapter(Context context, ArrayList<Session> values) {
        super(context, R.layout.entryrowlayout, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.session_list_layout, parent, false);

        Session session = values.get(position);
        TextView titleLabel = (TextView) rowView.findViewById(R.id.session_label);
        TextView dateLabel = (TextView) rowView.findViewById(R.id.session_mod_date);
        TextView sentencesList = (TextView) rowView.findViewById(R.id.sentence_list);
        TextView sentCountLabel = (TextView) rowView.findViewById(R.id.sentences_count);
        TextView recCountLabel = (TextView) rowView.findViewById(R.id.rec_count);

        titleLabel.setText(session.getName());
        dateLabel.setText("Date Modified: "+session.getSessionDir().lastModified());
        String[] sessionArray = session.getEntriesSentences();
        int maxSentencesToShow = 5;//idk 5 looks good to me so...
        sentencesList.setText(ArrayToNewLine(sessionArray,maxSentencesToShow));
        sentCountLabel.setText(""+sessionArray.length);
        recCountLabel.setText(""+session.getEntriesRecCount());



        if(position == selectedPosition){
            rowView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        }else{
            rowView.setBackgroundColor(Color.TRANSPARENT);
        }

        return rowView;
    }

    private String ArrayToNewLine(String[] chosenSentences, int maxToShow){

        if(chosenSentences == null || chosenSentences.length ==0)
            return "... ... ...\n... ... ...\n... ... ...";


        String showOnlyMaxSentences = "";
        for (int i = 0; i < (chosenSentences.length >= maxToShow ? maxToShow : chosenSentences.length); i++) {
            showOnlyMaxSentences += chosenSentences[i]+"\n";
        }


        return showOnlyMaxSentences;
    }



    /**
     * Set the highlighted position. This does Not mean the highlighted position is the same as the current position. Its up to you to check for that.
     * @param position
     */
    public void setSelection(int position){
        this.selectedPosition = position;
    }





}
