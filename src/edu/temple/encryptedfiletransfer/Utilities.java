package edu.temple.encryptedfiletransfer;

import android.content.Context;
import android.content.Intent;
 
public final class Utilities {
     
    //Server registration URL
    static final String SERVER_URL = "http://cis-linux2.temple.edu/~tud30441/";
 
    // Google project id
    static final String SENDER_ID = "22453893049";
 
    /**
     * Tag used on log messages.
     */
    static final String TAG = "EFT GCM";
 
    static final String DISPLAY_MESSAGE_ACTION = "edu.temple.encryptedfiletransfer.DISPLAY_MESSAGE";
 
    static final String EXTRA_MESSAGE = "message";
 
    /**
     * Notifies UI to display a message.
     * <p>
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
    static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }
}