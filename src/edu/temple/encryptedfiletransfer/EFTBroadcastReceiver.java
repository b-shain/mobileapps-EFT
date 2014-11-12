package edu.temple.encryptedfiletransfer;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class EFTBroadcastReceiver extends WakefulBroadcastReceiver {

	    private static final String TAG = "broadcast receiver";
	    public static final int NOTIFICATION_ID = 1;
	    private NotificationManager mNotificationManager;
	    NotificationCompat.Builder builder;
	    JSONObject jObj;

		public void onReceive(Context context, Intent intent) {
	        // Explicitly specify that EFTIntentService will handle the intent.
	        //ComponentName comp = new ComponentName(context.getPackageName(), EFTIntentService.class.getName());
	    	Intent service = new Intent(context, EFTIntentService.class);

	        // Start the service, keeping the device awake while it is launching.
	        Log.i("EFTBroadcastReceiver", "Starting broadtcast reciever service @ " + SystemClock.elapsedRealtime());
	        //startWakefulService(context, (intent.setComponent(comp)));
	        startWakefulService(context, service);
	        setResultCode(Activity.RESULT_OK);
	        
	        
	        
	        Bundle extras = intent.getExtras();
	        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
	        // The getMessageType() intent parameter must be the intent you received
	        // in your BroadcastReceiver.
	        String messageType = gcm.getMessageType(intent);

	        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
	            /*
	             * Filter messages based on message type. Since it is likely that GCM
	             * will be extended in the future with new message types, just ignore
	             * any message types you're not interested in, or that you don't
	             * recognize.
	             */
	            if (GoogleCloudMessaging.
	                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
	                sendNotification("Send error: " + extras.toString(), context);
	            } else if (GoogleCloudMessaging.
	                    MESSAGE_TYPE_DELETED.equals(messageType)) {
	                sendNotification("Deleted messages on server: " +
	                        extras.toString(), context);
	            // If it's a regular GCM message, do some work.
	            } else if (GoogleCloudMessaging.
	                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
	                //Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
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
	                sendNotification("Received: " + msgName, context);
	                Log.i(TAG, "Received: " + extras.toString());
	            }
	        }
	        // Release the wake lock provided by the WakefulBroadcastReceiver.
	        WakefulBroadcastReceiver.completeWakefulIntent(intent);
	    }

	    // Put the message into a notification and post it.
	    // This is just one simple example of what you might choose to do with
	    // a GCM message.
	    private void sendNotification(String msg, Context context) {
	        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

	        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, DeviceRegistrationActivity.class), 0);

	        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
	        .setSmallIcon(R.drawable.ic_stat_gcm)
	        .setContentTitle("EFT Notification")
	        .setStyle(new NotificationCompat.BigTextStyle()
	        .bigText(msg))
	        .setContentText(msg);

	        mBuilder.setContentIntent(contentIntent);
	        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	    }

	        
	        

}
