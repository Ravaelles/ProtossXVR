package jnibwapi.protoss;

import java.util.ArrayList;

import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

public class ProtossGateway {

	public static UnitTypes ZEALOT = UnitTypes.Protoss_Zealot;
	public static UnitTypes DRAGOON = UnitTypes.Protoss_Dragoon;
	public static UnitTypes DARK_TEMPLAR = UnitTypes.Protoss_Dark_Templar;
	public static UnitTypes HIGH_TEMPLAR = UnitTypes.Protoss_High_Templar;

	private static int zealotBuildRatio = 60;
	private static int dragoonBuildRatio = 60;
	private static int darkTemplarBuildRatio = 100;
	// private static int highTemplarBuildRatio = 19;

	private static final UnitTypes buildingType = UnitTypes.Protoss_Gateway;
	private static XVR xvr = XVR.getInstance();

	public static boolean shouldBuild() {
		if (xvr.canAfford(150) && UnitCounter.weHavePylonFinished()) {
			int barracks = UnitCounter.getNumberOfUnits(buildingType);

			// 0 barracks
			if (barracks == 0) {
				return true;
			}

			// 1 barracks
			if (barracks == 1
					&& UnitCounter
							.weHaveBuilding(UnitTypes.Protoss_Cybernetics_Core)) {
				return true;
			}

			// 2 barracks or more
			if (UnitCounter.getNumberOfUnits(buildingType) >= 2
					&& UnitCounter.getNumberOfUnits(UnitManager.BASE) >= 2
					&& UnitCounter
							.weHaveBuilding(UnitTypes.Protoss_Citadel_of_Adun)) {
				int HQs = UnitCounter.getNumberOfUnits(UnitManager.BASE);
				if ((double) barracks / HQs < 2.6 || xvr.canAfford(850)) {
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
			Constructing.construct(xvr, buildingType);
		}
	}

	protected static void act(Unit barracks) {
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
		;

		UnitTypes typeToBuild = ZEALOT;

		// Calculate ratio
		int totalRatio = zealotBuildRatio
				+ (dragoonAllowed ? dragoonBuildRatio : 0)
				+ (darkTemplarAllowed ? darkTemplarBuildRatio : 0);
		int totalInfantry = UnitCounter.getNumberOfInfantryUnits() + 1;

		// ZEALOT
		int zealotPercent = UnitCounter.getNumberOfUnits(ZEALOT) * totalRatio
				/ totalInfantry;
		if (zealotPercent < zealotBuildRatio) {
			return ZEALOT;
		}

		// DARK TEMPLAR
		if (darkTemplarAllowed) {
			int darkTemplarPercent = UnitCounter.getNumberOfUnits(DARK_TEMPLAR)
					* totalRatio / totalInfantry;
			if (darkTemplarPercent < darkTemplarBuildRatio) {
				return DARK_TEMPLAR;
			}
		}

		// DRAGOON
		if (dragoonAllowed) {
			int dragoonPercent = UnitCounter.getNumberOfUnits(DRAGOON)
					* totalRatio / totalInfantry;
			if (dragoonPercent < dragoonBuildRatio) {
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

}
