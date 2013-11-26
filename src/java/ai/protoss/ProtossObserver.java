package ai.protoss;

import java.util.ArrayList;
import java.util.HashMap;

import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;
import ai.handling.army.ArmyPlacing;
import ai.handling.map.MapPoint;
import ai.handling.map.MapPointInstance;
import ai.handling.units.UnitActions;
import ai.handling.units.UnitCounter;
import ai.managers.UnitManager;

public class ProtossObserver {

	private static final int MIN_DIST_BETWEEN_OBSERVERS = 6;

	private static final UnitTypes OBSERVER = UnitTypes.Protoss_Observer;
	private static XVR xvr = XVR.getInstance();

	/**
	 * Mapping of individual assignments of observers e.g. to scan hidden units.
	 */
	private static final HashMap<Unit, MapPoint> observersToPoints = new HashMap<Unit, MapPoint>();

	public static void tryToScanPoint(int x, int y) {
		Unit observer = getNearestFreeObserverTo(x, y);
		if (observer != null) {
			assignObserverToScan(observer, new MapPointInstance(x, y));
			UnitActions.moveTo(observer, x, y);
		}
	}

	public static void tryToScanUnit(Unit enemy) {
		if (enemy == null || enemy.getType().isTerranMine()
				|| enemy.getType().isObserver()) {
			return;
		}

		Unit observer = getNearestFreeObserverTo(enemy);
		if (observer != null) {

			// Check whether this unit isn't already being scanned by some
			// observer
			if (!isEnemyUnitAlreadyBeingScanned(enemy)) {
				assignObserverToScan(observer, enemy);
				UnitActions.moveTo(observer, enemy);
			}
		}
	}

	private static void markEnemyUnitAsNoLongerScanned(Unit enemy) {

		// Find the key (observer) that was scanning this enemy
		Unit observerKey = null;
		for (Unit observer : observersToPoints.keySet()) {
			if (observersToPoints.get(observer).equals(enemy)) {
				observerKey = observer;
				break;
			}
		}

		// Remove the mapping (info about scanning this unit)
		observersToPoints.remove(observerKey);
	}

	private static boolean isEnemyUnitAlreadyBeingScanned(Unit enemy) {
		return observersToPoints.values().contains(enemy);
	}

	private static void assignObserverToScan(Unit observer,
			MapPoint pointOrUnitToScan) {
		observersToPoints.put(observer, pointOrUnitToScan);
	}

	private static Unit getNearestFreeObserverTo(int x, int y) {
		return getNearestFreeObserverTo(new MapPointInstance(x, y));
	}

	private static Unit getNearestFreeObserverTo(
			MapPoint sortByDistanceToThisPoint) {
		for (Unit observer : xvr.getUnitsInRadius(
				sortByDistanceToThisPoint.getX(),
				sortByDistanceToThisPoint.getY(), 300,
				xvr.getUnitsOfType(OBSERVER))) {
			if (observer.isCompleted()
					&& isObserverFreeFromScanMissions(observer)) {
				return observer;
			}
		}
		return null;
	}

	private static boolean isObserverFreeFromScanMissions(Unit observer) {
		return !observersToPoints.containsKey(observer);
	}

	public static void hiddenUnitDetected(Unit unit) {
		if (unit.isEnemy() && unit.isHidden()) {
			if (UnitCounter.getNumberOfUnits(OBSERVER) > 0) {
				tryToScanUnit(unit);
			}
		}
	}

	/**
	 * Observer can be acting in several ways:<br />
	 * <br />
	 * - fully normal, standard behavior, going from choke to choke randomly<br />
	 * - following specific army unit (to ensure army has observers support)<br />
	 * - trying to scan hidden enemy unit or some point<br />
	 * */
	public static void act(Unit observer) {

		// TOP PRIORITY: Act when enemy detector or some AA building is nearby:
		// just run away, no matter what.
		if (UnitActions.runFromEnemyDetectorOrDefensiveBuildingIfNecessary(
				observer, false, true)) {
			return;
		}

		// Choose behavior according to current mission for of this observer
		if (isObserverSupposedToScanSomePoint(observer)) {
			actWhenScanningSomePoint(observer);
		} else if (isIndividualTaskAssigned(observer)) {
			actIndividual(observer);
		} else {
			actNormally(observer);
		}

		trySpreadingOutIfObserversTooClose(observer);
	}

	private static void actWhenScanningSomePoint(Unit observer) {
		MapPoint mapPointToScan = observersToPoints.get(observer);

		// Check whether or not the enemy unit we were supposed to scout exists
		// and/or should we stop scanning this location.
		if (mapPointToScan instanceof Unit) {
			Unit enemyToScan = (Unit) mapPointToScan;
			if (!enemyToScan.isHidden()
					|| xvr.getDistanceSimple(observer, enemyToScan) <= 3) {
				markEnemyUnitAsNoLongerScanned(enemyToScan);
				return;
			}
		}

		// If everything is okay, go to the given location.
		UnitActions.moveTo(observer, mapPointToScan);
	}

	private static boolean isObserverSupposedToScanSomePoint(Unit observer) {
		return observersToPoints.containsKey(observer);
	}

	private static void trySpreadingOutIfObserversTooClose(Unit observer) {
		ArrayList<Unit> observersNearby = xvr.getUnitsOfGivenTypeInRadius(
				OBSERVER, MIN_DIST_BETWEEN_OBSERVERS, observer, true);
		observersNearby.remove(observer); // Remove ourself

		// If there is at least one observer nearby, move away from it
		if (!observersNearby.isEmpty()) {
			UnitActions.moveAwayFromUnitIfPossible(observer,
					observersNearby.get(0), MIN_DIST_BETWEEN_OBSERVERS);
		}
	}

	private static boolean isIndividualTaskAssigned(Unit observer) {
		return getIndexOfObserver(observer) <= 2;
	}

	private static int getIndexOfObserver(Unit observer) {
		return xvr.getUnitsOfType(OBSERVER).indexOf(observer);
	}

	private static void actNormally(Unit observer) {

		// If observer is already moving, don't interrupt
		if (observer.isMoving()) {
			return;
		}

		// If we're not
		tryProtectingNewestBaseIfMakesSense(observer);
	}

	private static void actIndividual(Unit observer) {
		int observerIndex = getIndexOfObserver(observer);

		// Get the units to assign observers to.
		Unit unit1 = null;
		Unit unit2 = null;
		Unit unit3 = null;

		ArrayList<Unit> zealots = xvr.getUnitsOfType(UnitTypes.Protoss_Zealot);

		// Two observers should follow the main army
		if (observerIndex == 0) {
			if (!zealots.isEmpty()) {
				unit1 = zealots.get(0);
				UnitActions.moveTo(observer, unit1);
				return;
			}
		} else if (observerIndex == 1) {
			ArrayList<Unit> dragoons = xvr
					.getUnitsOfType(UnitTypes.Protoss_Dragoon);
			if (!dragoons.isEmpty()) {
				unit2 = dragoons.get(0);
				UnitActions.moveTo(observer, unit2);
				return;
			} else if (!zealots.isEmpty()) {
				unit2 = zealots.get(zealots.size() - 1);
				UnitActions.moveTo(observer, unit2);
				return;
			}
		} else if (observerIndex == 2) {
			ArrayList<Unit> arbiters = xvr
					.getUnitsOfType(UnitTypes.Protoss_Arbiter);
			if (!arbiters.isEmpty()) {
				unit3 = arbiters.get(0);
				UnitActions.moveTo(observer, unit3);
				return;
			} else {
				if (!zealots.isEmpty()) {
					unit3 = zealots.get(zealots.size() / 2);
					UnitActions.moveTo(observer, unit3);
					return;
				}
			}
		} else {
			UnitActions.moveTo(observer, ArmyPlacing.getArmyCenterPoint());
		}

		// If no previous action was successful, just act normally.
		actNormally(observer);
	}

	// private static void actionWhenNoMassiveAttack(Unit observer) {
	//
	// }
	//
	// private static void actionWhenMassiveAttack(Unit observer) {
	// boolean shouldFollowArmy = xvr.getUnitsOfType(OBSERVER).indexOf(
	// observer) <= 1;
	// if (MassiveAttack.getTargetUnit() != null && shouldFollowArmy) {
	//
	// int observersNearby = -1
	// + xvr.getUnitsOfGivenTypeInRadius(OBSERVER, 10, observer,
	// true).size();
	// if (observersNearby == 0) {
	// UnitActions.moveTo(observer, MassiveAttack.getTargetUnit());
	// } else {
	// UnitActions.spreadOutRandomly(observer);
	// }
	// } else {
	// goToRandomChokePoint(observer);
	// }
	// }

	private static void tryProtectingNewestBaseIfMakesSense(Unit observer) {

		// At least one observer should be near the last created base
		Unit newestBase = UnitCounter.getNumberOfUnits(UnitManager.BASE) > 1 ? xvr
				.getLastBase() : null;
		if (newestBase != null) {

			// Calculate how many observers there are near this base
			int observersNearBase = xvr.getNumberOfUnitsInRadius(
					newestBase.getX(), newestBase.getY(), 20,
					xvr.getUnitsOfType(OBSERVER));

			// No observers, move there.
			if (observersNearBase == 0) {
				UnitActions.moveTo(observer, newestBase);
			}

			// There's one observer at the base; check if we're this one.
			else if (observersNearBase == 1) {
				Unit observerNearBase = xvr.getUnitOfTypeNearestTo(OBSERVER,
						newestBase);

				// We're this observer
				if (observer.equals(observerNearBase)) {

				} else {
					goToRandomChokePoint(observer);
				}
			}

			// There's enough observers at base, do something else.
			else {
				goToRandomChokePoint(observer);
			}
		}

		// No new base, just go to random choke point.
		else {
			goToRandomChokePoint(observer);
		}
	}

	private static void goToRandomChokePoint(Unit observer) {
		UnitActions.goToRandomChokePoint(observer);
	}

}
