package edu.temple.encryptedfiletransfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import static edu.temple.encryptedfiletransfer.ServerUtils.getID;
import static edu.temple.encryptedfiletransfer.Utilities.TAG;
import static edu.temple.encryptedfiletransfer.Utilities.setListViewHeightBasedOnChildren;

public class SendFile extends Activity {

	TextView txtUserMessage;
	Cipher aesCipher;
	Button fileSearch, friendSearch, sendFile;
	JSONObject jObj;
	JSONArray jArray;
	ArrayList<String> friendList, list, idList;
	ExtendedArrayAdapter fileAdapter, friendAdapter;
	ListView friendListview, listview;
	
	String selectedFile, selectedUser, selectedUserID;

    AsyncTask<Void, Void, Void> friendsListTask, idTask;

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
				Log.d(TAG, "Friend: " + jArray.getString(1).toString());
				Log.d(TAG, "Array 0: " + jArray.getString(0).toString());

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
	
    final Handler idHandle = new Handler(){
		@Override
		public void handleMessage(Message msg) {		 
			String idResponse = (String) msg.obj;
			if(idResponse == "")
			{
				//This should never trigger
				Toast.makeText(getApplicationContext(), "No ID was found for that user.", Toast.LENGTH_LONG).show();	            
			}
			else
			{
			try {
				jObj = new JSONObject(idResponse);
					Log.d(TAG, "Names 1: " + jObj.names().toString());
				jArray =  jObj.getJSONArray("IDs");
					Log.d(TAG, "IDs: " + jArray.toString());
					Log.d(TAG, "Names 2: " + jObj.names().toString());
				//jArray = jObj.getJSONArray("Friend");
					//Log.d(TAG, "ID: " + jArray.getString(1).toString());
					//Log.d(TAG, "Array 0: " + jArray.getString(0).toString());
			    for (int i = 0; i < jArray.length(); ++i) {
			    	String temp = jArray.getString(i).toString();
			    	JSONObject id = new JSONObject(temp);
			    	selectedUserID = id.getString("ID");
			    	Log.d(TAG, "ID: " + selectedUserID);
			      idList.add(selectedUserID);
			    }
			    selectedUserID.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Toast.makeText(getApplicationContext(), "The ID Response was : " + idResponse + ".", Toast.LENGTH_LONG).show();	            
			}
		}					
	};	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_file);
		
		//initialize the variables
		listview = (ListView) findViewById(R.id.listview);
		friendListview = (ListView) findViewById(R.id.friend_listview);
		txtUserMessage = (TextView) findViewById(R.id.txtVwSendFileWelcome);

		
		//hide the listviews
		friendListview.setVisibility(View.GONE);
		listview.setVisibility(View.GONE);
		
		Intent prevIntent = getIntent();
		final String userName = prevIntent.getStringExtra("username");
		String welcomeMessage = "Welcome "
				+ prevIntent.getStringExtra("username") + "!";

		txtUserMessage.setText(welcomeMessage);
		fileSearch = (Button) findViewById(R.id.btn_select_file);
		sendFile = (Button) findViewById(R.id.btn_sendFile);
		friendSearch = (Button) findViewById(R.id.btn_select_friend);

		//File storageDirectory       = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name));
		final File storageDirectory = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name));

		if (!storageDirectory.exists()) 
		{
			storageDirectory.mkdir();
			File test = new File(storageDirectory.toURI());
			File other = new File("other");
			test.renameTo(other);
		}
		
		
		fileSearch.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				//File[] testFiles = storageDirectory.listFiles();
				
				File[] files = storageDirectory.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname)
					{
						//If a file or directory is hidden, or unreadable, don't show it in the list.</div>
						if(pathname.isHidden())
							return false;

						if(!pathname.canRead())
							return false;

						//Show all directories in the list.
						if(!pathname.isDirectory())
							return true;

						return false;
					}
				});
				
				
				final TextView txt = (TextView) findViewById(R.id.textView_fileName);
				
				//get reference to listview and set it to visible
				listview = (ListView) findViewById(R.id.listview);
				listview.setVisibility(View.VISIBLE);
				
				
				list = new ArrayList<String>();
			    for (int i = 0; i < files.length; ++i) {
			      list.add(files[i].toString());
			    }
				list.get(0).toString();
			    fileAdapter = new ExtendedArrayAdapter(getApplicationContext(), R.layout.simple_list_item_1, list);
			    listview.setAdapter(fileAdapter);
			    
			    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			      @Override
			      public void onItemClick(AdapterView<?> parent, final View view,
			          int position, long id) {
			        final String item = (String) parent.getItemAtPosition(position);
			        selectedFile = item;
			        txt.setText("You have selected " + item + " to be sent.");
			        System.out.println(item + " was clicked");
			        listview.setVisibility(View.GONE);
//			        view.animate().setDuration(2000).alpha(0)
//			            .withEndAction(new Runnable() {
//			              @Override
//			              public void run() {
//			                list.remove(item);
//			                adapter.notifyDataSetChanged();
//			                view.setAlpha(1);
//			              }
//			            });
			      }

			    });
			    
				
//				if(files != null){
//					ArrayAdapter<Object> fileListAdapter = new ArrayAdapter<Object>(getApplicationContext(), R.layout.activity_send_file, R.id.textView_fileName, files);
//					ListView lv = (ListView) findViewById(R.id.listview);
//						if(lv != null)
//						{
//							lv.setAdapter(fileListAdapter);
//						}
//				}
//				else{
//					TextView txt = (TextView) findViewById(R.id.textView_fileName);
//					//R.id.fileName.text == "";
//					txt.setText("The directory is empty.");
//					//android.R.layout.simple_list_item_1;
//				}
				
				
			}
		});
		
		friendSearch.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				//File[] testFiles = storageDirectory.listFiles();
				
				final TextView txtFriend = (TextView) findViewById(R.id.textView_friendName);
				friendListview = (ListView) findViewById(R.id.friend_listview);
				friendList = new ArrayList<String>();
							
				//brandon
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
			        final String friendItem = (String) parent.getItemAtPosition(position);
			        txtFriend.setText("You have selected " + friendItem + " as the recipient.");
			        selectedUser = friendItem;
			        System.out.println(friendItem + " was clicked");
			        friendListview.setVisibility(View.GONE);
//			        view.animate().setDuration(2000).alpha(0)
//			            .withEndAction(new Runnable() {
//			              @Override
//			              public void run() {
//			                list.remove(item);
//			                adapter.notifyDataSetChanged();
//			                view.setAlpha(1);
//			              }
//			            });
			      }

			    });
			    
				
//				if(files != null){
//					ArrayAdapter<Object> fileListAdapter = new ArrayAdapter<Object>(getApplicationContext(), R.layout.activity_send_file, R.id.textView_fileName, files);
//					ListView lv = (ListView) findViewById(R.id.listview);
//						if(lv != null)
//						{
//							lv.setAdapter(fileListAdapter);
//						}
//				}
//				else{
//					TextView txt = (TextView) findViewById(R.id.textView_fileName);
//					//R.id.fileName.text == "";
//					txt.setText("The directory is empty.");
//					//android.R.layout.simple_list_item_1;
//				}
				
				
			}
		});
	
		sendFile.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				final TextView response = (TextView) findViewById(R.id.textView_response);

				try {
					aesCipher = Cipher.getInstance("AES");
				} catch (NoSuchAlgorithmException | NoSuchPaddingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					encryptfile();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
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

			Intent logoutIntent = new Intent(SendFile.this,
					MainActivity.class);
			// logoutIntent.putExtra("username",txtNewUserUsername.getText().toString());
			logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(logoutIntent);
			finish();
			showToast("You have logged out!");

			return true;
		case R.id.Home:
			Intent homeIntent = new Intent(SendFile.this,
					HomeActivity.class);
			homeIntent.putExtra("username", txtUserMessage.getText().toString()
					.substring(8, endIndex));
			homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(homeIntent);
			return true;
		case R.id.AddFriend:
			Intent addFriendIntent = new Intent(SendFile.this,
					AddFriend.class);
			addFriendIntent.putExtra("username", txtUserMessage.getText()
					.toString().substring(8, endIndex));
			addFriendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			addFriendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(addFriendIntent);
			return true;
		case R.id.SendFile:
			Intent fileIntent = new Intent(SendFile.this, SendFile.class);
			fileIntent.putExtra("username", txtUserMessage.getText().toString()
					.substring(8, endIndex));
			fileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			fileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(fileIntent);
			return true;
		case R.id.ViewLog:
			Intent logIntent = new Intent(SendFile.this, ViewLog.class);
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
	
	
	public void encryptfile() throws NoSuchAlgorithmException
	{
		
		idList = new ArrayList<String>();		
		//brandon
        idTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {              
            	Message msg = idHandle.obtainMessage();
            	String message = getID(getApplicationContext(), selectedUser);
            	Log.e(TAG, message);
            	msg.obj = message;                	
				idHandle.sendMessage(msg);
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
            	//brandon - Because AsyncTasks can only be used once
                idTask = null;
            }
        };
        
        //AS long as there is a selected recipient
        if(selectedUser != "" && selectedUser != null){
		idTask.execute(null, null, null);
		if(selectedUserID != "" && selectedUserID != null){
		try
	      { 		
			File storageDirectory = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name));			
			storageDirectory.mkdir();		
			File eftDirectory = new File(Environment.getExternalStorageDirectory().toString() + "/eft");
			
			if (!eftDirectory.exists()) {
				eftDirectory.mkdir();
	        }
			
			 //File f = new File(eftDirectory + "/test.txt");  
	           
	         final String fileName = /*uri.getQueryParameter("filename")*/ "test" + ".txt";
	         File f;
	         //FileOutputStream fileOut = new FileOutputStream( f = new File (eftDirectory, fileName));
	         FileOutputStream fileOut = new FileOutputStream( f = new File (storageDirectory, fileName));
	         System.out.println(f.getAbsolutePath());
	         //ObjectOutputStream out = new ObjectOutputStream(fileOut);
	         String tst = ("This is a test");	         
	         fileOut.write(tst.getBytes());
	         //out.writeObject(tst);
	         //out.close();
	         fileOut.close();
	         System.out.println("Serialized data is saved in: " + storageDirectory.toString());
	         
	         //Unencrypted file has been stored
	         String UUID = android.os.Build.SERIAL;
		     System.out.println("String UUID: " + UUID);	
	         System.out.println("Int UUID: " + Integer.parseInt(UUID));
	         
	         //Retrieve other users serial
	         String UUID_2 = selectedUserID;
	         System.out.println("String UUID_2: " + UUID_2);
	         System.out.println("Int UUID_2: " + Integer.parseInt(UUID_2));
	         long combo = (Long.parseLong(UUID) * Long.parseLong(UUID_2));
	         long x = Long.parseLong("1000000000000");
	         while (combo < 0)
	         {
	        	 combo = (Long.parseLong(UUID) * Long.parseLong(UUID_2)) - x;
	        	 x = x + Long.parseLong("1000000000000");
	         }
	         long combo2 = Integer.parseInt(UUID_2) * Integer.parseInt(UUID);
	         //String combination = (UUID + UUID_2);
		     System.out.println("Long Combination: " + combo);
		     System.out.println("Combination 2: " + combo2);


	         //combination.getBytes();
	         //SecretKey key = new SecretKeySpec(combination.getBytes("UTF-8"), "AES");
	         byte[] key = (Long.toString(combo)).getBytes("UTF-8");
	         System.out.println("Combination to string: " + Long.toString(combo));
	         String uuid = new String(key);
		     System.out.println("UUID combination Array: " + uuid);
	         MessageDigest sha = MessageDigest.getInstance("SHA-1");
	         key = sha.digest(key);
	         uuid = new String(key);
		     System.out.println("Digested Key: " + uuid);
	         key = Arrays.copyOf(key, 16); // use only first 128 bit
	         uuid = new String(key);
		     System.out.println("128-Bits of the Key Array: " + uuid);

	         	         
	         SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
	         
	         final String encFileName = /*uri.getQueryParameter("filename")*/ "test_copy" + ".txt";
	         File c = new File (storageDirectory, encFileName);
	         
	         final String decFileName = /*uri.getQueryParameter("filename")*/ "test_dec_copy" + ".txt";
	         File d = new File (storageDirectory, decFileName);
	         
	         //Select UUID from Associated User where Username = friendName
	         
	         //getID(getApplicationContext(), selectedUser);
	         selectedUser.toString();
	         
	         encrypt(f, c, secretKeySpec);	    
	         
	         decrypt(c, d, secretKeySpec);
	         
	      }catch(IOException i)
	      {
	          i.printStackTrace();
	      } catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try
	      {
			File storageDirectory = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name));			
			//storageDirectory.mkdir();
			File eftDirectory = new File(Environment.getExternalStorageDirectory().toString() + "/eft");			
			if (!eftDirectory.exists()) {
				eftDirectory.mkdir();
	        }
			
			 //File f = new File(eftDirectory + "/test.txt");  
			 final String decFileName = /*uri.getQueryParameter("filename")*/ "test_dec_copy" + ".txt";
	         File d = new File (storageDirectory, decFileName);
	         final String fileName = /*uri.getQueryParameter("filename")*/ "test_copy" + ".txt";
	         File f;
	         FileInputStream fileIn = new FileInputStream(f = new File (storageDirectory, fileName));
	         BufferedReader reader = new BufferedReader(new InputStreamReader(fileIn));
	         String line = reader.readLine();
		     System.out.println("Deserialized encrypted file: " + line);
	         fileIn.close();
	         
	         fileIn = new FileInputStream(d);
	         reader = new BufferedReader(new InputStreamReader(fileIn));
	         line = reader.readLine();
		     System.out.println("Deserialized decrypted file: " + line);
	         fileIn.close();
	         
	         //ObjectInputStream in = new ObjectInputStream(fileIn);
	         String result = fileIn.toString();
		      System.out.println(result);
	         //e = (Employee) in.readObject();
	         //in.close();

	      }catch(IOException i)
	      {
	         i.printStackTrace();
	         return;
	      }
	      System.out.println("Deserialized file...");
		
        }
	      
        }
		
	}
	
	public void encrypt(File in, File out, SecretKeySpec aeskeySpec) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
	    aesCipher.init(Cipher.ENCRYPT_MODE, aeskeySpec);
	   
	    FileInputStream is = new FileInputStream(in);
	    CipherOutputStream os = new CipherOutputStream(new FileOutputStream(out), aesCipher);
	   
	    copy(is, os);
	   
	    is.close();
	    os.close();
	  }
	 
	  public void decrypt(File in, File out, SecretKeySpec aeskeySpec) throws IOException, InvalidKeyException {
	    aesCipher.init(Cipher.DECRYPT_MODE, aeskeySpec);
	   
	    CipherInputStream is = new CipherInputStream(new FileInputStream(in), aesCipher);
	    FileOutputStream os = new FileOutputStream(out);
	   
	    copy(is, os);
	   
	    is.close();
	    os.close();
	  }
	 
	  private void copy(InputStream is, OutputStream os) throws IOException {
	    int i;
	    byte[] b = new byte[1024];
	    while((i=is.read(b))!=-1) {
	      os.write(b, 0, i);
	    }
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



