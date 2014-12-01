package edu.temple.encryptedfiletransfer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;

//this is where the user selects their friend to send a file or accepts file send requests 
public class HomeActivity extends Activity {

	TextView txtUserMessage;
	TextView txtInstructions;
	Button btnAddFriend;
	Button btnViewLog;
	Button btnSendFile;
	ListView lstvwFiles;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		txtUserMessage = (TextView) findViewById(R.id.txtVwWelcome);
		txtInstructions = (TextView) findViewById(R.id.txtVwHomeInstructions);
		btnAddFriend = (Button) findViewById(R.id.btnAddNewFriend);
		//btnViewLog = (Button) findViewById(R.id.btnViewLog);
		btnSendFile = (Button) findViewById(R.id.SendFile);

		btnSendFile = (Button) findViewById(R.id.btnSendFile);
		
		lstvwFiles = (ListView) findViewById(R.id.lstViewFiles);
		
		
		Intent loggedInIntent = getIntent();

		String welcomeMessage = "Welcome " + loggedInIntent.getStringExtra("username") + "!";

		txtUserMessage.setText(welcomeMessage);

		String instructions = "You can now use EFT to add a friend/file recipient, and/or send files "
				+ " Click a button below or use the menu above to get started.";

		txtInstructions.setText(instructions);
		
		btnAddFriend.setOnClickListener(new View.OnClickListener() {
			int nameIndex = txtUserMessage.getText().toString().indexOf("!");
			
			@Override
			public void onClick(View v) {
				Intent addFriendIntent = new Intent(HomeActivity.this,
						AddFriend.class);
				addFriendIntent.putExtra("username", txtUserMessage.getText()
						.toString().substring(8, nameIndex));
				addFriendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				addFriendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(addFriendIntent);
				
			}
		});
		
		/*btnViewLog.setOnClickListener(new View.OnClickListener() {
			int nameIndex = txtUserMessage.getText().toString().indexOf("!");
			
			@Override
			public void onClick(View v) {
				Intent logIntent = new Intent(HomeActivity.this, ViewLog.class);
				logIntent.putExtra("username", txtUserMessage.getText().toString()
						.substring(8, nameIndex));
				logIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				logIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(logIntent);
				
			}
		});*/
		
		btnSendFile.setOnClickListener(new View.OnClickListener() {
			int nameIndex = txtUserMessage.getText().toString().indexOf("!");
			
			@Override
			public void onClick(View v) {
				Intent fileIntent = new Intent(HomeActivity.this, SendFile.class);
				fileIntent.putExtra("username", txtUserMessage.getText().toString()
						.substring(8, nameIndex));
				fileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				fileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(fileIntent);
			}
		});
		
	}

	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();

		inflater.inflate(R.menu.main, menu);

		return true;

	}

	public boolean onOptionsItemSelected(MenuItem item) {

		int endIndex = txtUserMessage.getText().toString().indexOf("!");
		
		switch (item.getItemId()) {
		case R.id.Logout:

			Intent logoutIntent = new Intent(HomeActivity.this,
					MainActivity.class);
			// logoutIntent.putExtra("username",txtNewUserUsername.getText().toString());
			logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(logoutIntent);
			finish();
			showToast("You have logged out!");

			return true;
		case R.id.Home:
			Intent homeIntent = new Intent(HomeActivity.this,
					HomeActivity.class);
			homeIntent.putExtra("username", txtUserMessage.getText().toString()
					.substring(8, endIndex));
			homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(homeIntent);
			return true;
		case R.id.AddFriend:
			Intent addFriendIntent = new Intent(HomeActivity.this,
					AddFriend.class);
			addFriendIntent.putExtra("username", txtUserMessage.getText()
					.toString().substring(8, endIndex));
			addFriendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			addFriendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(addFriendIntent);
			return true;
		case R.id.SendFile:
			Intent fileIntent = new Intent(HomeActivity.this, SendFile.class);
			fileIntent.putExtra("username", txtUserMessage.getText().toString()
					.substring(8, endIndex));
			fileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			fileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(fileIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}

	}

	public void showToast(String message) {

		Toast toast = Toast.makeText(getApplicationContext(), message,
				Toast.LENGTH_SHORT);

		toast.show();

	}

}

