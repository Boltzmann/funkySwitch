package com.grunzphi.funkySwitch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.ImageView;
//import com.jcraft.jsch.*;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

import com.grunzphi.BuildConfig;
import com.grunzphi.R;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import android.app.ActionBar;
import android.app.Fragment;

public class MainActivity extends Activity {

	public static final String PREFS_NAME = "funkyPrefs";
	String host;
	String userName;
	
	public interface Constants {
		String LOG = "com.grunzphi.funkySwitch";
	} 

	private static final int RESULT_SETTINGS = 1;
	
	private String filePath;
	
	// Declare Tab Variables
	ActionBar.Tab Tab1, Tab2, Tab3;
	Fragment fragmentTab1 = new FragmentTab1();
	Fragment fragmentTab2 = new FragmentTab2();
	Fragment fragmentTab3 = new FragmentTab3();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ActionBar actionBar = getActionBar();

		// Restore preferences
		SharedPreferences settings = getPreferences(0);
		filePath = settings.getString(getString(R.string.prefKey_privKeyFilePath), "");
		
		// Uncomment, if you want to show actionbar Icon
		// actionBar.setDisplayShowHomeEnabled(true);

		// Set actionbar Title
		actionBar.setTitle("Funky Switch");

		// Create actionbar Tabs
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Set Tab Icon and Titles
		Tab1 = actionBar.newTab().setText("Lights");
		Tab2 = actionBar.newTab().setText("VIBs");
		Tab3 = actionBar.newTab().setText("Specials");

		// Set Tab Listeners
		Tab1.setTabListener(new TabListener(fragmentTab1));
		Tab2.setTabListener(new TabListener(fragmentTab2));
		Tab3.setTabListener(new TabListener(fragmentTab3));

		// Add tabs to action bar
		actionBar.addTab(Tab1);
		actionBar.addTab(Tab2);
		actionBar.addTab(Tab3);

		// sets background color of the action bar
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ff8c00")));
		actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#edd309")));
	}

	/**
	 * To test Internet connection.
	 */
	private boolean isOnline() {
		ConnectivityManager cm =
				(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}

	/**
	 * Handles ssh-connection.
	 */
	private class open_close_ssh extends AsyncTask<String, Void, String> {
		/**
		 * @param switchCommand The command that should be executed.
		 * @return The command that was executed or an error message that the connection cannot be established.
		 * TODO change parameter to String... so that several commands can be executed in one ssh-session.
		 */
		private String UserAuthPubKey(String switchCommand) {
			try {
				JSch jsch = new JSch();

				//TODO implement user settings edit text box to set user name.
				int port = 22;

				String privateKey = filePath; 
				//TODO System.out to logger
				System.out.println(privateKey);

				jsch.addIdentity(privateKey);
				//TODO System.out to logger
				System.out.println("identity added ");
				
				System.out.println(userName);
				Session session = jsch.getSession(userName, host, port);
				//TODO System.out to logger
				System.out.println("session created.");

				java.util.Properties config = new java.util.Properties();
				config.put("StrictHostKeyChecking", "no");
				session.setConfig(config);

				session.connect();
				// SSH Channel
				ChannelExec channelssh = (ChannelExec)
						session.openChannel("exec");
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				channelssh.setOutputStream(baos);

				// Execute command
				// TODO for each loop to exec more commands.
				channelssh.setCommand(switchCommand);
				channelssh.connect();
				channelssh.disconnect();
				session.disconnect();
				return switchCommand + " was executed.";
			} catch (Exception e) {
				System.err.println(e);
				e.printStackTrace();
				return "No ssh connection possible.";
			}
		}

		/**
		 * Method is executed when open_close_ssh().execute(...) is called.
		 */
		protected String doInBackground(String... commands) {
			return UserAuthPubKey(commands[0]);
		}

		/**
		 * Method is executed after open_close_ssh().execute(...) was called.
		 */
		protected void onPostExecute(String abc) {
			System.out.println(abc);
		}
	}


	/**
	 * Create a file on host to test whether the app works.
	 * @param view The current view.
	 */
	public void createTestFile(View view) {
		if (isOnline()) {
			new open_close_ssh().execute("touch test.txt");
			if (BuildConfig.DEBUG) {
				Log.e(Constants.LOG, "testFile created.");
			} 
		} else System.err.println("You are not online.");
	}

	
	/**
	 * Switches object on or off, depending on the given state. Displays an error
	 * message, if there is no Internet connection.
	 * @param state: on or off
	 * @param device: the device to be switched on or off
	 */
	private void switchOverSSH(String state, String device) {
		if (isOnline()) {
			setHostByPrefs();
			setUserByPrefs();
			if (BuildConfig.DEBUG) {
				Log.e(Constants.LOG, "switchOverSSH called");
			} 
			new open_close_ssh().execute("switch " + state + " " + device);
			Log.e(Constants.LOG, "switch " + state + " " + device);
		} else {
			System.err.println("You are not online.");
			throwErrorMessage("You are not online.");
		}
	}

	/**
	 * Switches device(s) on or off, depending on the button pressed.
	 * @param view The current view, here the button pressed.
	 */
	public void sendMessage(View view) {
		String tag = view.getTag().toString();
		String[] devices = tag.split(",");
		String state = devices[0];
		for (int i = 1; i < devices.length; i++) {
			switchOverSSH(state,devices[i]);
		}
	}


	/**
	 * Opens file dialog to choose RSA key???
	 * @param view The current view, here the button pressed.
	 */
	public void activity_simple_open(View view){
		// see http://stackoverflow.com/questions/29425408/local-file-access-on-google-chrome-arc/29426331#29426331
	    Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
	    intent.setType("text/");
	    startActivityForResult(intent, 0);
//		Intent intent = new Intent(this, FileChooserActivity.class);
//		this.startActivityForResult(intent, 0);
	}


	private void activity_android_open(View view) {
	    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
	    intent.addCategory(Intent.CATEGORY_OPENABLE);	
	    intent.setType("*/*");
	    startActivityForResult(intent, 0);
	}
	
//	/**
//	 * Import RSA key by fileChooserDialog.
//	 */
//	public void importKey(View view) {
//		// Create the dialog.
//		FileChooserDialog dialog = new FileChooserDialog(MainActivity.this);
//
//		// Show the dialog.
//		dialog.show();
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
//		case R.id.action_addButton:
//			addButton();
//			break;
		case R.id.action_settings:
			Intent i = new Intent(this, UserSettingActivity.class);
			startActivityForResult(i, RESULT_SETTINGS);
			break;
		default: break;
		}
		return true;
	}

//	TODO Add button permanently. 
//	TODO Assign action to new button.
//	TODO Match button with layout - put it in the right place, in the right colors.
//	public void addButton(View view) {
//		//the layout on which you are working
//		GridLayout layout = (GridLayout) findViewById(R.id.fragmenttab3_layout);
//
//		//set the properties for button
//		Button btnTag = new Button(this);
//		btnTag.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//		btnTag.setText("Not permanent.");
//		btnTag.setId(8);
//
//		//add button to the layout
//		layout.addView(btnTag);
//	}

	public void throwErrorMessage(String msg){
		Context context = getApplicationContext();
		CharSequence text = msg;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//	    super.onActivityResult(requestCode, resultCode, data);
//	    if (resultCode == Activity.RESULT_OK && requestCode == 5) {
//	        ImageView imgView = new ImageView(this);
//	        imgView.setImageURI(data.getData());
//	        setContentView(imgView);
//	    }    
//	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		if (resultCode == Activity.RESULT_OK) {
//			filePath = "";
//
//			Bundle bundle = data.getExtras();
//			if(bundle != null)
//			{
//				if(bundle.containsKey(FileChooserActivity.OUTPUT_NEW_FILE_NAME)) {
//					File folder = (File) bundle.get(FileChooserActivity.OUTPUT_FILE_OBJECT);
//					String name = bundle.getString(FileChooserActivity.OUTPUT_NEW_FILE_NAME);
//					filePath = folder.getAbsolutePath() + "/" + name;
//				} else {
//					File file = (File) bundle.get(FileChooserActivity.OUTPUT_FILE_OBJECT);
//					filePath = file.getAbsolutePath();
//				}
//			}
//
//			// We need an Editor object to make preference changes.
//			// All objects are from android.context.Context
//			SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
		//			SharedPreferences.Editor editor = settings.edit();
		//			editor.putString(getString(R.string.prefKey_privKeyFilePath), filePath);
		//			editor.commit();
		//		}
		// TODO Auto-generated method stub
		showToast("DONE");
			if(resultCode==RESULT_OK){
				filePath = data.getData().getPath();
			}
			// We need an Editor object to make preference changes.
			// All objects are from android.context.Context
			SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(getString(R.string.prefKey_privKeyFilePath), filePath);
			editor.commit();
	}

	private void setHostByPrefs(){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		host=sharedPref.getString(getString(R.string.keyHost),"not readable");
	}
	
	private void setUserByPrefs(){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		userName=sharedPref.getString(getString(R.string.userName), "not readable");
	}
	
	private void showToast(String message){
		Toast toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG);
		toast.show();
	}
}
