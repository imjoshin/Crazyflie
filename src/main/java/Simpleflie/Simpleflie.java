package Simpleflie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.bitcraze.crazyflie.lib.crazyflie.ConnectionAdapter;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.Crazyradio;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.CommanderPacket;
import se.bitcraze.crazyflie.lib.param.Param;
import se.bitcraze.crazyflie.lib.param.ParamListener;
import se.bitcraze.crazyflie.lib.toc.Toc;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

public class Simpleflie {

	private Crazyflie mCrazyflie;
	private long thrust = 0;
	private float pitch = 0;
	private float roll = 0;
	private float yaw = 0;

    List<String> mParamCheckList = new ArrayList<String>();
    List<String> mParamGroups = new ArrayList<String>();
	
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

	public Simpleflie(int channel){
		init(channel);
	}
	
	private void init(int channel){
		mCrazyflie = new Crazyflie(new RadioDriver(new UsbLinkJava()));
		mCrazyflie.getDriver().addConnectionListener(new ConnectionAdapter() {
			/*
			 * This callback is called from the Crazyflie API when a Crazyflie
			 * has been connected and the TOCs have been downloaded.
			 */
			public void connected(String connectionInfo) {
				System.out.println("CONNECTED to " +  connectionInfo);

				// Start a separate thread to do the motor test.
				// Do not hijack the calling thread!
				//                Thread(target=self._ramp_motors).start()
				//DO WHAT NOW
			}

			/*
			 * Callback when the Crazyflie is disconnected (called in all cases)
			 */
			public void disconnected(String connectionInfo) {
				System.out.println("DISCONNECTED from " +  connectionInfo);
			}

			/*
			 * Callback when connection initial connection fails (i.e no Crazyflie at the specified address)
			 */
			public void connectionFailed(String connectionInfo, String msg) {
				System.out.println("CONNECTION FAILED: " +  connectionInfo + " Msg: " + msg);
			}

			/*
			 * Callback when disconnected after a connection has been made (i.e Crazyflie moves out of range)
			 *
			 */
			public void connectionLost(String connectionInfo) {
				System.out.println("CONNECTION LOST: " +  connectionInfo);
			}

		});

		ConnectionData connectionData = new ConnectionData(channel, Crazyradio.DR_250KPS);        
		mCrazyflie.connect(connectionData);

		System.out.println("Connection to " + connectionData);
	}
	
	public void disconnect(){
		mCrazyflie.disconnect();
	}
	
	public void startMonitor(){
		/*
		if(isConnected()){
			mCrazyflie.getParam().addParamListener(new ParamListener("pid_rate", "pitch_kd") {
	            @Override
	            public void updated(String name, Number value) {
	                System.out.println("Readback: " + name + "=" + value);
	
	                // End the example by closing the link (will cause the app to quit)
	                if ((Float) value == 0.00f) {
	                    mCrazyflie.disconnect();
	                }
	            }
	        });
		}
		*/
	}
	
	public void setValues(String ... args){
		if(!mCrazyflie.isConnected()) {System.out.println("NOT CONNECTED"); return;}
		
		for(int i = 0; i < args.length; i += 2){
			if(args[i].toLowerCase().compareTo("thrust") == 0){
				thrust = Long.parseLong(args[i + 1]);
			}else if(args[i].toLowerCase().compareTo("pitch") == 0){
				pitch = Long.parseLong(args[i + 1]);
			}else if(args[i].toLowerCase().compareTo("roll") == 0){
				roll = Long.parseLong(args[i + 1]);
			}else if(args[i].toLowerCase().compareTo("yaw") == 0){
				yaw = Long.parseLong(args[i + 1]);
			}else{
				System.out.println("Invalid value name: " + args[i]);
				return;
			}
			
			System.out.println("Sending packet...\troll: " + roll + "\tpitch: " + pitch + "\tyaw: " + yaw + "\tthrust: " + thrust);
			mCrazyflie.sendPacket(new CommanderPacket(roll, pitch, yaw, (char) thrust));
		}
		
	}
	
	public Number getParam(String parameterName) {
		if(isConnected()) {
            Param mParam = mCrazyflie.getParam();
            mParam.requestParamUpdate(parameterName);
			return mParam.getValue(parameterName);
			//return mCrazyflie.getParam().getValue(parameterName);
        
            //Param mParam = mCrazyflie.getParam();
            //mParam.requestParamUpdate(parameterName);
            //String[] paraName = parameterName.split("\\.");
            //return mParam.getValuesMap().get(paraName[0]).get(paraName[1]);
        } else {
            return null;
        }
    }
	
	public boolean isConnected(){
		return mCrazyflie != null && mCrazyflie.isConnected();
	}

}
