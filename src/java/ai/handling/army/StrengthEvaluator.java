package ai.handling.army;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;
import ai.core.Debug;
import ai.core.XVR;
import ai.handling.map.MapExploration;
import ai.protoss.ProtossGateway;

public class StrengthEvaluator {

	private static XVR xvr = XVR.getInstance();
	private static final int BATTLE_RADIUS_ENEMIES = 11;
	private static final int BATTLE_RADIUS_ALLIES = 9;
	private static final double CRITICAL_RATIO_THRESHOLD = 0.7;
	private static final double ENEMY_RANGE_WEAPON_STRENGTH_BONUS = 1.9;
	private static final int RANGE_BONUS_IF_ENEMY_DEF_BUILDING_NEAR = 6;

	private static boolean changePlanToBuildAntiAirUnits = false;

	private static int _rangeBonus = 0;

	/**
	 * Calculates ground advantage for given unit, based on nearby enemies and
	 * allied units. Value less than 1 means we would probably loss the fight.
	 * For convenience, use other static methods of this class that return
	 * boolean type.
	 */
	public static double calculateStrengthRatioFor(Unit unit) {
		_rangeBonus = 0;

		// Define enemy units nearby and our units nearby
		ArrayList<Unit> enemyUnits = getEnemiesNear(unit);

		// If there's no units return -1, it means it's fully safe.
		if (enemyUnits.isEmpty()) {
			_rangeBonus = 0;
			return -1;
		}

		// If there's at least one building like cannon, sunken colony, bunker,
		// then increase range of units search and look again for enemy units.
		if (isOneOfUnitsDefensiveBuilding(enemyUnits)) {
			_rangeBonus += RANGE_BONUS_IF_ENEMY_DEF_BUILDING_NEAR;
			enemyUnits = getEnemiesNear(unit);
		}

		ArrayList<Unit> ourUnits = getOurUnitsNear(unit);

		// ==================================
		// Calculate hit points and ground attack values of units nearby

		double ourHitPoints = calculateHitPointsOf(ourUnits);
		double enemyHitPoints = calculateHitPointsOf(enemyUnits);

		double ourAttack = calculateTotalAttackOf(ourUnits, false);
		double enemyAttack = calculateTotalAttackOf(enemyUnits, true);

		// ==================================
		// Calculate "strength" for us and for the enemy, being correlated
		// values of hit points and attack values
		double ourStrength = ourHitPoints / (enemyAttack + 0.1);
		double enemyStrength = enemyHitPoints / (ourAttack + 0.1);

		// Final strength value for us is ratio (comparison) of our and enemy
		// calculated strengths
		// Its range is (0.0; Infinity).
		// Ratio 1.0 means forces are perfectly equal (according to this metric)
		// Ratio < 1.0 means
		double ratio = ourStrength / enemyStrength;

		// System.out.println("\n========= RATIO: " + ratio);
		// System.out.println("WE: " + ourStrength);
		// for (Unit unit : ourUnits) {
		// System.out.println("   " + unit.getName() + " -> "
		// + unit.getGroundAttackNormalized());
		// }
		// System.out.println("ENEMY: " + enemyStrength);
		// for (Unit unit : enemyUnits) {
		// System.out.println("   " + unit.getName() + " -> "
		// + unit.getGroundAttackNormalized());
		// }

		_rangeBonus = 0;
		return ratio;
	}

	private static boolean isOneOfUnitsDefensiveBuilding(Collection<Unit> units) {
		for (Unit unit : units) {
			if (unit.isDefensiveGroundBuilding()) {
				return true;
			}
		}
		return false;
	}

	private static double calculateTotalAttackOf(ArrayList<Unit> units,
			boolean forEnemy) {
		int total = 0;
		// int dragoons = 0;
		for (Unit unit : units) {
			double attackValue = unit.getGroundAttackNormalized();
			UnitType type = unit.getType();
			if (type.getGroundWeapon().getMaxRange() > 100) {
				attackValue *= ENEMY_RANGE_WEAPON_STRENGTH_BONUS;
			}
			if (forEnemy) {
				total += attackValue;
				if (unit.isDefensiveGroundBuilding()) {
					total += 24;
					if (type.isBunker()) {
//						total += 32; // That's too optmistics, people repair them!
						total += 50;
					}
				}
			} else {
				total += attackValue;
			}
		}
		return total;
	}

	private static double calculateHitPointsOf(ArrayList<Unit> units) {
		int total = 0;
		for (Unit unit : units) {
			UnitType type = unit.getType();

			if (unit.isCompleted()
					&& (!type.isBuilding() || unit.isDefensiveGroundBuilding())) {
				total += unit.getHitPoints() + unit.getShields();
				if (type.isMedic()) {
					total += 60;
				}
			}
		}
		return total;
	}

	private static ArrayList<Unit> getEnemiesNear(Unit ourUnit) {
		return xvr.getUnitsInRadius(ourUnit, BATTLE_RADIUS_ENEMIES
				+ _rangeBonus,
				xvr.getEnemyArmyUnitsIncludingDefensiveBuildings());
	}

	private static ArrayList<Unit> getOurUnitsNear(Unit ourUnit) {
		ArrayList<Unit> unitsInRadius = xvr.getUnitsInRadius(ourUnit,
				BATTLE_RADIUS_ALLIES,
				xvr.getArmyUnitsIncludingDefensiveBuildings());
		for (Iterator<Unit> iterator = unitsInRadius.iterator(); iterator
				.hasNext();) {
			Unit unit = (Unit) iterator.next();
			if (unit.getShields() < 5) {
				iterator.remove();
			}
			if (unit.isDefensiveGroundBuilding()) {
				if (xvr.getDistanceBetween(unit, ourUnit) >= 3) {
					iterator.remove();
				}
			}
		}
		return unitsInRadius;
		// return xvr.getUnitsInRadius(ourUnit, BATTLE_RADIUS_ALLIES,
		// xvr.getArmyUnitsIncludingDefensiveBuildings());
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

	/**
	 * Will this unit (and its companions nearby) win the fight with nearby
	 * enemies, with huge probability. This situation is typical and strong
	 * go-for-it, as it means we are decisively stronger than the enemy.
	 */
	public static boolean isStrengthRatioFavorableFor(Unit unit) {
		double strengthRatio = calculateStrengthRatioFor(unit);
		if (strengthRatio < 0) {
			return true;
		}
		return !(strengthRatio < 1.6);
	}

	/**
	 * Will this unit (and its companions nearby) lose the fight with nearby
	 * enemies almost certainly. This situation is a sure-loss.
	 */
	public static boolean isStrengthRatioCriticalFor(Unit unit) {
		double strengthRatio = calculateStrengthRatioFor(unit);
		if (strengthRatio < 0) {
			return false;
		}
		return strengthRatio < CRITICAL_RATIO_THRESHOLD;
	}

}
