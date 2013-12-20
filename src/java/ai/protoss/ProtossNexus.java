package ai.protoss;

import java.util.ArrayList;
import java.util.HashMap;

import jnibwapi.model.BaseLocation;
import jnibwapi.model.Map;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;
import ai.handling.constructing.Constructing;
import ai.handling.constructing.ShouldBuildCache;
import ai.handling.map.MapPoint;
import ai.handling.units.UnitCounter;
import ai.managers.BotStrategyManager;
import ai.managers.StrategyManager;
import ai.managers.UnitManager;
import ai.managers.WorkerManager;
import ai.utils.RUtilities;

public class ProtossNexus {

	private static XVR xvr = XVR.getInstance();

	public static final int MAX_WORKERS = 65;

	private static int MAX_DIST_OF_MINERAL_FROM_BASE = 12;
	private static final int ARMY_UNITS_PER_NEW_BASE = 10;
	private static final int MIN_WORKERS = 19;
	public static final int WORKERS_PER_GEYSER = 4;

	private static MapPoint _secondBase = null;
	private static MapPoint _cachedNextBaseTile = null;

	private static final UnitTypes buildingType = UnitTypes.Protoss_Nexus;

	public static void buildIfNecessary() {
		if (shouldBuild()) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			Constructing.construct(xvr, buildingType);
		}
		ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
	}

	public static boolean shouldBuild() {
		int bases = UnitCounter.getNumberOfUnits(buildingType);
		int gateways = UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Gateway);
		int battleUnits = UnitCounter.getNumberOfBattleUnits();

		if (xvr.canAfford(750)) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			return true;
		}

		if (xvr.getTimeSecond() >= 380 && bases == 1
				&& !Constructing.weAreBuilding(UnitManager.BASE)) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			return true;
		}

		if (bases >= 2 && battleUnits <= bases * 9 && !xvr.canAfford(700)) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
			return false;
		}

		if (battleUnits < StrategyManager.getMinBattleUnits() + 2) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
			return false;
		}

		int quickUnitsThreshold = BotStrategyManager.isExpandWithCannons() ? 4 : 13;

		if (battleUnits >= quickUnitsThreshold && xvr.canAfford(350) || xvr.canAfford(500)) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			return true;
		}

		// FORCE quick expansion if we're rich
		if (xvr.canAfford(330)) {
			if (gateways >= 3 && battleUnits >= (BotStrategyManager.isExpandWithCannons() ? 7 : 15)
					&& !XVR.isEnemyTerran()) {
				ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
				return true;
			}

			// int thresholdBattleUnits = xvr.getENEMY().isProtoss() ? 9 : 5;
			int thresholdBattleUnits = ProtossGateway.MIN_UNITS_FOR_DIFF_BUILDING - 4;
			if (battleUnits < thresholdBattleUnits || !xvr.canAfford(350)) {
				ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
				return false;
			} else {
				ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
				return true;
			}
		}
		if (xvr.canAfford(410)) {
			if (battleUnits < 8) {
				ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
				return false;
			} else {
				ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
				return true;
			}
		}

		// Initially, we must wait to have at least 3 barracks to build first
		// base.
		if (bases == 1) {
			if (UnitCounter.getNumberOfUnitsCompleted(UnitTypes.Protoss_Gateway) <= 2
					&& !xvr.canAfford(500)) {
				ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
				return false;
			}
		}

		// More than one base
		else {

			// But if we already have another base...
			if ((bases * ARMY_UNITS_PER_NEW_BASE > battleUnits && !xvr.canAfford(550))) {
				ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
				return false;
			}
		}

		ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
		return true;
	}

	public static void act() {
		for (Unit base : xvr.getUnitsOfTypeCompleted(buildingType)) {
			ProtossNexus.act(base);
		}
	}

	public static void act(Unit base) {

		// Calculate number of workers at nearby geyser, if there's too many
		// of them, send some of them away
		checkNumberOfGasWorkersAt(base);

		// If we don't have enough workers at our base
		checkNumberOfWorkersOverallAtBase(base);

		// =========================================
		// ======== SSCAI FIX: Remove 0 minerals ===
		checkIfRemoveZeroMineralsCrystal(base);
	}

	private static void checkNumberOfWorkersOverallAtBase(Unit base) {
		if (shouldBuildWorkers(base)) {
			if (base.getTrainingQueueSize() == 0) {
				xvr.buildUnit(base, UnitManager.WORKER);
			}
		}

		// We have more than optimal number workers at base
		else {
			// tooManyWorkersAtBase(base);
		}
	}

	private static void checkNumberOfGasWorkersAt(Unit base) {
		if (!ProtossNexus.isExistingCompletedAssimilatorNearBase(base)) {
			return;
		}

		int gasGatherersForBase = getNumberofGasGatherersForBase(base);
		if (gasGatherersForBase > WORKERS_PER_GEYSER) {
			int overLimitWorkers = gasGatherersForBase - WORKERS_PER_GEYSER;

			// Check whether the geyser isn't depleted
			if (xvr.getUnitOfTypeNearestTo(UnitTypes.Protoss_Assimilator, base).getResources() < 40) {
				overLimitWorkers = gasGatherersForBase - 1;
			}

			// We can send workers only if there's another base
			if (overLimitWorkers > 0) {
				haveOverLimitGasWorkers(base, overLimitWorkers);
			}
		} else {
			int numRequiredWorkers = WORKERS_PER_GEYSER - gasGatherersForBase;
			int optimalMineralWorkersAtBase = getOptimalMineralGatherersAtBase(base)
					- WORKERS_PER_GEYSER;
			double mineralWorkersToOptimalRatio = (double) getNumberOfMineralGatherersForBase(base)
					/ optimalMineralWorkersAtBase;
			if (mineralWorkersToOptimalRatio < 0.5) {
				numRequiredWorkers--;
			}
			if (mineralWorkersToOptimalRatio < 0.6) {
				numRequiredWorkers--;
			}
			if (mineralWorkersToOptimalRatio < 0.7) {
				numRequiredWorkers--;
			}
			if (numRequiredWorkers <= 0) {
				numRequiredWorkers = 1;
			}

			ArrayList<Unit> gatherers = getMineralWorkersNearBase(base);
			for (int i = 0; i < numRequiredWorkers && i < gatherers.size(); i++) {
				Unit gathererToAssign = gatherers.get(i);
				WorkerManager.gatherResources(gathererToAssign, base);
			}
		}
	}

	private static void haveOverLimitGasWorkers(Unit base, int overLimitWorkers) {
		ArrayList<Unit> gatherers = getGasWorkersNearBase(base);
		// for (int i = 0; i < overLimitWorkers && i < gatherers.size();
		// i++) {
		// UnitActions.moveTo(gatherers.get(i), base);
		// }
		ArrayList<Unit> mineralsInNeihgborhood = xvr.getUnitsOfGivenTypeInRadius(
				UnitTypes.Resource_Mineral_Field, 25, base, false);
		if (!mineralsInNeihgborhood.isEmpty()) {
			for (Unit worker : gatherers) {
				// WorkerManager
				// .forceGatherMinerals(
				// worker,
				// (Unit) RUtilities
				// .getRandomListElement(mineralsInNeihgborhood));
				WorkerManager.gatherResources(worker, base);
			}
		}
	}

	private static void checkIfRemoveZeroMineralsCrystal(Unit base) {
		final int SEARCH_IN_RADIUS = 30;
		final int ACT_IF_AT_LEAST_N_WORKERS = 20;

		// Create list of mineral gatheres near base. It's essential not to use
		// defined method, as we need to significantly increase seek range.
		if (UnitCounter.getNumberOfUnits(UnitManager.WORKER) < ACT_IF_AT_LEAST_N_WORKERS) {
			return;
		}

		ArrayList<Unit> workers = xvr.getWorkers();
		ArrayList<Unit> mineralWorkersNearBase = new ArrayList<>();
		for (Unit worker : workers) {
			if (worker.isGatheringMinerals() && !worker.isConstructing()
					&& xvr.getDistanceBetween(base, worker) <= SEARCH_IN_RADIUS) {
				mineralWorkersNearBase.add(worker);
			}
		}

		// If we have at least X mineral workers near base, send one of them to
		// gather this lonely mineral-obstacle
		if (mineralWorkersNearBase.size() >= ACT_IF_AT_LEAST_N_WORKERS) {
			ArrayList<Unit> mineralsAroundTheBase = xvr.getUnitsOfGivenTypeInRadius(
					UnitTypes.Resource_Mineral_Field, SEARCH_IN_RADIUS, base, false);

			for (Unit mineral : mineralsAroundTheBase) {
				if (mineral.getResources() == 0 && !mineral.isBeingGathered()) {
					// int max = (3 + UnitCounter
					// .getNumberOfUnits(UnitTypes.Protoss_Pylon)) / 3;
					// if (max >= mineralWorkersNearBase.size()) {
					// max = mineralWorkersNearBase.size() - 1;
					// }
					int max = 1;
					Unit unitToUse = mineralWorkersNearBase.get(RUtilities.rand(0, max));
					// Unit unitToUse = mineralWorkersNearBase.get(RUtilities
					// .rand(0, mineralWorkersNearBase.size() - 1));
					WorkerManager.forceGatherMinerals(unitToUse, mineral);
				}
			}
		}
	}

	/** Find building tile for new base. */
	public static MapPoint getTileForNextBase(boolean forceNewSolution) {

		// Try to get cached value
		if (_cachedNextBaseTile != null && !forceNewSolution) {
			return _cachedNextBaseTile;
		}

		// ===============================
		BaseLocation nearestFreeBaseLocation = getNearestFreeBaseLocation();
		if (nearestFreeBaseLocation != null) {
			MapPoint point = nearestFreeBaseLocation;

			// MapPoint point = new MapPointInstance(
			// nearestFreeBaseLocation.getTx(),
			// nearestFreeBaseLocation.getTy());
			// Debug.message(xvr, "Tile for new base: " + point.getTx() + ","
			// + point.getTy());
			System.out.println("Tile for new base: " + point);
			_cachedNextBaseTile = Constructing.getLegitTileToBuildNear(xvr.getRandomWorker(),
					buildingType, point, 0, 30, false);
			System.out.println(" processed: " + _cachedNextBaseTile);
			System.out.println();
		} else {
			// if (UnitCounter.getNumberOfUnits(UnitManager.BASE) <= 1) {
			// Debug.message(xvr, "Error! No place for next base!");
			System.out.println("Error! No place for next base!");
			// }
			_cachedNextBaseTile = null;
		}

		return _cachedNextBaseTile;
	}

	private static BaseLocation getNearestFreeBaseLocation() {
		Unit expansionCenter = xvr.getFirstBase();
		// if (!xvr.getLastBase().equals(xvr.getFirstBase())) {
		// expansionCenter = xvr.getLastBase();
		// }
		if (expansionCenter == null) {
			return null;
		}
		Map map = xvr.getBwapi().getMap();
		BaseLocation nearestFreeBaseLocation = null;
		double nearestDistance = 999999;
		for (BaseLocation location : xvr.getBwapi().getMap().getBaseLocations()) {

			// If there's already a base there don't build. Check for both our
			// and enemy bases.
			if (existsBaseNear(location.getX(), location.getY())) {
				// || !xvr.getBwapi().isVisible(location)
				continue;
			}

			// Check if the new base is connected to the main base by land.
			// Region newBaseRegion = xvr.getBwapi().getMap()
			// .getRegion(location.getX(), location.getY());
			// if (!map.isConnected(location, expansionCenter)) {
			// continue;
			// }

			// Look for for the closest base and remember it.
			double distance = map.getGroundDistance(location, expansionCenter) / 32;
			if (distance < 0) { // -1 means there's no path
				continue;
			}

			// double distance = xvr.getDistanceBetween(location.getX(),
			// location.getY(), mainBase.getX(), mainBase.getY());
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearestFreeBaseLocation = location;
			}
		}

		return nearestFreeBaseLocation;
	}

	private static boolean existsBaseNear(int x, int y) {
		// Unit baseTerran = xvr.getUnitOfTypeNearestTo(
		// UnitTypes.Protoss_Command_Center, x, y);
		// Unit baseProtoss =
		// xvr.getUnitOfTypeNearestTo(UnitTypes.Protoss_Nexus,
		// x, y);
		// Unit baseZerg = xvr.getUnitOfTypeNearestTo(UnitTypes.Zerg_Hatchery,
		// x,
		// y);
		// if (baseZerg != null && xvr.getDistanceBetween(baseZerg, x, y) < 15)
		// {
		// return false;
		// }
		// if (baseTerran != null && xvr.getDistanceBetween(baseTerran, x, y) <
		// 15) {
		// return false;
		// }
		// if (baseProtoss != null
		// && xvr.getDistanceBetween(baseProtoss, x, y) < 15) {
		// return false;
		// }

		for (Unit unit : xvr.getUnitsInRadius(x, y, 14)) {
			if (unit.getType().isBase()) {
				return true;
			}
		}

		return false;
	}

	private static boolean shouldBuildWorkers(Unit base) {
		int workers = UnitCounter.getNumberOfUnits(UnitManager.WORKER);
		int pylons = UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Pylon);
		boolean weAreBuildingPylon = Constructing.weAreBuilding(UnitTypes.Protoss_Pylon);

		if (workers >= MAX_WORKERS) {
			return false;
		}

		// Quick FIRST PYLON
		// if (BotStrategyManager.isExpandWithCannons()) {
		if (workers == 8 && (pylons == 0 || !weAreBuildingPylon) && !xvr.canAfford(150)) {
			return false;

		}
		// }

		// Quick FIRST CANNON
		if (BotStrategyManager.isExpandWithCannons()) {
			if (pylons == 2) {
				if (!Constructing.weAreBuilding(UnitTypes.Protoss_Photon_Cannon)
						&& !xvr.canAfford(200) && workers >= 20) {
					return false;
				} else {
					return xvr.canAfford(184);
				}
			}
		}

		if (workers < MIN_WORKERS) {
			return true;
		}

		int bases = UnitCounter.getNumberOfUnits(UnitManager.BASE);
		int cannons = UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Photon_Cannon);

		if (workers > bases * 25) {
			return false;
		}

		if (BotStrategyManager.isExpandWithCannons()) {
			if (workers >= MIN_WORKERS && cannons < 2) {
				return false;
			}
		}

		// if (UnitCounter.getNumberOfBattleUnits() < 3 * workers) {
		// return false;
		// }

		int workersNearBase = getNumberOfWorkersNearBase(base);
		double existingToOptimalRatio = (double) workersNearBase
				/ getOptimalMineralGatherersAtBase(base);

		// If we have only one base and already some workers, promote more
		// gateways
		int gateways = UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Gateway);
		if (UnitCounter.getNumberOfUnits(buildingType) == 1 && gateways <= 3) {
			if (existingToOptimalRatio > 0.5 + 0.12 * gateways) {
				return false;
			}
		}

		// Check if need to build some building. If so then check if we can
		// afford to train worker, we don't want to steal resources.
		int[] shouldBuildAnyBuilding = Constructing.shouldBuildAnyBuilding();
		if (shouldBuildAnyBuilding != null) {

			// If we can't afford extra 50 minerals, disallow building of
			// worker. BUT if we don't have even 40% of mineral gatherers then
			// allow.
			if (!xvr.canAfford(shouldBuildAnyBuilding[0] + 100) && existingToOptimalRatio > 0.4) {
				return false;
			}
		}

		return (existingToOptimalRatio < 1 ? true : false);
	}

	private static int getNumberOfWorkersNearBase(Unit base) {
		return xvr.countUnitsOfGivenTypeInRadius(UnitManager.WORKER, 15, base.getX(), base.getY(),
				true);
	}

	public static int getOptimalMineralGatherersAtBase(Unit base) {
		int numberOfMineralsNearbyBase = xvr.countMineralsInRadiusOf(12, base.getX(), base.getY());
		return (int) (2.59 * numberOfMineralsNearbyBase) + WORKERS_PER_GEYSER;
	}

	public static Unit getNearestBaseForUnit(MapPoint point) {
		double nearestDistance = 9999999;
		Unit nearestBase = null;

		for (Unit base : xvr.getUnitsOfTypeCompleted(buildingType)) {
			double distance = xvr.getDistanceBetween(base, point);
			if (distance < nearestDistance) {
				distance = nearestDistance;
				nearestBase = base;
			}
		}

		return nearestBase;
	}

	public static Unit getNearestMineralGathererForUnit(Unit base) {
		double nearestDistance = 9999999;
		Unit nearestBase = null;

		for (Unit scv : xvr.getWorkers()) {
			if (scv.isGatheringMinerals()) {
				double distance = xvr.getDistanceBetween(scv, base);
				if (distance < nearestDistance) {
					distance = nearestDistance;
					nearestBase = scv;
				}
			}
		}

		return nearestBase;
	}

	public static int getNumberofGasGatherersForBase(Unit base) {
		int result = 0;
		int MAX_DISTANCE = 10;

		for (Unit worker : xvr.getWorkers()) {
			if (worker.isGatheringGas()) {
				double distance = xvr.getDistanceBetween(worker, base);
				if (distance < MAX_DISTANCE) {
					result++;
				}
			}
		}

		return result;
	}

	public static int getNumberOfMineralGatherersForBase(Unit base) {
		int result = 0;
		int MAX_DISTANCE = 12;

		for (Unit worker : xvr.getWorkers()) {
			if (worker.isGatheringMinerals()) {
				double distance = xvr.getDistanceBetween(worker, base);
				if (distance < MAX_DISTANCE) {
					result++;
				}
			}
		}

		return result;
	}

	public static ArrayList<Unit> getMineralsNearBase(Unit base) {
		return getMineralsNearBase(base, MAX_DIST_OF_MINERAL_FROM_BASE);
	}

	public static ArrayList<Unit> getMineralsNearBase(Unit base, int maxDist) {
		HashMap<Unit, Double> minerals = new HashMap<Unit, Double>();

		for (Unit mineral : xvr.getMineralsUnits()) {
			double distance = xvr.getDistanceBetween(mineral, base);
			// double distance = xvr.getBwapi().getMap()
			// .getGroundDistance(mineral, base) / 32;
			if (distance < 0) {
				continue;
			}

			if (distance <= maxDist && mineral.getResources() > 1) {
				minerals.put(mineral, distance);
			}
		}

		ArrayList<Unit> sortedList = new ArrayList<Unit>();
		sortedList.addAll(RUtilities.sortByValue(minerals, true).keySet());
		return sortedList;
	}

	public static ArrayList<Unit> getWorkersNearBase(Unit nearestBase) {
		ArrayList<Unit> units = new ArrayList<Unit>();
		for (Unit worker : xvr.getWorkers()) {
			if (xvr.getDistanceBetween(nearestBase, worker) < 20) {
				units.add(worker);
			}
		}
		return units;
	}

	public static ArrayList<Unit> getMineralWorkersNearBase(Unit nearestBase) {
		ArrayList<Unit> units = new ArrayList<Unit>();
		for (Unit worker : xvr.getWorkers()) {
			if (worker.isGatheringMinerals() && xvr.getDistanceBetween(nearestBase, worker) < 20) {
				units.add(worker);
			}
		}
		return units;
	}

	public static ArrayList<Unit> getGasWorkersNearBase(Unit nearestBase) {
		ArrayList<Unit> units = new ArrayList<Unit>();
		for (Unit worker : xvr.getWorkers()) {
			if (worker.isGatheringGas() && xvr.getDistanceBetween(nearestBase, worker) < 12) {
				units.add(worker);
			}
		}
		return units;
	}

	public static void initialMineralGathering() {
		ArrayList<Unit> minerals = getMineralsNearBase(xvr.getFirstBase());

		int counter = 0;
		for (Unit unit : getWorkersNearBase(xvr.getFirstBase())) {
			WorkerManager.forceGatherMinerals(unit, minerals.get(counter));

			counter++;
		}
	}

	public static ArrayList<Unit> getBases() {
		return xvr.getUnitsOfType(UnitTypes.Protoss_Nexus);
	}

	public static UnitTypes getBuildingtype() {
		return buildingType;
	}

	public static boolean isExistingCompletedAssimilatorNearBase(Unit nearestBase) {
		ArrayList<Unit> inRadius = xvr.getUnitsOfGivenTypeInRadius(
				ProtossAssimilator.getBuildingtype(), 12, nearestBase, true);

		if (!inRadius.isEmpty() && inRadius.get(0).isCompleted()
				&& inRadius.get(0).getResources() > 50) {
			return true;
		} else {
			return false;
		}
	}

	public static Unit getExistingCompletedAssimilatorNearBase(Unit nearestBase) {
		ArrayList<Unit> inRadius = xvr.getUnitsOfGivenTypeInRadius(
				ProtossAssimilator.getBuildingtype(), 12, nearestBase, true);

		if (!inRadius.isEmpty() && inRadius.get(0).isCompleted()
				&& inRadius.get(0).getResources() > 50) {
			return inRadius.get(0);
		} else {
			return null;
		}
	}

	public static Unit getRandomBase() {
		ArrayList<Unit> bases = getBases();
		if (bases.isEmpty()) {
			return null;
		}
		return (Unit) RUtilities.randomElement(bases);
	}

	public static MapPoint getSecondBaseLocation() {
		if (_secondBase != null) {
			return _secondBase;
		} else {
			_secondBase = ProtossNexus.getTileForNextBase(true);
			return _secondBase;
		}
	}

	public static void updateNextBaseToExpand() {
		getTileForNextBase(true);
	}

}
