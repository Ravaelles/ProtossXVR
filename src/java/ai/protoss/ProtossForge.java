package ai.protoss;

import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;
import ai.handling.constructing.Constructing;
import ai.handling.constructing.ShouldBuildCache;
import ai.handling.map.MapExploration;
import ai.handling.units.UnitCounter;
import ai.managers.BotStrategyManager;

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
		// int pylons = UnitCounter.getNumberOfPylonsCompleted();
		int gateways = UnitCounter.getNumberOfUnits(ProtossGateway
				.getBuildingType());
//		int gatewaysFinished = UnitCounter
//				.getNumberOfUnitsCompleted(ProtossGateway.getBuildingType());

		// Version for expansion with cannons
		if (BotStrategyManager.isExpandWithCannons()) {
			if (forges == 0
					&& (ProtossPylon.calculateExistingPylonsStrength() >= 0.86 || xvr
							.canAfford(132))
					&& !Constructing.weAreBuilding(buildingType)) {
				MapExploration.disableChokePointsNearFirstBase();
				// if (UnitCounter.getNumberOfBattleUnits() >= 15) {
				ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
				return true;
				// }
			}
		}

		// Version for expansion with gateways
		if (BotStrategyManager.isExpandWithGateways()) {
			if (forges == 0 && gateways >= 3
					&& !Constructing.weAreBuilding(buildingType)) {
				// if (UnitCounter.getNumberOfBattleUnits() >=
				// ProtossGateway.MIN_UNITS_FOR_DIFF_BUILDING - 8) {
				ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
				return true;
				// }
			}
		}

		if (forges == 1
				&& UnitCounter.getNumberOfUnits(ProtossGateway
						.getBuildingType()) >= 4 && xvr.canAfford(650)
				&& !Constructing.weAreBuilding(buildingType)) {
			if (UnitCounter.getNumberOfBattleUnits() >= 18) {
				ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
				return true;
			}
		}

		if (forges == 2 && xvr.canAfford(900)
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
