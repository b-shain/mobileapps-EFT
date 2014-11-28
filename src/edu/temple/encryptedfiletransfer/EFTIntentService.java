package edu.temple.encryptedfiletransfer;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
//package edu.temple.encryptedfiletransfr;
//
//import android.app.Activity;
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//
//import com.google.android.gms.gcm.GoogleCloudMessaging;
//
//import static edu.temple.encryptedfiletransfer.Utilities.SENDER_ID;
//import static edu.temple.encryptedfiletransfer.Utilities.displayMessage;
// 
//public class IntentService extends Activity {
// 
//    private static final String TAG = "GCMIntentService";
// 
//    Intent intent = new Intent();
//    
//    private EFTBroadcastReceiver bReceiver = new EFTBroadcastReceiver() {
//    	@Override
//    	public void onReceive(Context context, Intent intent) {
//    	loadStream();
//    	}
//    	};
//    
//    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
//    String messageType = gcm.getMessageType(intent);
//
//    // Filter messages based on message type. It is likely that GCM will be extended in the future
//    // with new message types, so just ignore message types you're not interested in, or that you
//    // don't recognize.
//    if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
//       // It's an error.
//    } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
//       // Deleted messages on the server.
//    } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
//       // It's a regular GCM message, do some work.
//    }
//
//    
//    /**
//     * Method called on device registered
//     **/
//    protected void onRegistered(Context context, String registrationId) {
//        Log.i(TAG, "Device registered: regId = " + registrationId);
//        displayMessage(context, "Your device registred with GCM");
//        Log.d("NAME", MainActivity.name);
//        ServerUtils.register(context, MainActivity.name, MainActivity.email, registrationId);
//    }
// 
//    /**
//     * Method called on device un registred
//     * */
//    protected void onUnregistered(Context context, String registrationId) {
//        Log.i(TAG, "Device unregistered");
//        displayMessage(context, getString(R.string.gcm_unregistered));
//        ServerUtils.unregister(context, registrationId);
//    }
// 
//    /**
//     * Method called on Receiving a new message
//     * */
//    protected void onMessage(Context context, Intent intent) {
//        Log.i(TAG, "Received message");
//        String message = intent.getExtras().getString("price");
//         
//        displayMessage(context, message);
//        // notifies user
//        generateNotification(context, message);
//    }
// 
//    /**
//     * Method called on receiving a deleted message
//     * */
//    protected void onDeletedMessages(Context context, int total) {
//        Log.i(TAG, "Received deleted messages notification");
//        String message = getString(R.string.gcm_deleted, total);
//        displayMessage(context, message);
//        // notifies user
//        generateNotification(context, message);
//    }
// 
//    /**
//     * Method called on Error
//     * */
//    public void onError(Context context, String errorId) {
//        Log.i(TAG, "Received error: " + errorId);
//        displayMessage(context, getString(R.string.gcm_error, errorId));
//    }
// 
//    protected boolean onRecoverableError(Context context, String errorId) {
//        // log message
//        Log.i(TAG, "Received recoverable error: " + errorId);
//        displayMessage(context, getString(R.string.gcm_recoverable_error,
//                errorId));
//        return super.onRecoverableError(context, errorId);
//    }
// 
//    /**
//     * Issues a notification to inform the user that server has sent a message.
//     */
//    private static void generateNotification(Context context, String message) {
//        int icon = R.drawable.ic_launcher;
//        long when = System.currentTimeMillis();
//        NotificationManager notificationManager = (NotificationManager)
//                context.getSystemService(Context.NOTIFICATION_SERVICE);
//        Notification notification = new Notification(icon, message, when);
//         
//        String title = context.getString(R.string.app_name);
//         
//        Intent notificationIntent = new Intent(context, MainActivity.class);
//        // set intent so it does not start a new activity
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
//                Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent intent =
//                PendingIntent.getActivity(context, 0, notificationIntent, 0);
//        notification.setLatestEventInfo(context, title, message, intent);
//        notification.flags |= Notification.FLAG_AUTO_CANCEL;
//         
//        // Play default notification sound
//        notification.defaults |= Notification.DEFAULT_SOUND;
//         
//        // Vibrate if vibrate is enabled
//        notification.defaults |= Notification.DEFAULT_VIBRATE;
//        notificationManager.notify(0, notification);     
// 
//    }
// 
//}
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class EFTIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    JSONObject jObj;
    
    static final String TAG = "EFT INTENT";

    
    public EFTIntentService() {
        super("EFTIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    	android.os.Debug.waitForDebugger();
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);
    	Log.i("EFTIntentService", "got yo message @ " + SystemClock.elapsedRealtime());
    	Log.i("EFTIntentService", messageType + " @ " + SystemClock.elapsedRealtime());


        if (!extras.isEmpty()) {  // has effect of unparsing Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                
                //sendNotification("Received: " + extras.toString());
            	
             // Post notification of received message.
                String msgName = "";
                try {
                	extras.getString("message");
                	
                	jObj = new JSONObject(extras.getString("message"));
                	jObj.names();
                	msgName = jObj.getString("msg_title");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                sendNotification("Received: " + msgName);
                Log.i(TAG, "Received: " + extras.toString());

            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        EFTBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
      //Main Activity is the activity we would like to open
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
	        .setSmallIcon(R.drawable.ic_stat_gcm)
	        .setContentTitle("EFT Notification")
	        .setStyle(new NotificationCompat.BigTextStyle()
	        .bigText(msg))
	        .setContentText(msg)
	        .setDefaults(Notification.DEFAULT_SOUND);
	        mBuilder.setContentIntent(contentIntent);
	        
	        //In order to display more messages have different NOTIFICATION_IDS
	        int ID =  (int) SystemClock.elapsedRealtime();
	        //mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	        mNotificationManager.notify(ID, mBuilder.build());
    }
    
 // Put the message into a notification and post it.
 		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
 	    private void sendNotification(String msg, Context context) {
 	        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

 	        //Main Activity is the activity we would like to open
 	        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);

 	        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
 	        .setSmallIcon(R.drawable.ic_stat_gcm)
 	        .setContentTitle("EFT Notification")
 	        .setStyle(new NotificationCompat.BigTextStyle()
 	        .bigText(msg))
 	        .setContentText(msg)
 	        .setDefaults(Notification.DEFAULT_SOUND);
 	        mBuilder.setContentIntent(contentIntent);
 	        
 	        //In order to display more messages have different NOTIFICATION_IDS
 	        int ID =  (int) SystemClock.elapsedRealtime();
 	        //mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
 	        mNotificationManager.notify(ID, mBuilder.build());
 	    }
    
}