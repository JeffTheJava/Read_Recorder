package com.litmantech.readrecorder.read;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.litmantech.readrecorder.R;
import com.litmantech.readrecorder.fileexplore.FileBrowser;
import com.litmantech.readrecorder.fileexplore.SessionBrowser;
import com.litmantech.readrecorder.utilities.UiUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by Jeff_Dev_PC on 6/8/2016.
 */
public class OpenSessionDialog implements View.OnClickListener {
    private static final String TAG = "NewSessionDialog";

    private final Context context;
    private final AlertDialog dialog;
    private final TextView sessionNameLabel;
    private final TextView sessionInfoTxt;
    private final TextView sentencesList;
    private final TextView explorerLabel;

    private String[] chosenSentences = null;
    private int numberOfRecordings = 0;
    private int createdDate = 0;
    private String chosenSessionName = "";


    private SessionBrowser sessionBrowser;
    private File sessionDir;

    private OnFileSelectedListener onFileSelectedListener;

    private final SessionBrowser.OnFileSelectedListener fileSelectedListener = new SessionBrowser.OnFileSelectedListener() {
        @Override
        public void onFileSelected(File fileSelected) {
            sessionDir = fileSelected;
            chosenSentences = GetSentences(sessionDir);
            numberOfRecordings = GetRecordingsCount(sessionDir);
            UpdateDialogUI();
        }

        @Override
        public void onFilesUnselected() {
            UpdateDialogUI();
        }

        @Override
        public void onDirChange(File dirSelected) {
            UpdateDialogUI();
        }
    };

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

    public interface OnFileSelectedListener {
        void onNewSession(Session session);
        void onDismissed();
    }


    /**
     * Instantiating a new NewSessionDialog() will automatically show it on screen()
     * @param context
     */
    public OpenSessionDialog(Context context, File lastGoodSessionDir){
        this.context = context;
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Open Session");
        String header = "Explore - Choose a Recording Session";


        LayoutInflater inflater = ((AppCompatActivity)context).getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.open_session_dialog, null);
        builder.setView(dialogView);


        sessionNameLabel = (TextView) dialogView.findViewById(R.id.open_session_name);
        sessionNameLabel.setHint("Chosen Session");

        sessionInfoTxt = (TextView) dialogView.findViewById(R.id.open_session_info);
        sessionInfoTxt.setText("0 Sentences, 0 Recordings, 00-00-00 00:00");

        sentencesList  = (TextView) dialogView.findViewById(R.id.open_sentence_list);
        sentencesList.setText("... ... ...\n... ... ...\n... ... ...");

        explorerLabel = (TextView) dialogView.findViewById(R.id.file_explorer_label);


        TextView openListLabel = (TextView) dialogView.findViewById(R.id.open_list_label);
        openListLabel.setText(header);

        builder.setCancelable(false);
        builder.setPositiveButton("Open Session",null);
        builder.setNegativeButton("Cancel", null);

        dialog = builder.show();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                OnDismissed(dialog);
            }
        });

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(this);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(this);


        StartFileBrowser(lastGoodSessionDir);
        UpdateDialogUI();
    }

    private void StartFileBrowser(File lastGoodDir) {
        sessionBrowser = new SessionBrowser(context, (ListView) dialog.findViewById(R.id.listView2), Environment.getExternalStorageDirectory());
        sessionBrowser.setOnFileSelected(fileSelectedListener);
        if(lastGoodDir != null)
            sessionBrowser.GoToDir(lastGoodDir.getParentFile(), true);
        else
            sessionBrowser.GoToRoot(true);
    }

    private void OkButtonClicked() {
        //String[] mTestArray = getResources().getStringArray(R.array.testArray);
        try {

            Session session = new Session(context,sessionDir);

            if(onFileSelectedListener!=null)
                onFileSelectedListener.onNewSession(session);

            UpdateDialogUI();
            dialog.dismiss();

        } catch (IOException e) {
            e.printStackTrace();
        //erro un able to open session
        }

    }

    @Override
    public void onClick(View v) {
        if(v == dialog.getButton(AlertDialog.BUTTON_POSITIVE)){
            OkButtonClicked();

        } else if(v == dialog.getButton(AlertDialog.BUTTON_NEGATIVE)){
            dialog.dismiss();
        }
    }



    private void UpdateDialogUI(){
        String selectedFileName = "*.txt";
        boolean okBtnEnabled = false;

        if(sessionDir != null) {
            chosenSessionName = sessionDir.getName();
            okBtnEnabled = true;
        }

        if(sessionDir != null) {
            explorerLabel.setText(sessionDir.getAbsolutePath());
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(okBtnEnabled);

        int numberOfSentences = chosenSentences==null?0:chosenSentences.length;
        sessionInfoTxt.setText(numberOfSentences+" Sentences, "+numberOfRecordings+" Recordings, "+GetDate(createdDate));
        sessionNameLabel.setText(chosenSessionName);
        String showOnly3Sentences = "... ... ...\n... ... ...\n... ... ...";
        if(chosenSentences!=null) {
            showOnly3Sentences="";
            for (int i = 0; i < (chosenSentences.length >= 3 ? 3 : chosenSentences.length); i++) {
                showOnly3Sentences += chosenSentences[i]+"\n";
            }
        }
        sentencesList.setText(showOnly3Sentences);


    }

    private String GetDate(int createdDate) {
        return "00-00-00 00:00";
    }

    private void OnDismissed(DialogInterface dialog) {
        if(onFileSelectedListener!=null)
            onFileSelectedListener.onDismissed();
        onFileSelectedListener =null;
    }

    public void setOnFileSelectedListener(OnFileSelectedListener onFileSelectedListener){
        this.onFileSelectedListener = onFileSelectedListener;
    }

}
