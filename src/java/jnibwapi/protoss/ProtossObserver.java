package jnibwapi.protoss;

import java.util.ArrayList;

import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.xvr.ArmyPlacing;
import jnibwapi.xvr.UnitActions;
import jnibwapi.xvr.UnitCounter;
import jnibwapi.xvr.UnitManager;

public class ProtossObserver {

	private static final UnitTypes OBSERVER = UnitTypes.Protoss_Observer;
	private static XVR xvr = XVR.getInstance();

	public static void tryToScanPoint(int x, int y) {
		Unit observer = getNearestObserverTo(x, y);
		if (observer != null) {
			UnitActions.moveTo(observer, x, y);
		}
	}

	private static Unit getNearestObserverTo(int x, int y) {
		return xvr.getUnitOfTypeNearestTo(OBSERVER, x, y);
	}

	public static void hiddenUnitDetected(Unit unit) {
		if (unit.isEnemy() && (unit.isCloaked() || unit.isBurrowed())) {
			if (UnitCounter.getNumberOfUnits(OBSERVER) > 0) {
				tryToScanPoint(unit.getX(), unit.getY());
			}
		}
	}

	public static void act(Unit observer) {

		// TOP PRIORITY: Act when enemy detector or some AA building is nearby:
		// just run away
		if (UnitActions.runFromEnemyDetectorOrDefensiveBuildingIfNecessary(observer, true)) {
			return;
		}

		if (isIndividualTaskAssigned(observer)) {
			actIndividual(observer);
		} else {
			actNormally(observer);
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
		} else if (observerIndex == 3) {
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
