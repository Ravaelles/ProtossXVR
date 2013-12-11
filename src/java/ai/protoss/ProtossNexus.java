package ai.protoss;

import java.util.ArrayList;
import java.util.HashMap;

import jnibwapi.model.BaseLocation;
import jnibwapi.model.Map;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.Debug;
import ai.core.XVR;
import ai.handling.constructing.Constructing;
import ai.handling.constructing.ShouldBuildCache;
import ai.handling.map.MapPoint;
import ai.handling.units.UnitCounter;
import ai.managers.UnitManager;
import ai.managers.WorkerManager;
import ai.utils.RUtilities;

public class ProtossNexus {

	private static XVR xvr = XVR.getInstance();

	public static final int MAX_WORKERS = 65;

	private static int MAX_DIST_OF_MINERAL_FROM_BASE = 19;
	private static final int ARMY_UNITS_PER_NEW_BASE = 10;
	private static final int MIN_WORKERS = 15;
	private static final int WORKERS_PER_GEYSER = 3;

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

		// FORCE quick expansion if we're rich
		if (xvr.canAfford(330)) {
			if (gateways >= 3 && battleUnits > 10) {
				return true;
			}
			
			// int thresholdBattleUnits = xvr.getENEMY().isProtoss() ? 9 : 5;
			int thresholdBattleUnits = ProtossGateway.MIN_UNITS_FOR_DIFF_BUILDING - 4;
			if (battleUnits < thresholdBattleUnits || !xvr.canAfford(350)) {
				return false;
			} else {
				return true;
			}
		}
		if (xvr.canAfford(430)) {
			if (battleUnits < 8) {
				return false;
			} else {
				return true;
			}
		}

		// Initially, we must wait to have at least 3 barracks to build first
		// base.
		if (bases == 1) {
			if (UnitCounter
					.getNumberOfUnitsCompleted(UnitTypes.Protoss_Gateway) <= 2) {
				return false;
			}
		}

		// More than one base
		else {

			// But if we already have another base...
			if ((bases * ARMY_UNITS_PER_NEW_BASE > battleUnits && !xvr
					.canAfford(550))) {
				return false;
			}
		}

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
		int gasGatherersForBase = getNumberofGasGatherersForBase(base);
		if (gasGatherersForBase > WORKERS_PER_GEYSER) {
			int overLimitWorkers = gasGatherersForBase - WORKERS_PER_GEYSER;

			// Check whether the geyser isn't depleted
			if (xvr.getUnitOfTypeNearestTo(UnitTypes.Protoss_Assimilator, base)
					.getResources() == 0) {
				overLimitWorkers = gasGatherersForBase - 1;
			}

			// We can send workers only if there's another base
			if (overLimitWorkers > 0
					&& UnitCounter.getNumberOfUnits(buildingType) > 1) {
				ArrayList<Unit> gatherers = getGasWorkersNearBase(base);
				// for (int i = 0; i < overLimitWorkers && i < gatherers.size();
				// i++) {
				// UnitActions.moveTo(gatherers.get(i), base);
				// }
				ArrayList<Unit> mineralsInNeihgborhood = xvr
						.getUnitsOfGivenTypeInRadius(
								UnitTypes.Resource_Mineral_Field, 25, base,
								false);
				if (!mineralsInNeihgborhood.isEmpty()) {
					for (Unit worker : gatherers) {
						WorkerManager
								.forceGatherMinerals(
										worker,
										(Unit) RUtilities
												.getRandomListElement(mineralsInNeihgborhood));
					}
				}
			}
		}

		// If we don't have enough SCVs at our base
		if (shouldBuildWorkers(base)) {
			if (base.getTrainingQueueSize() == 0) {
				xvr.buildUnit(base, UnitManager.WORKER);
			}
		}

		// We have more than optimal number workers at base
		else {

			// If we have workers at base over optimal amount and we have
			// another
			// base try sending them to the new base
			int overLimitWorkers = getNumberOfMineralGatherersForBase(base)
					- getOptimalMineralGatherersAtBase(base) - 1;
			if (overLimitWorkers > 0
					&& UnitCounter.getNumberOfUnitsCompleted(buildingType) > 1) {
				ArrayList<Unit> gatherers = getMineralWorkersNearBase(base);

				ArrayList<Unit> mineralsInNeihgborhood = xvr
						.getUnitsOfGivenTypeInRadius(
								UnitTypes.Resource_Mineral_Field, 25, base,
								false);
				if (!mineralsInNeihgborhood.isEmpty()) {
					for (Unit worker : gatherers) {
						if (RUtilities.rand(0, 15) == 0) {
							WorkerManager
									.forceGatherMinerals(
											worker,
											(Unit) RUtilities
													.getRandomListElement(mineralsInNeihgborhood));
						}
					}
				}

				// boolean sendSomewhere = false;
				// for (Unit otherBase :
				// xvr.getUnitsOfTypeCompleted(buildingType)) {
				// if (!base.equals(otherBase)) {
				// int overLimitWorkersAtOtherBase =
				// getNumberOfMineralGatherersForBase(otherBase)
				// - getOptimalMineralGatherersAtBase(otherBase);
				// if (overLimitWorkers <= overLimitWorkersAtOtherBase) {
				// continue;
				// }
				//
				// for (int i = 0; i < overLimitWorkers
				// && i < gatherers.size(); i++) {
				// // UnitActions.moveTo(gatherers.get(i), otherBase);
				// WorkerManager.gatherResources(gatherers.get(i),
				// otherBase);
				// }
				// sendSomewhere = true;
				// break;
				// }
				// }
				//
				// // If not send anywhere, kill the damned units!!!
				// if (!sendSomewhere) {
				// for (int i = 0; i < overLimitWorkers
				// && i < gatherers.size(); i++) {
				// // gatherers.get(i).markUnitToScrap();
				// UnitActions.attackEnemyUnit(gatherers.get(i),
				// MapExploration.getRandomEnemyBuilding());
				// }
				// }
			}
		}

		// =========================================
		// ======== SSCAI FIX: Remove 0 minerals ===
		checkIfRemoveZeroMineralsCrystal(base);
	}

	private static void checkIfRemoveZeroMineralsCrystal(Unit base) {
		final int SEARCH_IN_RADIUS = 30;
		final int ACT_IF_AT_LEAST_N_WORKERS = 20;

		// Create list of mineral gatheres near base. It's essential not to use
		// defined method, as we need to significantly increase seek range.
		ArrayList<Unit> workers = xvr.getWorkers();
		if (workers.size() < ACT_IF_AT_LEAST_N_WORKERS) {
			return;
		}

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
			ArrayList<Unit> mineralsAroundTheBase = xvr
					.getUnitsOfGivenTypeInRadius(
							UnitTypes.Resource_Mineral_Field, SEARCH_IN_RADIUS,
							base, false);

			for (Unit mineral : mineralsAroundTheBase) {
				if (mineral.getResources() == 0 && !mineral.isBeingGathered()) {
					// int max = (3 + UnitCounter
					// .getNumberOfUnits(UnitTypes.Protoss_Pylon)) / 3;
					// if (max >= mineralWorkersNearBase.size()) {
					// max = mineralWorkersNearBase.size() - 1;
					// }
					int max = 1;
					Unit unitToUse = mineralWorkersNearBase.get(RUtilities
							.rand(0, max));
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

		Map map = xvr.getBwapi().getMap();

		Unit expansionCenter = xvr.getFirstBase();
		if (expansionCenter == null) {
			return null;
		}

		// Region mainBaseRegion = map.getRegion(mainBase.getTx(),
		// mainBase.getTy());
		double nearestDistance = 999999;
		BaseLocation nearestFreeBaseLocation = null;

		for (BaseLocation location : xvr.getBwapi().getMap().getBaseLocations()) {

			// If there's already a base there don't build. Check for both our
			// and enemy bases.
			if (existsBaseNear(location.getX(), location.getY())) {
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
			// System.out.println("TO NEXT BASE " + location.toStringLocation()
			// + " DIST = " + distance);
			if (distance < 0) { // -1 means there's no path
				// System.out.println("NO PATH TO: " + location);
				continue;
			}

			// double distance = xvr.getDistanceBetween(location.getX(),
			// location.getY(), mainBase.getX(), mainBase.getY());
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearestFreeBaseLocation = location;
			}
		}

		if (nearestFreeBaseLocation != null) {
			MapPoint point = nearestFreeBaseLocation;

			// System.out.println("OK, NEAREST IS " + point.toStringLocation()
			// + " DIST = " + nearestDistance);
			// System.out.println();

			// MapPoint point = new MapPointInstance(
			// nearestFreeBaseLocation.getTx(),
			// nearestFreeBaseLocation.getTy());
			// Debug.message(xvr, "Tile for new base: " + point.getTx() + ","
			// + point.getTy());
			_cachedNextBaseTile = Constructing.getLegitTileToBuildNear(
					xvr.getRandomWorker(), buildingType, point, 0, 30, false);
		} else {
			if (UnitCounter.getNumberOfUnits(UnitManager.BASE) <= 1) {
				Debug.message(xvr, "Error! No place for next base!");
			}
			_cachedNextBaseTile = null;
		}

		return _cachedNextBaseTile;
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

		if (workers >= MAX_WORKERS) {
			return false;
		}
		if (workers < MIN_WORKERS) {
			return true;
		}

		// if (UnitCounter.getNumberOfBattleUnits() < 3 * workers) {
		// return false;
		// }

		int workersNearBase = getNumberOfWorkersNearBase(base);
		double existingToOptimalRatio = (double) workersNearBase
				/ getOptimalMineralGatherersAtBase(base);

		// If we have only one base and already some workers, promote more
		// gateways
		if (UnitCounter.getNumberOfUnits(buildingType) == 1
				&& UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Gateway) < 3) {
			if (existingToOptimalRatio > 0.5) {
				return false;
			}
		}

		// Check if need to build some building. If so then check if we can
		// afford to train SCV, we don't want to steal resources.
		int[] shouldBuildAnyBuilding = Constructing.shouldBuildAnyBuilding();
		if (shouldBuildAnyBuilding != null) {

			// If we can't afford extra 50 minerals, disallow building of
			// worker. BUT if we don't have even 40% of mineral gatherers then
			// allow.
			if (!xvr.canAfford(shouldBuildAnyBuilding[0] + 100)
					&& existingToOptimalRatio > 0.4) {
				return false;
			}
		}

		return (existingToOptimalRatio < 1 ? true : false);
	}

	private static int getNumberOfWorkersNearBase(Unit base) {
		return xvr.countUnitsOfGivenTypeInRadius(UnitManager.WORKER, 15,
				base.getX(), base.getY(), true);
	}

	public static int getOptimalMineralGatherersAtBase(Unit base) {
		int numberOfMineralsNearbyBase = xvr.countMineralsInRadiusOf(12,
				base.getX(), base.getY());
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
		int MAX_DISTANCE = 12;

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
		HashMap<Unit, Double> minerals = new HashMap<Unit, Double>();

		for (Unit mineral : xvr.getMineralsUnits()) {
			double distance = xvr.getDistanceBetween(mineral, base);
			// double distance = xvr.getBwapi().getMap()
			// .getGroundDistance(mineral, base) / 32;
			if (distance < 0) {
				continue;
			}

			if (distance <= MAX_DIST_OF_MINERAL_FROM_BASE
					&& mineral.getResources() > 100) {
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
			if (worker.isGatheringMinerals()
					&& xvr.getDistanceBetween(nearestBase, worker) < 20) {
				units.add(worker);
			}
		}
		return units;
	}

	public static ArrayList<Unit> getGasWorkersNearBase(Unit nearestBase) {
		ArrayList<Unit> units = new ArrayList<Unit>();
		for (Unit worker : xvr.getWorkers()) {
			if (worker.isGatheringGas()
					&& xvr.getDistanceBetween(nearestBase, worker) < 12) {
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

	public static boolean isExistingCompletedAssimilatorNearBase(
			Unit nearestBase) {
		ArrayList<Unit> inRadius = xvr.getUnitsOfGivenTypeInRadius(
				ProtossAssimilator.getBuildingtype(), 12, nearestBase.getX(),
				nearestBase.getY(), true);

		if (!inRadius.isEmpty() && inRadius.get(0).isCompleted()) {
			return true;
		} else {
			return false;
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
			// System.out.println("getSecondBaseLocation first");
			_secondBase = ProtossNexus.getTileForNextBase(true);
			return _secondBase;
		}
	}

	public static void updateNextBaseToExpand() {
		// System.out.println("TEST next base");
		getTileForNextBase(true);
	}

}
