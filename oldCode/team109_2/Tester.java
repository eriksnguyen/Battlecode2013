package team109_2;

import java.util.*;
import battlecode.common.*;

public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Random oRand = new Random(1977);
		for(int i = 0; i < 10; i++){
			System.out.println(oRand.nextInt(100));
		}
	}

}
