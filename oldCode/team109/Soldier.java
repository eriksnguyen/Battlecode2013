package team109;

import java.util.ArrayList;
import java.util.Random;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.Upgrade;

public class Soldier extends MyRobot {

	private static RobotController rc;
	private static MapLocation enemyHQ, allyHQ;
	private static Team myTeam, enemyTeam;
	private static int defuseCost;
	private static int dissenter;
	private static int distToEnemy_2, distToAlly_2;
	private static int id;
	private static int round;
	private static int strat;// 0=attack,1=encampments,2=miner
	private static String[] strategies = { "attacker", "camper", "miner" };
	private static MapLocation prevLocation;
	private static MapLocation currLocation;
	private static MapLocation[] encampments;
	private static int HQSeparation;
	private static Random rand;
	private static Robot[] enemyRobots;
	private static Robot[] allyRobots;
	private static Robot closestShield;
	private static int distToShield;
	private static MapLocation shieldLoc;
	private static Robot closestMed;
	private static int distToMed;
	public static MapLocation medLoc;
	private static RobotInfo info;
	private static int seed;
	private static int coefficientOfSuperman;
	private static int mapHeight, mapWidth, mapDiag_2;
	private static boolean wntb;// we'renukinthisbitch
	private static boolean nonoGoAhead;// a boolean telling robots to NOT set a
										// mine after spawning
	private static MapLocation[] badmines;

	/*
	 * Code for a miner/mine sweeper? Code for dissenter
	 */

	protected static void runNuke(RobotController myRc)
	{
		wntb=true;
		run(myRc);
	}
	protected static void run(RobotController myRc) {

		
		rc = myRc;
		enemyHQ = rc.senseEnemyHQLocation();
		allyHQ = rc.senseHQLocation();
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		defuseCost = 12;
		prevLocation = null;
		currLocation = rc.getLocation();
		HQSeparation = allyHQ.distanceSquaredTo(enemyHQ);
		seed = rc.getRobot().getID();
		rand = new Random((seed>>2 + 1)<<2 + 1);
		mapHeight = rc.getMapHeight();
		mapWidth = rc.getMapWidth();
		mapDiag_2 = mapHeight * mapHeight + mapWidth * mapWidth;
		nonoGoAhead = false;
		coefficientOfSuperman = 1;

		try {
			if (rc.readBroadcast(getChannel(Clock.getRoundNum(), 6)) == 4) {
				if(Clock.getRoundNum() < 20){
					earlyEncamp();
				}
				strictRush();
			} else {
				normalRun();
			}
		} catch (GameActionException e1) {
			e1.printStackTrace();
		}
	}
	
	private static MapLocation setRushEncampment() throws GameActionException{
		MapLocation ret = null;
		int distance = 1000000;
		
		for(MapLocation loc : encampments){//Each iteration at worst 2050 bytecode

			if(Clock.getBytecodesLeft() < 3500)//Ensures that the turn can be finished
				break;
			
			if(rc.canSenseSquare(loc)){
				GameObject obj = rc.senseObjectAtLocation(loc);
				if((obj != null && obj.getTeam() == myTeam))
					continue;
			}
			
			int temp = loc.distanceSquaredTo(currLocation);
			if(distance > temp){
				distance = temp;
				ret = loc;
			}
		}
		
		return ret;
	}
	
	private static void earlyEncamp() throws GameActionException{
		mode = 1;
		encampments = rc.senseEncampmentSquares(allyHQ, 4000, Team.NEUTRAL);
		MapLocation target = setRushEncampment();
		encampmentType = Clock.getRoundNum() < 10 ? RobotType.SHIELDS : RobotType.SUPPLIER;
		while(true){
			try{
				if(rc.isActive()){
					if(rc.canSenseSquare(target)){
						Robot r = (Robot) rc.senseObjectAtLocation(target);
						if(r != null && !r.equals(rc.getRobot())){
							encampments = rc.senseEncampmentSquares(currLocation, 1000, Team.NEUTRAL);
							target = setRushEncampment();
						}
					}
					moveTo(target);
				}
			} catch ( Exception e){
				e.printStackTrace();
			}
		}
	}

	private static boolean prettyDamnClose()throws GameActionException{
		return (Math.abs(enemyHQ.x - allyHQ.x)<5 || Math.abs(enemyHQ.y - allyHQ.y)<5) && HQSeparation<=900;
	}
	private static void strictRush() {
		System.out.println("Strict rush");
		MapLocation rallyPoint = findRallyPoint();
		while (true) {
			try {
				if (rc.isActive()) {
					enemyRobots = rc.senseNearbyGameObjects(Robot.class, 14,
							enemyTeam);
					allyRobots = rc.senseNearbyGameObjects(Robot.class, 20,
							myTeam);
					currLocation = rc.getLocation();
					if (Clock.getRoundNum() > 201) {
						if (enemyRobots.length == 0) {
							moveTo(enemyHQ);
						} else {
							moveTo(findClosest(enemyRobots));
						}
					} else {
						boolean tooFar = currLocation.distanceSquaredTo(enemyHQ) > currLocation.distanceSquaredTo(allyHQ);
						if (enemyRobots.length != 0
								&& (allyRobots.length > 7 || allyRobots.length >= enemyRobots.length)
								&& !tooFar) {
							moveTo(findClosest(enemyRobots));
						} else if (tooFar) {
							moveTo(rallyPoint);
						} else {
							if (rc.senseMine(currLocation) == null)
								rc.layMine();
							else {
								Direction dir = rand.nextInt(10) < 5 ? currLocation
										.directionTo(enemyHQ) : directions[rand
										.nextInt(63) / 8];
								moveTo(currLocation.add(dir));
							}
						}
					}
				}

			} catch (Exception e) {
				System.out.println("Strict Rush Error");
				e.printStackTrace();
				rc.breakpoint();
			}
			rc.yield();
		}
	}

	private static MapLocation findRallyPoint() {
		return new MapLocation((enemyHQ.x + 3 * allyHQ.x) / 4,
				(enemyHQ.y + 3 * allyHQ.y) / 4);
	}

	private static void normalRun() throws GameActionException {
		// System.out.println("Normal run");
		if (Clock.getRoundNum() < 350)// first 350 rounds
			strat = rand.nextInt(42)<21? 1:2;
		else {
			/*
			 * id=rc.getRobot().getID(); int determ=id%12; if(determ<10)strat=0;
			 * else strat = determ==10 ? 1 : 2;
			 */
			dissenter = rand.nextInt(100);
			if (dissenter > 32) {
				strat = 0;
				if (dissenter >= 95)
					coefficientOfSuperman = 20;
			} else if (dissenter > 14)
				strat = 1;
			else
				strat = 2;
		}

		while (true) {
			try {

				if (defuseCost > 5 && rc.hasUpgrade(Upgrade.DEFUSION))
					defuseCost = 5;

				if (rc.isActive()) {
					round = Clock.getRoundNum();
					distToAlly_2 = currLocation.distanceSquaredTo(allyHQ);
					distToEnemy_2 = currLocation.distanceSquaredTo(enemyHQ);
					enemyRobots = rc.senseNearbyGameObjects(Robot.class,
							1000000, enemyTeam);
					allyRobots = rc.senseNearbyGameObjects(Robot.class, 14,
							myTeam);
					info = rc.senseRobotInfo(rc.getRobot());
					currLocation = info.location;
					if (round < 500)
						defaultStrategy(false);
					else if (round < 700)
						defaultStrategy(true);
					else
						/* if(round<1200) */defaultStrategy(false);
					// else letsMakeANuke();

				}

			} catch (Exception e) {
				System.out.println("Soldier Error");
				e.printStackTrace();
				rc.breakpoint();
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
			if (rc.canSenseObject(arobot)) {
				RobotInfo arobotInfo = rc.senseRobotInfo(arobot);
				int dist = arobotInfo.location.distanceSquaredTo(rc
						.getLocation());
				if (dist < closestDist) {
					closestDist = dist;
					closestEnemy = arobotInfo.location;
				}
			}
		}
		return closestEnemy;
	}

	private static void findClosestTypes()
			throws GameActionException {
		//SHIELDS: mod = 1;
		//MEDBAY: mod = 2;
		int broadCast = rc.readBroadcast(getChannel(Clock.getRoundNum(), 1));
		if(properMessage(broadCast)){
			shieldLoc = messageLocation(broadCast);
			distToShield = rc.getLocation().distanceSquaredTo(shieldLoc);
			closestShield = (Robot) rc.senseObjectAtLocation(shieldLoc);
		} else {
			shieldLoc = null;
			distToShield = 999999;
			closestShield = null;
		}
		broadCast = rc.readBroadcast(getChannel(Clock.getRoundNum(),2));
		if(properMessage(broadCast)){
			medLoc = messageLocation(broadCast);
			distToMed = rc.getLocation().distanceSquaredTo(medLoc);
			closestMed = (Robot) rc.senseObjectAtLocation(medLoc);
		}
		else{
			medLoc=null;
			distToMed = 999999;
			closestMed=null;
		}
	}

	private static MapLocation findClosest(MapLocation[] points)
			throws GameActionException {
		MapLocation cur = rc.getLocation();
		int close = 999999, closestInd = -1;
		for (int i = 0; i < points.length; i++) {
			int dis = cur.distanceSquaredTo(points[i]);
			if (dis < close) {
				close = dis;
				closestInd = i;
			}
		}
		if (closestInd == -1)
			return cur;
		else
			return points[closestInd];
	}

	/*
	 * Rushes to go capture encampments
	 */
	private static void initialRush(int clockLim) {
		MapLocation target;
		mode = 1;

		try {
			/*
			 * badmines=rc.senseNonAlliedMineLocations(allyHQ,12);
			 * while((badmines =
			 * rc.senseNonAlliedMineLocations(allyHQ,12)).length > 0) {
			 * moveTo(findClosest(badmines)); rc.yield(); }
			 */
			encampments = rc.senseEncampmentSquares(currLocation, 1000,
					Team.NEUTRAL);
			target = setEncampmentTarget(null);
			while (target != null && Clock.getRoundNum() < clockLim) {
				if (defuseCost > 5 && rc.hasUpgrade(Upgrade.DEFUSION))
					defuseCost = 5;

				target = rushCode(target);

				rc.yield();
			}

		} catch (Exception e) {
			System.out.println("Rush Error");
			e.printStackTrace();
			rc.breakpoint();
		}

		mode = 0;
	}

	private static MapLocation rushCode(MapLocation target)
			throws GameActionException {
		if (rc.isActive()) {
			if (rc.canSenseSquare(target)) {
				Robot r = (Robot) rc.senseObjectAtLocation(target);
				if (r != null && !r.equals(rc.getRobot())) {
					encampments = rc.senseEncampmentSquares(currLocation, 1000,
							Team.NEUTRAL);
					target = setEncampmentTarget(null);
				}
			}
			moveTo(target);
		}
		return target;
	}

	// Looks around randomly while laying mines and killing any enemy robots in
	// sight.*********************
	private static void initialRoam(int clockLim) {
		currLocation = rc.getLocation();
		mode = 3;
		while (Clock.getRoundNum() < clockLim) {
			try {
				if (defuseCost > 5 && rc.hasUpgrade(Upgrade.DEFUSION))
					defuseCost = 5;

				if (rc.isActive()) {
					Robot[] enemyRobots = rc.senseNearbyGameObjects(
							Robot.class, 1000000, enemyTeam);

					if (enemyRobots.length != 0) {
						int temp = mode;
						mode = 0;
						moveTo(findClosest(enemyRobots));
						mode = temp;
					} else {
						if (currLocation.distanceSquaredTo(enemyHQ) <= HQSeparation + 8
								&& rc.senseMine(currLocation) == null) {
							rc.layMine();
						} else {
							// Choose a random direction, and move that way if
							// possible
							Direction dir = rand.nextInt(10) < 5 ? currLocation
									.directionTo(enemyHQ) : directions[rand
									.nextInt(63) / 8];
							moveTo(currLocation.add(dir));
						}
					}

				}
			} catch (Exception e) {
				System.out.println("Roam Error");
				e.printStackTrace();
				rc.breakpoint();
			}
			rc.yield();
		}
		mode = 0;
	}

	/*
	 * finds the nearest encampment for use
	 */
	private static void defaultStrategy(boolean defense)
			throws GameActionException {
		rc.setIndicatorString(1, strategies[strat] + coefficientOfSuperman);
		if (allyRobots.length > 5)
			moveTo(enemyHQ);
		else if (info.shields > 4000) {
			strat = 0;
			coefficientOfSuperman = 20;
		} else {
			if (strat > 0) {
				if (strat == 1)// if camper
					beCamper(defense);
				else
					// if miner
					beMiner();
			} else
				// if attacker
				beAttacker(defense, true);
		}

	}

	private static void beCamper(boolean defense) throws GameActionException {
		encampments = rc.senseEncampmentSquares(rc.getLocation(), 1000,
				Team.NEUTRAL);
		if (encampments.length == 0) {
			strat = 0;
			beAttacker(defense, true);
		} else {
			MapLocation target = setEncampmentTarget(null);
			int temp = mode;
			mode = 1;
			while (target != null) {
				if (defuseCost > 5 && rc.hasUpgrade(Upgrade.DEFUSION))
					defuseCost = 5;
				
				target = rushCode(target);
				rc.yield();
			}
			mode = temp;
		}
	}

	private static void beMiner() throws GameActionException {
		if (rc.senseMine(currLocation) == null
				&& currLocation.distanceSquaredTo(enemyHQ) <= HQSeparation + 15) {
			rc.layMine();
		} else {
			Direction dir = rand.nextInt(10) < 5 ? currLocation
					.directionTo(enemyHQ) : directions[rand.nextInt(63) / 8];
			moveTo(currLocation.add(dir));
		}
	}

	/*
	 * private static Direction findNonRetardedDir() {
	 * 
	 * }
	 */
	private static MapLocation[] validSquares(MapLocation L, int r) {
		ArrayList<MapLocation> list = new ArrayList<MapLocation>();
		for (int i = L.x + 1; i < mapWidth; i++) {
			for (int j = L.y + 1; j < mapHeight; j++)
				if ((i - L.x) * (i - L.x) + (j - L.y) * (j - L.y) <= r)
					list.add(new MapLocation(i, j));
		}
		return list.toArray(new MapLocation[list.size()]);
	}

	private static void beAttacker(boolean defense, boolean getShield)
			throws GameActionException {
		if (enemyRobots.length == 0) { // No nearby enemies
			// If we have a big enough possy to go "exploring"
			findClosestTypes();
			if (closestMed !=null 
					&& distToMed<distToEnemy_2
					&& info.energon<10)
				moveTo(medLoc);
			else if (getShield
					&& closestShield != null
					&& distToShield < distToEnemy_2
					&& info.shields < 100 * coefficientOfSuperman)
				moveTo(shieldLoc);
			else if (rc.senseNearbyGameObjects(Robot.class, 30, myTeam).length > (round < 501 ? 5
					: 10))
				if (defense
						&& !nonoGoAhead
						&& rc.senseMine(currLocation) == null
						&& distToEnemy_2 < allyHQ.distanceSquaredTo(enemyHQ) + 10) {
					rc.layMine();
					nonoGoAhead = true;
				} else
					moveTo(enemyHQ);
			else {
				if (distToAlly_2 * 10 < mapDiag_2) {
					moveTo(allyHQ);
				} else if (distToEnemy_2 * 10 < mapDiag_2) {
					moveTo(enemyHQ);
				} else {
					if (distToEnemy_2 <= HQSeparation
							&& rc.senseMine(currLocation) == null) {
						rc.layMine();
					} else {
						moveTo(enemyHQ);
					}
				}
			}
		} else {// found an enemy
			int temp = mode;
			mode = 0;
			moveTo(findClosest(enemyRobots));
			mode = temp;
		}
	}

	private static MapLocation setEncampmentTarget(MapLocation prevTarget)
			throws GameActionException {

		MapLocation ret = null;
		int distance = 1000000;

		for (MapLocation loc : encampments) {// Each iteration at worst 2050
												// bytecode

			if (Clock.getBytecodesLeft() < 3500)// Ensures that the turn can be
												// finished
				break;

			if (loc.equals(prevTarget)) {
				continue;
			} else if (rc.canSenseSquare(loc)) {
				GameObject obj = rc.senseObjectAtLocation(loc);
				if ((obj != null && obj.getTeam() == myTeam)
						|| hqClustered(loc))
					continue;
			}

			int temp = loc.distanceSquaredTo(currLocation);
			if (distance > temp) {
				distance = temp;
				ret = loc;
			}
		}

		

		return ret;
	}

	/*
	 * Costs ~60 bytecode
	 */
	private static void setEncampmentType(int dist_to_aHQ_2, int dist_to_eHQ_2)
			throws GameActionException {
		if (dist_to_eHQ_2 < Math.min(HQSeparation / 2, 63))
			encampmentType = RobotType.ARTILLERY;
		else if (dist_to_aHQ_2 < Math.min(HQSeparation / 3, 63)) {
			encampmentType=RobotType.ARTILLERY;
		} else if (dist_to_eHQ_2 > dist_to_aHQ_2 + HQSeparation) {// Behind HQ
																	// to enemy
																	// HQ
			encampmentType = rand.nextInt(10) < 5 ? RobotType.SUPPLIER
					: RobotType.GENERATOR;
		} else {
			int random = rand.nextInt(5);
			switch (random) {
			case 2:
				encampmentType = RobotType.SUPPLIER;
				break;
			case 4:
				encampmentType = RobotType.GENERATOR;
				break;
			default: encampmentType = RobotType.ARTILLERY;
			}
		}

	}

	/*
	 * Worst Case BYTECODE Cost: 2000
	 */
	private static boolean hqClustered(MapLocation loc)
			throws GameActionException {
		if (!allyHQ.isAdjacentTo(loc))
			return false;

		// Counts number of encampments currently next to HQ
		Robot[] robots = rc.senseNearbyGameObjects(Robot.class, allyHQ, 3,
				myTeam);
		int numEncamp = 0;
		for (Robot r : robots) {
			if (r.equals(rc.getRobot()))
				continue;
			numEncamp++;
		}

		// Counts how many non-damaging squares adjacent to HQ
		int openSpace = 0;
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				if (!inRange(allyHQ.x + i, allyHQ.y + j) || (i == 0 && j == 0))
					continue;

				Team mine = rc.senseMine(allyHQ.add(i, j));
				if (mine == null || mine == myTeam)
					openSpace++;
			}
		}

		return numEncamp * 2 >= openSpace;
	}

	/*
	 * makes sure that a target is within memory bounds
	 */
	private static boolean inRange(int x, int y) {
		return x > -1 && y > -1 && x < rc.getMapWidth()
				&& y < rc.getMapHeight();
	}

	/*
	 * Ensures that there is enough power for the rest of the team to move if
	 * capturing
	 */
	private static boolean enoughPowerToCapture() {
		return rc.senseCaptureCost() < (rc.getTeamPower() - rc
				.senseNearbyGameObjects(Robot.class, 1000000, myTeam).length * 2);
	}

	/*
	 * Moves to a location. If mode is for capturing, captures. if mode is for
	 * defusing, defuses. For now assume Mode 0 = attack -> requires adjacent
	 * mode 1 = capture -> requires on top of mode 2 = defuse mine -> requires
	 * adjacent mode 3 = plant mine -> requires on top of
	 * 
	 * WORST CASE BYTECODE COST: ~1600 (with goToLocation code included)
	 * 
	 * NOTE: the current algorithm has the units park if target is reached
	 * instead of cycle around randomly. Before the cycling did less damage but
	 * could potentially save units (though only very slightly) from sustaining
	 * damage. Cycling around the target specifically would have a mix of both
	 * effects, but could cost lots of bytecode. Also, the current goToLocation
	 * method is best with group movement and awful for singular movement. Will
	 * need to tweek the heuristics to check some more.... HMMMMM
	 */
	private static int mode;
	private static RobotType encampmentType;

	private static void moveTo(MapLocation target) throws GameActionException {
		if (target == null)
			return;

		currLocation = rc.getLocation();
		Team mine = rc.senseMine(currLocation);
		int[] dirs = target.equals(enemyHQ) ? DIRS_3 : DIRS_5;
		if(mine == enemyTeam){//Enemy mine underneath -> MOVE OFF
			goToLocation(target, true, dirs);
		} else if(mode % 2 == 0){//Attack | defuse modes
			if(mode == 2 && currLocation.isAdjacentTo(target) && mine != myTeam){//Target is in front
				rc.defuseMine(target);
			} else {//Target is far away
				goToLocation(target, false, dirs);
			}
		} else {//capture | plant modes
			if(currLocation.equals(target)){//at location
				if(mode == 1 && enoughPowerToCapture()){
						setEncampmentType(target.distanceSquaredTo(allyHQ),
								target.distanceSquaredTo(enemyHQ));
						rc.setIndicatorString(0, "" + encampmentType);
					rc.captureEncampment(encampmentType);
				} else if(mine == null){
					rc.layMine();
				}
			} else if(currLocation.isAdjacentTo(target) && rc.senseMine(target) == enemyTeam){
				rc.defuseMine(target);
			} else{//target is far away
				goToLocation(target, false, dirs);
			}
		}
	}

	/*
	 * Tries to make a move into the next location. If currently on a mine, then
	 * allow movement in all directions, else only allow movement in 5 specific directions
	 * 
	 * WORST CASE BYTECODE COST: 1478
	 * 
	 * UPDATES and BYTECODE added from update: 
	 * Include Manhattan Distance in comparison		+ 80
	 * Prevent movement to previous square			+100
	 * Allow backwards movement if already on mine	+350 //because 8 directions can be checked instead of 5 directions
	 * 
	 * NOTES:
	 * Unsure how algorithm will work when taking into account mass movement. However
	 * there are many upgrades that can be done.
	 */
	private static void goToLocation(MapLocation target, boolean hasMine, int[] dirs) throws GameActionException{
		Direction dir = findBestPath(target, hasMine ? DIRS_8 : dirs);
		prevLocation = new MapLocation(currLocation.x, currLocation.y);
		if(dir != null){
			MapLocation ahead = currLocation.add(dir);
			Team m = rc.senseMine(ahead);
			if(m != null && m != myTeam){
				rc.defuseMine(ahead);
			} else{
				rc.move(dir);
			}
		}
	}
	
	//All directions
	protected static Direction[] directions = new Direction[]{
		Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
		Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.SOUTH_WEST
	};
	
	//Direction cycling options
	protected static final int[] DIRS_3 = new int[]{0, 1, -1};// Forces unit to always move forward
	protected static final int[] DIRS_5	= new int[]{0, 1, -1, 2, -2};
	protected static final int[] DIRS_8	= new int[]{0, 1, -1, 2, -2, 3, -3, 4};
	
	/*
	 * Finds the best next move
	 */
	private static Direction findBestPath(MapLocation target, int[] allowedSteps) {
		Direction dir = currLocation.directionTo(target);
		Direction lookAt = dir;

		Node first = null;
		for (int d : allowedSteps) {
			lookAt = directions[(dir.ordinal() + d + 8) % 8];
			MapLocation next = currLocation.add(lookAt);
			if (rc.canMove(lookAt) && !next.equals(prevLocation)) {
				Node temp = new Node(currLocation.add(lookAt), target, lookAt);
				if (first == null || first.compareTo(temp) > 0)
					first = temp;
			}
		}
		if (first == null) {
			return null;
		}
		return first.temp;
	}

	private static class Node {

		public int cost;
		public int cHeuristic, mHeuristic;
		public Team mine;
		public Direction temp;

		public Node(MapLocation l, MapLocation target, Direction d) {
			temp = d;
			mine = rc.senseMine(l);
			int dx = Math.abs(l.x - target.x), dy = Math.abs(l.y - target.y);
			cHeuristic = Math.max(dx, dy);// Chebyshev heuristic
			mHeuristic = dx + dy; // Manhattan Heuristic
			cost = ((mine != null && mine != myTeam) ? defuseCost : 0);
		}

		/*
		 * if the Chebyshev heuristic difference is 0, then check by Manhattan
		 * heuristic
		 */
		public int compareTo(Node p) {
			return cost
					- p.cost
					+ (cHeuristic == p.cHeuristic ? mHeuristic - p.mHeuristic
							: cHeuristic - p.cHeuristic);
		}
	}
}
