package edu.temple.encryptedfiletransfer;

import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class EFTBroadcastReceiver extends WakefulBroadcastReceiver {

	    NotificationCompat.Builder builder;
	    JSONObject jObj;

		public void onReceive(Context context, Intent intent) {
	        // Explicitly specify that EFTIntentService will handle the intent.
	        ComponentName comp = new ComponentName(context.getPackageName(), EFTIntentService.class.getName());

	        // Start the service, keeping the device awake while it is launching.
	        Log.i("EFTBroadcastReceiver", "Starting broadtcast reciever service @ " + SystemClock.elapsedRealtime());
	        startWakefulService(context, (intent.setComponent(comp)));
	        setResultCode(Activity.RESULT_OK);

}

}
