package edu.temple.encryptedfiletransfer;

import java.nio.charset.Charset;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class AddFriend extends Activity implements CreateNdefMessageCallback, OnNdefPushCompleteCallback{
	
	TextView txtUserMessage, txtNFC;
	NfcAdapter mNfcAdapter;
	private static final int MESSAGE_SENT = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_friend);
		
		txtUserMessage = (TextView) findViewById(R.id.txtVwAddFriendWelcome);
		
		Intent prevIntent = getIntent();

		String welcomeMessage = "Welcome " + prevIntent.getStringExtra("username") + "!";

		txtUserMessage.setText(welcomeMessage);
		
		// Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            txtNFC = (TextView) findViewById(R.id.textView_nfc);
            txtNFC.setText("NFC is not available on this device.");
        }
        // Register callback to set NDEF message
        mNfcAdapter.setNdefPushMessageCallback(this, this);
        // Register callback to listen for message-sent success
        mNfcAdapter.setOnNdefPushCompleteCallback(this, this);

	}
	
	@Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }
	
	
	/**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        txtNFC.setText(new String(msg.getRecords()[0].getPayload()));
    }

    /**
     * Creates a custom MIME type encapsulated in an NDEF record
     *
     * @param mimeType
     */
    public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
        return mimeRecord;
    }

	
	
	@Override
	 protected void onResume(){
		TextView mTextView = (TextView) findViewById(R.id.textView_nfc);
	     super.onResume();
	     Intent intent = getIntent();
	     //Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	     //Log.i("detected tag", detectedTag.toString());
	     if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
	            processIntent(getIntent());
	        }
	     
	     
	     if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
	         Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
	 
	         NdefMessage message = (NdefMessage) rawMessages[0]; // only one message transferred
	         Toast.makeText(this, new String(message.getRecords()[0].getPayload()), Toast.LENGTH_LONG).show();
	        
	         String temp = new String(message.getRecords()[0].getPayload());
	         Log.i("payload", temp);
	         mTextView.setText(new String(message.getRecords()[0].getPayload()));
	 
	     } else
	    	 //Toast.makeText(this, "Waiting for NDEF Message", Toast.LENGTH_LONG).show();
	         mTextView.setText("Waiting for NDEF Message");
	 
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

			Intent logoutIntent = new Intent(AddFriend.this,
					MainActivity.class);
			// logoutIntent.putExtra("username",txtNewUserUsername.getText().toString());
			logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(logoutIntent);
			finish();
			showToast("You have logged out!");

			return true;
		case R.id.Home:
			Intent homeIntent = new Intent(AddFriend.this,
					HomeActivity.class);
			homeIntent.putExtra("username", txtUserMessage.getText().toString()
					.substring(8, endIndex));
			homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(homeIntent);
			return true;
		case R.id.AddFriend:
			Intent addFriendIntent = new Intent(AddFriend.this,
					AddFriend.class);
			addFriendIntent.putExtra("username", txtUserMessage.getText()
					.toString().substring(8, endIndex));
			addFriendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			addFriendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(addFriendIntent);
			return true;
		case R.id.SendFile:
			Intent fileIntent = new Intent(AddFriend.this, SendFile.class);
			fileIntent.putExtra("username", txtUserMessage.getText().toString()
					.substring(8, endIndex));
			fileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			fileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(fileIntent);
			return true;
		case R.id.ViewLog:
			Intent logIntent = new Intent(AddFriend.this, ViewLog.class);
			logIntent.putExtra("username", txtUserMessage.getText().toString()
					.substring(8, endIndex));
			logIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			logIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(logIntent);
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

	@Override
	public void onNdefPushComplete(NfcEvent event) {
        // A handler is needed to send messages to the activity when this
        // callback occurs, because it happens from a binder thread
        nfcHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
	}

	 private final Handler nfcHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            case MESSAGE_SENT:
	                Toast.makeText(getApplicationContext(), "Message sent!", Toast.LENGTH_LONG).show();
	                break;
	            }
	        }
	    };

	
	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		Time time = new Time();
        time.setToNow();
        String text = ("Beam me up!\n\n" +
                "Beam Time: " + time.format("%H:%M:%S"));
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { createMimeRecord(
                        "application/edu.temple.encryptedfiletransfer", text.getBytes())
         /**
          * The Android Application Record (AAR) is commented out. When a device
          * receives a push with an AAR in it, the application specified in the AAR
          * is guaranteed to run. The AAR overrides the tag dispatch system.
          * You can add it back in to guarantee that this
          * activity starts when receiving a beamed message. For now, this code
          * uses the tag dispatch system.
          */
          ,NdefRecord.createApplicationRecord("edu.temple.encryptedfiletransfer")
        });
        return msg;

	}
}
