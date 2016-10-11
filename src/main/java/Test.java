import Simpleflie.Simpleflie;


public class Test {
	//public static final Logger log = LoggerFactory.getLogger(CrazyflieTest.class);
    //private static UsbLinkJava mUsbLinkJava;
	
	public static void main(String[] args) {
		System.out.println("Starting test");
        Simpleflie drone = new Simpleflie(80);
        while(!drone.isConnected());    
        
        
        
        int max = 43000;
        int min = 10000;
        int thrust = 0;
        drone.setValues("thrust", "0");
        //if(drone.getValue("yaw") == null) System.out.println("NULL");
        double startingYaw = 160;
        
        double i = 1.1;
        int sign = 1;
        while (i > 1){
        	//System.out.println(drone.getParam("gyro.x"));
        	//System.out.println(drone.getParam("stabilizer.roll"));
        	thrust = (int) ((max - min) * Math.log(i)) + min;
            if(thrust > max){
            	thrust = max;
            	sign = -1;
            }
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i += ((thrust < 30000) ? .2 : .02) * sign;
            
            
            double pitch = drone.getValue("pitch").doubleValue();
            if(pitch > 1){
            	pitch -= .2;
            	//drone.setValues("pitch", "-.2");
            }else if(pitch < -1){
            	pitch += .2;
            	//drone.setValues("pitch", "+.2");
            }
            
            double roll = drone.getValue("roll").doubleValue();
            if(roll > 1){
            	roll -= .2;
            	//drone.setValues("roll", "-.2");
            }else if(roll < -1){
            	roll += .2;
            	//drone.setValues("roll", "+.2");
            }
            
            double yaw = drone.getValue("yaw").doubleValue();
            if(yaw > startingYaw + 1){
            	yaw -= .2;
            	//drone.setValues("yaw", "-.2");
            }else if(yaw < startingYaw - 1){
            	yaw += .2;
            	//drone.setValues("yaw", "+.2");
            }

            //drone.setValues("pitch", "" + pitch, "roll", "" + roll, "yaw", "" + yaw);
            drone.setValues("thrust", "" + thrust, "pitch", "" + pitch, "roll", "" + roll, "yaw", "" + yaw);
        }
        
        
        drone.setValues("thrust", "0");
        
        drone.disconnect();
	}

}
