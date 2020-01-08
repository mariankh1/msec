package com.app.malloc.malloc.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by niraj.sanghani on 10/26/2017.
 */

/**
 * Check for permissions
 *
 * {@link android.Manifest.permission}
 */
public class Permissions {

    public static void getPermissions(Context context, List<String> permissionList, int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permit(context, permissionList, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }
    }

    public static void getPermissions(Context context, String[] permissionArray, int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permit(context, Arrays.asList(permissionArray), REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static void permit(final Context context, List<String> permissionList, final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
        final List<String> permissionsNeeded = new ArrayList<String>();

        for (String s : permissionList) {
            if (!checkPermission(context, s)) {
                permissionsNeeded.add(s);
            }
        }

        if (permissionsNeeded.size() > 0) {
            // Need Rationale
            String message = "You need to grant access ...";
            showMessageOKCancel(context, message,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                            int PERMISSION_ALL = 1;
                            String[] PERMISSIONS = {
                                    android.Manifest.permission.READ_CONTACTS,
                                    android.Manifest.permission.WRITE_CONTACTS,
                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    android.Manifest.permission.READ_SMS,
                                    android.Manifest.permission.CAMERA
                            };


                                ActivityCompat.requestPermissions((Activity) context, PERMISSIONS, PERMISSION_ALL);



                        }
                    });
            return;
        }

        //return;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private static boolean checkPermission(Context context, /*List<String> permissionsNeeded,*/ String permission) {
        if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            //permissionsNeeded.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission))
                return false;
        }
        return true;
    }

    private static void showMessageOKCancel(Context context, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

}