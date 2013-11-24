package ai.protoss;

import java.util.ArrayList;

import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;
import ai.handling.constructing.Constructing;
import ai.handling.constructing.ShouldBuildCache;
import ai.handling.units.UnitCounter;
import ai.managers.UnitManager;

public class ProtossGateway {

	public static UnitTypes ZEALOT = UnitTypes.Protoss_Zealot;
	public static UnitTypes DRAGOON = UnitTypes.Protoss_Dragoon;
	public static UnitTypes DARK_TEMPLAR = UnitTypes.Protoss_Dark_Templar;
	public static UnitTypes HIGH_TEMPLAR = UnitTypes.Protoss_High_Templar;

	private static int zealotBuildRatio = 72;
	private static int dragoonBuildRatio = 18;
	private static int darkTemplarBuildRatio = 30;
	// private static int highTemplarBuildRatio = 19;

	private static final UnitTypes buildingType = UnitTypes.Protoss_Gateway;
	private static XVR xvr = XVR.getInstance();

	public static boolean shouldBuild() {
		if (xvr.canAfford(150) && UnitCounter.weHavePylonFinished()) {
			int barracks = UnitCounter.getNumberOfUnits(buildingType);

			// 0 barracks
			if (barracks == 0
					&& (UnitCounter.weHaveBuilding(ProtossForge.getBuildingType())
					|| xvr.canAfford(150))) {
				return true;
			}

			// 1 barracks
			if (barracks == 1
					&& UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Pylon) >= 2) {
				return true;
			}

			// 2 barracks or more
			if (UnitCounter.getNumberOfUnits(buildingType) >= 2
					&& UnitCounter.getNumberOfUnitsCompleted(UnitManager.BASE) >= 2
					&& UnitCounter
							.weHaveBuilding(UnitTypes.Protoss_Robotics_Facility)
					&& UnitCounter
							.weHaveBuilding(UnitTypes.Protoss_Citadel_of_Adun)) {
				int HQs = UnitCounter.getNumberOfUnits(UnitManager.BASE);
				if ((double) barracks / HQs < 1.73 || xvr.canAfford(850)) {
					return true;
				}
			}
		}
		return false;
	}

	public static ArrayList<Unit> getAllObjects() {
		return xvr.getUnitsOfTypeCompleted(buildingType);
	}

	public static void enemyIsProtoss() {
		dragoonBuildRatio *= 2.5;
	}

	public static void buildIfNecessary() {
		if (shouldBuild()) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			Constructing.construct(xvr, buildingType);
		}
		ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
	}

	public static void act(Unit barracks) {
		int[] buildingQueueDetails = Constructing.shouldBuildAnyBuilding();
		int freeMinerals = xvr.getMinerals();
		int freeGas = xvr.getGas();
		if (buildingQueueDetails != null) {
			freeMinerals -= buildingQueueDetails[0];
			freeGas -= buildingQueueDetails[1];
		}

		if (buildingQueueDetails == null || freeMinerals >= 100) {
			if (barracks.getTrainingQueueSize() == 0) {
				xvr.buildUnit(barracks,
						defineUnitToBuild(freeMinerals, freeGas));
			}
		}
	}

	private static UnitTypes defineUnitToBuild(int freeMinerals, int freeGas) {
		boolean dragoonAllowed = UnitCounter
				.weHaveBuildingFinished(UnitTypes.Protoss_Cybernetics_Core)
				&& (freeMinerals >= 125 && freeGas >= 50);
		boolean darkTemplarAllowed = UnitCounter
				.weHaveBuildingFinished(UnitTypes.Protoss_Templar_Archives)
				&& (freeMinerals >= 125 && freeGas >= 100);

		UnitTypes typeToBuild = ZEALOT;

		// Calculate ratio
		double totalRatio = zealotBuildRatio
				+ (dragoonAllowed ? dragoonBuildRatio : 0)
				+ (darkTemplarAllowed ? darkTemplarBuildRatio : 0);
		double totalInfantry = UnitCounter.getNumberOfInfantryUnits() + 1;

		// ZEALOT
		double zealotPercent = UnitCounter.getNumberOfUnits(ZEALOT)
				/ totalInfantry;
		if (zealotPercent < zealotBuildRatio / totalRatio) {
			return ZEALOT;
		}

		// DARK TEMPLAR
		if (darkTemplarAllowed) {
			double darkTemplarPercent = (double) UnitCounter
					.getNumberOfUnits(DARK_TEMPLAR) / totalInfantry;
			if (darkTemplarPercent < darkTemplarBuildRatio / totalRatio) {
				return DARK_TEMPLAR;
			}
		}

		// DRAGOON
		if (dragoonAllowed) {
			double dragoonPercent = (double) UnitCounter
					.getNumberOfUnits(DRAGOON) / totalInfantry;
			if (dragoonPercent < dragoonBuildRatio / totalRatio) {
				return DRAGOON;
			}
		}

		// int medicPercent = UnitCounter
		// .getNumberOfUnits(UnitTypes.Protoss_Medic)
		// * totalRatio
		// / totalInfantry;
		// if (medicPercent < darkTemplarBuildRatio) {
		// return UnitTypes.Protoss_Medic;
		// }

		// UnitTypes.Protoss_

		return typeToBuild;
	}

	public static UnitTypes getBuildingType() {
		return buildingType;
	}

	public static void changePlanToAntiAir() {
		zealotBuildRatio = 10;
		dragoonBuildRatio = 70;
		darkTemplarBuildRatio = 10;
	}

}
