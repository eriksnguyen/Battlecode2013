package teamNukem;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;

public class HQ extends MyRobot{
	
	public static RobotController rc;
	public static MapLocation enemyHQ, allyHQ;
	public static Team myTeam, enemyTeam;

	public static void run(RobotController myRc){
		
		rc = myRc;
		enemyHQ = rc.senseEnemyHQLocation();
		allyHQ = rc.senseHQLocation();
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		
		while(true){
			try{
				if(rc.isActive()){
					
				}
				
			} catch (Exception e){
				System.out.println("HQ Error");
				e.printStackTrace();
				rc.breakpoint();
			}
			rc.yield();
		}
	}
}
