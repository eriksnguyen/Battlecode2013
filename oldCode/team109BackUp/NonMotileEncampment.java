package team109BackUp;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class NonMotileEncampment extends MyRobot{

	protected static void run(RobotController myRc){
		
		int priority;
		switch(myRc.getType()){
			case GENERATOR: priority = 5;	break;
			case SUPPLIER: 	priority = 4;	break;
			case MEDBAY: 	priority = 2;	break;
			default:		priority = 1; //Shield
		}
		
		try {
			//Initial broadcast
			myRc.broadcast(getChannel(Clock.getRoundNum(), priority), getDistressMessage(myRc.getLocation(), priority));
		} catch (GameActionException e1) {
			e1.printStackTrace();
		}
		
		while(true){
			try{//broadcast for future use
				if(priority < 3)
					myRc.broadcast(getChannel(Clock.getRoundNum() + 1, priority), getDistressMessage(myRc.getLocation(), priority));
			} catch (Exception e){
				System.out.println(myRc.getType()+" Error");
				e.printStackTrace();
				myRc.breakpoint();
			}
			myRc.yield();//end turn
		}
	}
}
