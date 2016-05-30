package com.litmantech.readrecorder.fileexplore;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jeff_Dev_PC on 5/29/2016.
 */
public class FileBrowser {
    private final String GO_BACK_KEY = "... ... ..";

    public interface OnFileSelectedListener {
        /**
         * Called when a view has been clicked.
         *
         * @param fileSelected The file the user has selected
         */
        void onFileSelected(File fileSelected);
        void onFilesUnselected(File fileSelected);
        void onDirSelected(File dirSelected);
    }


    private final Context context;
    private final ArrayList values;
    private String currentPath = "";
    private String title;
    private final ArrayAdapter adapter;
    private final ListView listView;
    private final File rootDir;
    private OnFileSelectedListener onFileSelected;

    public FileBrowser(final Context context, final ListView listView, File rootDir) {
        this.context = context;
        this.listView = listView;
        this.rootDir = rootDir;
        // Use the current directory as title
        if(currentPath.isEmpty()){
            currentPath = "/";

        }
        setTitle(currentPath);

        // Read all files sorted into the values-array
        values = new ArrayList();


        // Put the data into the list
         adapter = new ArrayAdapter(context,
                android.R.layout.simple_list_item_2, android.R.id.text1, values);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String filename = (String) parent.getItemAtPosition(position);
                File currentFile;
                if (currentPath.endsWith(File.separator)) {
                    currentFile = new File(currentPath + filename);
                } else {
                    currentFile = new File(currentPath + File.separator + filename);
                }

                if (currentFile.isDirectory()) {


                    UpdateAdapter(currentFile.getAbsolutePath());


                } else if(filename.toLowerCase().trim().contentEquals(GO_BACK_KEY.toLowerCase().trim())){
                    GoBack();
                } else{
                    Toast.makeText(context, filename + " is not a directory", Toast.LENGTH_LONG).show();
                    if(onFileSelected != null){
                        onFileSelected.onFileSelected(currentFile);
                    }

                }
            }
        });
    }

    public void GoToRoot(){
        UpdateAdapter(rootDir.getAbsolutePath());
    }

    private void UpdateAdapter(String filename) {
        values.clear();


        currentPath = filename;
        File dir = new File(filename);
        if( !dir.getAbsolutePath().contentEquals(rootDir.getAbsolutePath())){
            values.add(GO_BACK_KEY);
        }

        if(onFileSelected != null){
            onFileSelected.onDirSelected(dir);
        }

        if (!dir.canRead()) {
            setTitle(getTitle() + " (inaccessible)");
        }
        String[] list = dir.list();
        if (list != null) {
            for (String file : list) {
                if (!file.startsWith(".")) {
                    values.add(file);
                }
            }
        }
        Collections.sort(values);

        adapter.notifyDataSetChanged();
        listView.setSelectionFromTop(0,0);
    }


    public boolean GoBack() {

        File currentLocation = new File(currentPath);

        if(currentLocation.getAbsolutePath().contentEquals(rootDir.getAbsolutePath())){
            return false;//we are at root we did NOT go back
        }

        String backPath = currentLocation.getParentFile().getAbsolutePath();
        UpdateAdapter(backPath);
        return true;//not at the root so we DID go back
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
