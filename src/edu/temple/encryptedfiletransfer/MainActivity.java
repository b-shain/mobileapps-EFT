package edu.temple.encryptedfiletransfer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
						registrationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						registrationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(registrationIntent);

					}
				});

			}
		});

	}
}
