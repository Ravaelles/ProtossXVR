package ai.protoss;

import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;
import ai.handling.constructing.Constructing;
import ai.handling.constructing.ShouldBuildCache;
import ai.handling.units.UnitCounter;
import ai.managers.UnitManager;

public class ProtossCyberneticsCore {

	private static final UnitTypes buildingType = UnitTypes.Protoss_Cybernetics_Core;
	private static XVR xvr = XVR.getInstance();

	private static boolean forcedShouldBuild = false;

	public static void buildIfNecessary() {
		if (shouldBuild()) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			Constructing.construct(xvr, buildingType);
		}
		ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
	}

	public static boolean shouldBuild() {
		int battleUnits = UnitCounter.getNumberOfBattleUnits();
		int cybernetics = UnitCounter.getNumberOfUnits(buildingType);
		boolean weAreBuilding = Constructing.weAreBuilding(buildingType);

		// If we have set the flag to enforce building it.
		if (forcedShouldBuild && cybernetics == 0 && !weAreBuilding) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			return true;
		}

		if (!weAreBuilding && cybernetics == 0
				&& UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Gateway) >= 2) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			return true;
		}

		if ((ProtossGateway.LIMIT_ZEALOTS || UnitCounter
				.weHaveBuilding(UnitTypes.Protoss_Assimilator))
				&& cybernetics == 0
				&& !weAreBuilding) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			return true;
		}

		if (UnitCounter.getNumberOfUnitsCompleted(UnitTypes.Protoss_Nexus) == 3
				&& UnitCounter.getNumberOfUnits(buildingType) == 0) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			return true;
		}

		if (UnitCounter.getNumberOfUnitsCompleted(UnitTypes.Protoss_Forge) == 0
				&& UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Photon_Cannon) == 0) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
			return false;
		}

		if (UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Photon_Cannon) >= ProtossPhotonCannon
				.calculateMaxCannonStack() && battleUnits >= 4) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
			return false;
		}

		if (!weAreBuilding
				&& UnitCounter.weHaveBuilding(UnitTypes.Protoss_Forge)
				&& !UnitCounter.weHaveBuilding(buildingType)
				&& (UnitCounter.weHaveBuilding(UnitTypes.Protoss_Assimilator) || xvr.canAfford(320))
				&& xvr.canAfford(132)) {
			if ((UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Gateway) >= 3 || xvr.canAfford(320))
					&& (battleUnits >= ProtossGateway.MIN_UNITS_FOR_DIFF_BUILDING || UnitCounter
							.getNumberOfUnits(UnitManager.BASE) > 1)) {
				ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
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

	public static void forceShouldBuild() {
		forcedShouldBuild = true;
	}

}
