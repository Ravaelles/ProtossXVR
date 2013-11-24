package ai.handling.army;

import java.util.ArrayList;

import ai.core.Debug;
import ai.core.XVR;
import ai.handling.map.MapExploration;
import ai.protoss.ProtossGateway;
import jnibwapi.model.Unit;

public class StrengthEvaluator {

	private static XVR xvr = XVR.getInstance();
	private static int BATTLE_RADIUS = 12;
	
	private static boolean changePlanToBuildAntiAirUnits = false;

	public static double calculateOurStrengthRatio(Unit ourUnit) {
		ArrayList<Unit> ourUnits = getOurUnitsNear(ourUnit);
		ArrayList<Unit> enemyUnits = getEnemiesNear(ourUnit);

		double ourHitPoints = calculateHitPointsOf(ourUnits);
		double enemyHitPoints = calculateHitPointsOf(enemyUnits);

		double ourAttack = calculateTotalAttackOf(ourUnits);
		double enemyAttack = calculateTotalAttackOf(enemyUnits);

		double ourStrength = ourHitPoints / enemyAttack;
		double enemyStrength = enemyHitPoints / ourAttack;

		return ourStrength / enemyStrength;
	}

	private static double calculateTotalAttackOf(ArrayList<Unit> units) {
		int total = 0;
		for (Unit unit : units) {
			total += unit.getGroundAttack();
		}
		return total;
	}

	private static double calculateHitPointsOf(ArrayList<Unit> units) {
		int total = 0;
		for (Unit unit : units) {
			total += unit.getHitPoints();
		}
		return total;
	}

	private static ArrayList<Unit> getEnemiesNear(Unit ourUnit) {
		return xvr.getUnitsInRadius(BATTLE_RADIUS, ourUnit.getX(),
				ourUnit.getY(), xvr.getEnemyArmyUnitsIncludingDefensiveBuildings());
	}

	private static ArrayList<Unit> getOurUnitsNear(Unit ourUnit) {
		return xvr.getUnitsInRadius(BATTLE_RADIUS, ourUnit.getX(),
				ourUnit.getY(), xvr.getArmyUnitsIncludingDefensiveBuildings());
	}

	// ==================================
	
	public static void checkIfBuildMoreAntiAirUnits() {
		if (!changePlanToBuildAntiAirUnits) {
			boolean changeOfPlans = false;
			
			if (countEnemyAirUnits() > 5) {
				Debug.message(xvr, "Start building Anti-Air units");
				changeOfPlans = true;
			}
			
			if (changeOfPlans) {
				changePlanToBuildAntiAirUnits = true;
				ProtossGateway.changePlanToAntiAir();
			}
		}
	}

	private static int countEnemyAirUnits() {
		int counter = 0;
		for (Unit enemy : MapExploration.getEnemyUnitsDiscovered()) {
			if (enemy.getType().isFlyer() && enemy.getAirWeaponCooldown() > 0) {
				counter++;
			}
		}
		return counter;
	}
	
}
