import Simpleflie.Simpleflie;


public class Test {
	//public static final Logger log = LoggerFactory.getLogger(CrazyflieTest.class);
    //private static UsbLinkJava mUsbLinkJava;
	
	public static void main(String[] args) {
		System.out.println("Starting test");
        Simpleflie drone = new Simpleflie(80);
        while(!drone.isConnected());
        
        int max = 20000;
        int min = 10000;
        int thrust = min;
        int step = 1000;
        drone.setValues("thrust", "0");
        
        for(int i = min; i < max + min; i += step){
            drone.setValues("thrust", "" + thrust);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thrust += step * ((i >= max) ? -1 : 1);
        }
        
        drone.setValues("thrust", "0");
        drone.disconnect();
	}

}
