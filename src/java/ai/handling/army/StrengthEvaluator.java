package ai.handling.army;

import java.util.ArrayList;
import java.util.Iterator;

import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;
import ai.core.Debug;
import ai.core.XVR;
import ai.handling.map.MapExploration;
import ai.managers.StrategyManager;
import ai.protoss.ProtossGateway;

public class StrengthEvaluator {

	private static XVR xvr = XVR.getInstance();
	private static final int BATTLE_RADIUS_ENEMIES = 12;
	private static final int BATTLE_RADIUS_ALLIES = 11;
	private static final double CRITICAL_RATIO_THRESHOLD = 0.7;
	private static final double FAVORABLE_RATIO_THRESHOLD = 1.6;
	private static final double ENEMY_RANGE_WEAPON_STRENGTH_BONUS = 1.9;
	private static final int RANGE_BONUS_IF_ENEMY_DEF_BUILDING_NEAR = 6;
	private static final int DEFENSIVE_BUILDING_ATTACK_BONUS = 24;
	// private static final int IF_CANNONS_WAIT_FOR_N_UNITS = 7;

	private static boolean changePlanToBuildAntiAirUnits = false;

	private static int _rangeBonus = 0;
	private static ArrayList<Unit> _ourUnits;

	/**
	 * Calculates ground advantage for given unit, based on nearby enemies and
	 * allied units. Value less than 1 means we would probably loss the fight.
	 * For convenience, use other static methods of this class that return
	 * boolean type.
	 */
	public static double calculateStrengthRatioFor(Unit unit) {
		_rangeBonus = 0;

		// If there's at least one building like cannon, sunken colony, bunker,
		// then increase range of units search and look again for enemy units.
		if (xvr.getEnemyDefensiveGroundBuildingNear(unit.getX(), unit.getY(), BATTLE_RADIUS_ENEMIES
				+ RANGE_BONUS_IF_ENEMY_DEF_BUILDING_NEAR) != null) {
			_rangeBonus += RANGE_BONUS_IF_ENEMY_DEF_BUILDING_NEAR;
		}

		// Define enemy units nearby and our units nearby
		ArrayList<Unit> enemyUnits = getEnemiesNear(unit);

		// If there's no units return -1, it means it's fully safe.
		if (enemyUnits.isEmpty()) {
			_rangeBonus = 0;
			return -1;
		}

		ArrayList<Unit> ourUnits = getOurUnitsNear(unit);
		_ourUnits = ourUnits;

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

		if (ourUnits.size() >= 3 && ratio < 0.8) {
			// StrategyManager.waitUntilMinBattleUnits(10);
			StrategyManager.waitForMoreUnits();
		}

		_rangeBonus = 0;
		return ratio;
	}

	// private static boolean isOneOfUnitsDefensiveBuilding(Collection<Unit>
	// units) {
	// for (Unit unit : units) {
	// if (unit.isDefensiveGroundBuilding()) {
	// return true;
	// }
	// }
	// return false;
	// }

	private static double calculateTotalAttackOf(ArrayList<Unit> units, boolean forEnemy) {
		int total = 0;
		int seconds = xvr.getTimeSecond();
		int defensiveBuildings = 0;
		int vultures = 0;
		// int dragoons = 0;
		for (Unit unit : units) {
			double attackValue = unit.getGroundAttackNormalized();
			UnitType type = unit.getType();
			if (type.isWorker()) {
				continue;
			}

			if (type.getGroundWeapon().getMaxRange() > 100) {
				attackValue *= ENEMY_RANGE_WEAPON_STRENGTH_BONUS;
			}
			if (forEnemy) {
				total += attackValue;

				if (type.isVulture()) {
					vultures++;
					total -= attackValue * 1.4;
				}
				if (type.isHydralisk()) {
					total -= attackValue * 0.4;
				}
				if (type.isFirebat()) {
					total += attackValue * 0.3;
				}
				if (type.isDragoon()) {
					// dragoons++;
					total += attackValue * 0.4;
				}

				// Handle defensive buildings
				if (unit.isDefensiveGroundBuilding()) {
					total += DEFENSIVE_BUILDING_ATTACK_BONUS;
					if (unit.isCompleted()) {
						defensiveBuildings++;

						if (seconds >= 550) {
							if (type.isBunker()) {
								total += 40;
							}
						} else {
							total -= DEFENSIVE_BUILDING_ATTACK_BONUS;
						}
					}
					if (!ProtossGateway.LIMIT_ZEALOTS && !type.isBunker() && seconds < 550) {
						total -= DEFENSIVE_BUILDING_ATTACK_BONUS;
						total -= attackValue * 0.7;
					} else {
						total -= DEFENSIVE_BUILDING_ATTACK_BONUS;
						total -= attackValue;
					}
				}

				// Carriers
				if (unit.getType().isInterceptor()) {
					total -= attackValue * 0.75;
				}
			} else {
				total += attackValue;
			}
		}

		if (defensiveBuildings >= 2 && _ourUnits.size() < 7 && XVR.isEnemyProtoss()) {
			StrategyManager.waitForMoreUnits();
			total = 99999;
		}
		if (defensiveBuildings > 0 && defensiveBuildings <= 7 && _ourUnits.size() >= 7) {
			if (!ProtossGateway.LIMIT_ZEALOTS) {
				total /= 2;
			} else {
				total = 0;
			}
		}

		if ((vultures >= 3 || defensiveBuildings >= 3) && !ProtossGateway.LIMIT_ZEALOTS
				&& xvr.getTimeSecond() < 600) {
			StrategyManager.waitForMoreUnits();
			ProtossGateway.LIMIT_ZEALOTS = true;
			Debug.message(xvr, "Dont build zealots mode enabled");
		}

		return total;
	}

	private static double calculateHitPointsOf(ArrayList<Unit> units) {
		int total = 0;
		for (Unit unit : units) {
			UnitType type = unit.getType();

			if (unit.isCompleted() && (!type.isBuilding() || unit.isDefensiveGroundBuilding())) {
				total += unit.getHitPoints() + unit.getShields();
				if (type.isMedic()) {
					total += 60;
				}
			}
		}
		return total;
	}

	private static ArrayList<Unit> getEnemiesNear(Unit ourUnit) {
		ArrayList<Unit> unitsInRadius = xvr.getUnitsInRadius(ourUnit, BATTLE_RADIUS_ENEMIES
				+ _rangeBonus, xvr.getEnemyArmyUnitsIncludingDefensiveBuildings());
		for (Iterator<Unit> iterator = unitsInRadius.iterator(); iterator.hasNext();) {
			Unit unit = (Unit) iterator.next();
			if (unit.getType().isBuilding()
					&& (!unit.isDefensiveGroundBuilding() || !unit.isCompleted())) {
				iterator.remove();
			}
		}
		return unitsInRadius;
	}

	private static ArrayList<Unit> getOurUnitsNear(Unit ourUnit) {
		ArrayList<Unit> unitsInRadius = xvr.getUnitsInRadius(ourUnit, BATTLE_RADIUS_ALLIES,
				xvr.getArmyUnitsIncludingDefensiveBuildings());
		for (Iterator<Unit> iterator = unitsInRadius.iterator(); iterator.hasNext();) {
			Unit unit = (Unit) iterator.next();
			if (unit.getShields() < 5) {
				iterator.remove();
			} else if (unit.isDefensiveGroundBuilding()) {
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
		return !(strengthRatio < FAVORABLE_RATIO_THRESHOLD);
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
