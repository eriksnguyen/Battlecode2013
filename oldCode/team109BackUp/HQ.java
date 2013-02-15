package team109BackUp;

import java.util.ArrayList;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.Upgrade;

public class HQ extends MyRobot {

	public static RobotController rc;
	public static MapLocation enemyHQ, allyHQ;
	public static Team myTeam, enemyTeam;
	public static int mapH, mapW;
	public static MapLocation[] mainVector, encSquares;

	public static void run(RobotController myRc) {
		rc = myRc;
		enemyHQ = rc.senseEnemyHQLocation();
		allyHQ = rc.senseHQLocation();
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		mapH = rc.getMapHeight();
		mapW = rc.getMapWidth();
		
		
		double prevPower = 0.0;
		startupCode();
		
		MapLocation rallyPoint = null;
		boolean notYet1 = true, notYet2 = true, notYet3 = true;
		while (true) {
			try {
				resetChannels();
				if (rc.isActive()) {
					if (tooManyUnits(prevPower)) {
						for (Upgrade u : upgrades) {
							if (!rc.hasUpgrade(u)) {
								rc.researchUpgrade(u);
								break;
							}
						}
					} else {
						spawn();
					}
				}
				
				// War status
				int channel = getChannel(Clock.getRoundNum(), 0);

				// If dying or going to die
				if (rc.getEnergon() < 300 || Clock.getRoundNum() > 2000
						|| rc.senseEnemyNukeHalfDone()) {
					rallyPoint = enemyHQ;
					rc.broadcast(channel, 10);
				} else {
					rallyPoint = null;
				}
				
				
				if (strictRush) {
					rc.broadcast(channel + 6, 4);// Strict rush mode
				} 
				
				prevPower = rc.getTeamPower();

			} catch (Exception e) {
				System.out.println("HQ Error");
				e.printStackTrace();
				rc.breakpoint();
			}
			rc.yield();
		}
	}
	
	private static void resetChannels() throws GameActionException{
		for(int i = 0; i < 7; i++)
			rc.broadcast(getChannel(Clock.getRoundNum() + 1, i), 0);
	}

	private static void spawn() throws GameActionException {
		// Preferable direction for spawn
		int pref = rc.getLocation().directionTo(enemyHQ).ordinal() + 8;
		Direction dir = Direction.NONE;// Default
		for (int i = 0; i < 6; i++) {
			if (rc.canMove(dir = Direction.values()[(pref + i) % 8])
					&& rc.senseMine(rc.getLocation().add(dir)) != Team.NEUTRAL) {
				break;
			} else if (rc.canMove(dir = Direction.values()[(pref - i) % 8])
					&& rc.senseMine(rc.getLocation().add(dir)) != Team.NEUTRAL) {
				break;
			}
		}
		if (rc.canMove(dir))
			rc.spawn(dir);
	}

	private static boolean tooManyUnits(double prevPower) {
		double teamPower = rc.getTeamPower();
		return (teamPower
				- prevPower
				- rc.senseNearbyGameObjects(Robot.class, 1000000, myTeam).length
				* 2 < 0)
				&& rc.getTeamPower() < 70;

	}

	private static Upgrade[] upgrades = new Upgrade[] { Upgrade.DEFUSION,
			Upgrade.PICKAXE, Upgrade.FUSION, Upgrade.VISION, Upgrade.NUKE };
	private static boolean strictRush = false;

	private static void startupCode() {
		try {
			spawn();
			// If separation distance between HQs is less than 1/3 diagonal
			// distance. Strict Rush
			if (Math.sqrt(allyHQ.distanceSquaredTo(enemyHQ)) * 3 < Math
					.sqrt(mapH * mapH + mapW * mapW)) {
				strictRush = true;
				int channel = getChannel(Clock.getRoundNum(), 6);
				rc.broadcast(channel + 6, 4);
			} else {// Otherwise

			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}

	}

	private static MapLocation[] getMainVector() {
		ArrayList<MapLocation> list = new ArrayList<MapLocation>();
		Direction initialDir = allyHQ.directionTo(enemyHQ);
		MapLocation prev = allyHQ.add(initialDir);
		MapLocation stop = enemyHQ.subtract(initialDir);
		while (prev.distanceSquaredTo(enemyHQ) > 2) {
			list.add(prev);
			prev = prev.add(prev.directionTo(enemyHQ));
		}
		list.add(prev);
		return list.toArray(new MapLocation[list.size()]);
	}

	/*
	 * Processes the map;
	 */
	private static boolean nuke;
	private static void processMap() {
		MapLocation[] initNeutralMines = rc.senseMineLocations(allyHQ, 1000000,
				Team.NEUTRAL);
		int area = mapH * mapW;
		int density = initNeutralMines.length * 100 / area;
		int HQSeparation_2 = allyHQ.distanceSquaredTo(enemyHQ);
		
		int radius = Math.min(Math.min(allyHQ.x, allyHQ.y), 10);
		if(radius < 1)
			area = 3 * radius * radius / 4;
		else if( radius < 5)
			area = 9 * radius * radius / 4;
		else
			area = 3 * radius * radius;
		int localDensity = rc.senseMineLocations(allyHQ, 100, Team.NEUTRAL).length * 100 / area;
		
		if(density > 70){
			nuke = true;
		} else if(density > 40){
			if(HQSeparation_2 > 1600)
				nuke = true;
			if(localDensity > 40 && HQSeparation_2 > 289)
				nuke = true;
		}
		
		if(localDensity < 35 && HQSeparation_2 < 1600 || density < 30)
			strictRush = true;
	}
}
