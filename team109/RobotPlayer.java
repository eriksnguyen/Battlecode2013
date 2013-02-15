package team109;

import battlecode.common.RobotController;

public class RobotPlayer {

	public static void run(RobotController myRc) {
		
		switch(myRc.getType()){
			case ARTILLERY: Artillery.run(myRc); break;
			case HQ: HQ.run(myRc); break;
			case SOLDIER: Soldier.runNuke(myRc); break;
			default: NonMotileEncampment.run(myRc);
		}
	}
}
