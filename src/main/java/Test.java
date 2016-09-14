import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyflie.CrazyflieTest;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.Crazyradio;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;


public class Test {
	public static final Logger log = LoggerFactory
			.getLogger(CrazyflieTest.class);
    private static UsbLinkJava mUsbLinkJava;
	
	public static void main(String[] args) {
        System.out.println("Scanning interfaces for Crazyflies...");
        RadioDriver radioDriver = new RadioDriver(new UsbLinkJava());
        List<ConnectionData> foundCrazyflies = radioDriver.scanInterface();
        radioDriver.disconnect();
        System.out.println("Crazyflies found:");
        for (ConnectionData connectionData : foundCrazyflies) {
            System.out.println(connectionData);
        }
	}

}
