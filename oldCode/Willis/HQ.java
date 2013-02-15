package Willis;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.Upgrade;

public class HQ extends MyRobot{
	
	public static RobotController rc;
	public static MapLocation enemyHQ, allyHQ;
	public static Team myTeam, enemyTeam;
	public static int mapH, mapW;

	public static void run(RobotController myRc){
		
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
		while(true){
			try{
				if(rc.isActive()){
					if(tooManyUnits(prevPower)){
						for(Upgrade u: upgrades){
							if(!rc.hasUpgrade(u)){
								rc.researchUpgrade(u); break;
							}
						}
					} else {
						spawn();
					}
				}
				// If dying or going to die
				if(rc.getEnergon() < 300 || Clock.getRoundNum() > 2000
						|| rc.senseEnemyNukeHalfDone()){
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
				
				if(strictRush){
					rc.broadcast(channel + 1, 4);//Strict rush mode
				} else if (rc.senseNearbyGameObjects(Robot.class, 1000000, rc.getTeam().opponent()).length < 3) {
					rc.broadcast(channel + 1, 3);// Send soldiers into field-mine mode
				}
				prevPower = rc.getTeamPower();
				
			} catch (Exception e){
				System.out.println("HQ Error");
				e.printStackTrace();
				rc.breakpoint();
			}
			rc.yield();
		}
	}
	private static void spawn() throws GameActionException{
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
	private static boolean tooManyUnits(double prevPower){
		double teamPower = rc.getTeamPower();
		return (teamPower - prevPower - rc.senseNearbyGameObjects(Robot.class, 1000000, myTeam).length* 2 < 2)
				&& rc.getTeamPower() < 70;
		
	}
	
	private static Upgrade[] upgrades = new Upgrade[] {Upgrade.FUSION, Upgrade.PICKAXE, Upgrade.DEFUSION, Upgrade.VISION, Upgrade.NUKE};
	private static boolean strictRush = false;
	private static void startupCode(){
		try {
			spawn();
			//If separation distance between HQs is less than 1/3 diagonal distance. Strict Rush
			if(Math.sqrt(allyHQ.distanceSquaredTo(enemyHQ)) * 3 < Math.sqrt(mapH * mapH + mapW * mapW)){
				strictRush = true;
				int channel = getChannel();
				rc.broadcast(channel + 1, 4);
			} else {//Otherwise
				
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
		
	}
	/*
	 * Processes the map;
	 */
	private static void processMap(){
		MapLocation[] initNeutralMines = rc.senseMineLocations(allyHQ, 1000000, Team.NEUTRAL);
		int bucketH = mapH/5, bucketW = mapW/5, widthMod = mapW % 5, heightMod = mapH % 5;
		int[][] density = new int[5][5];
		
		for(MapLocation l: initNeutralMines){
			density[l.y/bucketH][l.x/bucketW]++;
		}
		int aveDensity = 0;
		for(int y = 0; y < 5; y++){
			for(int x = 0; x < 5; x++){
				int width = bucketW, height = bucketH;
				if(y == 4)
					height += heightMod;
				if(x == 4)
					width += widthMod;
				aveDensity += (density[y][x] = 100*density[y][x]/(width * height));
			}
		}
		
		aveDensity /= 25;
		
		if(aveDensity > 40){
			//TODO: Tri pattern
		} else {
			//TODO: 
		}
	}
}
