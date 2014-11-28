package edu.temple.encryptedfiletransfer;
import static edu.temple.encryptedfiletransfer.ServerUtils.register;
import static edu.temple.encryptedfiletransfer.ServerUtils.logIn;
import static edu.temple.encryptedfiletransfer.Utilities.TAG;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

//this will be the login activity. 
public class MainActivity extends Activity {

	final Context context = this;
	Button btnLogin;
	Button btnRegister;
	EditText txtUsername;
	EditText txtPassword;
	
    AsyncTask<Void, Void, Void> mLoginTask;

    final Handler webHandle = new Handler(){
		@Override
		public void handleMessage(Message msg) {		 
			String loginResponse = (String) msg.obj;
			//Toast.makeText(getApplicationContext(), "The Login Response was : " + loginResponse + ". Which should be your UserID", Toast.LENGTH_LONG).show();	            
	    }				
	};	
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnRegister = (Button) findViewById(R.id.btnRegister);
		txtUsername = (EditText) findViewById(R.id.edttxtUsername);
		txtPassword = (EditText) findViewById(R.id.edttxtPassword);

		// Brett - will handle logging a user into EFT
		btnLogin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {		
				//brandon
		        mLoginTask = new AsyncTask<Void, Void, Void>() {
		            @Override
		            protected Void doInBackground(Void... params) {              
		            	Message msg = webHandle.obtainMessage();
		            	String message = logIn(getApplicationContext(), "usrname","pass");
		            	Log.e(TAG, message);
		            	msg.obj = message;                	
						webHandle.sendMessage(msg);
		                return null;
		            }
		            @Override
		            protected void onPostExecute(Void result) {
		            	//brandon - Because AsyncTasks can only be used once
		                mLoginTask = null;
		            }
		        };
				mLoginTask.execute(null, null, null);
				
				// Brett - transition user to home screen once user has been
				// logged in successfully
				Intent successfulLoginIntent = new Intent(MainActivity.this,
						HomeActivity.class);
				successfulLoginIntent.putExtra("username", txtUsername
						.getText().toString());
				startActivity(successfulLoginIntent);
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

				final PopupWindow popupWindow = new PopupWindow(popupView,
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

						String feedback = txtNewUserFName.getText().toString() + " " + txtNewUserLName.getText().toString() +
								" , you have successfully registered!";
						
						Toast.makeText(getApplicationContext(), 
								feedback, Toast.LENGTH_LONG).show();
						
						//Brett - after successful registration
						popupWindow.dismiss();

						Intent registrationIntent = new Intent(MainActivity.this, HomeActivity.class);
						registrationIntent.putExtra("username",txtNewUserUsername.getText().toString());
						startActivity(registrationIntent);

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
	
}