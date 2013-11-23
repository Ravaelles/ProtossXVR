package jnibwapi;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import jnibwapi.model.Player;
import jnibwapi.model.Unit;
import jnibwapi.protoss.ProtossNexus;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.xvr.Constructing;
import jnibwapi.xvr.MapExploration;
import jnibwapi.xvr.MapPoint;
import jnibwapi.xvr.MassiveAttack;
import jnibwapi.xvr.TechnologyManager;
import jnibwapi.xvr.UnitCounter;
import jnibwapi.xvr.UnitManager;
import jnibwapi.xvr.WorkerManager;

public class XVR {

	/** Less = faster. */
	public static final int GAME_SPEED = 0;

	private static final int WHAT_IS_NEAR_DISTANCE = 12;

	public static Player ENEMY;
	public static int ENEMY_ID;
	private static String ENEMY_RACE = "Undefined";
	public static Player SELF;
	public static int SELF_ID;

	private static XVR lastInstance;

	private XVRClient client;
	private JNIBWAPI bwapi;

	private int time = 0;

	@SuppressWarnings("static-access")
	public XVR(XVRClient bwapiClient) {
		this.lastInstance = this;
		this.client = bwapiClient;
		this.bwapi = bwapiClient.getBwapi();
	}

	// =====================================================

	// This method is called every 30th frame (approx. once a second).
	public void act() {
		try {
			time++;

			// Calculate numbers of units by type, so it can be used in other
			// methods.
			if (getTime() % 4 == 0) {
				UnitCounter.recalculateUnits();
			}

			if (getTime() % 10 == 0) {
				MapExploration.updateInfoAboutHiddenUnits();
			}

			// See if we're strong enough to attack the enemy
			if (getTime() % 30 == 0) {
				MassiveAttack.evaluateMassiveAttackOptions();
			}

			// Handle technologies
			if (getTime() % 48 == 0) {
				TechnologyManager.act();
			}

			// Now let's mine minerals with your idle workers.
			if (getTime() % 11 == 0) {
				handleWorkersBehaviour();
			}

			// Handle units in neighborhood of army units.
			if (getTime() % 30 == 0) {
				UnitManager.actWithArmyUnitsWhenEnemyNearby();
			}

			// And let's build some Supply Depots if we are low on supply (if
			// free
			// supply is less than 3).
			if (getTime() % 40 == 0) {
				handleConstruction();
			}

			// Handle behavior of units
			if (getTime() % 30 == 0) {
				handleUnitsAndBuildingsBehavior();
			}

			// if (getTime() % 70 == 0) {
			// MapExploration.removeNonExistingEnemyUnits();
			// }
		} catch (Exception e) {
			System.err.println("-----------------------------------------");
			e.printStackTrace();
			// RUtilities.displayException(e, "Error", "An error occured:");
		}
	}

	public void handleWorkersBehaviour() {
		WorkerManager.act();
	}

	public void handleConstruction() {
		Constructing.act(this);
	}

	public void handleUnitsAndBuildingsBehavior() {
		UnitManager.act();
	}

	public void unitCreated(int unitID) {
		// System.out.println("UNIT CREATED " + Unit.getByID(unitID).getTypeID()
		// + "     / "
		// + UnitTypes.Protoss_Refinery.ordinal());

		// Unit unit = Unit.getMyUnitByID(unitID);
		// if (unit.getTypeID() == UnitTypes.Protoss_Refinery.ordinal()) {
		// TerranCommandCenter.sendSCVsToRefinery(unit);
		// }
		// UnitType unitType =
		// UnitType.getUnitTypeByID(Unit.getByID(unitID).getTypeID());
		// if (unitType.isBuilding()) {
		// TerranConstructing.removeIsBeingBuilt(unitType);
		// }
	}

	// =========================================================
	// Getters

	public static XVR getInstance() {
		return lastInstance;
	}

	public XVRClient getClient() {
		return client;
	}

	public JNIBWAPI getBwapi() {
		return bwapi;
	}

	// =========================================================
	// UTILITIES

	public int getTime() {
		return time;
	}

	public int getTimeDifferenceBetweenNowAnd(int oldTime) {
		return time - oldTime;
	}

	public void buildUnit(Unit building, UnitTypes type) {
		if (building == null || type == null) {
			return;
		}

		getBwapi().train(building.getID(), type.ordinal());
	}

	public ArrayList<Unit> getUnitsOfType(UnitTypes unitType) {
		return getUnitsOfType(unitType.ordinal());
	}

	public ArrayList<Unit> getUnitsOfType(int unitType) {
		ArrayList<Unit> objectsOfThisType = new ArrayList<Unit>();

		for (Unit unit : bwapi.getMyUnits()) {
			if (unit.getTypeID() == unitType) {
				objectsOfThisType.add(unit);
			}
		}

		return objectsOfThisType;
	}

	public ArrayList<Unit> getUnitsOfTypeCompleted(UnitTypes unitType) {
		ArrayList<Unit> objectsOfThisType = new ArrayList<Unit>();

		for (Unit unit : bwapi.getMyUnits()) {
			if (unit.getTypeID() == unitType.getID() && unit.isCompleted()) {
				objectsOfThisType.add(unit);
			}
		}

		return objectsOfThisType;
	}

	public int countUnitsOfType(UnitTypes unitType) {
		int counter = 0;

		for (Unit unit : bwapi.getMyUnits()) {
			if (unit.getTypeID() == unitType.ordinal()) {
				counter++;
			}
		}

		return counter;
	}

	public ArrayList<Unit> getUnitsNonWorker() {
		ArrayList<Unit> objectsOfThisType = new ArrayList<Unit>();

		for (Unit unit : bwapi.getMyUnits()) {
			if (!unit.isWorker() && unit.isCompleted()) {
				objectsOfThisType.add(unit);
			}
		}

		return objectsOfThisType;
	}

	public ArrayList<Unit> getArmyUnitsIncludingDefensiveBuildings() {
		ArrayList<Unit> objectsOfThisType = new ArrayList<Unit>();

		for (Unit unit : bwapi.getMyUnits()) {
			if (!unit.isWorker() && unit.isCompleted()
					&& unit.isDefensiveBuilding()) {
				objectsOfThisType.add(unit);
			}
		}

		return objectsOfThisType;
	}

	public ArrayList<Unit> getEnemyArmyUnitsIncludingDefensiveBuildings() {
		ArrayList<Unit> objectsOfThisType = new ArrayList<Unit>();

		for (Unit unit : bwapi.getEnemyUnits()) {
			if (!unit.isWorker() || unit.isDefensiveBuilding()) {
				objectsOfThisType.add(unit);
			}
		}

		return objectsOfThisType;
	}

	public ArrayList<Unit> getWorkers() {
		ArrayList<Unit> objectsOfThisType = new ArrayList<Unit>();

		for (Unit unit : bwapi.getMyUnits()) {
			if (unit.isWorker() && unit.isCompleted()) {
				objectsOfThisType.add(unit);
			}
		}

		return objectsOfThisType;
	}

	public int getMinerals() {
		return bwapi.getSelf().getMinerals();
	}

	public int getGas() {
		return bwapi.getSelf().getGas();
	}

	public int getSuppliesFree() {
		return (bwapi.getSelf().getSupplyTotal() - bwapi.getSelf()
				.getSupplyUsed()) / 2;
	}

	public int getSuppliesTotal() {
		return (bwapi.getSelf().getSupplyTotal()) / 2;
	}

	public double getDistanceBetween(Unit u1, Point point) {
		return getDistanceBetween(u1, point.x, point.y);
	}

	public double getDistanceBetween(Unit u1, MapPoint point) {
		return getDistanceBetween(u1, point.getX(), point.getX());
	}

	public double getDistanceBetween(Unit u1, Unit u2) {
		return getDistanceBetween(u1, u2.getX(), u2.getY());
	}

	public double getDistanceBetween(MapPoint point, int x, int y) {
		if (point == null) {
			return 0;
		}
		return getDistanceBetween(point.getX(), point.getY(), x, y);
	}

	public double getDistanceBetween(int x1, int y1, int x2, int y2) {
		return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)) / 32;
	}

	public ArrayList<Unit> getMineralsUnits() {
		ArrayList<Unit> objectsOfThisType = new ArrayList<Unit>();

		for (Unit unit : bwapi.getNeutralUnits()) {
			if (unit.getTypeID() == UnitTypes.Resource_Mineral_Field.ordinal()) {
				objectsOfThisType.add(unit);
			}
		}

		return objectsOfThisType;
	}

	public ArrayList<Unit> getGeysersUnits() {
		ArrayList<Unit> objectsOfThisType = new ArrayList<Unit>();

		for (Unit unit : bwapi.getNeutralUnits()) {
			if (unit.getTypeID() == UnitTypes.Resource_Vespene_Geyser.ordinal()) {
				objectsOfThisType.add(unit);
			}
		}

		return objectsOfThisType;
	}

	public Unit getFirstBase() {
		ArrayList<Unit> bases = getUnitsOfType(UnitManager.BASE.ordinal());
		if (!bases.isEmpty()) {
			return bases.get(0);
		} else {
			return null;
		}
	}

	public Unit getUnitOfTypeNearestTo(UnitTypes type, Unit closeTo) {
		return getUnitOfTypeNearestTo(type, closeTo.getX(), closeTo.getY());
	}

	public Unit getUnitOfTypeNearestTo(UnitTypes type, int x, int y) {
		double nearestDistance = 999999;
		Unit nearestUnit = null;

		for (Unit otherUnit : getUnitsOfType(type)) {
			if (!otherUnit.isCompleted()) {
				continue;
			}

			double distance = getDistanceBetween(otherUnit, x, y);
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearestUnit = otherUnit;
			}
		}

		return nearestUnit;
	}

	public boolean canAfford(int minerals) {
		return canAfford(minerals, 0, 0);
	}

	public boolean canAfford(int minerals, int gas) {
		return canAfford(minerals, gas, 0);
	}

	public boolean canAfford(int minerals, int gas, int supply) {
		if (minerals > 0 && getMinerals() < minerals) {
			return false;
		}
		if (gas > 0 && getGas() < gas) {
			return false;
		}
		if (supply > 0 && getSuppliesFree() < supply) {
			return false;
		}
		return true;
	}

	public int countUnitsOfGivenTypeInRadius(UnitTypes type, int tileRadius,
			int x, int y, boolean onlyMyUnits) {
		int result = 0;
		List<Unit> unitsList = onlyMyUnits ? bwapi.getMyUnits() : bwapi
				.getNeutralUnits();
		for (Unit unit : unitsList) {
			if (type.ordinal() == unit.getTypeID()
					&& getDistanceBetween(unit, x, y) <= tileRadius) {
				result++;
			}
		}
		return result;
	}

	public ArrayList<Unit> getUnitsOfGivenTypeInRadius(UnitTypes type,
			int tileRadius, Unit unit, boolean onlyMyUnits) {
		return getUnitsOfGivenTypeInRadius(type, tileRadius, unit.getX(),
				unit.getY(), onlyMyUnits);
	}

	public ArrayList<Unit> getUnitsOfGivenTypeInRadius(UnitTypes type,
			int tileRadius, int x, int y, boolean onlyMyUnits) {
		HashMap<Unit, Double> unitToDistance = new HashMap<Unit, Double>();

		for (Unit unit : (onlyMyUnits ? bwapi.getMyUnits() : bwapi
				.getAllUnits())) {
			double distance = getDistanceBetween(unit, x, y);
			if (type.ordinal() == unit.getTypeID() && distance <= tileRadius) {
				unitToDistance.put(unit, distance);
			}
		}

		// Return listed sorted by distance ascending.
		ArrayList<Unit> resultList = new ArrayList<Unit>();
		resultList
				.addAll(RUtilities.sortByValue(unitToDistance, true).keySet());
		return resultList;
	}

	public int countUnitsInRadius(int x, int y, int tileRadius,
			boolean onlyMyUnits) {
		int result = 0;

		for (Unit unit : (onlyMyUnits ? bwapi.getMyUnits() : bwapi
				.getAllUnits())) {
			if (getDistanceBetween(unit, x, y) <= tileRadius) {
				result++;
			}
		}

		return result;
	}

	public ArrayList<Unit> getArmyUnitsInRadius(int x, int y, int tileRadius,
			boolean onlyMyUnits) {
		ArrayList<Unit> resultList = new ArrayList<Unit>();

		for (Unit unit : (onlyMyUnits ? bwapi.getMyUnits() : bwapi
				.getAllUnits())) {
			if (unit.getType().isArmy()
					&& getDistanceBetween(unit, x, y) <= tileRadius) {
				resultList.add(unit);
			}
		}

		return resultList;
	}

	public int countMineralsInRadiusOf(int tileRadius, int x, int y) {
		int result = 0;
		for (Unit unit : bwapi.getNeutralUnits()) {
			if (UnitTypes.Resource_Mineral_Field.ordinal() == unit.getTypeID()
					&& getDistanceBetween(unit, x, y) <= tileRadius) {
				result++;
			}
		}
		return result;
	}

	public ArrayList<Unit> getIdleArmyUnitsInRadiusOf(int x, int y,
			int tileRadius) {
		ArrayList<Unit> units = new ArrayList<Unit>();
		for (Unit unit : getUnitsNonWorker()) {
			if (unit.isIdle() && getDistanceBetween(unit, x, y) <= tileRadius) {
				units.add(unit);
			}
		}
		return units;
	}

	public ArrayList<Unit> getEnemyBuildingsVisible() {
		ArrayList<Unit> buildings = new ArrayList<Unit>();
		for (Unit unit : getBwapi().getEnemyUnits()) {
			if (unit.getType().isBuilding()) {
				buildings.add(unit);
			}
		}
		return buildings;
	}

	public Unit getUnitNearestFromList(MapPoint location, Collection<Unit> units) {
		return getUnitNearestFromList(location.getX(), location.getY(), units);
	}

	public Unit getUnitNearestFromList(int x, int y, Collection<Unit> units) {
		double nearestDistance = 999999;
		Unit nearestUnit = null;

		for (Unit otherUnit : units) {
			if (!otherUnit.isCompleted()) {
				continue;
			}

			double distance = getDistanceBetween(otherUnit, x, y);
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearestUnit = otherUnit;
			}
		}

		return nearestUnit;
	}

	public ArrayList<Unit> getEnemyUnitsVisible() {
		ArrayList<Unit> units = new ArrayList<Unit>();
		for (Unit unit : bwapi.getEnemyUnits()) {
			if (!unit.getType().isBuilding()) {
				units.add(unit);
			}
		}
		return units;
	}

	public Collection<Unit> getEnemyArmyUnits() {
		return MapExploration.getEnemyUnitsDiscovered();
	}

	public Collection<Unit> getEnemyBuildings() {
		return MapExploration.getEnemyBuildingsDiscovered();
	}

	public int getNumberOfUnitsInRadius(Unit unit, int tileRadius,
			ArrayList<Unit> unitsList) {
		return getNumberOfUnitsInRadius(unit.getX(), unit.getY(), tileRadius,
				unitsList);
	}

	public int getNumberOfUnitsInRadius(int x, int y, int tileRadius,
			ArrayList<Unit> unitsList) {
		int counter = 0;

		for (Unit unit : unitsList) {
			if (getDistanceBetween(unit, x, y) <= tileRadius) {
				counter++;
			}
		}

		return counter;
	}

	public ArrayList<Unit> getUnitsInRadius(int x, int y, int tileRadius) {
		return getUnitsInRadius(x, y, tileRadius, getAllUnits());
	}

	private ArrayList<Unit> getAllUnits() {
		ArrayList<Unit> allUnits = new ArrayList<Unit>();
		allUnits.addAll(bwapi.getAllUnits());
		return allUnits;
	}

	/** @return List of units from unitsList sorted ascending by distance. */
	public ArrayList<Unit> getUnitsInRadius(int x, int y, int tileRadius,
			Collection<Unit> unitsList) {
		HashMap<Unit, Double> unitToDistance = new HashMap<Unit, Double>();

		for (Unit unit : unitsList) {
			double distance = getDistanceBetween(unit, x, y);
			if (distance <= tileRadius) {
				unitToDistance.put(unit, distance);
			}
		}

		// Return listed sorted by distance ascending.
		ArrayList<Unit> resultList = new ArrayList<Unit>();
		resultList
				.addAll(RUtilities.sortByValue(unitToDistance, true).keySet());
		return resultList;
	}

	public static String getENEMY_RACE() {
		return ENEMY_RACE;
	}

	public static void setENEMY_RACE(String eNEMY_RACE) {
		ENEMY_RACE = eNEMY_RACE;
	}

	public Unit getRandomWorker() {
		for (Unit unit : bwapi.getMyUnits()) {
			if (unit.getTypeID() == UnitManager.WORKER.ordinal()
					&& !unit.isConstructing()) {
				return unit;
			}
		}
		return null;
	}

	public Unit getOptimalBuilder(MapPoint buildTile) {
		ArrayList<Unit> freeWorkers = new ArrayList<Unit>();
		for (Unit worker : getWorkers()) {
			if (worker.isCompleted() && !worker.isConstructing()
					&& !worker.isRepairing() && !worker.isUnderAttack()) {
				freeWorkers.add(worker);
			}
		}

		// Return the closest builder to the tile
		return getUnitNearestFromList((int) buildTile.getX() * 32,
				(int) buildTile.getY() * 32, freeWorkers);
	}

	public boolean isEnemyDetectorNear(int x, int y) {
		return getEnemyDetectorNear(x, y) != null;
	}

	public Unit getEnemyDetectorNear(int x, int y) {
		ArrayList<Unit> enemiesNearby = getUnitsInRadius(x, y,
				WHAT_IS_NEAR_DISTANCE, getEnemyUnitsVisible());
		for (Unit enemy : enemiesNearby) {
			if (enemy.getType().isDetector()) {
				return enemy;
			}
		}
		return null;
	}

	public boolean isEnemyDefensiveGroundBuildingNear(int x, int y) {
		ArrayList<Unit> enemiesNearby = getUnitsInRadius(x, y, 11,
				getEnemyBuildings());
		for (Unit enemy : enemiesNearby) {
			if (enemy.getType().isAttackCapable()
					&& enemy.getType().getGroundWeaponID() != -1) {
				return true;
			}
		}
		return false;
	}

	public boolean isEnemyDefensiveAirBuildingNear(int x, int y) {
		ArrayList<Unit> enemiesNearby = getUnitsInRadius(x, y,
				WHAT_IS_NEAR_DISTANCE, getEnemyBuildings());
		for (Unit enemy : enemiesNearby) {
			if (enemy.getType().isAttackCapable()
					&& enemy.getType().getAirWeaponID() != -1) {
				return true;
			}
		}
		return false;
	}

	public Unit getEnemyDefensiveGroundBuildingNear(int x, int y) {
		ArrayList<Unit> enemiesNearby = getUnitsInRadius(x, y,
				WHAT_IS_NEAR_DISTANCE, getEnemyBuildings());
		for (Unit enemy : enemiesNearby) {
			if (enemy.getType().isAttackCapable()
					&& enemy.getType().getGroundWeaponID() != -1) {
				return enemy;
			}
		}
		return null;
	}

	public Unit getEnemyDefensiveAirBuildingNear(int x, int y) {
		ArrayList<Unit> enemiesNearby = getUnitsInRadius(x, y,
				WHAT_IS_NEAR_DISTANCE, getEnemyBuildings());
		for (Unit enemy : enemiesNearby) {
			if (enemy.getType().isAttackCapable()
					&& enemy.getType().getAirWeaponID() != -1) {
				return enemy;
			}
		}
		return null;
	}

	public Unit getLastBase() {
		ArrayList<Unit> bases = getUnitsOfType(UnitManager.BASE.ordinal());
		if (!bases.isEmpty()) {
			return bases.get(bases.size() - 1);
		} else {
			return null;
		}
	}

	public ArrayList<Unit> getUnitsArmy() {
		ArrayList<Unit> objectsOfThisType = new ArrayList<Unit>();

		for (Unit unit : bwapi.getMyUnits()) {
			UnitType type = unit.getType();
			if (!type.isBuilding() && !type.isWorker()) {
				objectsOfThisType.add(unit);
			}
		}

		return objectsOfThisType;
	}

	/** Returns Manhattan distance in tiles. */
	public int getDistanceSimple(Unit unit, MapPoint safePlace) {
		return Math.abs(unit.getX() - safePlace.getX())
				+ Math.abs(unit.getY() - safePlace.getY()) / 32;
	}

	public Unit getBaseNearestToEnemy() {

		// Try to tell where may be some enemy base.
		Unit nearestEnemyBase = MapExploration.getNearestEnemyBase();
		if (nearestEnemyBase == null
				&& !MapExploration.getEnemyBuildingsDiscovered().isEmpty()) {
			nearestEnemyBase = MapExploration.getEnemyBuildingsDiscovered().iterator().next();
		}
		
		// If we have no knowledge at all about enemy position, return the last base.
		if (nearestEnemyBase == null) {
			return getLastBase();
		}
		else {
			Unit base = getUnitNearestFromList(nearestEnemyBase, ProtossNexus.getBases());
			if (base.equals(getFirstBase())) {
				base = getLastBase();
			}
			return base;
		}
	}

}
