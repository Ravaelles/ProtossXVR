package ai.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.Debug;
import ai.core.XVR;
import ai.handling.army.StrengthEvaluator;
import ai.handling.map.MapExploration;
import ai.handling.map.MapPoint;
import ai.handling.units.UnitActions;
import ai.protoss.ProtossNexus;
import ai.utils.RUtilities;

public class WorkerManager {

	public static final int GUY_TO_CHASE_OTHERS_INDEX = 3;
	public static final int EXPLORER_INDEX = 4;
	public static final int DEFEND_BASE_RADIUS = 23;

	private static XVR xvr = XVR.getInstance();

	private static int _counter;

	// ======================

	public static void act() {

		ArrayList<Unit> workers = xvr.getUnitsOfType(UnitManager.WORKER);
		// MapExploration.explorer = workers.size() > EXPLORER_INDEX ? workers
		// .get(EXPLORER_INDEX) : null;

		// ==================================
		// Detect Zergling rush, if it's early and we have just 1 infantry
		// completed, use Probes
		// _maxWorkerCounterToDefendBase =
		// defineMaxWorkerCounterToEarlyDefendBase();

		// ==================================
		_counter = 0;
		boolean shouldStopExploring = Debug.ourDeaths >= 2
				&& !MapExploration.getEnemyBuildingsDiscovered().isEmpty();
		for (Unit worker : workers) {
			if (_counter != EXPLORER_INDEX || shouldStopExploring) {
				WorkerManager.act(worker);
			} else {
				MapExploration.explore(worker);
			}

			_counter++;
		}
	}

	private static void defendBase(Unit worker) {
		Unit enemyToFight = xvr.getNearestEnemyInRadius(xvr.getFirstBase(), DEFEND_BASE_RADIUS);
		if (enemyToFight == null) {
			return;
		}
		boolean isEnemyWorker = enemyToFight.isWorker();
		double distToEnemy = worker.distanceTo(enemyToFight);

		// ==============================
		boolean shouldThisWorkerConsiderAttack = distToEnemy > 0 && distToEnemy < 17
				&& !worker.isAttacking() && enemyToFight.isDetected();
		boolean isTargetDangerous = !isEnemyWorker
				|| (isEnemyWorker && (enemyToFight.isConstructing()));
		boolean isTargetExtremelyDangerous = isEnemyWorker && enemyToFight.isConstructing()
				&& distToEnemy < 22;
		boolean isEnemyWorkerAttackingUs = isEnemyWorker && enemyToFight.isAttacking()
				&& distToEnemy <= 2;
		if ((shouldThisWorkerConsiderAttack && (isTargetDangerous || isEnemyWorkerAttackingUs)
				|| isTargetExtremelyDangerous) && distToEnemy < DEFEND_BASE_RADIUS) {
			// System.out.println("        ######## ATATCK");
//			Debug.message(xvr, "Attack unit: " + enemyToFight.getName());
			UnitActions.attackTo(worker, enemyToFight);
			return;
		}

		// ==========================
		// Dont allow enemy to build any buildings at our base!
		Unit enemyBuildingNearBase = xvr.getUnitNearestFromList(xvr.getFirstBase(),
				xvr.getEnemyBuildings());
		if (enemyBuildingNearBase != null) {
			double distToBuilding = xvr.getDistanceBetween(xvr.getFirstBase(),
					enemyBuildingNearBase);
			if (distToBuilding > 0 && distToBuilding <= DEFEND_BASE_RADIUS
					&& enemyBuildingNearBase.getType().isBuilding()) {
				Debug.message(xvr, "Attack building: " + enemyBuildingNearBase.getName());
				UnitActions.attackEnemyUnit(worker, enemyBuildingNearBase);
				return;
			}
		}
	}

	public static void act(Unit unit) {
		if (unit.equals(MapExploration.getExplorer())) {
			return;
		}

		if (_counter == GUY_TO_CHASE_OTHERS_INDEX) {
			Unit enemyWorkerNearMainBase = xvr.getEnemyWorkerInRadius(25, xvr.getFirstBase());
			UnitActions.attackEnemyUnit(unit, enemyWorkerNearMainBase);
			return;
		}

		// Defend base?
		// if (_maxWorkerCounterToDefendBase > -1) {
		defendBase(unit);
		// }

		if (unit.isAttacking() && unit.distanceTo(xvr.getFirstBase()) < 17) {
			return;
		}

		if (unit.isAttacking() && unit.distanceTo(xvr.getFirstBase()) < 17) {
			return;
		}

		// ==================================

		// If we should destroy this unit
		// if (unit.isShouldScrapUnit()) {
		// UnitActions
		// .attackTo(unit, MapExploration.getNearestEnemyBuilding());
		// return;
		// }

		// If this worker is attacking, and he's far from base, make him go
		// back.
		int distToMainBase = xvr.getDistanceSimple(unit, xvr.getFirstBase());
		if (unit.isAttacking()
				&& distToMainBase >= 7
				|| (unit.isConstructing() && unit.getShields() < 20 && StrengthEvaluator
						.isStrengthRatioCriticalFor(unit))) {
			UnitActions.moveTo(unit, xvr.getFirstBase());
			return;
		}

		// Act with worker that is under attack
		if (unit.isUnderAttack()) {

			// If nearest enemy is worker, attack this bastard!
			Unit nearestEnemy = xvr.getUnitNearestFromList(unit, xvr.getBwapi().getEnemyUnits());
			if (nearestEnemy != null) {
				if (xvr.getDistanceSimple(unit, xvr.getFirstBase()) <= 9 && !unit.isConstructing()) {
					UnitActions.attackEnemyUnit(unit, nearestEnemy);
					return;
				}
			}

			// ================================
			// Don't attack, do something else
			MapPoint goTo = null;

			// Try to go to the nearest bunker
			Unit defensiveBuildings = xvr.getUnitOfTypeNearestTo(UnitTypes.Protoss_Photon_Cannon,
					unit);
			if (defensiveBuildings != null) {
				goTo = defensiveBuildings;
			} else {
				goTo = xvr.getFirstBase();
			}

			if (goTo != null) {
				if (xvr.getDistanceSimple(unit, goTo) >= 15) {
					UnitActions.moveTo(unit, goTo.getX(), goTo.getY());
				} else {
					UnitActions.moveTo(unit, goTo.getX() + 5 - RUtilities.rand(0, 12), goTo.getY()
							+ 5 - RUtilities.rand(0, 12));
					UnitActions.callForHelp(unit, false);
				}
			}
		}

		// Act with idle worker
		if (unit.isIdle() && !unit.isGatheringGas() && !unit.isGatheringMinerals()
				&& !unit.isAttacking()) {

			// Find the nearest base for this SCV
			Unit nearestBase = ProtossNexus.getNearestBaseForUnit(unit);

			// If base exists try to gather resources
			if (nearestBase != null) {
				gatherResources(unit, nearestBase);
				return;
			}
		}

		// Act with unit that is possibly stuck e.g. by just built Protoss
		// building, yeah it happens this shit.
		else if (unit.isConstructing() && !unit.isMoving()) {
			UnitActions.moveTo(unit, ProtossNexus.getNearestBaseForUnit(unit));
			return;
		}

		// // If unit is building something check if there's no duplicate
		// // constructions going on
		// else if (unit.isConstructing()) {
		// Constructing.removeDuplicateConstructionsPending(unit);
		// }
	}

	public static void gatherResources(Unit worker, Unit nearestBase) {
		boolean existsAssimilatorNearBase = ProtossNexus
				.isExistingCompletedAssimilatorNearBase(nearestBase);

		// if (UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Assimilator) > 0)
		// {
		// System.out.println("ASSIM " + nearestBase.toStringLocation() + ": "
		// + existsAssimilatorNearBase + " "
		// + ProtossNexus.getNumberofGasGatherersForBase(nearestBase));
		// }
		if (existsAssimilatorNearBase
				&& ProtossNexus.getNumberofGasGatherersForBase(nearestBase) < ProtossNexus.WORKERS_PER_GEYSER) {
			gatherGas(worker, nearestBase);
		} else {
			gatherMinerals(worker, nearestBase);
		}
	}

	private static void gatherGas(Unit worker, Unit base) {
		Unit onGeyser = xvr.getUnitOfTypeNearestTo(UnitTypes.Protoss_Assimilator, base);
		if (onGeyser != null) {
			xvr.getBwapi().rightClick(worker.getID(), onGeyser.getID());
		}
	}

	private static void gatherMinerals(Unit gathererToAssign, Unit nearestBase) {
		Unit mineral = getOptimalMineralForGatherer(gathererToAssign, nearestBase);
		if (mineral != null) {
			xvr.getBwapi().rightClick(gathererToAssign.getID(), mineral.getID());
		}
	}

	public static void forceGatherMinerals(Unit gathererToAssign, Unit mineral) {
		if (gathererToAssign.isCarryingMinerals()) {
			Unit nearBase = ProtossNexus.getNearestBaseForUnit(gathererToAssign);
			xvr.getBwapi().rightClick(gathererToAssign.getID(), nearBase.getID());
			return;
		} else if (gathererToAssign.isCarryingGas()) {
			Unit nearBase = ProtossNexus.getNearestBaseForUnit(gathererToAssign);
			xvr.getBwapi().rightClick(gathererToAssign.getID(), nearBase.getID());
			return;
		}

		if (mineral != null) {
			xvr.getBwapi().rightClick(gathererToAssign.getID(), mineral.getID());
		}
	}

	public static void forceGatherGas(Unit gathererToAssign, Unit nearestBase) {
		Unit onGeyser = ProtossNexus.getExistingCompletedAssimilatorNearBase(nearestBase);

		if (gathererToAssign.isCarryingMinerals()) {
			Unit nearBase = ProtossNexus.getNearestBaseForUnit(gathererToAssign);
			xvr.getBwapi().rightClick(gathererToAssign.getID(), nearBase.getID());
			return;
		} else if (gathererToAssign.isCarryingGas()) {
			Unit nearBase = ProtossNexus.getNearestBaseForUnit(gathererToAssign);
			xvr.getBwapi().rightClick(gathererToAssign.getID(), nearBase.getID());
			return;
		}

		if (onGeyser != null) {
			xvr.getBwapi().rightClick(gathererToAssign.getID(), onGeyser.getID());
		}
	}

	private static Unit getOptimalMineralForGatherer(Unit gathererToAssign, Unit nearestBase) {

		// Get the minerals that are closes to the base.
		ArrayList<Unit> minerals = ProtossNexus.getMineralsNearBase(nearestBase);
		int counter = 0;
		while (minerals.isEmpty()) {
			minerals = ProtossNexus.getMineralsNearBase(nearestBase, 15 + 10 * counter++);
		}

		// if (minerals.isEmpty()) {
		// // minerals = xvr.getMineralsUnits();
		// minerals = xvr
		// .getUnitsInRadius(nearestBase, 17 + (UnitCounter
		// .getNumberOfUnits(UnitManager.BASE) - 1) * 13, xvr
		// .getMineralsUnits());
		// }

		// Get workers
		ArrayList<Unit> workers = ProtossNexus.getWorkersNearBase(nearestBase);

		// Build mapping of number of worker to mineral
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
			if (worker.isGatheringMinerals()) {
				Unit mineral = Unit.getByID(worker.getTargetUnitID());
				// System.out.println(mineral);
				// }
				// if (scv.isGatheringMinerals()) {

				if (workersAtMineral.containsKey(mineral)) {
					workersAtMineral.put(mineral, workersAtMineral.get(mineral) + 1);
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
		Collections.shuffle(minerals);
		for (Unit mineral : minerals) {
			if (!workersAtMineral.containsKey(mineral)
					|| workersAtMineral.get(mineral) <= minimumGatherersAssigned) {
				return mineral;
			}
		}
		return minerals.isEmpty() ? null : (Unit) RUtilities.getRandomListElement(minerals);
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
					|| (!otherUnit.isGatheringMinerals() && !otherUnit.isGatheringGas())
					|| otherUnit.isConstructing()) {
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
			if (!otherUnit.isCompleted() || otherUnit.isRepairing() || otherUnit.isConstructing()) {
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
