package edu.temple.encryptedfiletransfer;

import static edu.temple.encryptedfiletransfer.ServerUtils.register;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class DeviceRegistrationActivity extends Activity { 
	
	String registrationID;
    
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    @SuppressWarnings("unused")
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "Your-Sender-ID";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "DeviceRegistration";

    // Asyntask
    AsyncTask<Void, Void, Void> mRegisterTask;

    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;
    //EFTIntentService service = new EFTIntentService();
    String regid;
    

 
    @SuppressLint("HandlerLeak")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //startService(service);
        
     // Check device for Play Services APK.
//        if (checkPlayServices()) {
//
//        }
        
        
        final Handler webHandle = new Handler(){
			@Override
			public void handleMessage(Message msg) {		 
				//display.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
				
				//to enable javascript
				//display.getSettings().setJavaScriptEnabled(true);
				
				//to display the HTML code in the webView
				//display.loadData(registrationID, "text/html", "UTF-8");
				registrationID = (String) msg.obj;
				storeRegistrationId(getApplicationContext(), registrationID);
				Toast.makeText(getApplicationContext(), "Device is now registered with GCM: " + registrationID, Toast.LENGTH_LONG).show();	            

		    }				
		};
        
                mRegisterTask = new AsyncTask<Void, Void, Void>() {
 
                    @Override
                    protected Void doInBackground(Void... params) {
                        // Register on our server
                        // On server creates a new user
                        //ServerUtilities.register(context, name, email, regId);
                    	
                    	String SENDER_ID = "22453893049";
        		        // Get GCM registration id
                     final Context context = getApplicationContext();
        			 GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        			 
        			 
        			 
        			 String registrationID = "";
        			try {
        				String storedID = getRegistrationId(getApplicationContext());
        				if(storedID == "")
        				{
        				registrationID = gcm.register(SENDER_ID);
        				storeRegistrationId(getApplicationContext(), registrationID);
        		        register(getApplicationContext(), "usrname","pass","emAddress",registrationID);
        				}
        				else{      					
        					registrationID = getRegistrationId(getApplicationContext());
        				}
        			} catch (IOException e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}
                    	
                    	Message msg = webHandle.obtainMessage();
						msg.obj = registrationID;
						webHandle.sendMessage(msg);
                        return null;
                    }
 
                    @Override
                    protected void onPostExecute(Void result) {
                        mRegisterTask = null;
                    }
                    
 
                };

                mRegisterTask.execute(null, null, null);
            }
       
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
//    private boolean checkPlayServices() {
//        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
//        if (resultCode != ConnectionResult.SUCCESS) {
//            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
//                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
//                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
//            } else {
//                Log.i(TAG, "This device is not supported.");
//                finish();
//            }
//            return false;
//        }
//        return true;
//    }
//    
//    }
    
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
    
    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(DeviceRegistrationActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }    

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

}      
 

