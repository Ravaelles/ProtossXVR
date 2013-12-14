package ai.protoss;

import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;
import ai.handling.constructing.Constructing;
import ai.handling.constructing.ShouldBuildCache;
import ai.handling.units.UnitCounter;
import ai.managers.BotStrategyManager;
import ai.managers.UnitManager;

public class ProtossAssimilator {

	private static XVR xvr = XVR.getInstance();

	private static final UnitTypes buildingType = UnitTypes.Protoss_Assimilator;

	public static boolean shouldBuild() {
		int minGateways = BotStrategyManager.isExpandWithCannons() ? 3 : 4;

		if (UnitCounter.getNumberOfUnitsCompleted(UnitTypes.Protoss_Forge) == 0
				&& UnitCounter
						.getNumberOfUnits(UnitTypes.Protoss_Photon_Cannon) == 0) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			return false;
		}

		if (!Constructing.weAreBuilding(buildingType)
				&& UnitCounter.getNumberOfUnits(buildingType) < UnitCounter
						.getNumberOfUnitsCompleted(UnitManager.BASE)
				&& (UnitCounter
						.weHaveBuilding(UnitTypes.Protoss_Cybernetics_Core) || ProtossGateway.LIMIT_ZEALOTS)) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			return true;
		}

		if (!Constructing.weAreBuilding(buildingType)
				&& (UnitCounter
						.weHaveBuilding(UnitTypes.Protoss_Cybernetics_Core)
						|| UnitCounter.getNumberOfUnits(UnitManager.GATEWAY) >= minGateways || xvr
							.canAfford(700))
				&& UnitCounter.getNumberOfUnits(buildingType) < UnitCounter
						.getNumberOfUnitsCompleted(UnitManager.BASE)) {
			if (UnitCounter.getNumberOfBattleUnits() >= ProtossGateway.MIN_UNITS_FOR_DIFF_BUILDING) {
				ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
				return true;
			}
		}

		if (UnitCounter.getNumberOfUnits(buildingType) < UnitCounter
				.getNumberOfUnitsCompleted(UnitManager.BASE)
				&& xvr.canAfford(750)) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			return true;
		}

		ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
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
