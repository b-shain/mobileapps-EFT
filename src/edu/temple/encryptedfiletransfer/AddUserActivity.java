package edu.temple.encryptedfiletransfer;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AddUserActivity extends Activity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_user);
		//EditText mEdittext = (EditText) findViewById(R.id.editText_nfc);
		
	}
	
	@Override
	 protected void onResume(){
		TextView mTextView = (TextView) findViewById(R.id.textView_nfc);
	     super.onResume();
	     Intent intent = getIntent();
	     //Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	     //Log.i("detected tag", detectedTag.toString());
	     if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
	         Parcelable[] rawMessages = intent.getParcelableArrayExtra(
	                 NfcAdapter.EXTRA_NDEF_MESSAGES);
	 
	         NdefMessage message = (NdefMessage) rawMessages[0]; // only one message transferred
	         Toast.makeText(this, new String(message.getRecords()[0].getPayload()), Toast.LENGTH_LONG).show();
	        
	         String temp = new String(message.getRecords()[0].getPayload());
	         Log.i("payload", temp);
	         mTextView.setText(new String(message.getRecords()[0].getPayload()));
	 
	     } else
	    	 //Toast.makeText(this, "Waiting for NDEF Message", Toast.LENGTH_LONG).show();
	         mTextView.setText("Waiting for NDEF Message");
	 
	 }

}
