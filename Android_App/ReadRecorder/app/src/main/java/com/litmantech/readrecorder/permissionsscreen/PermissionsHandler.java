package com.litmantech.readrecorder.permissionsscreen;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.litmantech.readrecorder.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jeff_Dev_PC on 6/26/2016.
 */
public class PermissionsHandler {

    private static final String TAG = "PermissionsHandler";
    private static final int PERMISSIONS_CODE = 55;


    private int recAudioPermission;
    private int exReadStoragePermission;
    private int exWriteStoragePermission;
    private Context context;
    private OnPermissionsListener permissionsListener;


    public void RequestAndroidPermissions(Context context, @NonNull OnPermissionsListener permissionsListener) {
        this.context = context;
        this.permissionsListener = permissionsListener;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            recAudioPermission = ContextCompat.checkSelfPermission(context,  Manifest.permission.RECORD_AUDIO);
            exReadStoragePermission = ContextCompat.checkSelfPermission(context,  Manifest.permission.READ_EXTERNAL_STORAGE);
            exWriteStoragePermission = ContextCompat.checkSelfPermission(context,  Manifest.permission.WRITE_EXTERNAL_STORAGE);

        }else{
            recAudioPermission = PackageManager.PERMISSION_GRANTED;
            exReadStoragePermission = PackageManager.PERMISSION_GRANTED;
            exWriteStoragePermission = PackageManager.PERMISSION_GRANTED;
        }

        //String[] permissionList =
        ArrayList<String> permissionList = new ArrayList<>();

        if(recAudioPermission!= PackageManager.PERMISSION_GRANTED) permissionList.add(Manifest.permission.RECORD_AUDIO);
        if(exReadStoragePermission!= PackageManager.PERMISSION_GRANTED) permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        if(exWriteStoragePermission!= PackageManager.PERMISSION_GRANTED) permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(permissionList.size() == 0){
            ((MainActivity) context).onRequestPermissionsResult(PERMISSIONS_CODE,
                    new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    new int[]{recAudioPermission,exReadStoragePermission,exWriteStoragePermission});
        }else {
            ActivityCompat.requestPermissions((Activity) context,permissionList.toArray(new String[permissionList.size()]), PERMISSIONS_CODE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        HashMap<String,Integer> permissionsMap = new HashMap<>();

        if (requestCode == PERMISSIONS_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];
                permissionsMap.put(permission, grantResult);

            }
        }

        if(permissionsMap.containsKey(Manifest.permission.RECORD_AUDIO)) {
            recAudioPermission = permissionsMap.get(Manifest.permission.RECORD_AUDIO);
        }

        if(permissionsMap.containsKey(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            exReadStoragePermission = permissionsMap.get(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if(permissionsMap.containsKey(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            exWriteStoragePermission = permissionsMap.get(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }


        ((MainActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //if we made it here then we have all permissions
                if(HaveAllPermissions(new int[]{recAudioPermission,exReadStoragePermission,exWriteStoragePermission}))
                    permissionsListener.OnPermissionsSuccessful();
                else
                    permissionsListener.OnPermissionsFail();
            }
        });

    }


    private boolean HaveAllPermissions(@NonNull int... permissions){
        for(int permission: permissions){
            if(permission != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }


    /**
     * Interface definition for a callback to be invoked when a permission is granted.
     */
    public interface OnPermissionsListener {
        /**
         * Called permission has not been granted.
         *
         */
        void OnPermissionsFail();

        /**
         * Called when a view has been granted.
         *
         */
        void OnPermissionsSuccessful();
    }
}
