package ai.handling.army;

import java.util.ArrayList;

import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;
import ai.core.Debug;
import ai.core.XVR;
import ai.handling.map.MapExploration;
import ai.protoss.ProtossGateway;

public class StrengthEvaluator {

	private static XVR xvr = XVR.getInstance();
	private static int BATTLE_RADIUS_ENEMIES = 11;
	private static int BATTLE_RADIUS_ALLIES = 9;

	private static boolean changePlanToBuildAntiAirUnits = false;

	public static double calculateStrengthRatioFor(Unit unit) {
		ArrayList<Unit> enemyUnits = getEnemiesNear(unit);
		if (enemyUnits.isEmpty()) {
			return -1;
		}

		ArrayList<Unit> ourUnits = getOurUnitsNear(unit);

		double ourHitPoints = calculateHitPointsOf(ourUnits);
		double enemyHitPoints = calculateHitPointsOf(enemyUnits);

		double ourAttack = calculateTotalAttackOf(ourUnits);
		double enemyAttack = calculateTotalAttackOf(enemyUnits);

		double ourStrength = ourHitPoints / (enemyAttack + 0.1);
		double enemyStrength = enemyHitPoints / (ourAttack + 0.1);

		double ratio = ourStrength / enemyStrength;

//		System.out.println("\n========= RATIO: " + ratio);
//		System.out.println("WE: " + ourStrength);
//		for (Unit unit : ourUnits) {
//			System.out.println("   " + unit.getName() + " -> "
//					+ unit.getGroundAttackNormalized());
//		}
//		System.out.println("ENEMY: " + enemyStrength);
//		for (Unit unit : enemyUnits) {
//			System.out.println("   " + unit.getName() + " -> "
//					+ unit.getGroundAttackNormalized());
//		}

		return ratio;
	}

	private static double calculateTotalAttackOf(ArrayList<Unit> units) {
		int total = 0;
		for (Unit unit : units) {
			if (unit.isDefensiveBuilding()) {
				total += 8;
				if (unit.getType().isBunker()) {
					total += 32;
				}
			}
			total += unit.getGroundAttackNormalized();
		}
		return total;
	}

	private static double calculateHitPointsOf(ArrayList<Unit> units) {
		int total = 0;
		for (Unit unit : units) {
			UnitType type = unit.getType();

			if (unit.isCompleted()
					&& (!type.isBuilding() || unit.isDefensiveBuilding())) {
				total += unit.getHitPoints() + unit.getShields();
				if (type.isMedic()) {
					total += 60;
				}
			}
		}
		return total;
	}

	private static ArrayList<Unit> getEnemiesNear(Unit ourUnit) {
		return xvr.getUnitsInRadius(ourUnit, BATTLE_RADIUS_ENEMIES,
				xvr.getEnemyArmyUnitsIncludingDefensiveBuildings());
	}

	private static ArrayList<Unit> getOurUnitsNear(Unit ourUnit) {
		return xvr.getUnitsInRadius(ourUnit, BATTLE_RADIUS_ALLIES,
				xvr.getArmyUnitsIncludingDefensiveBuildings());
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

	public static boolean isStrengthRatioFavorableFor(Unit unit) {
		double strengthRatio = calculateStrengthRatioFor(unit);
		if (strengthRatio < 0) {
			return true;
		}
		return !(strengthRatio < 1.6);
	}
	
	public static boolean isStrengthRatioCriticalFor(Unit unit) {
		double strengthRatio = calculateStrengthRatioFor(unit);
		if (strengthRatio < 0) {
			return false;
		}
		return strengthRatio < 0.7;
	}

}
