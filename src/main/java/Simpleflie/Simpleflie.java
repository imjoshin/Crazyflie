package Simpleflie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import se.bitcraze.crazyflie.lib.crazyflie.ConnectionAdapter;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.Crazyradio;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.CommanderPacket;
import se.bitcraze.crazyflie.lib.log.LogConfig;
import se.bitcraze.crazyflie.lib.log.LogListener;
import se.bitcraze.crazyflie.lib.log.Logg;
import se.bitcraze.crazyflie.lib.param.Param;
import se.bitcraze.crazyflie.lib.param.ParamListener;
import se.bitcraze.crazyflie.lib.toc.Toc;
import se.bitcraze.crazyflie.lib.toc.VariableType;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

public class Simpleflie {

	private Crazyflie mCrazyflie;
	private long thrust = 0;		
	private float pitch = 0;
	private float roll = 0;
	private float yaw = 0;
	private int port;

	private Logg logg;
	private LogConfig logConfig;
	
	private boolean showPackets = false;
	private int failSafe = 30;
	
	private boolean safetyTrigger = true;
	private Map<String, Number> params = new HashMap<String, Number>();
	
	/**
	 * Creates a new Simpleflie object and connects to the first Crazyflie
	 * interface port found. This will wait until it connects successfully,
	 * unless no Crazyflie interfaces were found. 
	 * 
	 * @author Josh Johnson (jocjohns@mtu.edu)
	 */
	public Simpleflie(){
		// Scan for Crazyflies and use the first one found
		System.out.println("Scanning interfaces for Crazyflies...");

		RadioDriver radioDriver = new RadioDriver(new UsbLinkJava());
		List<ConnectionData> foundCrazyflies = radioDriver.scanInterface();
		radioDriver.disconnect();

		System.out.println("Crazyflies found:");
		for (ConnectionData connectionData : foundCrazyflies) {
			System.out.println(connectionData);
		}
		
		if(foundCrazyflies.size() > 0){
			init(foundCrazyflies.get(0).getChannel());
		}else{
			System.out.println("No Crazyflies found.");
		}
	}
	
	/**
	 * Creates a new Simpleflie object and connects to the port specified. 
	 * This will wait until it connects successfully. The commonly used port
	 * is port 80.
	 *
	 * @param channel an integer specifying which port to connect to
	 * @author Josh Johnson (jocjohns@mtu.edu)
	 */
	public Simpleflie(int channel){
		init(channel);
	}
	
	private void init(int channel){
		this.port = channel;
		mCrazyflie = new Crazyflie(new RadioDriver(new UsbLinkJava()));
		
		
		mCrazyflie.getDriver().addConnectionListener(new ConnectionAdapter() {
			
			// This callback is called form the Crazyflie API when a Crazyflie has been connected 
			// and the TOCs have been downloaded.
			public void setupFinished(String connectionInfo){
				logConfig = new LogConfig("stabalizer", 10);
				logConfig.addVariable("stabilizer.pitch", VariableType.FLOAT);
				logConfig.addVariable("stabilizer.roll", VariableType.FLOAT);
				logConfig.addVariable("stabilizer.yaw", VariableType.FLOAT);
				logConfig.addVariable("gyro.x", VariableType.FLOAT);
				logConfig.addVariable("gyro.y", VariableType.FLOAT);
				logConfig.addVariable("gyro.z", VariableType.FLOAT);
				
		        logg = mCrazyflie.getLogg();

		        if (logg != null) {
		            logg.addConfig(logConfig);

		            logg.addLogListener(new LogListener() {

		                public void logConfigAdded(LogConfig logConfig) {
		                    String msg = "";
		                    if(logConfig.isAdded()) {
		                        msg = "' added";
		                    } else {
		                        msg = "' deleted";
		                    }
		                    //System.out.println("LogConfig '" + logConfig.getName() + msg);
		                }

		                public void logConfigError(LogConfig logConfig) {
		                    System.err.println("Error when logging '" + logConfig.getName() + "': " + logConfig.getErrNo());
		                }

		                public void logConfigStarted(LogConfig logConfig) {
		                    String msg = "";
		                    if(logConfig.isStarted()) {
		                        msg = "' started";
		                    } else {
		                        msg = "' stopped";
		                    }
		                    //System.out.println("LogConfig '" + logConfig.getName() + msg);
		                }

		                public void logDataReceived(LogConfig logConfig, Map<String, Number> data, int timestamp) {
	                    	params.putAll(data);
		                }

		            });

		            // Start the logging
		            logg.start(logConfig);
		        }else{
		        	System.out.println("Logg was null");
		        }
			}
			
			// This callback is called from the Crazyflie API when a Crazyflie has been connected 
			// and the TOCs have been downloaded.
			public void connected(String connectionInfo) {
				System.out.println("CONNECTED to " +  connectionInfo);
			}

			//Callback when the Crazyflie is disconnected (called in all cases)
			public void disconnected(String connectionInfo) {
				System.out.println("DISCONNECTED from " +  connectionInfo);
			}

			//Callback when connection initial connection fails (i.e no Crazyflie at the specified address)
			public void connectionFailed(String connectionInfo, String msg) {
				System.out.println("CONNECTION FAILED: " +  connectionInfo + " Msg: " + msg);
			}

			// Callback when disconnected after a connection has been made (i.e Crazyflie moves out of range)
			public void connectionLost(String connectionInfo) {
				System.out.println("CONNECTION LOST: " +  connectionInfo);
			}

		});

		ConnectionData connectionData = new ConnectionData(channel, Crazyradio.DR_250KPS);        
		mCrazyflie.connect(connectionData);

		System.out.println("CONNECTING TO " + connectionData);
		
		while(!isConnected()); 
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Disconnects the Crazyflie and deletes all necessary logs.
	 */
	public void disconnect(){
		mCrazyflie.disconnect();
		logg.stop(logConfig);
        logg.delete(logConfig);
	}
	
	/**
	 * Returns a Number object from the given parameter name.
	 * If the parameter name is not valid, null is returned.
	 * <p>
	 * Logged parameters include pitch, roll, yaw, gyro.x,
	 * gryo.y, and gyro.z
	 * 
	 * @param paramName the name of the parameter to obtain
	 * @returns a Number object with representing the paramName
	 */
	public Number getValue(String paramName){
		if(paramName.compareTo("pitch") == 0 || paramName.compareTo("roll") == 0 || paramName.compareTo("yaw") == 0){
			paramName = "stabilizer." + paramName;
		}
		
		if(params.containsKey(paramName))
			return params.get(paramName);
		else{
			System.err.println("Invalid value name: " + paramName);
			return null;
		}
	}
	
	/**
	 * Sets multiple values of the Crazyflie. Valid parameters are
	 * pitch, roll, yaw, and thrust. When calling setValues(), all
	 * parameters must be strings (including values), with the values
	 * being supplied after the parameter name. Not all parameters are
	 * needed to be set on each call.
	 * <p>
	 * Example usage: drone.setValues("thrust", "" + 15000, "pitch", "" + 2.5, "roll", "" + 1.3);
	 * <p>
	 * setValues() will pass a thrust of 0 to the Crazyflie if the pitch 
	 * or roll surpasses the fail safe ({@link #setFailSafe}).
	 * 
	 * @param args parameter names and values
	 */
	public void setValues(String ... args){
		if(!mCrazyflie.isConnected()) {System.out.println("NOT CONNECTED"); return;}
		
		for(int i = 0; i < args.length; i += 2){
			String param = args[i];
			String value = args[i + 1];
			if(param.toLowerCase().compareTo("thrust") == 0){
				if(value.charAt(0) == '+'){
					thrust += Long.parseLong(value.substring(1));
				}else if(value.charAt(0) == '-'){
					thrust -= Long.parseLong(value.substring(1));
				}else{
					thrust = Long.parseLong(value);
				}
			}else if(param.toLowerCase().compareTo("pitch") == 0){
				if(value.charAt(0) == '+'){
					pitch = getValue("pitch").floatValue() + Float.parseFloat(value.substring(1));
				}else if(value.charAt(0) == '-'){
					pitch = getValue("pitch").floatValue() + Float.parseFloat(value.substring(1));
				}else{
					pitch = Float.parseFloat(value);
				}
			}else if(param.toLowerCase().compareTo("roll") == 0){
				if(value.charAt(0) == '+'){
					roll = getValue("roll").floatValue() + Float.parseFloat(value.substring(1));
				}else if(value.charAt(0) == '-'){
					roll = getValue("roll").floatValue() - Float.parseFloat(value.substring(1));
				}else{
					roll = Float.parseFloat(value);
				}
			}else if(param.toLowerCase().compareTo("yaw") == 0){
				if(value.charAt(0) == '+'){
					yaw = getValue("yaw").floatValue() + Float.parseFloat(value.substring(1));
				}else if(value.charAt(0) == '-'){
					yaw = getValue("yaw").floatValue() - Float.parseFloat(value.substring(1));
				}else{
					yaw = Float.parseFloat(value);
				}
			}else{
				System.err.println("Invalid value name: " + param);
				return;
			}
		}
		
		if(failSafe != -1 && safetyTrigger && (Math.abs(pitch) > failSafe || Math.abs(roll) > failSafe)){
			thrust = 0;
			pitch = 0;
			roll = 0;
		}
		
		if(showPackets) System.out.println("Sending packet...\troll: " + roll + "\tpitch: " + pitch + "\tyaw: " + yaw + "\tthrust: " + thrust);
		mCrazyflie.sendPacket(new CommanderPacket(roll, pitch, yaw, (char) thrust));
		
	}
	
	/**
	 * Determines if the Simpleflie object is connected to
	 * a Crazyflie.
	 * 
	 * @returns a boolean stating if the Crazyflie is connected
	 */
	public boolean isConnected(){
		return mCrazyflie != null && mCrazyflie.isConnected();
	}
	
	/**
	 * Set whether or not packets sent to the Crazyflie should
	 * be shown in the terminal. This is mostly for debugging.
	 * 
	 * @param showPackets whether or not packets are printed to the terminal
	 */
	public void setShowPackets(boolean showPackets){
		this.showPackets = showPackets;
	}
	
	/**
	 * Set the fail safe of the Crazyflie. If the absolute value of the 
	 * pitch or roll surpasses the fail safe, a thrust of 0 will be sent 
	 * on each {@link setValues} until it is safe. Setting the fail safe 
	 * to -1 will disable this check.
	 * <p>
	 * Default value is 30.
	 * 
	 * @param failSafe the fail safe amount
	 */
	public void setFailSafe(int failSafe){
		this.failSafe = failSafe;
	}
	
	/**
	 * Returns a string representation of the Simpleflie object.
	 * This is represented by an output of all parameters being
	 * logged, as well as the port of the Crazyflie.
	 */
	public String toString(){
		String ret = "port: " + port + "\n";
		for (Entry<String, Number> entry : params.entrySet()) {
            ret += entry.getKey() + ": " + entry.getValue() + "\n";
        }
		return ret;
	}

}
