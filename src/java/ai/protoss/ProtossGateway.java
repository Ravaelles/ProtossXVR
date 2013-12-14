package ai.protoss;

import java.util.ArrayList;

import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;
import ai.handling.constructing.Constructing;
import ai.handling.constructing.ShouldBuildCache;
import ai.handling.units.UnitCounter;
import ai.managers.BotStrategyManager;
import ai.managers.UnitManager;

public class ProtossGateway {

	public static UnitTypes ZEALOT = UnitTypes.Protoss_Zealot;
	public static UnitTypes DRAGOON = UnitTypes.Protoss_Dragoon;
	public static UnitTypes DARK_TEMPLAR = UnitTypes.Protoss_Dark_Templar;
	public static UnitTypes HIGH_TEMPLAR = UnitTypes.Protoss_High_Templar;

	private static boolean isPlanAntiAirActive = false;

	public static int MIN_UNITS_FOR_DIFF_BUILDING = 20;

	public static boolean LIMIT_ZEALOTS = false;

	private static int zealotBuildRatio = 20;
	private static int dragoonBuildRatio = 50;
	private static int darkTemplarBuildRatio = 50;
	// private static int highTemplarBuildRatio = 19;

	private static final int MINIMAL_HIGH_TEMPLARS = 2;
	// private static final int MAX_ZEALOTS = 5;

	private static final UnitTypes buildingType = UnitTypes.Protoss_Gateway;
	private static XVR xvr = XVR.getInstance();

	public static boolean shouldBuild() {
		if (UnitCounter.weHavePylonFinished()) {
			int gateways = UnitCounter.getNumberOfUnits(buildingType);
			int bases = UnitCounter.getNumberOfUnitsCompleted(UnitManager.BASE);
			
			if (ProtossNexus.shouldBuild() && !xvr.canAfford(500)) {
				ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
				return false;
			}

			// ### Version ### Expansion with cannons
			if (BotStrategyManager.isExpandWithCannons()) {
				int cannons = UnitCounter
						.getNumberOfUnitsCompleted(UnitTypes.Protoss_Photon_Cannon);
				if ((cannons >= ProtossPhotonCannon.MAX_CANNON_STACK || xvr
						.canAfford(300)) && gateways <= 2 && xvr.canAfford(155)) {
					ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
					return true;
				}
			}

			// ### Version ### Expansion with gateways
			if (BotStrategyManager.isExpandWithGateways()) {
				if (gateways <= 3 && (isMajorityOfGatewaysTrainingUnits())
						&& xvr.canAfford(134)) {
					ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
					return true;
				} else {
					if (!UnitCounter
							.weHaveBuilding(UnitTypes.Protoss_Cybernetics_Core)
							|| !UnitCounter
									.weHaveBuilding(UnitTypes.Protoss_Citadel_of_Adun)) {
						ShouldBuildCache.cacheShouldBuildInfo(buildingType,
								false);
						return false;
					}
				}
			}

			if (bases <= 1) {
				ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
				return false;
			}

			if (gateways >= 3 && xvr.canAfford(140)) {
				if (isMajorityOfGatewaysTrainingUnits()) {
					ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
					return true;
				}
			}

			// 3 barracks or more
			if (gateways >= 3 && (gateways <= 5 || xvr.canAfford(520))) {
				if (isMajorityOfGatewaysTrainingUnits()) {
					ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
					return true;
				}
			}
			if (gateways >= 2
					&& bases >= 2
					&& UnitCounter
							.weHaveBuilding(UnitTypes.Protoss_Observatory)
					&& UnitCounter
							.weHaveBuilding(UnitTypes.Protoss_Citadel_of_Adun)) {
				int HQs = UnitCounter.getNumberOfUnits(UnitManager.BASE);
				if ((double) gateways / HQs <= 2 && xvr.canAfford(560)) {
					if (isMajorityOfGatewaysTrainingUnits()) {
						ShouldBuildCache.cacheShouldBuildInfo(buildingType,
								true);
						return true;
					}
				}
			}
		}

		if (xvr.canAfford(1500)) {
			if (isMajorityOfGatewaysTrainingUnits()) {
				ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
				return true;
			}
		}

		ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
		return false;
	}

	private static boolean isMajorityOfGatewaysTrainingUnits() {
		ArrayList<Unit> allObjects = xvr.getUnitsOfType(buildingType);
		int all = allObjects.size();
		int busy = 0;
		for (Unit gateway : allObjects) {
			if (gateway.isTraining()) {
				busy++;
			}
		}

		return ((double) busy / all) >= (0.75 + all * 2) || all <= 2;
	}

	public static ArrayList<Unit> getAllObjects() {
		return xvr.getUnitsOfTypeCompleted(buildingType);
	}

	public static void enemyIsProtoss() {
	}

	public static void enemyIsTerran() {
		darkTemplarBuildRatio /= 7;
	}

	public static void enemyIsZerg() {
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

		boolean shouldAlwaysBuild = xvr.canAfford(100)
				&& UnitCounter.getNumberOfBattleUnits() <= MIN_UNITS_FOR_DIFF_BUILDING;
		if (shouldAlwaysBuild || buildingQueueDetails == null
				|| freeMinerals >= 100) {
			if (barracks.getTrainingQueueSize() == 0) {
				xvr.buildUnit(barracks,
						defineUnitToBuild(freeMinerals, freeGas));
			}
		}
	}

	private static UnitTypes defineUnitToBuild(int freeMinerals, int freeGas) {
		int zealots = UnitCounter.getNumberOfUnits(ZEALOT);

		// If we don't have Observatory build than disallow production of units
		// which cost lot of gas.
		int forceFreeGas = 0;
		int darkTemplarGasBonus = 0;
		if (!UnitCounter.weHaveBuilding(ProtossObservatory.getBuildingtype())) {
			forceFreeGas = 100 + (UnitCounter.getNumberOfUnits(DARK_TEMPLAR) + UnitCounter
					.getNumberOfUnits(DRAGOON)) * 5;

			// If there's less than 2 Dark Templars, create them. Only then
			// start conserving gas for buildings like Observatory.
			if (UnitCounter.getNumberOfUnits(DARK_TEMPLAR) < 2) {
				darkTemplarGasBonus = forceFreeGas;
			}
		}

		boolean dragoonAllowed = UnitCounter
				.weHaveBuildingFinished(UnitTypes.Protoss_Cybernetics_Core)
				&& (freeMinerals >= 125 && (freeGas - forceFreeGas) >= 50);
		boolean darkTemplarAllowed = UnitCounter
				.weHaveBuildingFinished(UnitTypes.Protoss_Templar_Archives)
				&& (freeMinerals >= 125 && (freeGas - forceFreeGas + darkTemplarGasBonus) >= 100);
		boolean highTemplarAllowed = darkTemplarAllowed
				&& (freeMinerals >= 50 && (freeGas - forceFreeGas) >= 200);

		UnitTypes typeToBuild = ZEALOT;

		// Calculate ratio
		double totalRatio = zealotBuildRatio
				+ (dragoonAllowed ? dragoonBuildRatio : 0)
				+ (darkTemplarAllowed ? darkTemplarBuildRatio : 0);
		double totalInfantry = UnitCounter.getNumberOfInfantryUnits() + 1;

		// ===========================================================

		// DARK TEMPLAR
		if (darkTemplarAllowed) {
			int darkTemplars = UnitCounter.getNumberOfUnits(DARK_TEMPLAR);
			int highTemplars = UnitCounter.getNumberOfUnits(HIGH_TEMPLAR);

			// Build some HIGH Templars if there'are none.
			if (highTemplarAllowed
					&& ((darkTemplars >= 5 || darkTemplarBuildRatio < 10)
							&& highTemplars < 2 || freeGas > 1000)) {
				return HIGH_TEMPLAR;
			}

			double darkTemplarPercent = (double) darkTemplars / totalInfantry;
			if (darkTemplarPercent < darkTemplarBuildRatio / totalRatio) {
				return DARK_TEMPLAR;
			}
		}

		int dragoons = UnitCounter.getNumberOfUnits(DRAGOON);

		// DRAGOON
		if (dragoonAllowed) {
			double dragoonPercent = (double) dragoons / totalInfantry;
			if (dragoons < 1 || dragoonPercent < dragoonBuildRatio / totalRatio) {
				return DRAGOON;
			}
		}

		// HIGH TEMPLAR
		if (highTemplarAllowed
				&& UnitCounter.getNumberOfUnits(HIGH_TEMPLAR) < MINIMAL_HIGH_TEMPLARS) {
			return HIGH_TEMPLAR;
		}

		// ZEALOT
		double zealotPercent = zealots / totalInfantry;
		if (zealotPercent < zealotBuildRatio / totalRatio || LIMIT_ZEALOTS) {
			if (LIMIT_ZEALOTS) {
				if (zealots <= 5
						|| zealotPercent < (zealotBuildRatio / totalRatio / 2.5)
						|| xvr.canAfford(810)) {
					return ZEALOT;
				}
				return null;
			} else {
				return ZEALOT;
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
		if (isPlanAntiAirActive) {
			return;
		}

		isPlanAntiAirActive = true;
		zealotBuildRatio = 10;
		dragoonBuildRatio = 70;
		darkTemplarBuildRatio = 10;
	}

	public static boolean isPlanAntiAirActive() {
		return isPlanAntiAirActive;
	}

}
