package com.litmantech.readrecorder;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.litmantech.readrecorder.audio.Playback;
import com.litmantech.readrecorder.audio.Recorder;
import com.litmantech.readrecorder.fileexplore.FileBrowser;
import com.litmantech.readrecorder.read.Session;
import com.litmantech.readrecorder.read.line.Entry;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private Recorder recorder;
    private Playback playback;
    private boolean testAudioStuffON = false;
    private Button prevBTN;
    private Button nextBTN;
    private Button recBTN;
    private Session session;
    private TextView currentSentenceTXT;
    private EditText sessionDirNameETXT;
    private Button openSessionBTN;
    private Button newSessionBTN;
    private TextView currentSessionLabelTXT;
    private FileBrowser fileBrowser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//dont let phone sleep

        if (ContextCompat.checkSelfPermission(this,  Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {


        }

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                55);

        currentSentenceTXT = (TextView) findViewById(R.id.current_sentence);
        currentSessionLabelTXT = (TextView) findViewById(R.id.session_dir_label);
        prevBTN = (Button) findViewById(R.id.prev_btn);
        recBTN  = (Button) findViewById(R.id.rec_btn);
        nextBTN = (Button) findViewById(R.id.next_btn);
        newSessionBTN = (Button) findViewById(R.id.new_session_btn);
        openSessionBTN = (Button) findViewById(R.id.open_session_btn);

        ListView listView = (ListView) findViewById(R.id.listview);




        prevBTN.setOnClickListener(this);
        recBTN.setOnClickListener(this);
        nextBTN.setOnClickListener(this);
        newSessionBTN.setOnClickListener(this);
        openSessionBTN.setOnClickListener(this);


        String[] mTestArray = getResources().getStringArray(R.array.testArray);
        String sessionDirName = Session.DEFAULT_DIR_NAME;
        session = new Session(this,sessionDirName,mTestArray);
        UpdateUI();
    }


    @Override
    protected void onResume(){
        super.onResume();
        if(testAudioStuffON)
            RunTest();

        if(recorder == null)
            recorder = new Recorder();

        recorder.Start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(testAudioStuffON){
            playback.Stop();
        }
        recorder.Stop();
    }

    private void RunTest() {

        if(playback == null)
            playback = new Playback();


        LinkedBlockingQueue audioListener = playback.Play();

        recorder.setAudioListener(audioListener);

    }


    @Override
    public void onClick(View v) {
        if(v == prevBTN){
            session.PreviousEntry();
        }else if(v == nextBTN){
            session.NextEntry();
        }else if(v == recBTN){
            if(session.isRecording())
                session.StopRecording();
            else
                session.StartRecording(recorder);
        }else if(v == newSessionBTN){
            ShowNewSessionDialog();

        }else if(v == openSessionBTN){

        }


        UpdateUI();
    }

    private void ShowNewSessionDialog() {
        final Context context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Session Setup");
        String title = "Explorer - Choose a .txt file";
        String sessionName = "Session1";

        //final EditText input = new EditText(this);
        //builder.setView(input);


        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.new_session_dialog, null);
        builder.setView(dialogView);

        final EditText sessionNameView = (EditText) dialogView.findViewById(R.id.session_name_edit);
        sessionNameView.setText(sessionName);
        sessionNameView.setSelection(sessionNameView.getText().toString().length());


        final EditText txtFile = (EditText) dialogView.findViewById(R.id.selected_file_edit);
        txtFile.setText("*.txt");//this does not mean anything. just to show the user we are looking for any txt file
        txtFile.setEnabled(false);//use cant edit this Yet
        txtFile.setSelection(txtFile.getText().toString().length());

        final TextView explorerLabel = (TextView) dialogView.findViewById(R.id.file_explorer_label);
        explorerLabel.setText("...");


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text = sessionNameView.getText().toString();
                String[] mTestArray = getResources().getStringArray(R.array.testArray);
                session = new Session(context,m_Text,mTestArray);
                UpdateUI();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();


        fileBrowser = new FileBrowser(this, (ListView) dialogView.findViewById(R.id.listView2), Environment.getExternalStorageDirectory());
        fileBrowser.setOnFileSelected(new FileBrowser.OnFileSelectedListener() {
            @Override
            public void onFileSelected(File fileSelected) {
                txtFile.setText(fileSelected.getName());
                txtFile.setSelection(txtFile.getText().toString().length());

            }

            @Override
            public void onFilesUnselected() {
                txtFile.setText("*.txt");
                txtFile.setSelection(txtFile.getText().toString().length());

            }

            @Override
            public void onDirSelected(File dirSelected) {
                explorerLabel.setText(dirSelected.getAbsolutePath());
            }
        });
        fileBrowser.GoToRoot();
    }

    /**
     * ONLY call this on the UI thread.!!!!
     */
    private void UpdateUI() {
        Entry currentEntry = session.getCurrentEntry();
        if(session.isRecording()){
            prevBTN.setEnabled(false);
            nextBTN.setEnabled(false);
        }else{
            prevBTN.setEnabled(true);
            nextBTN.setEnabled(true);
        }

        currentSentenceTXT.setText(currentEntry.getSentence());
        currentSessionLabelTXT.setText("Session Name:"+session.getName());

    }

    @Override
    public void onBackPressed() {
        fileBrowser.GoBack();
    }
}
