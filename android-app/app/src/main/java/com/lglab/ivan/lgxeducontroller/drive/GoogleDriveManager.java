package com.lglab.ivan.lgxeducontroller.drive;


import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;

public class GoogleDriveManager {

    public static final String TAG = "drive_log_in";

    public static final int RC_SIGN_IN = 0;
    public static final int RC_OPEN_ITEM = 1;

    public static GoogleSignInClient GoogleSignInClient;
    public static DriveClient DriveClient;
    public static DriveResourceClient DriveResourceClient;

}
