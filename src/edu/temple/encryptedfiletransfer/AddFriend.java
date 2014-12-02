package edu.temple.encryptedfiletransfer;

import static edu.temple.encryptedfiletransfer.ServerUtils.getFriends;
import static edu.temple.encryptedfiletransfer.Utilities.TAG;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.temple.encryptedfiletransfer.SendFile.ExtendedArrayAdapter;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import static edu.temple.encryptedfiletransfer.ServerUtils.getFriends;
import static edu.temple.encryptedfiletransfer.ServerUtils.associateFriends;

public class AddFriend extends Activity implements CreateNdefMessageCallback, OnNdefPushCompleteCallback{
	
	TextView txtUserMessage, txtNFC;
	NfcAdapter mNfcAdapter;
	private static final int MESSAGE_SENT = 1;
	IntentFilter[] intentFiltersArray;
	PendingIntent pendingIntent;
	String userName, UUID;
	JSONObject jObj;
	JSONArray jArray;
	ArrayList<String> friendList;
	ExtendedArrayAdapter friendAdapter;
	ListView friendListview;
	String friendUserName, friendUUID;
	
	AsyncTask<Void, Void, Void> friendsListTask, addFriendTask;

    final Handler friendsListHandle = new Handler(){
		@Override
		public void handleMessage(Message msg) {		 
			String friendsListResponse = (String) msg.obj;
			if(friendsListResponse == "")
			{
				//they are not registered OR they entered incorrect username/password
				Toast.makeText(getApplicationContext(), "You have no friends =(", Toast.LENGTH_LONG).show();	            
			}
			else
			{
			try {
				jObj = new JSONObject(friendsListResponse);
				jObj.names();
				Log.d(TAG, "Names 1: " + jObj.names().toString());
				jArray =  jObj.getJSONArray("Friends");
				Log.d(TAG, "Friends: " + jArray.toString());
				jObj.names();
				Log.d(TAG, "Names 2: " + jObj.names().toString());
				//jArray = jObj.getJSONArray("Friend");
				//Log.d(TAG, "Friend: " + jArray.getString(1).toString());
				//Log.d(TAG, "Array 0: " + jArray.getString(0).toString());

			    for (int i = 0; i < jArray.length(); ++i) {
			    	String temp = jArray.getString(i).toString();
			    	JSONObject friend = new JSONObject(temp);
			    	String thisFriend = friend.getString("Friend");
			      friendList.add(thisFriend);
			    }
				friendList.get(0).toString();
				friendAdapter = new ExtendedArrayAdapter(getApplicationContext(), R.layout.simple_list_item_1, friendList);
			    friendListview.setAdapter(friendAdapter);
				friendListview.setVisibility(View.VISIBLE);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Toast.makeText(getApplicationContext(), "The Friends List Response was : " + friendsListResponse + ".", Toast.LENGTH_LONG).show();	            
			}
		}					
	};	

    final Handler addFriendHandle = new Handler(){
		@Override
		public void handleMessage(Message msg) {		 

				friendAdapter = new ExtendedArrayAdapter(getApplicationContext(), R.layout.simple_list_item_1, friendList);
			    friendListview.setAdapter(friendAdapter);
				friendListview.setVisibility(View.VISIBLE);

		}					
	};	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_friend);
		
		//initialize the variables
				friendListview = (ListView) findViewById(R.id.friend_listview);
				txtUserMessage = (TextView) findViewById(R.id.txtVwAddFriendWelcome);
				txtNFC = (TextView) findViewById(R.id.textView_nfc);
				
				//hide the listviews
		//friendListview.setVisibility(View.GONE);
		

		Intent prevIntent = getIntent();

		String welcomeMessage = "Welcome " + prevIntent.getStringExtra("username") + "!";
		UUID = android.os.Build.SERIAL;
		userName = prevIntent.getStringExtra("username");
		txtUserMessage.setText(welcomeMessage);
		
		// Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            
            txtNFC.setText("NFC is not available on this device.");
        }
        // Register callback to set NDEF message
        mNfcAdapter.setNdefPushMessageCallback(this, this);
        // Register callback to listen for message-sent success
        mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        
        pendingIntent = PendingIntent.getActivity(
        	    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");    /* Handles all MIME based dispatches.
                                           You should specify only the ones that you need. */
        }
        catch (MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
       intentFiltersArray = new IntentFilter[] {ndef, };
	    friendList = new ArrayList<String>();
//     //brandon
       friendsListTask = new AsyncTask<Void, Void, Void>() {
           @Override
           protected Void doInBackground(Void... params) {              
           	Message msg = friendsListHandle.obtainMessage();
           	String message = getFriends(getApplicationContext(), userName);
           	//String message = logIn(getApplicationContext(), txtUsername.getText().toString(), txtPassword.getText().toString());
           	Log.e(TAG, message);
           	msg.obj = message;                	
				friendsListHandle.sendMessage(msg);
               return null;
           }
           @Override
           protected void onPostExecute(Void result) {
           	//brandon - Because AsyncTasks can only be used once
               friendsListTask = null;
           }
       };
       
		friendsListTask.execute(null, null, null);
				

//			    friendAdapter = new ExtendedArrayAdapter(getApplicationContext(), R.layout.simple_list_item_1, friendList);
//			    friendListview.setAdapter(friendAdapter);
//
//			    friendListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//			      @Override
//			      public void onItemClick(AdapterView<?> parent, final View view,
//			          int position, long id) {
//			        //final String friendItem = (String) parent.getItemAtPosition(position);
//			        //txtFriend.setText("You have selected " + friendItem + " as the recipient.");
//			        //selectedUser = friendItem;
//			        //System.out.println(friendItem + " was clicked");
//			        //friendListview.setVisibility(View.GONE);
//			      }
//
//			    });			
//			    friendListview.setVisibility(View.VISIBLE);

	}
	
	@Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }
	
	
//	/**
//     * Parses the NDEF Message from the intent and prints to the TextView
//     */
//    void processIntent(Intent intent) {
//        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
//                NfcAdapter.EXTRA_NDEF_MESSAGES);
//        // only one message sent during the beam
//        NdefMessage msg = (NdefMessage) rawMsgs[0];
//        // record 0 contains the MIME type, record 1 is the AAR, if present
//        txtNFC.setText(new String(msg.getRecords()[0].getPayload()));
//    }

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
		txtNFC = (TextView) findViewById(R.id.textView_nfc);
	     super.onResume();
	     Intent intent = getIntent();
	     //Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	     //Log.i("detected tag", detectedTag.toString());
	     //if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {processIntent(getIntent());}
	     
	 	if (NfcAdapter.getDefaultAdapter(this) != null) {
	 		
	 		NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null);
	 		// Check if the Activity was started from Beam
		     if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
		         Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		 
		         NdefMessage message = (NdefMessage) rawMessages[0]; // only one message transferred
		         Toast.makeText(this, new String(message.getRecords()[0].getPayload()), Toast.LENGTH_LONG).show();
		        
		         String temp = new String(message.getRecords()[0].getPayload());
		         Log.i("Payload: ", temp);	         
				 Log.i("UUID: ", friendUUID = temp.split(",")[0]);
		         Log.i("Username: ", friendUserName = temp.split(",")[1]);
		         txtNFC.setText(new String(message.getRecords()[0].getPayload()));
		         
		         //brandon
		         addFriendTask = new AsyncTask<Void, Void, Void>() {
		             @Override
		             protected Void doInBackground(Void... params) {              
		             	Message msg = addFriendHandle.obtainMessage();
		             	String message = associateFriends(getApplicationContext(), userName, friendUserName, UUID, friendUUID);
		             	//String message = logIn(getApplicationContext(), txtUsername.getText().toString(), txtPassword.getText().toString());
		             	Log.e(TAG, message);
		             	msg.obj = message;                	
		  				addFriendHandle.sendMessage(msg);
		                 return null;
		             }
		             @Override
		             protected void onPostExecute(Void result) {
		             	//brandon - Because AsyncTasks can only be used once
		            	 addFriendTask = null;
		             }
		         };
		         
		  		addFriendTask.execute(null, null, null);
		  		//friendListview.setVisibility(View.VISIBLE);
		         
		  		 friendList = new ArrayList<String>();
//		       //brandon
		         friendsListTask = new AsyncTask<Void, Void, Void>() {
		             @Override
		             protected Void doInBackground(Void... params) {              
		             	Message msg = friendsListHandle.obtainMessage();
		             	String message = getFriends(getApplicationContext(), userName);
		             	//String message = logIn(getApplicationContext(), txtUsername.getText().toString(), txtPassword.getText().toString());
		             	Log.e(TAG, message);
		             	msg.obj = message;                	
		  				friendsListHandle.sendMessage(msg);
		                 return null;
		             }
		             @Override
		             protected void onPostExecute(Void result) {
		             	//brandon - Because AsyncTasks can only be used once
		                 friendsListTask = null;
		             }
		         };
		         
		  		friendsListTask.execute(null, null, null);
		  				friendListview.setVisibility(View.VISIBLE);

		  			    friendAdapter = new ExtendedArrayAdapter(getApplicationContext(), R.layout.simple_list_item_1, friendList);
		  			    friendListview.setAdapter(friendAdapter);

		  			    friendListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

		  			      @Override
		  			      public void onItemClick(AdapterView<?> parent, final View view,
		  			          int position, long id) {
		  			      }

		  			    });			
		     } else
		    	 //Toast.makeText(this, "Waiting for NDEF Message", Toast.LENGTH_LONG).show();
		    	 txtNFC.setText("Waiting for NDEF Message");
	 		}
	     

	 
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
	                Toast.makeText(getApplicationContext(), "User information sent!", Toast.LENGTH_LONG).show();
	                break;
	            }
	        }
	    };

	
	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		
        //android.os.Build.SERIAL is sent to the other device to be paired
        String text = (android.os.Build.SERIAL + "," + userName);
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { createMimeRecord(
                        "application/edu.temple.encryptedfiletransfer", text.getBytes())
          ,NdefRecord.createApplicationRecord("edu.temple.encryptedfiletransfer")
        });
        return msg;

	}
	
	
	public class ExtendedArrayAdapter extends ArrayAdapter<String> {

	    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

	    public ExtendedArrayAdapter(Context context, int textViewResourceId,
	        List<String> objects) {
	      super(context, textViewResourceId, objects);
	      for (int i = 0; i < objects.size(); ++i) {
	        mIdMap.put(objects.get(i), i);
	      }
	    }

	    @Override
	    public long getItemId(int position) {
	      String item = getItem(position);
	      return mIdMap.get(item);
	    }

	    @Override
	    public boolean hasStableIds() {
	      return true;
	    }

	  }
	
}
