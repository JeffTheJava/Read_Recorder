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
import com.litmantech.readrecorder.utilities.UiUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by Jeff_Dev_PC on 6/3/2016.
 */
public class NewSessionDialog implements View.OnClickListener {
    private static final String TAG = "NewSessionDialog";

    private final Context context;
    private final AlertDialog dialog;

    private FileBrowser fileBrowser;
    private File textDoc;
    private File currentDir;

    private OnFileSelectedListener onFileSelectedListener;
    private final EditText sessionNameView;
    private final EditText txtFile;
    private final TextView explorerLabel;

    private final FileBrowser.OnFileSelectedListener fileSelectedListener = new FileBrowser.OnFileSelectedListener() {
        @Override
        public void onFileSelected(File fileSelected) {
            textDoc = fileSelected;
            UpdateDialogUI();
        }

        @Override
        public void onFilesUnselected() {
            textDoc = null;
            UpdateDialogUI();
        }

        @Override
        public void onDirChange(File dirSelected) {
            currentDir = dirSelected;
            UpdateDialogUI();
        }
    };

    public interface OnFileSelectedListener {
        void onNewSession(Session session);
        void onDismissed();
    }


    /**
     * Instantiating a new NewSessionDialog() will automatically show it on screen()
     * @param context
     */
    public NewSessionDialog(Context context, File lastGoodSessionDir){
        this.context = context;
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("New Session Setup");
        String header = "Explore - Choose a .txt file";


        LayoutInflater inflater = ((AppCompatActivity)context).getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.new_session_dialog, null);
        builder.setView(dialogView);

        sessionNameView = (EditText) dialogView.findViewById(R.id.session_name_edit);
        sessionNameView.setText(lastGoodSessionDir==null?Session.DEFAULT_DIR_NAME:lastGoodSessionDir.getName());
        sessionNameView.setSelection(sessionNameView.getText().toString().length());


        txtFile = (EditText) dialogView.findViewById(R.id.selected_file_edit);
        txtFile.setEnabled(false);//use cant edit this text box Yet
        txtFile.setSelection(txtFile.getText().toString().length());

        explorerLabel = (TextView) dialogView.findViewById(R.id.file_explorer_label);
        explorerLabel.setText("...");

        TextView explorerHeader = (TextView) dialogView.findViewById(R.id.file_explorer_label);
        explorerHeader.setText(header);

        builder.setCancelable(false);
        builder.setPositiveButton("Create Session",null);
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
    }

    private void StartFileBrowser(File lastGoodDir) {
        fileBrowser = new FileBrowser(context, (ListView) dialog.findViewById(R.id.listView2), Environment.getExternalStorageDirectory());
        fileBrowser.setOnFileSelected(fileSelectedListener);
        if(lastGoodDir != null)
            fileBrowser.GoToDir(lastGoodDir.getParentFile());
        else
            fileBrowser.GoToRoot();
    }

    private void OkButtonClicked(String sessionDirName) {
        //String[] mTestArray = getResources().getStringArray(R.array.testArray);
        try {
            if(sessionDirName.length() <=0){
                Toast.makeText(context, "ERROR:: File Name must be at 1 charter or larger", Toast.LENGTH_LONG).show();
                Log.e(TAG,"ERROR:: File Name must be at 1 charter or larger");
                return;//jump out. the file name is two short
            }

            String[] newLineTextDoc = UiUtil.OpenTxtDocLineByLine(textDoc);
            Session session = new Session(context, sessionDirName, newLineTextDoc);

            if(onFileSelectedListener!=null)
                onFileSelectedListener.onNewSession(session);

            UpdateDialogUI();
            dialog.dismiss();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SessionExistsException e) {
            File existingSession = e.getSession();
            Toast.makeText(context, "Session named:\n"+existingSession.getName() +"\nAlready exist, please choose another name or select open", Toast.LENGTH_LONG).show();
            Log.e(TAG,e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        if(v == dialog.getButton(AlertDialog.BUTTON_POSITIVE)){
            String sessionDirName = sessionNameView.getText().toString();
            OkButtonClicked(sessionDirName);

        } else if(v == dialog.getButton(AlertDialog.BUTTON_NEGATIVE)){
            dialog.dismiss();
        }
    }



    private void UpdateDialogUI(){
        String selectedFileName = "*.txt";
        boolean okBtnEnabled = false;

        if(textDoc != null) {
            selectedFileName = textDoc.getName();
            okBtnEnabled = true;
        }

        txtFile.setText(selectedFileName);//this does not mean anything. just to show the user we are looking for any txt file
        txtFile.setSelection(txtFile.getText().toString().length());

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(okBtnEnabled);

        if(currentDir != null) {
            explorerLabel.setText(currentDir.getAbsolutePath());
        }

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
