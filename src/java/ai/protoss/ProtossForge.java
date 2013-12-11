package ai.protoss;

import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;
import ai.handling.constructing.Constructing;
import ai.handling.constructing.ShouldBuildCache;
import ai.handling.units.UnitCounter;

public class ProtossForge {

	private static final UnitTypes buildingType = UnitTypes.Protoss_Forge;
	private static XVR xvr = XVR.getInstance();

	public static void buildIfNecessary() {
		if (shouldBuild()) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			Constructing.construct(xvr, buildingType);
		}
		ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
	}

	public static boolean shouldBuild() {
		int forges = UnitCounter.getNumberOfUnits(buildingType);
//		int gateways = UnitCounter.getNumberOfUnits(ProtossGateway
//				.getBuildingType());
//		int gatewaysFinished = UnitCounter
//				.getNumberOfUnitsCompleted(ProtossGateway.getBuildingType());

		// Version for expansion with cannons
		if (forges == 0 && xvr.canAfford(134) //UnitCounter.weHavePylonFinished()
				&& !Constructing.weAreBuilding(buildingType)) {
			// if (UnitCounter.getNumberOfBattleUnits() >= 15) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			return true;
			// }
		}

		// Version for expansion with gateways
		// if (forges == 0 && (gateways >= 3 || gatewaysFinished >= 2)
		// && !Constructing.weAreBuilding(buildingType)) {
		// // if (UnitCounter.getNumberOfBattleUnits() >=
		// // ProtossGateway.MIN_UNITS_FOR_DIFF_BUILDING - 8) {
		// ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
		// return true;
		// // }
		// }

		if (forges == 1
				&& UnitCounter.getNumberOfUnits(ProtossGateway
						.getBuildingType()) >= 3 && xvr.canAfford(650)
				&& !Constructing.weAreBuilding(buildingType)) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			return true;
		}

		if (forges == 2 && xvr.canAfford(1300)
				&& !Constructing.weAreBuilding(buildingType)) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			return true;
		}

		ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
		return false;
	}

	public static Unit getOneNotBusy() {
		for (Unit unit : xvr.getUnitsOfType(buildingType)) {
			if (unit.isBuildingNotBusy()) {
				return unit;
			}
		}
		return null;
	}

	public static UnitTypes getBuildingType() {
		return buildingType;
	}

}
