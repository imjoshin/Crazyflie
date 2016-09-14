import Simpleflie.Simpleflie;


public class Test {
	//public static final Logger log = LoggerFactory.getLogger(CrazyflieTest.class);
    //private static UsbLinkJava mUsbLinkJava;
	
	public static void main(String[] args) {
		System.out.println("Starting test");
        Simpleflie drone = new Simpleflie(80);
        while(!drone.isConnected());
        

        drone.setValues("thrust", "0");
        
        for(int i = 10; i < 20; i++){
            drone.setValues("thrust", "" + i * 1000);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        for(int i = 20; i > 10; i--){
            drone.setValues("thrust", "" + i * 1000);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        drone.setValues("thrust", "0");
        drone.disconnect();
	}

}
