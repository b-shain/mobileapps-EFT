package edu.temple.encryptedfiletransfer;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MessageSender extends Activity implements NfcAdapter.CreateNdefMessageCallback{

	 @Override
	    protected void onCreate(Bundle savedInstanceState) {

		 EditText mEditText = (EditText) findViewById(R.id.editText_nfc);
		 //TextView mTextView = (TextView) findViewById(R.id.textView_nfc);
	       NfcAdapter mAdapter = NfcAdapter.getDefaultAdapter(this);
	        if (mAdapter == null) {
	        	Toast.makeText(this, "Sorry this device does not have NFC.", Toast.LENGTH_LONG).show();
	            mEditText.setText("Sorry this device does not have NFC.");
	            return;
	        }
	 
	        if (!mAdapter.isEnabled()) {
	            Toast.makeText(this, "Please enable NFC via Settings.", Toast.LENGTH_LONG).show();
	        }
	 
	        mAdapter.setNdefPushMessageCallback(this, this);

	    }
	
	public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
		EditText mEditText = (EditText) findViewById(R.id.editText_nfc);
	     String message = mEditText.getText().toString();
	     //String message = "nfc test message";
	     NdefRecord ndefRecord = NdefRecord.createMime("text/plain", message.getBytes());
	     NdefMessage ndefMessage = new NdefMessage(ndefRecord);
	     return ndefMessage;
	 }
	
}
