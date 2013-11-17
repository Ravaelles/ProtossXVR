package jnibwapi.protoss;

import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

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

		// Act when enemy detector or some AA building is nearby: just run away
		if (xvr.isEnemyDetectorNear(observer.getX(), observer.getY())
				|| xvr.isEnemyDefensiveAirBuildingNear(observer.getX(),
						observer.getY())) {
			
			// Try to move away from this enemy detector on N tiles.
			UnitActions.moveAwayFromUnitIfPossible(observer,
					xvr.getEnemyDetectorNear(observer.getX(), observer.getY()), 3);
			return;
		}

		// If there's active attack we absolutely have to help the army.
		if (MassiveAttack.isAttackPending()) {
			actionWhenMassiveAttack(observer);
		}

		// There's no massive attack right now, do usual stuff.
		else {
			actionWhenNoMassiveAttack(observer);
		}
	}

	private static void actionWhenNoMassiveAttack(Unit observer) {

		// If observer is already moving, don't interrupt
		if (observer.isMoving()) {
			return;
		}

		// If we're not
		tryProtectingNewestBaseIfMakesSense(observer);
	}

	private static void actionWhenMassiveAttack(Unit observer) {
		if (MassiveAttack.getTargetUnit() != null) {
			UnitActions.moveTo(observer, MassiveAttack.getTargetUnit());
		} else {
			actStandard(observer);
		}
	}

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
					actStandard(observer);
				}
			}

			// There's enough observers at base, do something else.
			else {
				actStandard(observer);
			}
		}

		// No new base, just go to random choke point.
		else {
			actStandard(observer);
		}
	}

	private static void actStandard(Unit observer) {
		UnitActions.goToRandomChokePoint(observer);
	}

}
