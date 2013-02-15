package team109BackUp;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;

public abstract class MyRobot {

	protected static int hashChannel(int x, int y, int r){
		x /= 10;
		y /= 10;
		int d1 = r % 6;
		int d2 = r * r % 100;
		StringBuilder b = new StringBuilder();
		b.append(d1);
		b.append(x);
		b.append(y);
		b.append(String.format("%02d", d2));
		return Integer.parseInt(b.toString());
	}
	
	protected static int getDistressMessage(MapLocation l, int priority) {
		StringBuilder b = new StringBuilder();
		b.append(1);// Security #
		b.append(priority);// Priority of Message
		b.append(3);// # Soldiers needed
		b.append(String.format("%04d", MapLocationToInt(l)));// Map Location as Int
		b.append(1);// Soldier mode
		b.append(47);// Security #

		return Integer.parseInt(b.toString());
	}
	
	protected static boolean properMessage(int i){
		String s = i + "";
		return s.matches("1\\d3\\d\\d\\d\\d147");
	}
	
	protected static MapLocation messageLocation(int i){
		return IntToMapLocation(i/1000 %10000);
	}
	
	protected static void distressSignal(RobotController rc, int priority) throws GameActionException{
		//Get nearby enemies
		Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, 33,
				rc.getTeam().opponent());
		
		if (enemies.length > 0) {//If enemies nearby
			int channel = hashChannel(rc.getLocation().x, rc.getLocation().y, Clock.getRoundNum());
			int currBroadcast = rc.readBroadcast(channel);
			
			//if security digits don't match or the original priority is lower
			if((currBroadcast % 100 != 47 && currBroadcast/1000000000 != 1) || 
					currBroadcast / 100000000 % 10 < priority){
				rc.broadcast(channel, getDistressMessage(rc.senseLocationOf(enemies[0]), priority));
				rc.setIndicatorString(0, "Posted Distress Signal");
			}
			
		} else {
			rc.setIndicatorString(0, "No problem");
		}
	}
	
	protected static int getChannel(int round, int i) {
		return (round * 3613 + i) % GameConstants.BROADCAST_MAX_CHANNELS;
	}

	protected static int MapLocationToInt(MapLocation loc) {
		if (loc == null)
			return 0;
		return loc.x * 100 + loc.y;
	}
	
	protected static MapLocation IntToMapLocation(int i) {
		int y = i % 100;
		int x = i / 100;
		if (x == 0 && y == 0) {
			return null;
		} else {
			return new MapLocation(x, y);
		}
	}

	protected static boolean includes(MapLocation[] alliedEncampments, MapLocation rallypoint) {
		for (MapLocation loc : alliedEncampments) {
			if (loc.equals(rallypoint))
				return true;
		}
		return false;
	}
	
	/*************************************HQ Specific Channels*************************************/
	
	protected static int getEmergencyChannel(int r){
		int d1 = r % 6;
		int d2 = r * r % 100;
		int d3 = 80 + r % 20;
		StringBuilder b = new StringBuilder();
		b.append(d1);
		b.append(String.format("%02d", d3));
		b.append(String.format("%02d", d2));
		return Integer.parseInt(b.toString());
	}
	
	protected static int attackChannel1(int r){
		int d1 = r % 6;
		int d2 = r * r % 100;
		StringBuilder b = new StringBuilder();
		b.append(d1);
		b.append(String.format("%02d", d2));
		b.append(d1);
		b.append(3);
		return Integer.parseInt(b.toString());
	}
	
	protected static int attackChannel2(int r){
		int d1 = r % 6;
		int d2 = r * r % 100;
		StringBuilder b = new StringBuilder();
		b.append(d1);
		b.append(String.format("%02d", d2));
		b.append(d1);
		b.append(7);
		return Integer.parseInt(b.toString());
	}
	
	/*************************************HQ Specific Channels*************************************/
}
