package ai.handling.army;

import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;
import ai.handling.map.MapPoint;
import ai.handling.map.MapPointInstance;
import ai.handling.units.UnitActions;
import ai.handling.units.UnitCounter;
import ai.managers.UnitManager;
import ai.protoss.ProtossGateway;
import ai.protoss.ProtossNexus;

public class ArmyPlacing {

	private static XVR xvr = XVR.getInstance();

	public static MapPoint getArmyGatheringPointFor(Unit unit) {
		int bases = UnitCounter.getNumberOfUnits(UnitManager.BASE);

		// Unit runTo = xvr.getUnitOfTypeNearestTo(
		// UnitManager.BASE, xvr.getFirstBase());
		MapPoint runTo = null;

		// If only one base, then go to nearest cannon
		if (bases == 1) {
			Unit base = xvr.getLastBase();
			runTo = base;

			// Unit forge = null;
			// if (UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Forge) > 1) {
			// forge = xvr.getUnitsOfType(UnitTypes.Protoss_Forge).get(0);
			// } else {
			// forge = xvr.getUnitOfTypeNearestTo(
			// UnitTypes.Protoss_Forge, base);
			// }
			// if (forge != null) {
			// runTo = forge;
			// }

			Unit cannon = xvr.getUnitOfTypeNearestTo(
					UnitTypes.Protoss_Photon_Cannon, base);
			if (cannon != null) {
				runTo = cannon;
			}

			Unit gateway = xvr.getUnitOfTypeNearestTo(
					UnitTypes.Protoss_Gateway, base);
			if (runTo == null && gateway != null) {
				runTo = gateway;
			}
		}

		// // If only one base, then go to nearest cannon
		// if (UnitCounter.getNumberOfUnits(UnitManager.BASE) == 1
		// && UnitCounter.weHaveBuilding(UnitTypes.Protoss_Photon_Cannon)) {
		// Unit base = xvr.getFirstBase();
		// ArrayList<Unit> allCannons = xvr.getUnitsOfGivenTypeInRadius(
		// UnitTypes.Protoss_Photon_Cannon, 300, base.getX(),
		// base.getY(), true);
		// runTo = allCannons.get(allCannons.size() - 1);
		// }

		// Try to go to the base nearest to enemy
		else if (bases > 1) {

			// If there's stub for new base, go there
			if (xvr.countUnitsInRadius(ProtossNexus.getTileForNextBase(false),
					10, true) >= 2) {
				runTo = ProtossNexus.getTileForNextBase(false);
			} else {
				Unit baseNearestToEnemy = xvr.getBaseNearestToEnemy();
				if (baseNearestToEnemy.equals(xvr.getFirstBase())) {
					runTo = xvr.getLastBase();
				} else {
					runTo = baseNearestToEnemy;
				}
			}
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
		if (xvr.getDistanceSimple(unit, safePlace) >= 30) {
			UnitActions.moveTo(unit, safePlace);
		}
		else {
			UnitActions.attackTo(unit, safePlace);
		}
		// }
	}

	// public static void goToNearestUnitIfNotAlreadyThere(Unit unit) {
	// // System.out.println("goToNearestUnitIfNotAlreadyThere");
	//
	// // First, just escape.
	// Unit otherUnit = xvr.getUnitNearestFromList(unit.getX(), unit.getY(),
	// xvr.getUnitsNonWorker());
	// if (otherUnit == null) {
	// goToSafePlaceIfNotAlreadyThere(unit);
	// } else {
	//
	// // // Calculate distance to it
	// // double distance = xvr.getDistanceBetween(unit, otherUnit);
	// // if (distance > 40) {
	// // UnitManager.attackTo(unit, otherUnit.getX(), otherUnit.getY());
	// // } else {
	// UnitActions.attackTo(unit, otherUnit.getX(), otherUnit.getY());
	// // }
	// }
	// }

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
