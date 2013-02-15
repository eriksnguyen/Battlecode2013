package team109BackUp;

import battlecode.common.RobotController;

public class RobotPlayer {

	public static void run(RobotController myRc) {
		
		switch(myRc.getType()){
			case ARTILLERY: Artillery.run(myRc); break;
			case HQ: HQ.run(myRc); break;
			case SOLDIER: Soldier.run(myRc); break;
			default: NonMotileEncampment.run(myRc);
		}
	}
}
