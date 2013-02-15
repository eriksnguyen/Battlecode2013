package team109_1_26;

import battlecode.common.RobotController;

public class NonMotileEncampment extends MyRobot{

	protected static void run(RobotController myRc){
		
		int priority;
		switch(myRc.getType()){
			case GENERATOR: priority = 5;	break;
			case SUPPLIER: 	priority = 4;	break;
			case MEDBAY: 	priority = 3;	break;
			default:		priority = 0;
		}
		
		while(true){
			try{
				//Sees if a distress signal needs to be posted
				distressSignal(myRc, priority);
			} catch (Exception e){
				System.out.println(myRc.getType()+" Error");
				e.printStackTrace();
				myRc.breakpoint();
			}
			myRc.yield();//end turn
		}
	}
}
