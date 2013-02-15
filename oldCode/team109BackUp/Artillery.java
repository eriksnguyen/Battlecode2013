package team109BackUp;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class Artillery extends MyRobot{

	private static RobotController rc;
	private static MapLocation enemyHQ, allyHQ;
	private static Team myTeam, enemyTeam;
	
	protected static void run(RobotController myRc) {
		rc = myRc;
		enemyHQ = rc.senseEnemyHQLocation();
		allyHQ = rc.senseHQLocation();
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		
		while (true) {
			MapLocation target = null;
			try {
				if (rc.isActive()) {
					//Check nearby enemies
					Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, 63, enemyTeam);
					
					target = findTarget(enemies);//Select target
					if (target != null) {//Attack Target
						rc.setIndicatorString(0, "Round: " + Clock.getRoundNum()+" | Target: " + target.toString());
						rc.attackSquare(target);
					}
				}
				if (Clock.getBytecodesLeft() > 200) {//Status updates
					distressSignal(rc, 2);
				}
			} catch (Exception e) {
				System.out.println("Artillery Error");
				e.printStackTrace();
				rc.breakpoint();
			}
			rc.yield();//Pause turn
		}
	}

	/*
	 * TODO: change heuristic to also take into account unit type/action.
	 * e.g. Soldier on encampment (could be capturing), soldier, encampment
	 * HQ is attacked, near EnemyHQ
	 */
	private static MapLocation findTarget(Robot[] enemies) throws GameActionException {
		if (enemies.length == 0){
			if(rc.getLocation().distanceSquaredTo(enemyHQ) < 64){
				return enemyHQ;
			}
			return null;
		}

		MapLocation target = null;
		int tHeuristic = 1;

		for (Robot rb : enemies) {
			MapLocation loc = rc.senseLocationOf(rb);

			int heuristic = 0;

			//Calculate the damage done in the area of attack
			//there is a 2X penalty for doing damage to an ally
			
			for (int i = -1; i < 2; i++) {
				for (int j = -1; j < 2; j++) {

					if (!inRange(loc.x + i, loc.y + j))
						continue;

					MapLocation next = loc.add(i, j);
					if (rc.canSenseSquare(next)) {
						Robot r = (Robot) rc.senseObjectAtLocation(next);
						if (r == null)
							continue;

						boolean soldier = rc.senseRobotInfo(r).type == RobotType.SOLDIER;
						
						if (r.getTeam() == myTeam)
							heuristic += 30;//Damage to team is worse
						else
							heuristic -= (i == 0 && j == 0 ? (soldier? 40 : 60) : 15);//than damage to enemy
					}
				}
			}

			if (heuristic < tHeuristic) {
				tHeuristic = heuristic;
				target = loc;
			}
						
			if(Clock.getBytecodesLeft() < 2000){//Takes the best choice for a given turn
				break;
			}
		}

		if (target == null || tHeuristic > 0) {
			return null;
		}
		
		return target;

	}

	/*
	 * makes sure that a target is within memory bounds
	 */
	private static boolean inRange(int x, int y) {
		return x > -1 && y > -1 && x < rc.getMapWidth() && y < rc.getMapHeight();
	}
}
