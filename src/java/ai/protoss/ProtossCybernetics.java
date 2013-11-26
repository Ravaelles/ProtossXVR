package ai.protoss;

import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;
import ai.handling.constructing.Constructing;
import ai.handling.constructing.ShouldBuildCache;
import ai.handling.units.UnitCounter;

public class ProtossCybernetics {

	private static final UnitTypes buildingType = UnitTypes.Protoss_Cybernetics_Core;
	private static XVR xvr = XVR.getInstance();

	public static void buildIfNecessary() {
		if (shouldBuild()) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			Constructing.construct(xvr, buildingType);
		}
		ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
	}

	public static boolean shouldBuild() {
		if (!Constructing.weAreBuilding(buildingType)
				&& UnitCounter.weHaveBuilding(UnitTypes.Protoss_Forge)
				&& xvr.canAfford(150)) {
			if (UnitCounter
					.getNumberOfUnitsCompleted(UnitTypes.Protoss_Gateway) >= 2
					&& !UnitCounter.weHaveBuilding(buildingType)) {
				return true;
			}
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

	public static UnitTypes getBuildingtype() {
		return buildingType;
	}

}
