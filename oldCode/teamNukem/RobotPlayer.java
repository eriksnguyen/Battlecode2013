package teamNukem;

import java.util.ArrayList;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.Upgrade;

public class RobotPlayer {

	private static RobotController rc;
	private static MapLocation rallyPoint;

	private static MapLocation enemyHQ, allyHQ, currLocation;

	private static MapLocation[] encampments;
	private static MapLocation target;
	private static Team myTeam, enemyTeam;
	private static Robot[] alliedRobots,enemyRobots;
	private static MapLocation[] encsqrs;
	private static int mapH,mapL;

	public static void run(RobotController myRc) {
		
		
		/*switch(myRc.getType()){
			case ARTILLERY: Artillery.run(myRc); break;
			case HQ: HQ.run(myRc); break;
			case SOLDIER: Soldier.run(myRc); break;
			default: NonMotileEncampment.run(myRc);
		}
		*/
		
		rc = myRc;
		enemyHQ = rc.senseEnemyHQLocation();
		allyHQ = rc.senseHQLocation();
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();

		switch (myRc.getType()) {
		case SOLDIER:
			soldier();
			break;
		case HQ:
			hq();
			break;
		case ARTILLERY:
			Artillery.run(myRc);
			break;
		default:
			NonMotileEncampment.run(myRc);
		}

	}

	/*************************************** SOLDIER **********************************************/
	private static int mode = 1;

	private static void soldier() {
		try {
			initialRush();// first 150 rounds
			initialRoam();// 150 - 350 rounds
			double dissenter = Math.random();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Initial Error");
		}
		mapH=rc.getMapHeight();
		mapL=rc.getMapWidth();
		

		while (true) {
			try {
				currLocation=rc.getLocation();
				int channel = getChannel();

				int ir = rc.readBroadcast(channel + 1);
				if (ir > 0 && ir < 3) {
					mode = ir;
				}
				double dissenter = Math.random();
				if (rc.isActive()) {
					enemyRobots = 
							rc.senseNearbyGameObjects(Robot.class, 1000000, rc.getTeam().opponent());
					alliedRobots=rc.senseNearbyGameObjects(Robot.class, 1000000, rc.getTeam());
					int round=Clock.getRoundNum();
					encsqrs = rc.senseAlliedEncampmentSquares();
				  if(dissenter>.15){
					if (enemyRobots.length == 0)//no nearby enemies
					{
						
						int diag=mapH*mapH + mapL*mapL;
						if(currLocation.distanceSquaredTo(allyHQ)<diag/3.5 && rc.senseNearbyGameObjects(Robot.class,30,rc.getTeam()).length>(round<=500?4:9)
								//and we have enough to go "exploring"
								|| currLocation.distanceSquaredTo(allyHQ)<=12)//or we're too close to hq
							goToLocation(enemyHQ);
						else{
							if (currLocation.distanceSquaredTo(enemyHQ)<=allyHQ.distanceSquaredTo(enemyHQ) 
									&& rc.senseMine(rc.getLocation()) == null) {
								rc.layMine();
							} else {
								// Choose a random direction, and move that way if
								// possible
								Direction dir;
								if(Math.random()<=.5)dir=currLocation.directionTo(enemyHQ);
								else dir=Direction.values()[(int) (Math.random() * 8)];
								if (rc.canMove(dir)) {
									MapLocation ahead = rc.getLocation().add(dir);
									Team m = rc.senseMine(ahead);
									if (m != null && m != rc.getTeam()) {
										rc.defuseMine(ahead);
									} else {
										rc.move(dir);
									}
								}
							}
						}
					}
					else{// someone spotted
						MapLocation closestEnemy = findClosest(enemyRobots);
						
							// smartCountNeighbors(enemyRobots, closestEnemy);
							/*if((alliedRobots.length-encsqrs.length<(round<1800? 16:12)||//we're low on units
									alliedRobots.length-encsqrs.length/enemyRobots.length<=.9) &&
								HQloc.distanceSquaredTo(rc.getLocation())<=5 &&//this unit is close
								HQloc.distanceSquaredTo(closestEnemy)>9)//closest enemy is far
									goToLocation(HQloc);
							else*/
								goToLocation(closestEnemy);
						}
						
				   }
				  else if(dissenter>.08){
					  	encampments = rc.senseEncampmentSquares(rc.getLocation(),
					  			1000000, Team.NEUTRAL);
					  	
						setTarget(null);

						if (target == null)
							break;

						rc.setIndicatorString(0, "Going to target "
								+ MapLocationToInt(target));

						while (!rc.getLocation().equals(target)) {
							if (rc.isActive()) {
								if (rc.canSenseSquare(target)
										&& rc.senseObjectAtLocation(target) != null)
									setTarget(target);
								goToLocation(target);
							}
							rc.yield();
						}

						if (rc.isActive()) {
							if(rc.senseCaptureCost()<rc.getTeamPower()-rc.senseNearbyGameObjects(Robot.class,100000,rc.getTeam()).length*2)
							{double rand=Math.random();
								if(rand<.334)rc.captureEncampment(RobotType.ARTILLERY);
								else if(rand<.667){
									rc.captureEncampment(RobotType.SUPPLIER);
								}
								else{
									rc.captureEncampment(RobotType.GENERATOR);
								}
							rc.setIndicatorString(0, "CapturingEncampment "
									+ MapLocationToInt(target));
							target = null;}
						}
				  }
				  else{
						if (currLocation.distanceSquaredTo(enemyHQ)<=allyHQ.distanceSquaredTo(enemyHQ) && rc.senseMine(rc.getLocation()) == null) {
							rc.layMine();
						} else {
							// Choose a random direction, and move that way if
							// possible
							Direction dir;
							if(Math.random()<=.5)dir=currLocation.directionTo(enemyHQ);
							else dir=Direction.values()[(int) (Math.random() * 8)];
							if (rc.canMove(dir)) {
								MapLocation ahead = rc.getLocation().add(dir);
								Team m = rc.senseMine(ahead);
								if (m != null && m != rc.getTeam()) {
									rc.defuseMine(ahead);
								} else {
									rc.move(dir);
								}
							}

						}
				  }
				 }
				}catch (Exception e) {
				System.out.println("Soldier Exception");
				e.printStackTrace();
				rc.breakpoint();}
			
			if (Clock.getBytecodesLeft() < 5000) {
				System.out.println(Clock.getBytecodesLeft());
			}
			rc.yield();
		}
	}

	private static MapLocation findClosest(Robot[] enemyRobots)
			throws GameActionException {
		int closestDist = 1000000;
		MapLocation closestEnemy = null;
		for (int i = 0; i < enemyRobots.length; i++) {
			Robot arobot = enemyRobots[i];
			RobotInfo arobotInfo = rc.senseRobotInfo(arobot);
			int dist = arobotInfo.location.distanceSquaredTo(rc.getLocation());
			if (dist < closestDist) {
				closestDist = dist;
				closestEnemy = arobotInfo.location;
			}
		}
		return closestEnemy;
	}
	

	/*
	 * This is the code for the movement of the earliest of the robots.
	 */
	private static void initialRush() throws GameActionException {
		while (Clock.getRoundNum() < 150) {
			try {
				if (rc.isActive()) {
					encampments = rc.senseEncampmentSquares(rc.getLocation(),
							1000000, Team.NEUTRAL);

					setTarget(null);

					if (target == null)
						break;

					rc.setIndicatorString(0, "Going to target "
							+ MapLocationToInt(target));

					while (!rc.getLocation().equals(target)) {
						if (rc.isActive()) {
							if (rc.canSenseSquare(target)
									&& rc.senseObjectAtLocation(target) != null)
								setTarget(target);
							goToLocation(target);
						}
						rc.yield();
					}

					if (rc.isActive()) {
						if(rc.senseCaptureCost()<rc.getTeamPower()-rc.senseNearbyGameObjects(Robot.class,100000,rc.getTeam()).length*2)
						{rc.captureEncampment(RobotType.ARTILLERY);
						rc.setIndicatorString(0, "CapturingEncampment "
								+ MapLocationToInt(target));
						target = null;}
					}
				}
			} catch (Exception e) {
				System.out.println("Rush Error");
				e.printStackTrace();
				rc.breakpoint();
			}
			if (Clock.getBytecodesLeft() < 5000) {
				System.out.println(Clock.getBytecodesLeft());
			}
			rc.yield();
		}

	}

	private static void setTarget(MapLocation origTarget)
			throws GameActionException {
		int distance = 1000000;
		for (MapLocation loc : encampments) {
			if (loc.equals(origTarget)) {
				continue;
			} else if (rc.canSenseSquare(loc)) {
				GameObject obj = rc.senseObjectAtLocation(loc);
				if (obj != null && obj.getTeam() == rc.getTeam())
					continue;
			}
			int temp = loc.distanceSquaredTo(rc.getLocation());
			if (distance > temp){
				distance = temp;
				target = loc;
			}
		}
	}

	private static void initialRoam() throws GameActionException {
		while (Clock.getRoundNum() < 350) {
			try {
				if (rc.isActive()) {
					MapLocation loc = rc.getLocation(),enemyHQ=rc.senseEnemyHQLocation(),myHQ=rc.senseHQLocation();
					if (loc.distanceSquaredTo(enemyHQ)<=myHQ.distanceSquaredTo(enemyHQ)+8 && rc.senseMine(rc.getLocation()) == null) {
						rc.layMine();
					} else {
						// Choose a random direction, and move that way if
						// possible
						Direction dir;
						if(Math.random()<=.5)dir=loc.directionTo(enemyHQ);
						else dir=Direction.values()[(int) (Math.random() * 8)];
						if (rc.canMove(dir)) {
							MapLocation ahead = rc.getLocation().add(dir);
							Team m = rc.senseMine(ahead);
							if (m != null && m != rc.getTeam()) {
								rc.defuseMine(ahead);
							} else {
								rc.move(dir);
							}
						}

					}
				}
			} catch (Exception e) {
				System.out.println("Roaming Error");
				e.printStackTrace();
				rc.breakpoint();
			}
			if (Clock.getBytecodesLeft() < 5000) {
				System.out.println(Clock.getBytecodesLeft());
			}
			rc.yield();
		}
	}

	private static void goToLocation(MapLocation target) throws GameActionException{
		Direction dir = findBestPath(target);
		if(dir != null){
			MapLocation ahead = rc.getLocation().add(dir);
			Team m = rc.senseMine(ahead);
			if(m != null && m != rc.getTeam()){
				rc.defuseMine(ahead);
			} else{
				rc.move(dir);
			}
		}
	}
	
	//All directions
	private static Direction[] directions = new Direction[]{
		Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
		Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.SOUTH_WEST
	};
	
	//only lets you move from 0 to 180 degrees towards target (therefore. no backward movement)
	private static int[] dirs = new int[]{0, 1, -1, 2, -2};
	
	/*
	 * Depending on the target, it finds a set of the best paths thinking 7 steps ahead
	 */
	private static Direction findBestPath(MapLocation target){
		Node first = null;
		Direction dir = rc.getLocation().directionTo(target);
		Direction lookAt = dir;
		for(int d: dirs){
			lookAt = directions[(dir.ordinal() + d + 8) % 8];
			if(rc.canMove(lookAt)){
				Node temp = new Node(rc.getLocation().add(lookAt), target, lookAt);
				if(first == null || first.compareTo(temp) > 0)
					first = temp;
			}
		}

		if(first == null){
			return null;
		}
		
		return first.temp;
	}
	
	private static class Node implements Comparable<Node>{
		public int cost;
		public int heuristic;
		public Team mine;
		public Direction temp;
		public Node(MapLocation l, MapLocation target, Direction d){
			temp = d;
			mine = rc.senseMine(l);
			heuristic = Math.max(Math.abs(l.x - target.x), Math.abs(l.y - target.y));
			cost += ((mine != null && mine != myTeam) ? 13 : 1);
		}
		public int compareTo(Node p){
			return heuristic + cost - p.cost - p.heuristic;
		}
	}

	/******************************************* SOLDIER *****************************************/

	/******************************** HEAD QUARTERS CODE *****************************************/
	private static void hq() {
		double prevPower = 0.0;
		alliedRobots=rc.senseNearbyGameObjects(Robot.class, 1000000, rc.getTeam());
		encsqrs=rc.senseAlliedEncampmentSquares();
		mapH=rc.getMapHeight();
		mapL=rc.getMapWidth();
		//int diag=mapH*mapH+mapL*mapL;
		while (true) {
			try {
				if (rc.isActive()) {// If the HQ isn't in cool-down mode
					int round = Clock.getRoundNum();
					if (round>350 && (tooManyUnits(prevPower)  
							|| alliedRobots.length-encsqrs.length > 12//(getPercentMines(allyHQ,(int)(.25*diag))<=.6 ? 16:8)
							|| round%2==0 || round>1400)){
						if (!rc.hasUpgrade(Upgrade.PICKAXE)) {
							rc.researchUpgrade(Upgrade.PICKAXE);
						} else rc.researchUpgrade(Upgrade.NUKE);
					} else {
						// Preferable direction for spawn
						int pref = rc.getLocation()
								.directionTo(rc.senseEnemyHQLocation())
								.ordinal() + 8;
						Direction dir = Direction.NONE;// Default
						for (int i = 0; i < 6; i++) {
							if (rc.canMove(dir = Direction.values()[(pref + i) % 8])
									&& rc.senseMine(rc.getLocation().add(dir)) != Team.NEUTRAL) {
								break;
							} else if (rc
									.canMove(dir = Direction.values()[(pref - i) % 8])
									&& rc.senseMine(rc.getLocation().add(dir)) != Team.NEUTRAL) {
								break;
							}
						}
						if (rc.canMove(dir))
							rc.spawn(dir);
					}
				}

				// If dying or going to die
				if (rc.getEnergon() < 300 || Clock.getRoundNum() > 2000
						|| rc.senseEnemyNukeHalfDone()) {
					rallyPoint = enemyHQ;
				} else {
					rallyPoint = null;
				}

				// War status
				int channel = getChannel();
				int msg = MapLocationToInt(rallyPoint);
				rc.broadcast(channel, msg);
				rc.setIndicatorString(0, "Posted " + msg + " to channel: "
						+ channel);

				// Send soldiers into field-mine mode
				if (rc.senseNearbyGameObjects(Robot.class, 1000000, rc
						.getTeam().opponent()).length < 3) {
					rc.broadcast(channel + 1, 2);
				}

				prevPower = rc.getTeamPower();
			} catch (Exception e) {
				System.out.println("HQ Error");
				e.printStackTrace();
				rc.breakpoint();
			}
			if (Clock.getBytecodesLeft() < 5000) {
				System.out.println(Clock.getBytecodesLeft());
			}
			rc.yield();
		}

	}

	/*
	 * ensures small growth between rounds
	 */
	private static boolean tooManyUnits(double prevPower) {
		return (rc.getTeamPower()
				- prevPower
				- rc.senseNearbyGameObjects(Robot.class, 1000000, rc.getTeam()).length
				* 2 < 2)
				&& rc.getTeamPower() < 70;
	}
	private static MapLocation[] validSquares(MapLocation L, int r)
	{
		ArrayList<MapLocation> list = new ArrayList<MapLocation>();
		for(int i=0;i<mapL;i++)
		{
			for(int j=0;j<mapH;j++)
				if((i-L.x)*(i-L.x)+(j-L.y)*(j-L.y)<=r)list.add(new MapLocation(i,j));
		}
		return list.toArray(new MapLocation[list.size()]);
	}
	private static double getPercentMines(MapLocation c, int r)throws GameActionException
	{
		MapLocation[] mynes = rc.senseMineLocations(c,r,myTeam),
					  valid = validSquares(c,r);
		return ((double)mynes.length)/valid.length;
	}

	/******************************** HEAD QUARTERS CODE *****************************************/

	/*
	 * randomly hashed channel
	 */
	private static int getChannel() {
		return (Clock.getRoundNum() * 1029)
				% GameConstants.BROADCAST_MAX_CHANNELS;
	}

	private static int MapLocationToInt(MapLocation loc) {
		if (loc == null)
			return 0;
		return loc.x * 1000 + loc.y;
	}

	private static MapLocation IntToMapLocation(int i) {
		int y = i % 1000;
		int x = (i - y) / 1000;
		if (x == 0 && y == 0) {
			return null;
		} else {
			return new MapLocation(x, y);
		}
	}

	private static boolean includes(MapLocation[] alliedEncampments,
			MapLocation rallypoint) {
		for (MapLocation loc : alliedEncampments) {
			if (loc.equals(rallypoint))
				return true;
		}
		return false;
	}
}
