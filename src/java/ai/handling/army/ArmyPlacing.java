package ai.handling.army;

import jnibwapi.model.Unit;
import ai.core.XVR;
import ai.handling.map.MapPoint;
import ai.handling.map.MapPointInstance;
import ai.handling.units.UnitActions;
import ai.handling.units.UnitCounter;
import ai.managers.UnitManager;
import ai.protoss.ProtossGateway;

public class ArmyPlacing {

	private static XVR xvr = XVR.getInstance();

	public static MapPoint getArmyGatheringPointFor(Unit unit) {
		// Unit runTo = xvr.getUnitOfTypeNearestTo(
		// UnitManager.BASE, xvr.getFirstBase());
		Unit runTo = null;

		// If only one base, then go to nearest cannon
		if (UnitCounter.getNumberOfUnits(UnitManager.BASE) == 1) {
			Unit base = xvr.getFirstBase();
			runTo = base;
		}
		
//		// If only one base, then go to nearest cannon
//		if (UnitCounter.getNumberOfUnits(UnitManager.BASE) == 1
//				&& UnitCounter.weHaveBuilding(UnitTypes.Protoss_Photon_Cannon)) {
//			Unit base = xvr.getFirstBase();
//			ArrayList<Unit> allCannons = xvr.getUnitsOfGivenTypeInRadius(
//					UnitTypes.Protoss_Photon_Cannon, 300, base.getX(),
//					base.getY(), true);
//			runTo = allCannons.get(allCannons.size() - 1);
//		}

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

		// First, just escape.
		MapPoint safePlace = getArmyGatheringPointFor(unit);

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
