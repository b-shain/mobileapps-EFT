package edu.temple.encryptedfiletransfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import static edu.temple.encryptedfiletransfer.ServerUtils.getID;
import static edu.temple.encryptedfiletransfer.Utilities.SERVER_URL;
import static edu.temple.encryptedfiletransfer.Utilities.TAG;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
    Cipher aesCipher;
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
                String msgName = "", fileUrl = "", msgType = "", fileName = "", msgSender = "";
                try {
                	extras.getString("message");
                	
                	jObj = new JSONObject(extras.getString("message"));
                	System.out.println("Names: " + jObj.names());
                	msgName = jObj.getString("msg_title");
                	msgSender = jObj.getString("msg_sender");
                	fileUrl = jObj.getString("file_url");
                	msgType = jObj.getString("msg_type");
                	System.out.println("Message Name: " + msgName);
                	System.out.println("Message Sender: " + msgSender);
                	System.out.println("Message Type: " + msgType);
                	System.out.println("File Url: " + fileUrl);
                	int temp = fileUrl.split("/").length;
                	fileName = fileUrl.split("/")[(temp - 1)];
                	if(msgType.equals("fileDownload"))
                	{
                 		final File storageDirectory = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name));

                		//start file download
                		Downloadfile(new URL(fileUrl), fileName);
                		
                		//decrypt file
                		aesCipher = Cipher.getInstance("AES");
                     String UUID = android.os.Build.SERIAL;
         	         String str1 = "0x" + UUID;
           		     System.out.println("String UUID: " + UUID);	
           	         System.out.println("Int UUID: " + Long.decode(str1));
           	         JSONArray jArray;
           	         String message = getID(getApplicationContext(), msgSender);
           	         String selectedUserID = "";
           	      
           	   jObj = new JSONObject(message);
				Log.d(TAG, "Names 1: " + jObj.names().toString());
			jArray =  jObj.getJSONArray("IDs");
				Log.d(TAG, "IDs: " + jArray.toString());
				Log.d(TAG, "Names 2: " + jObj.names().toString());
		    for (int i = 0; i < jArray.length(); ++i) {
		    	String temporary = jArray.getString(i).toString();
		    	JSONObject id = new JSONObject(temporary);
		    	selectedUserID = id.getString("ID");
		    	Log.d(TAG, "ID: " + selectedUserID);
		    }
		    //selectedUserID.toString();
           	         
           	         //Retrieve other users serial
           	         String UUID_2 = selectedUserID;
           	         String str2 = "0x" + UUID_2;
           	         System.out.println("String UUID_2: " + UUID_2);
           	         System.out.println("Int UUID_2: " + Long.decode(str2));
     	         
                     Long.decode(str1);
           	         
           	         //Algorithm
           	         //long combo = (Long.parseLong(android.os.Build.SERIAL, 16) * Long.parseLong(UUID_2, 16));
           	         long combo = (Long.decode(str1) *  Long.decode(str2));
           	         long x = Long.parseLong("1000000000000");
           	         while (combo < 0)
           	         {
           	        	 combo = (Long.decode(str1) *  Long.decode(str2)) - x;
           	        	 x = x + Long.parseLong("1000000000000");
           	         }
           	         long combo2 = (Long.decode(str1) *  Long.decode(str2));
           	         //String combination = (UUID + UUID_2);
           		     System.out.println("Long Combination: " + combo);
           		     System.out.println("Combination 2: " + combo2);


           	         //combination.getBytes();
           	         //SecretKey key = new SecretKeySpec(combination.getBytes("UTF-8"), "AES");
           	         byte[] key = (Long.toString(combo)).getBytes("UTF-8");
           	         System.out.println("Combination to string: " + Long.toString(combo));
           	         String uuid = new String(key);
           		     System.out.println("UUID combination Array: " + uuid);
           	         MessageDigest sha = MessageDigest.getInstance("SHA-1");
           	         key = sha.digest(key);
           	         uuid = new String(key);
           		     System.out.println("Digested Key: " + uuid);
           	         key = Arrays.copyOf(key, 16); // use only first 128 bit
           	         uuid = new String(key);
           		     System.out.println("128-Bits of the Key Array: " + uuid);

           	         	         
           	         SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
           	         
           	     final String encFileName = fileName;
     	         File encFile = new File (storageDirectory, encFileName);
     	         
     	         final String decFileName = "(Decrypted)" + fileName.split("\\)")[1];
     	         File decrypFile = new File (storageDirectory, decFileName);
           	         
           	      decrypt(encFile, decrypFile, secretKeySpec);
           	      
                	}
				} catch (JSONException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException e) {
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
 	    
 	    
 	   private void Downloadfile(URL serverUrl, String fileName) throws MalformedURLException{
 		   final int BUFFER_SIZE = 4096;
 		final File storageDirectory = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name));

 		    //This is where the URI stuff belongs to access the server
 		  //serverUrl = new URL(SERVER_URL + "uploads/");
 		   
// 		   String ftpUrl = "ftp://%s:%s@%s/%s;type=i";
// 		   String host = "www.yourserver.com";
// 		   String user = "tom";
// 		   String pass = "secret";
 		  //String filePath = "/project/2012/Project.zip";
 		   //String savePath = "E:/Download/Project.zip";
 		  
 		   //ServerUrl = String.format(ftpUrl, user, pass, host, filePath);
 		  //System.out.println("URL: " + ftpUrl);
 		   
 		   try {	   
 		   URL url = serverUrl;
 		   URLConnection conn = url.openConnection();
 		   InputStream inputStream = conn.getInputStream();
 		  
 		  FileOutputStream outputStream = new FileOutputStream(new File(storageDirectory, fileName));
 		 
 		   byte[] buffer = new byte[BUFFER_SIZE];
 		   int bytesRead = -1;
 		   while ((bytesRead = inputStream.read(buffer)) != -1) {
 		   outputStream.write(buffer, 0, bytesRead);
 		   }
 		 
 		   outputStream.close();
 		   inputStream.close();
 		  
 		   System.out.println("File downloaded");
 		   } catch (IOException ex) {
 		   ex.printStackTrace();
 		   }
 		   }

 		  
 	  public void decrypt(File in, File out, SecretKeySpec aeskeySpec) throws IOException, InvalidKeyException {
 		    aesCipher.init(Cipher.DECRYPT_MODE, aeskeySpec);
 		   
 		    CipherInputStream is = new CipherInputStream(new FileInputStream(in), aesCipher);
 		    FileOutputStream os = new FileOutputStream(out);
 		   
 		   
 		    copy(is, os);
 		   
 		    is.close();
 		    os.close();
 		    
 		   File f;
	         FileInputStream fileIn = new FileInputStream(in);
	         BufferedReader reader = new BufferedReader(new InputStreamReader(fileIn));
	         String line = reader.readLine();
		     System.out.println("Deserialized encrypted file: " + line);
	         fileIn.close();
	         
	         fileIn = new FileInputStream(out);
	         reader = new BufferedReader(new InputStreamReader(fileIn));
	         line = reader.readLine();
		     System.out.println("Deserialized decrypted file: " + line);
	         fileIn.close();
		    
 		    
 		  }
 		 
 		  private void copy(InputStream is, OutputStream os) throws IOException {
 		    int i;
 		    byte[] b = new byte[1024];
 		    while((i=is.read(b))!=-1) {
 		      os.write(b, 0, i);
 		    }
 		  }

 	    
    
}