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
		if (!UnitCounter.weHaveBuilding(buildingType)
				&& UnitCounter.getNumberOfUnits(ProtossGateway
						.getBuildingType()) >= 2
				&& !Constructing.weAreBuilding(buildingType)) {
			// if (UnitCounter.getNumberOfBattleUnits() >= 15) {
			return true;
			// }
		}

		if (UnitCounter.getNumberOfUnits(buildingType) == 1
				&& UnitCounter.getNumberOfUnits(ProtossGateway
						.getBuildingType()) >= 3 && xvr.canAfford(650)
				&& !Constructing.weAreBuilding(buildingType)) {
			return true;
		}

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
