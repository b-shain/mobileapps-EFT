package edu.temple.encryptedfiletransfer;

import static edu.temple.encryptedfiletransfer.ServerUtils.getID;
import static edu.temple.encryptedfiletransfer.ServerUtils.sendReceivedNotification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
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

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

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
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);
    	Log.i("EFTIntentService", "got yo message @ " + SystemClock.elapsedRealtime());
    	Log.i("EFTIntentService", messageType + " @ " + SystemClock.elapsedRealtime());


        if (!extras.isEmpty()) {  // has effect of unparsing Bundle
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
                String msgName = "", fileUrl = "", msgType = "", fileName = "", msgSender = "", msgRecipient = "";
                try {
                	extras.getString("message");
                	
                	jObj = new JSONObject(extras.getString("message"));
                	System.out.println("Names: " + jObj.names());
                	msgName = jObj.getString("msg_title");
                	msgSender = jObj.getString("msg_sender");
                	msgRecipient = jObj.getString("msg_recipient");
                	msgType = jObj.getString("msg_type");
//                	System.out.println("Message Name: " + msgName);
//                	System.out.println("Message Sender: " + msgSender);
//                	System.out.println("Message Type: " + msgType);
//                	System.out.println("File Url: " + fileUrl);
                	if(msgType.equals("fileReceived"))
                	{              		
                        sendNotification(msgName);

                	}
                	else if(msgType.equals("fileDownload"))
                	{
                    	fileUrl = jObj.getString("file_url");
                    	int temp = fileUrl.split("/").length;
                    	fileName = fileUrl.split("/")[(temp - 1)];
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
           	      message = sendReceivedNotification(getApplicationContext(), msgRecipient, msgSender);
                  sendNotification(msgName);
                  Log.i(TAG, "Received: " + extras.toString());

                	}
				} catch (JSONException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

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