package jnibwapi.protoss;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import jnibwapi.RUtilities;
import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

public class WorkerManager {

	private static XVR xvr = XVR.getInstance();

	public static void act() {
		int counter = 0;
		for (Unit worker : xvr.getUnitsOfType(UnitManager.WORKER)) {
			if (counter != 5) {
				WorkerManager.act(worker);
			} else {
				MapExploration.explore(worker);
			}

			counter++;
		}
	}

	public static void act(Unit unit) {

		// Act with worker that is under attack
		if (unit.isUnderAttack()) {
			Point goTo = null;

			// Try to go to the nearest bunker
			Unit defensiveBuildings = xvr.getUnitOfTypeNearestTo(
					UnitTypes.Protoss_Photon_Cannon, unit);
			if (defensiveBuildings != null) {
				goTo = new Point(defensiveBuildings.getX(),
						defensiveBuildings.getY());
			} else {
				goTo = new Point(xvr.getFirstBase().getX(), xvr.getFirstBase()
						.getY());
			}

			if (goTo != null) {
				if (xvr.getDistanceBetween(unit, goTo) >= 15) {
					UnitActions.moveTo(unit, goTo.x, goTo.y);
				} else {
					UnitActions.moveTo(unit,
							goTo.x + 5 - RUtilities.rand(0, 12), goTo.y + 5
									- RUtilities.rand(0, 12));
					UnitActions.callForHelp(unit, false);
				}
			}
		}

		// Act with idle worker
		else if (unit.isIdle()) {

			// Find the nearest base for this SCV
			Unit nearestBase = ProtossNexus.getNearestBaseForUnit(unit);

			// If base exists try to gather resources
			if (nearestBase != null) {
				gatherResources(unit, nearestBase);
			}
		}

		// Act with unit that is possibly stuck e.g. by just built Protoss
		// building, yeah it happens this shit.
		else if (unit.isConstructing() && !unit.isMoving()) {
			UnitActions.moveTo(unit, ProtossNexus.getNearestBaseForUnit(unit));
		}

		// If unit is building something check if there's no duplicate
		// constructions going on
		else if (unit.isConstructing()) {
			Constructing.removeDuplicateConstructionsPending(unit);
		}

	}

	private static void gatherResources(Unit worker, Unit nearestBase) {
		boolean existsAssimilatorNearBase = ProtossNexus
				.isExistingCompletedAssimilatorNearBase(nearestBase);

		if (existsAssimilatorNearBase
				&& ProtossNexus.getNumberofGasGatherersForBase(nearestBase) <= 3) {
			gatherGas(worker, nearestBase);
		} else {
			gatherMinerals(worker, nearestBase);
		}
	}

	private static void gatherGas(Unit worker, Unit nearestBase) {
		Unit refinery = xvr.getUnitOfTypeNearestTo(
				UnitTypes.Protoss_Assimilator, nearestBase);
		if (refinery != null) {
			xvr.getBwapi().rightClick(worker.getID(), refinery.getID());
		}
	}

	private static void gatherMinerals(Unit gathererToAssign, Unit nearestBase) {
		Unit mineral = getOptimalMineralForGatherer(gathererToAssign,
				nearestBase);
		if (mineral != null) {
			xvr.getBwapi()
					.rightClick(gathererToAssign.getID(), mineral.getID());
		}
	}

	public static void forceGatherMinerals(Unit gathererToAssign, Unit mineral) {
		if (mineral != null) {
			xvr.getBwapi()
					.rightClick(gathererToAssign.getID(), mineral.getID());
		}
	}

	private static Unit getOptimalMineralForGatherer(Unit gathererToAssign,
			Unit nearestBase) {

		// Get the minerals that are closes to the base.
		ArrayList<Unit> minerals = ProtossNexus
				.getMineralsNearBase(nearestBase);

		// Get SCV
		ArrayList<Unit> workers = ProtossNexus.getWorkersNearBase(nearestBase);

		// Build mapping of number of SCVs to mineral unit
		HashMap<Unit, Integer> workersAtMineral = new HashMap<Unit, Integer>();
		for (Unit worker : workers) {
			// System.out.println();
			// System.out.println("scv.getTargetUnitID() = " +
			// scv.getTargetUnitID());
			// System.out.println("scv.getOrderTargetID() = " +
			// scv.getOrderTargetID());
			// System.out.println("scv.isGatheringMinerals() = " +
			// scv.isGatheringMinerals());
			// System.out.println("scv.getLastCommand() = " +
			// scv.getLastCommand());
			if (worker.getTargetUnitID() > 0) {
				Unit mineral = Unit.getByID(worker.getTargetUnitID());
				// System.out.println(mineral);
				// }
				// if (scv.isGatheringMinerals()) {

				if (workersAtMineral.containsKey(mineral)) {
					workersAtMineral.put(mineral,
							workersAtMineral.get(mineral) + 1);
				} else {
					workersAtMineral.put(mineral, 1);
				}
			}
		}

		// Get minimal value of gatherers assigned to one mineral
		int minimumGatherersAssigned = workersAtMineral.isEmpty() ? 0 : 9999;
		for (Integer value : workersAtMineral.values()) {
			if (minimumGatherersAssigned > value) {
				minimumGatherersAssigned = value;
			}
		}

		// Get the nearest mineral which has minimumGatherersAssigned
		for (Unit mineral : minerals) {
			if (!workersAtMineral.containsKey(mineral)
					|| workersAtMineral.get(mineral) <= minimumGatherersAssigned) {
				return mineral;
			}
		}
		return minerals.isEmpty() ? null : (Unit) RUtilities
				.getRandomListElement(minerals);
	}

	public static Unit findNearestWorkerTo(int x, int y) {
		Unit base = xvr.getUnitOfTypeNearestTo(UnitManager.BASE, x, y);
		if (base == null) {
			return null;
		}

		// return xvr.getUnitOfTypeNearestTo(UnitTypes.Protoss_SCV,
		// xvr.getFirstBase());

		double nearestDistance = 999999;
		Unit nearestUnit = null;

		for (Unit otherUnit : xvr.getUnitsOfType(UnitManager.WORKER)) {
			if (!otherUnit.isCompleted()
					|| (!otherUnit.isGatheringMinerals() && !otherUnit
							.isGatheringGas())) {
				continue;
			}

			double distance = xvr.getDistanceBetween(otherUnit, base);
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearestUnit = otherUnit;
			}
		}

		return nearestUnit;
	}

	public static Unit findNearestWorkerTo(Unit building) {
		return findNearestWorkerTo(building.getX(), building.getY());
	}

	public static Unit findNearestRepairerTo(Unit unit) {
		double nearestDistance = 999999;
		Unit nearestUnit = null;

		for (Unit otherUnit : xvr.getUnitsOfType(UnitManager.WORKER)) {
			if (!otherUnit.isCompleted() || otherUnit.isRepairing()
					|| otherUnit.isConstructing()) {
				continue;
			}

			double distance = xvr.getDistanceBetween(otherUnit, unit);
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearestUnit = otherUnit;
			}
		}

		return nearestUnit;
	}

}
