import Simpleflie.Simpleflie;


public class Test {
	//public static final Logger log = LoggerFactory.getLogger(CrazyflieTest.class);
    //private static UsbLinkJava mUsbLinkJava;
	
	public static void main(String[] args) {
		System.out.println("Starting test");
        Simpleflie drone = new Simpleflie(80);
        while(!drone.isConnected());
        /*
        drone.startMonitor();
        
        for(int i = 0; i < 100; i++){
        	System.out.println(drone.getParam("pid_rate.roll_kd").floatValue());
        	try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        */
        
        
        int max = 38000;
        int min = 10000;
        int thrust = 0;
        drone.setValues("thrust", "0");
        
        double i = 1.1;
        int sign = 1;
        while (i > 1){
        	//System.out.println(drone.getParam("gyro.x"));
        	//System.out.println(drone.getParam("stabilizer.roll"));
        	thrust = (int) ((max - min) * Math.log(i)) + min;
            if(thrust > max){
            	thrust = max;
            	sign = -1;
            	try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
        	drone.setValues("thrust", "" + thrust);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i += ((thrust < 30000) ? .3 : .05) * sign;
        }
        
        /*
        int min = 10000;
        int step = 4000;
        for(int i = min; i < max * 2 - min; i += step){
            drone.setValues("thrust", "" + thrust);
            try {
                Thread.sleep(265);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thrust += step * ((i >= max) ? -1 : 1);
        }
        */
        drone.setValues("thrust", "0");
        
        drone.disconnect();
	}

}
