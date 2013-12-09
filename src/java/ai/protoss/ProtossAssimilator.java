package ai.protoss;

import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;
import ai.handling.constructing.Constructing;
import ai.handling.constructing.ShouldBuildCache;
import ai.handling.units.UnitCounter;
import ai.managers.UnitManager;

public class ProtossAssimilator {

	private static XVR xvr = XVR.getInstance();

	private static final UnitTypes buildingType = UnitTypes.Protoss_Assimilator;

	public static boolean shouldBuild() {
		if (!Constructing.weAreBuilding(buildingType)
				&& xvr.canAfford(100)
				&& (UnitCounter
						.weHaveBuilding(UnitTypes.Protoss_Cybernetics_Core) || UnitCounter
						.getNumberOfUnits(UnitManager.GATEWAY) >= 2)
				&& UnitCounter.getNumberOfUnits(buildingType) < UnitCounter
						.getNumberOfUnitsCompleted(UnitManager.BASE)) {
			if (UnitCounter.getNumberOfBattleUnits() >= 6) {
				return true;
			}
		}
		return false;
	}

	public static void buildIfNecessary() {
		if (shouldBuild()) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			Constructing.construct(xvr, buildingType);
		}
		ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
	}

	public static UnitTypes getBuildingtype() {
		return buildingType;
	}

}
