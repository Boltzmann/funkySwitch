/**
 * stefan.bollmann@rwth-aachen.de 2015
 */
package com.grunzphi.funkySwitch;

/**
 * Edit super user crontab per console commands.
 * @author stefan
 *
 */
public class write2crontab {
	
	private String timeOfDay;
	private String theseDaysOfWeek;
	private String command;
	private String cronLine;
	
	public write2crontab(){}
	public write2crontab(String time, String DoW, String cmd){
		setTimeOfDay(time);
		setTheseDaysOfWeek(DoW);
		setCommand(cmd);
	}
	/**
	 * cronLine is the line that is added to crontab.
	 * TODO Change this test to implementation.
	 */
	public String getCommand2AddCmd2Cron(){
//		return "{echo \"0 0 0 0 0 some entry\"; } | touch -";
		return "sudo crontab -l | { cat; echo '* * * * * switch off N8Lamp; echo 'did it' >> /tmp/N8Lamp.log'; } | sudo crontab";
	}
	/**
	 * Sets timeOfDay to a String that can be written to crontab.
	 */
	public void setTimeOfDay(String time){
		timeOfDay = time;
	}
	/**
	 * Sets theseDaysOfWeek to a String that can be written to crontab.
	 */
	public void setTheseDaysOfWeek(String DoW){
		theseDaysOfWeek = DoW;
	}
	/**
	 * Sets the command to be used for crontab execution. 
	 */
	public void setCommand(String cmd){
		command = cmd;
	}
}
