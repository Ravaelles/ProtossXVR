package jnibwapi.protoss;

import java.awt.Point;

import jnibwapi.XVR;
import jnibwapi.model.Unit;

public class ArmyPlacing {

	private static XVR xvr = XVR.getInstance();

	public static Point getRetreatPointFor(Unit unit) {
		// Unit runTo = xvr.getUnitOfTypeNearestTo(
		// UnitManager.BASE, xvr.getFirstBase());
		Unit runTo = null;
		
		// Try to go to the last base
		if (!ProtossNexus.getBases().isEmpty()) {
//			runTo = ProtossNexus.getBases().get(
//					ProtossNexus.getBases().size() - 1);
			runTo = xvr.getLastBase();
		}

		if (runTo == null) {
			if (!ProtossGateway.getAllObjects().isEmpty()) {
				runTo = ProtossGateway.getAllObjects().get(0);
			}
		}

		if (runTo == null) {
			return null;
		} else {
			return new Point(runTo.getX(), runTo.getY());
		}
	}

	public static boolean shouldSpreadOut(Unit unit) {
		return unit.isIdle() && !unit.isMoving() && !unit.isAttacking()
				&& !unit.isUnderAttack();
	}

	public static void spreadOutRandomly(Unit unit) {

		// Act when enemy detector is nearby, run away
		if (!MassiveAttack.isAttackPending()
				&& (xvr.isEnemyDetectorNear(unit.getX(), unit.getY())
				|| xvr.isEnemyDefensiveGroundBuildingNear(unit.getX(), unit.getY()))) {
			Unit goTo = xvr.getFirstBase();
			UnitActions.attackTo(unit, goTo.getX(), goTo.getY());
			return;
		}

		// Act when enemy is nearby, run away
		Unit enemyNearby = TargetHandling.getEnemyNearby(unit, 8);
		if (enemyNearby != null && unit.isWorker() && unit.getHitPoints() > 18) {
			Unit goTo = xvr.getFirstBase();
			UnitActions.attackTo(unit, goTo.getX(), goTo.getY());
			return;
		}

		// System.out.println("###### SPREAD OUT ########");
		if (!unit.isMoving() && !unit.isUnderAttack()
				&& unit.getHitPoints() > 18) {

			// If distance to current target is smaller than N it means that
			// unit can spread out and scout nearby grounds
			if (xvr.getDistanceBetween(unit, unit.getTargetX(),
					unit.getTargetY()) < 18) {
				Point goTo = MapExploration.getNearestUnknownPointFor(
						unit.getX(), unit.getY(), true);
				if (goTo != null) {
					UnitActions.attackTo(unit, goTo.x, goTo.y);
				}
			}
		} else if (unit.isWorker() && unit.isUnderAttack()) {
			UnitActions.moveTo(unit, xvr.getFirstBase());
		}
	}

	public static void goToSafePlaceIfNotAlreadyThere(Unit unit) {
		// if (unit.isMoving()) {
		// return;
		// }

		// First, just escape.
		Point safePlace = getRetreatPointFor(unit);

		// Calculate distance to it
//		double distance = xvr.getDistanceBetween(unit, safePlace);
//		if (distance > 40) {
//			UnitActions.moveTo(unit, safePlace.x, safePlace.y);
//		} else if (distance >= 3) {
			UnitActions.attackTo(unit, safePlace.x, safePlace.y);
//		}
	}

	public static void goToNearestUnitIfNotAlreadyThere(Unit unit) {
		System.out.println("goToNearestUnitIfNotAlreadyThere");

		// First, just escape.
		Unit otherUnit = xvr.getUnitNearestFromList(unit.getX(), unit.getY(),
				xvr.getUnitsNonWorker());
		if (otherUnit == null) {
			goToSafePlaceIfNotAlreadyThere(unit);
		} else {

			// // Calculate distance to it
			// double distance = xvr.getDistanceBetween(unit, otherUnit);
			// if (distance > 40) {
			// UnitManager.attackTo(unit, otherUnit.getX(), otherUnit.getY());
			// } else {
			UnitActions.attackTo(unit, otherUnit.getX(), otherUnit.getY());
			// }
		}
	}

}
