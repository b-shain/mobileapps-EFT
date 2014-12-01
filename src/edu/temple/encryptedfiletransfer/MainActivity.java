package edu.temple.encryptedfiletransfer;
import static edu.temple.encryptedfiletransfer.ServerUtils.logIn;
import static edu.temple.encryptedfiletransfer.ServerUtils.checkIfUserExists;
import static edu.temple.encryptedfiletransfer.ServerUtils.register;
import static edu.temple.encryptedfiletransfer.Utilities.TAG;

import java.io.IOException;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

//this will be the login activity. 
public class MainActivity extends Activity {
	
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
	
	PopupWindow popupWindow;
	final Context context = this;
	Button btnLogin;
	Button btnRegister;
	EditText txtUsername;
	EditText txtPassword;
	
	//EditText txtNewUserFName,txtNewUserLName,txtNewUserEmail,txtNewUserUsername,txtNewUserPassword;
	
    AsyncTask<Void, Void, Void> loginTask, registerTask;

    final Handler loginHandle = new Handler(){
		@Override
		public void handleMessage(Message msg) {		 
			String loginResponse = (String) msg.obj;
			if(loginResponse == "")
			{
				//they are not registered OR they entered incorrect username/password
				Toast.makeText(getApplicationContext(), "Please register if you have not already, or you entered an incorrect username/password combination.", Toast.LENGTH_LONG).show();	            
			}
			else
			{
			// Brett - transition user to home screen once user has been
			// logged in successfully
			Intent successfulLoginIntent = new Intent(MainActivity.this,
					HomeActivity.class);
			successfulLoginIntent.putExtra("username", txtUsername
					.getText().toString());
			successfulLoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			successfulLoginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(successfulLoginIntent);
			finish(); //so the user can't go back to the login
			Toast.makeText(getApplicationContext(), "The Login Response was : " + loginResponse + ". Which should be your UserID", Toast.LENGTH_LONG).show();	            
			}
		}					
	};	
	
	final Handler registerHandle = new Handler(){
		@Override
		public void handleMessage(Message msg) {		 
			String[] registerResponse = (String[]) msg.obj;
			if(registerResponse[0].toString().equals(""))
			{
				//That user name is not registered to a user
				//store user and register
			
//				fields[0] = message;
//            	fields[1] = txtNewUserFName.getText().toString();
//            	fields[2] = txtNewUserLName.getText().toString();
//            	fields[3] = txtNewUserEmail.getText().toString();
//            	fields[4] = txtNewUserUsername.getText().toString();
//            	fields[5] = txtNewUserPassword.getText().toString();
//				
				
			String feedback = registerResponse[1].toString() + " " + registerResponse[2].toString() +
					" , you have successfully registered!";
			
			Toast.makeText(getApplicationContext(), 
					feedback, Toast.LENGTH_LONG).show();
			
			//Brett - after successful registration
			popupWindow.dismiss();

			Intent registrationIntent = new Intent(MainActivity.this, HomeActivity.class);
			registrationIntent.putExtra("username", registerResponse[4].toString());
			registrationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			registrationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(registrationIntent);         
				
			}
			else if(registerResponse[0].toString().equals("1"))
			{
				Toast.makeText(getApplicationContext(), "The username you have selected is already registered to another user", Toast.LENGTH_LONG).show();	            	
			}
			else
			{
				//You broke it.
			}
			
		}					
	};	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//this gives an error
//	      if (checkPlayServices()) {
//	    	  String temp = "services found";
//	       }
		
		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnRegister = (Button) findViewById(R.id.btnRegister);
		txtUsername = (EditText) findViewById(R.id.edttxtUsername);
		txtPassword = (EditText) findViewById(R.id.edttxtPassword);

		// Brett - will handle logging a user into EFT
		btnLogin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				//brandon
		        loginTask = new AsyncTask<Void, Void, Void>() {
		            @Override
		            protected Void doInBackground(Void... params) {              
		            	Message msg = loginHandle.obtainMessage();
		            	String message = logIn(getApplicationContext(), txtUsername.getText().toString(), txtPassword.getText().toString());
		            	Log.e(TAG, message);
		            	msg.obj = message;                	
						loginHandle.sendMessage(msg);
		                return null;
		            }
		            @Override
		            protected void onPostExecute(Void result) {
		            	//brandon - Because AsyncTasks can only be used once
		                loginTask = null;
		            }
		        };
		        
				loginTask.execute(null, null, null);

				//Brandon -- I relocated your code brett to be executed within the Login Handler

			}
		});

		// Brett - will handle when new user wants to register
		btnRegister.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				// Brett- get activity_registration.xml view
				LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
				final View popupView = layoutInflater.inflate(R.layout.activity_registration, null);

				popupWindow = new PopupWindow(popupView,
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

				Button btnDismiss = (Button) popupView.findViewById(R.id.dismiss);
				btnDismiss.setOnClickListener(new Button.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						popupWindow.dismiss();
					}
				});

				popupWindow.showAsDropDown(btnRegister, -700, 300);
				popupWindow.setFocusable(true);
				popupWindow.update();

				Button btnRegister = (Button) popupView
						.findViewById(R.id.register);
				btnRegister.setOnClickListener(new Button.OnClickListener() {

					//Brett - controls related to registration
					EditText txtNewUserFName = (EditText) popupView.findViewById(R.id.edttxtFirstName);
					EditText txtNewUserLName = (EditText) popupView.findViewById(R.id.edttxtLastName);
					EditText txtNewUserEmail = (EditText) popupView.findViewById(R.id.edttxtEmail);
					EditText txtNewUserUsername = (EditText) popupView.findViewById(R.id.edttxtRegUserName);
					EditText txtNewUserPassword = (EditText) popupView.findViewById(R.id.edttxtPassword);

					//Brett - write registration code here
					@Override
					public void onClick(View v) {

						registerTask = new AsyncTask<Void, Void, Void>() {
				            @SuppressWarnings({ "unused", "unused", "unused" })
							@Override
				            protected Void doInBackground(Void... params) {              
				            	Message msg = registerHandle.obtainMessage();
				            	//Check to see if username exists already
				            	String message = checkIfUserExists(getApplicationContext(), txtNewUserUsername.getText().toString());
				            	Log.e(TAG, message);
				            	if(message.equals(""))
				            	{
				            		//User name does not exist, and it is safe to register
				            		
				            		String SENDER_ID = "22453893049";
				            		
			        		        // Get GCM registration id
			                     final Context context = getApplicationContext();
			        			 GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
			        	
			        			 String registrationID = "";
			        			try {
			        				String storedID = getRegistrationId(getApplicationContext());
			        				if(storedID.equals(""))
			        				{
			        				registrationID = gcm.register(SENDER_ID);
			        				storeRegistrationId(getApplicationContext(), registrationID);
			        		        register(getApplicationContext(), txtNewUserFName.getText().toString(), txtNewUserLName.getText().toString(), txtNewUserUsername.getText().toString(),txtNewUserPassword.getText().toString(),txtNewUserEmail.getText().toString(),registrationID);
			        				}
			        				else{      					
			        					registrationID = getRegistrationId(getApplicationContext());
				        		        register(getApplicationContext(), txtNewUserFName.getText().toString(), txtNewUserLName.getText().toString(), txtNewUserUsername.getText().toString(),txtNewUserPassword.getText().toString(),txtNewUserEmail.getText().toString(),storedID);
			        				}
			        			} catch (IOException e) {
			        				// TODO Auto-generated catch block
			        				e.printStackTrace();
			        			}
				            	}
				            	
				            	String[] fields = new String[6];
				            	fields[0] = message;
				            	fields[1] = txtNewUserFName.getText().toString();
				            	fields[2] = txtNewUserLName.getText().toString();
				            	fields[3] = txtNewUserEmail.getText().toString();
				            	fields[4] = txtNewUserUsername.getText().toString();
				            	fields[5] = txtNewUserPassword.getText().toString();
				            	//msg.obj = message;
				            	msg.obj = fields;
								registerHandle.sendMessage(msg);
								
				                return null;
				            }
				            @Override
				            protected void onPostExecute(Void result) {
				            	//brandon - Because AsyncTasks can only be used once
				                registerTask = null;
				            }
				        };
				        
						registerTask.execute(null, null, null);
						
						
						//Brandon -- I relocated your code brett to be executed within the Registration Handler

					}
				});

			}
		});

	}
	
	@Override
	 protected void onResume(){
	     super.onResume();
	     Intent intent = getIntent();
	     if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
	         Parcelable[] rawMessages = intent.getParcelableArrayExtra(
	                 NfcAdapter.EXTRA_NDEF_MESSAGES);
	 
	         NdefMessage message = (NdefMessage) rawMessages[0]; // only one message transferred
	         Toast.makeText(this, new String(message.getRecords()[0].getPayload()), Toast.LENGTH_LONG).show();
	         //mTextView.setText(new String(message.getRecords()[0].getPayload()));
	 
	     } //else
	    	 //Toast.makeText(this, "Waiting for NDEF Message", Toast.LENGTH_LONG).show();
	         //mTextView.setText("Waiting for NDEF Message");
	 
	 }
	
	
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
    
    
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
    
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if the application was updated; if it was, it must clear out the registration ID
        // since the existing registration ID is not guaranteed to work with the new
        // application version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
    

    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }    

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
	
	
}