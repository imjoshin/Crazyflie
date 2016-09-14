package Simpleflie;

import java.util.List;

import se.bitcraze.crazyflie.lib.crazyflie.ConnectionAdapter;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.Crazyradio;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

public class Simpleflie {

	private Crazyflie mCrazyflie;
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
		
		Simpleflie(foundCrazyflies.get(0));
	}

	public Simpleflie(int channel){
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
}
