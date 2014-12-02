package edu.temple.encryptedfiletransfer;

import static edu.temple.encryptedfiletransfer.ServerUtils.getFriends;
import static edu.temple.encryptedfiletransfer.ServerUtils.getID;
import static edu.temple.encryptedfiletransfer.ServerUtils.sendDownloadNotification;
import static edu.temple.encryptedfiletransfer.Utilities.TAG;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

public class SendFile extends Activity {
	
	int serverResponseCode = 0;
	final String upLoadServerUri = "http://cis-linux2.temple.edu/~tud30441/upload.php";
    HttpURLConnection conn = null;
     DataOutputStream dos = null; 
    final String lineEnd = "\r\n";
    final String twoHyphens = "--";
    final String boundary = "*****";
    int bytesRead;
	int bytesAvailable;
	int bufferSize;
    byte[] buffer;
    final int maxBufferSize = 1 * 1024 * 1024;

	TextView txtUserMessage, response;
	Cipher aesCipher;
	Button fileSearch, friendSearch, sendFile;
	JSONObject jObj;
	JSONArray jArray;
	ArrayList<String> friendList, list, idList;
	ExtendedArrayAdapter fileAdapter, friendAdapter;
	ListView friendListview, listview;
	
	String userName, userID, selectedFile, selectedUser, selectedUserID, fileUrl;

    AsyncTask<Void, Void, Void> friendsListTask, idTask, notificationTask, uploadTask;

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
				//jObj.names();
				Log.d(TAG, "Names 2: " + jObj.names().toString());
				//jArray = jObj.getJSONArray("Friend");
				//Log.d(TAG, "Friend: " + jArray.getString(1).toString());
				//Log.d(TAG, "Array 0: " + jArray.getString(0).toString());

			    for (int i = 0; i < jArray.length(); ++i) {
			    	String temp = jArray.getString(i).toString();
			    	JSONObject friend = new JSONObject(temp);
			    	String thisFriend = friend.getString("Friend");
			    	Log.d(TAG, "Friend " + i + ": " + thisFriend);
			      friendList.add(thisFriend);
			    }
				//friendList.get(0).toString();
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

    final Handler notificationHandle = new Handler(){
		@Override
		public void handleMessage(Message msg) {		 
			String notificationResponse = (String) msg.obj;			
			Toast.makeText(getApplicationContext(), "The Notification Response was : " + notificationResponse + ".", Toast.LENGTH_LONG).show();	            			
		}					
	};	

    final Handler uploadHandle = new Handler(){
		@Override
		public void handleMessage(Message msg) {		 
			String uploadResponse = (String) msg.obj;			
			Toast.makeText(getApplicationContext(), "The Upload Response was : " + uploadResponse + ".", Toast.LENGTH_LONG).show();	            			
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
		fileSearch = (Button) findViewById(R.id.btn_select_file);
		sendFile = (Button) findViewById(R.id.btn_sendFile);
		friendSearch = (Button) findViewById(R.id.btn_select_friend);
		response = (TextView) findViewById(R.id.textView_response);
		
		//hide the listviews
		friendListview.setVisibility(View.GONE);
		listview.setVisibility(View.GONE);
		
		Intent prevIntent = getIntent();
		userName = prevIntent.getStringExtra("username");
		userID = android.os.Build.SERIAL;
		String welcomeMessage = "Welcome "
				+ prevIntent.getStringExtra("username") + "!";

		txtUserMessage.setText(welcomeMessage);

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
				
	         
		         try {
		        	 //This is to create a test file to be encrypted in case there are none in the application directory
						final String fileName = /*uri.getQueryParameter("filename")*/ "test" + ".txt";
				         File f;
				         //FileOutputStream fileOut = new FileOutputStream( f = new File (eftDirectory, fileName));
				         FileOutputStream fileOut = new FileOutputStream( f = new File (storageDirectory, fileName));
				         System.out.println(f.getAbsolutePath());
				         //ObjectOutputStream out = new ObjectOutputStream(fileOut);
				         String tst = ("This is a test file. If you can read this then the file is not encrypted.");
				         fileOut.write(tst.getBytes());
				         fileOut.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		         //out.writeObject(tst);
		         //out.close();
		         
				
				
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
				

			    friendAdapter = new ExtendedArrayAdapter(getApplicationContext(), R.layout.simple_list_item_1, friendList);
			    friendListview.setAdapter(friendAdapter);
			    friendListview.setVisibility(View.VISIBLE);
			    
			    friendListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			      @Override
			      public void onItemClick(AdapterView<?> parent, final View view,
			          int position, long id) {
			        final String friendItem = (String) parent.getItemAtPosition(position);
			        txtFriend.setText("You have selected " + friendItem + " as the recipient.");
			        selectedUser = friendItem;
			        System.out.println(friendItem + " was clicked");
			        friendListview.setVisibility(View.GONE);
			      }

			    });
			
			}
		});

		sendFile.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				//response = (TextView) findViewById(R.id.textView_response);

				try {
					if(selectedUser != "" && selectedUser != null && selectedFile != null && selectedFile != ""){
					response.setText("");
					aesCipher = Cipher.getInstance("AES");
					encryptfile();
					}
					else
					{
						response.setText("Please be sure to select a recipient and file to be sent.");
					}
				} catch (NoSuchAlgorithmException | NoSuchPaddingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
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
        if(selectedUser != "" && selectedUser != null && selectedFile != null && selectedFile != ""){
		idTask.execute(null, null, null);
		if(selectedUserID != "" && selectedUserID != null){
		try
	      { 		
			File storageDirectory = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name));			
			storageDirectory.mkdir();		
			
			 //File f = new File(eftDirectory + "/test.txt");  
	           
	         final String fileName = /*uri.getQueryParameter("filename")*/ "test" + ".txt";
	         File f;
	         //FileOutputStream fileOut = new FileOutputStream( f = new File (eftDirectory, fileName));
	         FileOutputStream fileOut = new FileOutputStream( f = new File (storageDirectory, fileName));
	         System.out.println(f.getAbsolutePath());
	         //ObjectOutputStream out = new ObjectOutputStream(fileOut);
	         String tst = ("This is a test file. If you can read this then the file is not encrypted.");	         
	         fileOut.write(tst.getBytes());
	         //out.writeObject(tst);
	         //out.close();
	         fileOut.close();
	         System.out.println("Serialized data is saved in: " + storageDirectory.toString());
	         
	         //Unencrypted file has been stored
	         String UUID = android.os.Build.SERIAL;  	         
   	         String str1 = "0x" + UUID;
		     System.out.println("String UUID: " + UUID);	
	         System.out.println("Int UUID: " + Long.decode(str1));
	         
	         //Retrieve other users serial
	         String UUID_2 = selectedUserID;
	         String str2 = "0x" + UUID_2;
	         System.out.println("String UUID_2: " + UUID_2);
	         System.out.println("Int UUID_2: " + Long.decode(str2));
	         

   	         
             //Long.decode(str1);
   	         
   	         //Algorithm
   	         //long combo = (Long.parseLong(android.os.Build.SERIAL, 16) * Long.parseLong(UUID_2, 16));
   	         long combo = (Long.decode(str1) *  Long.decode(str2));
   	         long x = Long.parseLong("1000000000000");
   	         while (combo < 0)
   	         {
   	        	 combo = (Long.decode(str1) *  Long.decode(str2)) - x;
   	        	 x = x + Long.parseLong("1000000000000");
   	         }
   	         long combo2 = (Long.decode(str1) *  Long.decode(str2));
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
	         
	         final String decFileName = fileName;
 	         File decrypFile = new File (storageDirectory, decFileName);
	         
	         final String encFileName = "(Encrypted)" + fileName;
 	         File encFile = new File (storageDirectory, encFileName);
	         
	         //final String encFileName = /*uri.getQueryParameter("filename")*/ "test_copy" + ".txt";
	         //File c = new File (storageDirectory, encFileName);
	         
	         //final String decFileName = /*uri.getQueryParameter("filename")*/ "test_dec_copy" + ".txt";
	         //File d = new File (storageDirectory, decFileName);
	         
	         //Select UUID from Associated User where Username = friendName
	         
	         //getID(getApplicationContext(), selectedUser);
	         selectedUser.toString();
	         
	         encrypt(f, encFile, secretKeySpec);	 
	         
	         //sendfile
	         encFile.getPath().toString();
	         
	         //int temp = selectedFile.split("/").length;
	         int temp = encFile.getPath().toString().split("/").length;
         	 String tempFileName = encFile.getPath().toString().split("/")[(temp - 1)];
         	 String tempFilePath = "";
         	 for(int i = 0; i < (temp - 1); i++)
         	 {
         		tempFilePath = tempFilePath + "/" + encFile.getPath().toString().split("/")[(i)];
         		 
         	 }
	         	//upload file
	           uploadFile(encFile.getPath().toString(), tempFilePath, tempFileName);
	         
	         	//store filepath
	            fileUrl = "http://cis-linux2.temple.edu/~tud30441/uploads/" + tempFileName;
	            //selectedFile = "http://cis-linux2.temple.edu/~tud30441/uploads/testFile.txt";
	         	//send GCM notification
				//brandon
	            
		        notificationTask = new AsyncTask<Void, Void, Void>() {
		            @Override
		            protected Void doInBackground(Void... params) {              
		            	Message msg = notificationHandle.obtainMessage();
		            	String message = sendDownloadNotification(getApplicationContext(), userName, selectedUser, fileUrl);
		            	//String message = logIn(getApplicationContext(), txtUsername.getText().toString(), txtPassword.getText().toString());
		            	Log.e(TAG, message);
		            	msg.obj = message;                	
						notificationHandle.sendMessage(msg);
		                return null;
		            }
		            @Override
		            protected void onPostExecute(Void result) {
		            	//brandon - Because AsyncTasks can only be used once
		            	notificationTask = null;
		            }
		        };
		        
		        notificationTask.execute(null, null, null);
	         
	         //decrypt(c, d, secretKeySpec);
	         
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
		
		File storageDirectory = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name));			
		
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
	  
	  public int uploadFile(final String sourceFileUri, final String uploadFilePath, final String uploadFileName) {
	        

	        final File sourceFile = new File(sourceFileUri);
	         
	        if (!sourceFile.isFile()) {
	             
	             //dialog.dismiss();
	              
	             Log.e("uploadFile", "Source File not exist :"
	                                 +uploadFilePath + "" + uploadFileName);
	              
	             /*runOnUiThread(new Runnable() {
	                 public void run() {
	                     messageText.setText("Source File not exist :"
	                             +uploadFilePath + "" + uploadFileName);
	                 }
	             });***/
	              
	             return 0;
	          
	        }
	        else
	        {
	 				//brandon
	 		        uploadTask = new AsyncTask<Void, Void, Void>() {
	 		            @Override
	 		            protected Void doInBackground(Void... params) {              
	 		            	Message msg = uploadHandle.obtainMessage();
	 		            	               	
//	 						uploadHandle.sendMessage(msg);
//	 		                return null;
//	 		            }
//	 		            @Override
//	 		            protected void onPostExecute(Void result) {
//	 		            	//brandon - Because AsyncTasks can only be used once
//	 		                uploadTask = null;
//	 		            }
//	 		        };
	 		        
//	 				uploadTask.execute(null, null, null);
	 		            	 try {
	 			                  
	                   // open a URL connection to the Servlet
	                 FileInputStream fileInputStream = new FileInputStream(sourceFile);
	                 URL url = new URL(upLoadServerUri);
	                  
	                 // Open a HTTP  connection to  the URL
	                 conn = (HttpURLConnection) url.openConnection();
	                 conn.setDoInput(true); // Allow Inputs
	                 conn.setDoOutput(true); // Allow Outputs
	                 conn.setUseCaches(false); // Don't use a Cached Copy
	                 conn.setRequestMethod("POST");
	                 conn.setRequestProperty("Connection", "Keep-Alive");
	                 conn.setRequestProperty("ENCTYPE", "multipart/form-data");
	                 conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
	                 conn.setRequestProperty("uploaded_file", sourceFileUri);
	                  
	                 dos = new DataOutputStream(conn.getOutputStream());
	        
	                 dos.writeBytes(twoHyphens + boundary + lineEnd);
	                 dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\"; filename=\"" + sourceFileUri + "\"" + lineEnd);
	                  
	                 dos.writeBytes(lineEnd);
	        
	                 // create a buffer of  maximum size
	                 bytesAvailable = fileInputStream.available();
	        
	                 bufferSize = Math.min(bytesAvailable, maxBufferSize);
	                 buffer = new byte[bufferSize];
	        
	                 // read file and write it into form...
	                 bytesRead = fileInputStream.read(buffer, 0, bufferSize); 
	                    
	                 while (bytesRead > 0) {
	                      
	                   dos.write(buffer, 0, bufferSize);
	                   bytesAvailable = fileInputStream.available();
	                   bufferSize = Math.min(bytesAvailable, maxBufferSize);
	                   bytesRead = fileInputStream.read(buffer, 0, bufferSize);  
	                    
	                  }
	        
	                 // send multipart form data necesssary after file data...
	                 dos.writeBytes(lineEnd);
	                 dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	        
	                 // Responses from the server (code and message)
	                 serverResponseCode = conn.getResponseCode();
	                 String serverResponseMessage = conn.getResponseMessage();
	                   
	                 Log.i("uploadFile", "HTTP Response is : "
	                         + serverResponseMessage + ": " + serverResponseCode);
	                  
//	                 if(serverResponseCode == 200){
//	                      
//	                     runOnUiThread(new Runnable() {
//	                          public void run() {
//	                               
//	                              String msg = "File Upload Completed.\n\n"
//	                                            +" http://cis-linux2.temple.edu/~tud30441//uploads/"
//	                                            +uploadFileName;
//	                               
//	                              //messageText.setText(msg);
//	                              Toast.makeText(SendFile.this, "File Upload Complete.",
//	                                           Toast.LENGTH_SHORT).show();
//	                          }
//	                      });               
//	                 }   
	                  
	                 //close the streams //
	                 fileInputStream.close();
	                 dos.flush();
	                 dos.close();
	                 

	            } catch (MalformedURLException ex) {
	                 
	                //dialog.dismiss(); 
	                ex.printStackTrace();
	                 
	                runOnUiThread(new Runnable() {
	                    public void run() {
	                        //messageText.setText("MalformedURLException Exception : check script url.");
	                        Toast.makeText(SendFile.this, "MalformedURLException",
	                                                            Toast.LENGTH_SHORT).show();
	                    }
	                });
	                 
	                Log.e("Upload file to server", "error: " + ex.getMessage(), ex); 
	            } catch (Exception e) {
	                 
	                //dialog.dismiss(); 
	                e.printStackTrace();
	                 
	                runOnUiThread(new Runnable() {
	                    public void run() {
	                        //messageText.setText("Got Exception : see logcat ");
	                        Toast.makeText(SendFile.this, "Got Exception : see logcat ",
	                                Toast.LENGTH_SHORT).show();
	                    }
	                });
	                Log.e("Upload file to server Exception", "Exception : "
	                                                 + e.getMessage(), e); 
	            }
	 		            	 
	 						uploadHandle.sendMessage(msg);
	 		                return null;
	 		            }
	 		            @Override
	 		            protected void onPostExecute(Void result) {
	 		            	//brandon - Because AsyncTasks can only be used once
	 		                uploadTask = null;
	 		            }
	 		        };
	 		        
	 				uploadTask.execute(null, null, null);
	            //dialog.dismiss();      
	            return serverResponseCode;
	             
	            
	            
	            
	         } // End else block
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



