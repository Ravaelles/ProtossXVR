package ai.protoss;

import ai.core.XVR;
import ai.handling.constructing.Constructing;
import ai.handling.constructing.ShouldBuildCache;
import ai.handling.units.UnitCounter;
import ai.managers.BotStrategyManager;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

public class ProtossCitadelOfAdun {

	private static final UnitTypes buildingType = UnitTypes.Protoss_Citadel_of_Adun;
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
				&& UnitCounter
						.weHaveBuilding(UnitTypes.Protoss_Cybernetics_Core)
				&& UnitCounter.getNumberOfUnits(ProtossGateway
						.getBuildingType()) >= 3
				&& !Constructing.weAreBuilding(buildingType)) {
			if (BotStrategyManager.isExpandWithCannons()) {
				if (UnitCounter.getNumberOfBattleUnits() >= 2) {
					ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
					return true;
				}
			} else {
				if (UnitCounter.getNumberOfBattleUnits() >= ProtossGateway.MIN_UNITS_FOR_DIFF_BUILDING
						|| xvr.getTimeSecond() > 800) {
					ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
					return true;
				}
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
