package jnibwapi.xvr;

import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;

public class BuildingManager {

	// private static final int REPAIR_BUILDING_WITH_WORKERS = 3;
	private static XVR xvr = XVR.getInstance();

	public static void act(Unit building) {
		UnitType buildingType = UnitType.getUnitTypeByID(building.getTypeID());
		if (buildingType == null || !buildingType.isBuilding()) {
			return;
		}

		// If under attack always call for help
		if (building.isUnderAttack()) {
			UnitActions.callForHelp(building, true);
		}

		// Cancel construction of buildings under attack and severely damaged
		if (building.isUnderAttack() && building.isBeingConstructed()) {
			boolean shouldCancelConstruction = false;

			if (shouldCancelConstruction) {
				xvr.getBwapi().cancelConstruction(building.getID());
			}
		}

		// Cancel construction of buildings under attack and severely damaged
		checkIfShouldCancelConstruction(building, buildingType);

		// // Bunker
		// // if (buildingType.isBunker()) {
		// if (building.isUnderAttack()
		// || building.getHitPoints() < building.getInitialHitPoints()) {
		// for (int workerCounter = 0; workerCounter <
		// REPAIR_BUILDING_WITH_WORKERS; workerCounter++) {
		// Unit repairer = WorkerManager.findNearestRepairerTo(building);
		// UnitActions.repair(repairer, building);
		// }
		//
		// if (building.isUnderAttack()) {
		// UnitActions.callForHelp(building);
		// }
		// }
		// // }
		
//		// Shield Battery
//		if (buildingType.getID() == ProtossShieldBattery.getBuildingType().ordinal()) {
//			ProtossShieldBattery.act(building);
//		}
	}

	private static void checkIfShouldCancelConstruction(Unit building,
			UnitType buildingType) {
		if (building.isUnderAttack() && building.isBeingConstructed()) {
			boolean shouldCancelConstruction = false;

			// If this is normal building and it's severely damaged.
			if (building.getHitPoints() < 0.4 * buildingType.getMaxHitPoints()) {
				shouldCancelConstruction = true;
			}

			// If it's base and...
			if (buildingType.isBase()) {

				// It's lil damaged and there's not too many units nearby.
				if (building.getHitPoints() < 0.6 * buildingType
						.getMaxHitPoints()
						&& xvr.getNumberOfUnitsInRadius(building, 15,
								xvr.getUnitsNonWorker()) < 3) {
					shouldCancelConstruction = true;
				}

				// It's pretty damaged, better scrap it
				if (building.getHitPoints() < 0.35 * buildingType
						.getMaxHitPoints()
						&& xvr.getNumberOfUnitsInRadius(building, 15,
								xvr.getUnitsNonWorker()) < 7) {
					shouldCancelConstruction = true;
				}
			}

			if (shouldCancelConstruction) {
				xvr.getBwapi().cancelConstruction(building.getID());
			}
		}
	}
}
