package com.grunzphi.funkySwitch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore.Files;
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
	 * Create a file on host to test whether the app works.
	 * @param view The current view.
	 */
	public void createTestFile(View view) {
		if (isOnline()) {
			setHostByPrefs();
			setUserByPrefs();
			new open_close_ssh().execute("touch test.txt", filePath, host, userName);
			write2crontab testCron = new write2crontab();
			new open_close_ssh().execute(testCron.getCommand2AddCmd2Cron(), 
					filePath, host, userName);
			if (BuildConfig.DEBUG) {
				Log.e(Constants.LOG, "testFile created.");
			} 
		} else {
			Log.e(Constants.LOG, "You are not online.");
			System.err.println("You are not online.");
			showToast("You are not online.");
		}
	}

	
	/**
	 * Switches object on or off, depending on the given state. Displays an error
	 * message, if there is no Internet connection.
	 * @param state: on or off
	 * @param device: the device to be switched on or off
	 */
	private void switchOverSSH(String state, String device) {
		cmdOverSSH("switch " + state + " " + device);
	}
	
	private void cmdOverSSH(String cmd){
		if (isOnline()) {
			setHostByPrefs();
			setUserByPrefs();
			if (BuildConfig.DEBUG) {
				Log.e(Constants.LOG, "cmdOverSSH called");
			} 
			new open_close_ssh().execute(cmd, filePath, host, userName);
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
	 * Sends commands to use with ssh.
	 * @param view The current view, here the button pressed.
	 */
	public void simpleSendCmd(View view) {
		String tag = view.getTag().toString();
		cmdOverSSH(tag);
	}	
	
	/**
	 * Opens file dialog to choose RSA key???
	 * @param view The current view, here the button pressed.
	 */
	public void activity_simple_open(View view){
		// see http://stackoverflow.com/questions/29425408/local-file-access-on-google-chrome-arc/29426331#29426331
	    Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
	    intent.addCategory(Intent.CATEGORY_OPENABLE);	
	    intent.setType("*/*");
	    startActivityForResult(intent, 0);
	}

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
		case R.id.action_settings:
			Intent i = new Intent(this, UserSettingActivity.class);
			startActivityForResult(i, RESULT_SETTINGS);
			break;
		default: break;
		}
		return true;
	}


	public void throwErrorMessage(String msg){
		Context context = getApplicationContext();
		CharSequence text = msg;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			if(resultCode==RESULT_OK){
				Uri uri = data.getData();
				if (isExternalStorageDocument(uri)) {
		            final String docId = DocumentsContract.getDocumentId(uri);
		            final String[] split = docId.split(":");
		            final String type = split[0];
		            if ("primary".equalsIgnoreCase(type)) {
		                filePath = Environment.getExternalStorageDirectory() + "/" + split[1];
		            }

		            // TODO handle non-primary volumes
		        }
				else{
					showToast("Result was not OK.");
				}
			}
//			showToast(filePath);
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
		showToast(host);
	}
	
	private void setUserByPrefs(){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		userName=sharedPref.getString(getString(R.string.userName), "not readable");
		showToast(userName);
	}
	
	protected void showToast(String message){
		Toast toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG);
		toast.show();
	}
	
	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	private boolean isExternalStorageDocument(Uri uri) {
	    return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}
}
