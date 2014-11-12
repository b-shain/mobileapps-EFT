package edu.temple.encryptedfiletransfer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//this is where the user selects their friend to send a file or accepts file send requests 
public class HomeActivity extends Activity {
	
	TextView txtUserMessage;
	TextView txtInstructions;
	Button btnAddFriend;
	Button btnViewLog;
	Button btnSendFile;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		txtUserMessage = (TextView) findViewById(R.id.txtVwWelcome);
		txtInstructions = (TextView) findViewById(R.id.txtVwHomeInstructions);
		btnAddFriend = (Button) findViewById(R.id.btnAddNewFriend);
		btnViewLog = (Button) findViewById(R.id.btnViewLog);
		btnSendFile = (Button) findViewById(R.id.SendFile);
		
		Intent loggedInIntent = getIntent();

		String welcomeMessage = "Welcome " + loggedInIntent.getStringExtra("username") + "!";
		
		txtUserMessage.setText(welcomeMessage);
		
		String instructions = "You can now use EFT to add a friend/file recipient, send files, and/or "
				+ "view a log of your previous transfers."
				+ " Click a button below or use the menu above to get started.";
		
		txtInstructions.setText(instructions);
		
		
		
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		 
		MenuInflater inflater = getMenuInflater();
		 
		inflater.inflate(R.menu.main, menu);
		 
		return true;
		 
		}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		
		
		switch (item.getItemId()) {
		case R.id.Logout:
			
			Intent logoutIntent = new Intent(HomeActivity.this, MainActivity.class);
			//logoutIntent.putExtra("username",txtNewUserUsername.getText().toString());
			startActivity(logoutIntent);
			
			showToast("You have logged out!");
			
			return true;
		case R.id.Home:
			
			Intent homeIntent = new Intent(HomeActivity.this, HomeActivity.class);
			int endIndex = txtUserMessage.getText().toString().indexOf("!");
			homeIntent.putExtra("username", txtUserMessage.getText().toString().substring(8, endIndex));
			startActivity(homeIntent);
			
			return true;
		default:
			return super.onOptionsItemSelected(item);
				
		}
		 
		

	}
	
	public void showToast(String message) {
	 
	Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
	 
	toast.show();
	 
	}
		 
		}
