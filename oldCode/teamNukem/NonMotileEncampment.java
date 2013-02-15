package teamNukem;

import battlecode.common.Clock;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.Team;

public class NonMotileEncampment extends MyRobot{
	public static RobotController rc;
	public static MapLocation enemyHQ, allyHQ;
	public static Team myTeam, enemyTeam;

	public static void run(RobotController myRc){
		
		rc = myRc;
		enemyHQ = rc.senseEnemyHQLocation();
		allyHQ = rc.senseHQLocation();
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		
		int priority;
		switch(rc.getType()){
			case GENERATOR: priority = 5;	break;
			case SUPPLIER: 	priority = 4;	break;
			case MEDBAY: 	priority = 3;	break;
			default:		priority = 0;
		}
		
		while(true){
			try{
				//Sees if a distress signal needs to be posted
				distressSignal(rc, priority);
			} catch (Exception e){
				System.out.println(rc.getType()+" Error");
				e.printStackTrace();
				rc.breakpoint();
			}
			rc.yield();//end turn
		}
	}
}
