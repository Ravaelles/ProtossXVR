package jnibwapi.xvr;

import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.protoss.ProtossGateway;
import jnibwapi.types.UnitType.UnitTypes;

public class ArmyPlacing {

	private static XVR xvr = XVR.getInstance();

	public static MapPoint getRetreatPointFor(Unit unit) {
		// Unit runTo = xvr.getUnitOfTypeNearestTo(
		// UnitManager.BASE, xvr.getFirstBase());
		Unit runTo = null;

		// If only one base, then go to nearest cannon
		if (UnitCounter.getNumberOfUnits(UnitManager.BASE) == 1
				&& UnitCounter.weHaveBuilding(UnitTypes.Protoss_Photon_Cannon)) {
			runTo = xvr.getUnitOfTypeNearestTo(UnitTypes.Protoss_Photon_Cannon,
					xvr.getFirstBase());
		}

		// Try to go to the base nearest to enemy
		else if (UnitCounter.weHaveBuilding(UnitManager.BASE)) {
			runTo = xvr.getBaseNearestToEnemy();
		}

		if (runTo == null) {
			if (!ProtossGateway.getAllObjects().isEmpty()) {
				runTo = ProtossGateway.getAllObjects().get(0);
			}
		}

		if (runTo == null) {
			return null;
		} else {
			return new MapPointInstance(runTo.getX(), runTo.getY());
		}
	}

	public static void goToSafePlaceIfNotAlreadyThere(Unit unit) {
		// if (unit.isMoving()) {
		// return;
		// }

		// First, just escape.
		MapPoint safePlace = getRetreatPointFor(unit);

		// Calculate distance to it
		// double distance = xvr.getDistanceBetween(unit, safePlace);
		// if (distance > 40) {
		// UnitActions.moveTo(unit, safePlace.x, safePlace.y);
		// } else if (distance >= 3) {
		if (xvr.getDistanceSimple(unit, safePlace) >= 11) {
			UnitActions.attackTo(unit, safePlace.getX(), safePlace.getY());
		}
		// }
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

	public static MapPoint getArmyCenterPoint() {
		int totalX = 0;
		int totalY = 0;
		int counter = 0;
		for (Unit unit : xvr.getUnitsNonWorker()) {
			totalX += unit.getX();
			totalY += unit.getY();
			counter++;
			if (counter > 10) {
				break;
			}
		}
		return new MapPointInstance((int) ((double) totalX / counter),
				(int) ((double) totalY / counter));
	}

}
