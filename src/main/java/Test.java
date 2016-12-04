import Simpleflie.Simpleflie;


public class Test {

	public static void main(String[] args) {
		System.out.println("Starting test");
		//thrust();
		circle(3);
	}

	public static void circle(int iterations){
		Simpleflie drone = new Simpleflie(80);       

		int thrust = 41000;
		drone.setValues("thrust", "0");
		//drone.setShowPackets(true);
		drone.setFailSafe(50);

		for(int k = 0; k < iterations; k++){
			double lastRoll = 1;
			double lastPitch = 0;
			for(int i = 2; i <= 360; i += 2){
				try {
					Thread.sleep(15);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				double x = 20 * Math.cos(i / 180.0 * Math.PI);
				double y = 20 * Math.sin(i / 180.0 * Math.PI);
				double roll = (lastRoll - x) / 3;
				double pitch = y - lastPitch;
				lastRoll = roll;
				lastPitch = pitch;
				
				pitch -= 10;

				System.out.println(round(roll, 3) + ", " + round(pitch, 3));
				drone.setValues("thrust", "" + thrust, "pitch", "" + round(pitch, 3), "roll", "" + round(roll, 3));//, "yaw", "" + yaw);
			}
		}

		drone.setValues("pitch", "0", "roll", "0");

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		drone.setValues("thrust", "0");

		drone.disconnect();
	}

	public static void thrust(){
		Simpleflie drone = new Simpleflie(80);       


		int max = 30000;
		int min = 10000;
		int thrust = 0;
		drone.setValues("thrust", "0");
		drone.setFailSafe(30);
		//if(drone.getValue("yaw") == null) System.out.println("NULL");
		double startingYaw = 999;

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
				Thread.sleep(150);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i += ((thrust < 30000) ? .2 : .02) * sign;


			double pitch = drone.getValue("pitch").doubleValue();
			if(pitch > 1){
				pitch -= .2;
			}else if(pitch < -1){
				pitch += .2;
			}

			double roll = drone.getValue("roll").doubleValue();
			if(roll > 1){
				roll -= .2;
			}else if(roll < -1){
				roll += .2;
			}

			double yaw = drone.getValue("yaw").doubleValue();
			if(startingYaw == 999){
				System.out.println("Set startingYaw to " + yaw);
				startingYaw = yaw;
			}
			if(yaw > startingYaw + 10){
				yaw -= 5;
			}else if(yaw < startingYaw - 10){
				yaw += 5;
			}

			drone.setValues("thrust", "" + thrust, "pitch", "" + pitch, "roll", "" + roll);//, "yaw", "" + yaw);
		}


		drone.setValues("thrust", "0");

		drone.disconnect();
	}

	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}
}
