package jnibwapi.protoss;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import jnibwapi.RUtilities;
import jnibwapi.XVR;
import jnibwapi.model.BaseLocation;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

public class ProtossNexus {

	private static XVR xvr = XVR.getInstance();

	private static int MAX_DIST_OF_MINERAL_FROM_BASE = 15;
	private static final int ARMY_UNITS_PER_NEW_BASE = 10;

	private static final UnitTypes buildingType = UnitTypes.Protoss_Nexus;

	public static void buildIfNecessary() {
		if (shouldBuild()) {
			Constructing.construct(xvr, buildingType);
		}
	}

	public static boolean shouldBuild() {
		int bases = UnitCounter.getNumberOfUnits(buildingType);
		int battleUnits = UnitCounter.getNumberOfBattleUnits();
		
		// FORCE quick expansion if we're rich 
		if (xvr.canAfford(580)) {
			return true;
		}

		// Initially, we must wait to have at least 3 barracks to build first
		// base.
		if (bases == 1) {
			if (battleUnits < 10
					|| Constructing.weAreBuilding(buildingType)
					|| UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Gateway) <= 1) {
				return false;
			}
		}

		// More than one base
		else {

			// But if we already have another base...
			if ((bases * ARMY_UNITS_PER_NEW_BASE > battleUnits && !xvr
					.canAfford(550))
					|| UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Gateway) <= 3) {
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
		if (gasGatherersForBase > 4) {
			int overLimitWorkers = gasGatherersForBase - 4;

			// We can send workers only if there's another base
			if (UnitCounter.getNumberOfUnits(buildingType) > 1) {
				ArrayList<Unit> gatherers = getGasWorkersNearBase(base);
				for (int i = 0; i < overLimitWorkers && i < gatherers.size(); i++) {
					UnitActions.moveTo(gatherers.get(i), base);
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
				for (Unit otherBase : xvr.getUnitsOfTypeCompleted(buildingType)) {
					if (!base.equals(otherBase)) {
						int overLimitWorkersAtOtherBase = getNumberOfMineralGatherersForBase(otherBase)
								- getOptimalMineralGatherersAtBase(otherBase);
						if (overLimitWorkers <= overLimitWorkersAtOtherBase) {
							continue;
						}

						ArrayList<Unit> gatherers = getMineralWorkersNearBase(base);
						for (int i = 0; i < overLimitWorkers
								&& i < gatherers.size(); i++) {
							UnitActions.moveTo(gatherers.get(i), otherBase);
						}
						break;
					}
				}
			}
		}
	}

	public static Point findTileForBase() {
		Unit mainBase = xvr.getFirstBase();
		double nearestDistance = 999999;
		BaseLocation nearestFreeBaseLocation = null;

		for (BaseLocation baseLocation : xvr.getBwapi().getMap()
				.getBaseLocations()) {
			if (existsBaseNear(baseLocation.getX(), baseLocation.getY())) {
				continue;
			}

			double distance = xvr.getDistanceBetween(baseLocation.getX(),
					baseLocation.getY(), mainBase.getX(), mainBase.getY());
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearestFreeBaseLocation = baseLocation;
			}
		}

		return new Point(nearestFreeBaseLocation.getTx(),
				nearestFreeBaseLocation.getTy());
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

		for (Unit unit : xvr.getUnitsInRadius(x, y, 10)) {
			if (unit.getType().isBase()) {
				return true;
			}
		}

		return false;
	}

	private static boolean shouldBuildWorkers(Unit base) {
		int workersNearBase = getNumberOfWorkersNearBase(base);
		double existingToOptimalRatio = (double) workersNearBase
				/ getOptimalMineralGatherersAtBase(base);

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
		return (int) (2.65 * numberOfMineralsNearbyBase);
	}

	public static Unit getNearestBaseForUnit(Unit unit) {
		double nearestDistance = 9999999;
		Unit nearestBase = null;

		for (Unit base : xvr.getUnitsOfTypeCompleted(buildingType)) {
			double distance = xvr.getDistanceBetween(base, unit);
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
			if (distance <= MAX_DIST_OF_MINERAL_FROM_BASE) {
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

}
