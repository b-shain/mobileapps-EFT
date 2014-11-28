package edu.temple.encryptedfiletransfer;

import android.app.Activity;
	 
	import static edu.temple.encryptedfiletransfer.Utilities.SERVER_URL;
import static edu.temple.encryptedfiletransfer.Utilities.TAG;
import static edu.temple.encryptedfiletransfer.Utilities.displayMessage;
	 


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
	 


	import android.content.Context;
import android.util.Log;
	 
	 
	public final class ServerUtils{
	    private static final int MAX_ATTEMPTS = 5;
	    private static final int BACKOFF_MILLI_SECONDS = 2000;
	    private static final Random random = new Random();
	 
	    /**
	     * Register this account/device pair within the server.
	     */
	    static void register(final Context context, String username, String password, String email, final String GCM_Reg_ID) {
	        Log.i(TAG, "registering device (GCM_Reg_ID = " + GCM_Reg_ID + ")");
	        String serverUrl = SERVER_URL + "registration.php";
	        Map<String, String> params = new HashMap<String, String>();     
	        params.put("UserName", username);
	        params.put("Password", password);
	        params.put("EmailAddress", email);
	        params.put("GCM_Reg_ID", GCM_Reg_ID);
	         
	        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
	        // Once GCM returns a registration id, we need to register on our server
	        // Since server might be down, we will retry it a few times.
	        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
	            Log.d(TAG, "Attempt #" + i + " to register");
	            try {
	                //displayMessage(context, context.getString(R.string.server_registering, i, MAX_ATTEMPTS));
	            	//TODO create non-hardcoded string
	            	displayMessage(context, "server is registering");
	            	post(serverUrl, params);
	            	
	            	//These lines are for registering the device with GCM, which is handled elsewhere now
	                //GCMRegistrar.setRegisteredOnServer(context, true);
	            	
	            	//TODO create non-hardcoded string
	                //String message = context.getString(R.string.server_registered);
	            	
		            String message = "This device has been registered.";
	                Utilities.displayMessage(context, message);
	                return;
	            } catch (IOException e) {
	                // Here we are simplifying and retrying on any error
	            	
	                Log.e(TAG, "Failed to register on attempt " + i + ":" + e);
	                if (i == MAX_ATTEMPTS) {
	                    break;
	                }
	                try {
	                    Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
	                    Thread.sleep(backoff);
	                } catch (InterruptedException e1) {
	                    // Activity finished before we complete - exit.
	                    Log.d(TAG, "Thread interrupted: abort remaining retries!");
	                    Thread.currentThread().interrupt();
	                    return;
	                }
	                // increase backoff exponentially
	                backoff *= 2;
	            }
	        }
	        //String message = context.getString(R.string.server_register_error,MAX_ATTEMPTS);
	        //TODO create non-hardcoded string
	        String message = "server_register_error";
	        Utilities.displayMessage(context, message);
	    }
	 
	    /**
	     * Unregister this account/device pair within the server.
	     */
	    static void unregister(final Context context, final String GCM_Reg_ID) {
	        Log.i(TAG, "unregistering device (GCM_Reg_ID = " + GCM_Reg_ID + ")");
	        String serverUrl = SERVER_URL + "/unregister";
	        Map<String, String> params = new HashMap<String, String>();
	        params.put("GCM_Reg_ID", GCM_Reg_ID);
	        try {
	            post(serverUrl, params);
	            
	            //This line is for un-registering the device with GCM, which is not handled currently, but should be handled elsewhere
	            //GCMRegistrar.setRegisteredOnServer(context, false);
	            
	            //String message = context.getString(R.string.server_unregistered);
		        //TODO create non-hardcoded string
	            String message = "This device has been unregistered.";
	            Utilities.displayMessage(context, message);
	        } catch (IOException e) {
	            // At this point the device is unregistered from GCM, but still
	            // registered in the server.
	            // We could try to unregister again, but it is not necessary:
	            // if the server tries to send a message to the device, it will get
	            // a "NotRegistered" error message and should unregister the device.
	            //String message = context.getString(R.string.server_unregister_error,e.getMessage());
		        
	        	//TODO create non-hardcoded string
	        	String message = "server_unregister_error";
	            Utilities.displayMessage(context, message);
	        }
	    }
	 
	    /**
	     * Register this account/device pair within the server.
	     */
	    static String logIn(final Context context, String username, String password) {
	        String serverUrl = SERVER_URL + "login.php";
	        Map<String, String> params = new HashMap<String, String>();     
	        params.put("UserName", username);
	        params.put("Password", password);
	         
	        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
	        // Once GCM returns a registration id, we need to register on our server
	        // Since server might be down, we will retry it a few times.
	        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
	            Log.d(TAG, "Attempt #" + i + " to log in.");
	            try {
	                //displayMessage(context, context.getString(R.string.server_registering, i, MAX_ATTEMPTS));
	            	//TODO create non-hardcoded string
	            	displayMessage(context, " device is logging in.");
	            	String response = post(serverUrl, params);
	            	
	            	//These lines are for registering the device with GCM, which is handled elsewhere now
	                //GCMRegistrar.setRegisteredOnServer(context, true);
	            	
	            	//TODO create non-hardcoded string
	                //String message = context.getString(R.string.server_registered);
	            	
		            String message = "This device has been logged in. " + response;
	                Utilities.displayMessage(context, message);
	                return response;
	            } catch (IOException e) {
	                // Here we are simplifying and retrying on any error	            	
	                Log.e(TAG, "Failed to log in on attempt " + i + ":" + e);
	                if (i == MAX_ATTEMPTS) {
	                    break;
	                }
	                try {
	                    Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
	                    Thread.sleep(backoff);
	                } catch (InterruptedException e1) {
	                    // Activity finished before we complete - exit.
	                    Log.d(TAG, "Thread interrupted: abort remaining retries!");
	                    Thread.currentThread().interrupt();
	                    return "This Was An Un-Successful Login Attempt.";
	                }
	                // increase backoff exponentially
	                backoff *= 2;
	            }
	        }
	        //String message = context.getString(R.string.server_register_error,MAX_ATTEMPTS);
	        //TODO create non-hardcoded string
	        String message = "server_register_error";
	        Utilities.displayMessage(context, message);
			return message;
	    }
	    
	    
	    /**
	     * Issue a POST request to the server.
	     *
	     * @param endpoint POST address.
	     * @param params request parameters.
	     *
	     * @throws IOException propagated from POST.
	     */
	    private static String post(String endpoint, Map<String, String> params) throws IOException {   	         
	        URL url;
	        String response = "";
		        try {
		            url = new URL(endpoint);
		        } catch (MalformedURLException e) {
		            throw new IllegalArgumentException("invalid url: " + endpoint);
		        }
	        StringBuilder bodyBuilder = new StringBuilder();
	        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
	        // constructs the POST body using the parameters
		        while (iterator.hasNext()) {
		            Entry<String, String> param = iterator.next();
		            bodyBuilder.append(param.getKey()).append('=')
		                    .append(param.getValue());
		            if (iterator.hasNext()) {
		                bodyBuilder.append('&');
		            }
		        }
	        String body = bodyBuilder.toString();
	        Log.v(TAG, "Posting '" + body + "' to " + url);
	        byte[] bytes = body.getBytes();
	        HttpURLConnection conn = null;
			        try {
			            Log.e("URL", "> " + url);
			            conn = (HttpURLConnection) url.openConnection();
			            conn.setDoOutput(true);
			            conn.setUseCaches(false);
			            conn.setFixedLengthStreamingMode(bytes.length);
			            conn.setRequestMethod("POST");
			            conn.setRequestProperty("Content-Type",
			                    "application/x-www-form-urlencoded;charset=UTF-8");
			            // post the request
			            OutputStream out = conn.getOutputStream();
			            out.write(bytes);
			            BufferedReader reader = new BufferedReader(new 
			            InputStreamReader(conn.getInputStream()));
			            StringBuilder sb = new StringBuilder();
			            String line = null;
			            // Read Server Response
			            while((line = reader.readLine()) != null)
			            {
			               sb.append(line);
			               break;
			            }
			            //this is the response from the server
			            response = sb.toString();
			            out.close();
			            // handle the response
			            int status = conn.getResponseCode();
			            if (status != 200) {
			              throw new IOException("Post failed with error code " + status);
			            }
			        } finally {
	            if (conn != null) {
	                conn.disconnect();
	            }
	        }
	        
	      return response;
	      }
	}