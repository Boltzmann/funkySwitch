package com.grunzphi.funkySwitch;

import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.content.Context.*;

import com.grunzphi.R;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * Handles ssh-connection.
 */
class open_close_ssh extends AsyncTask<String, Void, String> {
	
	String filePath = "";
	String userName = "";
	String host = "";
	
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
	 * Setter for filePath of private key for ssh connection.
	 */
	public void setFilePath(String tmp){
		filePath = tmp;
	}
	
	/**
	 * Setter for host name of ssh server.
	 */
	public void setHost(String tmp){
		host = tmp;
	}
	
	/**
	 * Setter for user name for ssh connection.
	 */
	public void setUser(String tmp){
		userName = tmp;
	}
	
	/**
	 * Method is executed when open_close_ssh().execute(...) is called.
	 * open_close_ssh().execute(String UserAuthPubKey, String filePath, 
	 * 											String Host, String userName)
	 */
	protected String doInBackground(String... commands) {
		setFilePath(commands[1]);
		setHost(commands[2]);
		setUser(commands[3]);
		return UserAuthPubKey(commands[0]);
	}

	/**
	 * Method is executed after open_close_ssh().execute(...) was called.
	 */
	protected void onPostExecute(String abc) {
		System.out.println(abc);
	}
}

