package com.litmantech.readrecorder.fileexplore;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.litmantech.readrecorder.read.Session;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Jeff_Dev_PC on 6/8/2016.
 */
public class SessionBrowser {
    private final String GO_BACK_KEY = "... ... ..";

    public interface OnFileSelectedListener {
        /**
         * Called when a view has been clicked.
         *
         * @param fileSelected The file the user has selected
         */
        void onFileSelected(File fileSelected);
        void onFilesUnselected();
        void onDirChange(File dirSelected);
    }


    private final Context context;
    private final ArrayList<Session> values;
    private String currentPath = "";
    private String title;
    private final SessionListAdapter adapter;
    private final ListView listView;
    private final File rootDir;
    private OnFileSelectedListener onFileSelected;

    public SessionBrowser(final Context context, final ListView listView, File rootDir) {
        this.context = context;
        this.listView = listView;
        this.rootDir = rootDir;
        // Use the current directory as title
        if(currentPath.isEmpty()){
            currentPath = "/";

        }
        setTitle(currentPath);

       values = new ArrayList<Session>();

        // Read all files sorted into the values-array
        //values = new ArrayList();


        // Put the data into the list
//        adapter = new ArrayAdapter(context,
//                android.R.layout.simple_list_item_2, android.R.id.text1, values);

        adapter =  new SessionListAdapter(context, values);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BrowserFile file = (BrowserFile) parent.getItemAtPosition(position);
                String fileName = file.fileName;
                File currentFile;
                if (currentPath.endsWith(File.separator)) {
                    currentFile = new File(currentPath + fileName);
                } else {
                    currentFile = new File(currentPath + File.separator + fileName);
                }

                if (currentFile.isDirectory()) {



                    UpdateAdapter(currentFile.getAbsolutePath());
                    if(onFileSelected != null){
                        onFileSelected.onFileSelected(currentFile);
                        adapter.setSelection(position);
                    }
                }
            }
        });
    }

    public void GoToRoot(boolean selectFirstItem){
        GoToDir(rootDir, selectFirstItem);
    }

    public void GoToDir(File goToDir, boolean selectFirstItem){
        UpdateAdapter(goToDir.getAbsolutePath());
        if(selectFirstItem && values.size() > 0){
            File firstDir = new File(currentPath + File.separator + values.get(0).getName());
            UpdateAdapter(firstDir.getAbsolutePath());
            if(onFileSelected != null){
                onFileSelected.onFileSelected(firstDir);
                adapter.setSelection(0);
            }
        }
    }

    private void UpdateAdapter(String filename) {


        currentPath = rootDir.getAbsolutePath();
        File dir = new File(filename);

        if(onFileSelected != null){
            onFileSelected.onDirChange(dir);
            onFileSelected.onFilesUnselected();
        }

        if (!dir.canRead()) {
            setTitle(getTitle() + " (inaccessible)");
        }

        if(dir.getAbsolutePath().contentEquals(rootDir.getAbsolutePath())) {
            values.clear();

            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.getName().startsWith(".")) {
                        if (file.isDirectory()) {
                            if (isSessionFolder(file)) {


                                try {
                                    Session session = new Session(context, file);
                                    values.add(session);// add if directory
                                }catch (IOException e){
                                    //TODO
                                }
                            }
                        }
                    }
                }
            }
            Collections.sort(values,new Comparator<Session>(){
                public int compare(Session file1, Session file2) {
                    return file1.getSessionDir().getAbsolutePath().compareToIgnoreCase(file2.getSessionDir().getAbsolutePath());
                }
            });
        }

        adapter.notifyDataSetChanged();
    }

    private boolean isSessionFolder(File file) {
        if(file == null) return false;

        String[] fileList = file.list();
        if(fileList == null) return false;


        for(String fileName: fileList){
           boolean hasHiddenFile =  fileName.trim().toLowerCase().contains(Session.HIDDEN_FILE_NAME.trim().toLowerCase());
            if(hasHiddenFile) return true;
        }

        return false;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setOnFileSelected(OnFileSelectedListener onFileSelected) {
        this.onFileSelected = onFileSelected;
    }
}
