package ai.core;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Player;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import ai.handling.map.MapExploration;
import ai.handling.map.MapPoint;
import ai.handling.map.MapPointInstance;
import ai.handling.units.UnitCounter;
import ai.managers.ArmyCreationManager;
import ai.managers.ConstructingManager;
import ai.managers.StrategyManager;
import ai.managers.TechnologyManager;
import ai.managers.UnitManager;
import ai.managers.WorkerManager;
import ai.protoss.ProtossGateway;
import ai.protoss.ProtossNexus;
import ai.utils.RUtilities;

/**
 * Main controller of AI. It contains main method act() and has many, many
 * utility functions that most of other classes use.
 */
public class XVR {

	/** Less = faster. */
	public static final int GAME_SPEED = 0;

	/**
	 * There are several methods of type like "getUnitsNear". This value is this
	 * "near" distance, expressed in game tiles (32 pixels).
	 */
	private static final int WHAT_IS_NEAR_DISTANCE = 13;

	private static Player ENEMY;
	public static int ENEMY_ID;
	private static String ENEMY_RACE = "Undefined";
	public static Player SELF;
	public static int SELF_ID;

	protected static boolean enemyTerran = false;
	protected static boolean enemyZerg = false;
	protected static boolean enemyProtoss = false;

	private static XVR lastInstance;
	private XVRClient client;
	private JNIBWAPI bwapi;

	/** Basically it is frame counter. */
	private int time = 0;

	// =====================================================

	@SuppressWarnings("static-access")
	public XVR(XVRClient bwapiClient) {
		this.lastInstance = this;
		this.client = bwapiClient;
		this.bwapi = bwapiClient.getBwapi();
	}

	// =====================================================

	/** This method is called every 30th frame (approx. once a second). */
	public void act() {
		try {
			time++;

			// Calculate numbers of units by type, so this info can be used in
			// other methods.
			if (getTime() % 4 == 0) {
				UnitCounter.recalculateUnits();
			}

			// If there are some enemy units invisible
			if (getTime() % 10 == 0) {
				MapExploration.updateInfoAboutHiddenUnits();
			}

			// See if we're strong enough to attack the enemy
			if (getTime() % 21 == 0) {
				StrategyManager.evaluateMassiveAttackOptions();
			}

			// Handle technologies
			if (getTime() % 23 == 0) {
				TechnologyManager.act();
			}

			// Now let's mine minerals with your idle workers.
			if (getTime() % 11 == 0) {
				WorkerManager.act();
			}

			// Handle behavior of units and buildings.
			// Handle units in neighborhood of army units.
			if (getTime() % 22 == 0) {
				// System.out.println();
				// for (Unit unit : MapExploration.getEnemyUnitsDiscovered()) {
				// System.out.println(unit.getName() + " ## visible:"
				// + unit.isVisible() + ", exists:" + unit.isExists()
				// + ", HP:" + unit.getHitPoints());
				// }

				UnitManager.act();
			}

			// Triple the frequency of "anti-hero" code
			if (getTime() % 22 == 7 || getTime() % 22 == 14) {
				UnitManager.applyStrengthEvaluatorToAllUnits();
			}

			// Avoid being under psionic storm, disruptive web etc.
			if (getTime() % 8 == 0) {
				UnitManager.avoidSeriousSpellEffectsIfNecessary();
			}

			// Handle Nexus behavior differently, more often.
			if (getTime() % 8 == 0) {
				ProtossNexus.act();
			}

			// Handle army building.
			if (getTime() % 13 == 0) {
				ArmyCreationManager.act();
			}

			// Handle constructing new buildings
			if (getTime() % 9 == 0) {
				ConstructingManager.act();
			}

			// if (getTime() % 70 == 0) {
			// MapExploration.removeNonExistingEnemyUnits();
			// }
		} catch (Exception e) {
			System.err.println("--------------------------------------");
			System.err.println("---------- NON CRITICAL ERROR OCCURED: ");
			e.printStackTrace();
			// RUtilities.displayException(e, "Error", "An error occured:");
		}
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

	public static boolean isEnemyTerran() {
		return enemyTerran;
	}

	public static boolean isEnemyZerg() {
		return enemyZerg;
	}

	public static boolean isEnemyProtoss() {
		return enemyProtoss;
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

	public int getTimeSecond() {
		return time / 30;
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

		for (Unit unit : getUnitsNonWorker()) {
			UnitType type = unit.getType();
			if (unit.isCompleted() && (!type.isBuilding() || unit.isDefensiveGroundBuilding())) {
				objectsOfThisType.add(unit);
			}
		}

		return objectsOfThisType;
	}

	public ArrayList<Unit> getEnemyArmyUnitsIncludingDefensiveBuildings() {
		ArrayList<Unit> objectsOfThisType = new ArrayList<Unit>();

		for (Unit unit : bwapi.getEnemyUnits()) {
			UnitType type = unit.getType();
			if ((!type.isBuilding() && !unit.isWorker() && !unit.getType().isLarvaOrEgg())
					|| (type.isBuilding() && unit.isDefensiveGroundBuilding())) {
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
		return (bwapi.getSelf().getSupplyTotal() - bwapi.getSelf().getSupplyUsed()) / 2;
	}

	public int getSuppliesTotal() {
		return (bwapi.getSelf().getSupplyTotal()) / 2;
	}

	public int getSuppliesUsed() {
		return getSuppliesTotal() - getSuppliesFree();
	}

	public double getDistanceBetween(Unit u1, Point point) {
		return getDistanceBetween(u1, point.x, point.y);
	}

	public double getDistanceBetween(MapPoint u1, MapPoint point) {
		if (u1 == null || point == null) {
			return -1;
		}
		return getDistanceBetween(u1.getX(), u1.getY(), point.getX(), point.getX());
	}

	public double getDistanceBetween(Unit u1, Unit u2) {
		if (u2 == null) {
			return -1;
		}
		return getDistanceBetween(u1, u2.getX(), u2.getY());
	}

	public double getDistanceBetween(MapPoint point, int x, int y) {
		if (point == null) {
			return -1;
		}
		return getDistanceBetween(point.getX(), point.getY(), x, y);
	}

	public double getDistanceBetween(int x1, int y1, int x2, int y2) {
		return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)) / 32;
	}

	public ArrayList<Unit> getMineralsUnits() {
		ArrayList<Unit> objectsOfThisType = new ArrayList<Unit>();

		int m1 = UnitTypes.Resource_Mineral_Field.ordinal();
		int m2 = UnitTypes.Resource_Mineral_Field_Type_2.ordinal();
		int m3 = UnitTypes.Resource_Mineral_Field_Type_3.ordinal();

		for (Unit unit : bwapi.getNeutralUnits()) {
			if (unit.getTypeID() == m1 || unit.getTypeID() == m2 || unit.getTypeID() == m3) {
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

	public Unit getUnitOfTypeNearestTo(UnitTypes type, MapPoint closeTo) {
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

	public int countUnitsOfGivenTypeInRadius(UnitTypes type, int tileRadius, MapPoint point,
			boolean onlyMyUnits) {
		if (point == null) {
			return -1;
		}
		return countUnitsOfGivenTypeInRadius(type, tileRadius, point.getX(), point.getY(),
				onlyMyUnits);
	}

	public int countUnitsOfGivenTypeInRadius(UnitTypes type, int tileRadius, int x, int y,
			boolean onlyMyUnits) {
		int result = 0;
		Collection<Unit> unitsList = onlyMyUnits ? bwapi.getMyUnits() : bwapi.getAllUnits();
		for (Unit unit : unitsList) {
			if (type.ordinal() == unit.getTypeID() && getDistanceBetween(unit, x, y) <= tileRadius) {
				result++;
			}
		}
		return result;
	}

	public ArrayList<Unit> getUnitsOfGivenTypeInRadius(UnitTypes type, int tileRadius,
			MapPoint point, boolean onlyMyUnits) {
		if (point == null) {
			return new ArrayList<>();
		}
		return getUnitsOfGivenTypeInRadius(type, tileRadius, point.getX(), point.getY(),
				onlyMyUnits);
	}

	public ArrayList<Unit> getUnitsOfGivenTypeInRadius(UnitTypes type, int tileRadius, int x,
			int y, boolean onlyMyUnits) {
		HashMap<Unit, Double> unitToDistance = new HashMap<Unit, Double>();

		for (Unit unit : (onlyMyUnits ? bwapi.getMyUnits() : bwapi.getAllUnits())) {
			double distance = getDistanceBetween(unit, x, y);
			if (type.ordinal() == unit.getTypeID() && distance <= tileRadius) {
				unitToDistance.put(unit, distance);
			}
		}

		// Return listed sorted by distance ascending.
		ArrayList<Unit> resultList = new ArrayList<Unit>();
		resultList.addAll(RUtilities.sortByValue(unitToDistance, true).keySet());
		return resultList;
	}

	public int countUnitsInRadius(MapPoint point, int tileRadius, boolean onlyMyUnits) {
		return countUnitsInRadius(point.getX(), point.getY(), tileRadius, onlyMyUnits);
	}

	public int countUnitsInRadius(int x, int y, int tileRadius, boolean onlyMyUnits) {
		return countUnitsInRadius(new MapPointInstance(x, y), tileRadius,
				(onlyMyUnits ? bwapi.getMyUnits() : bwapi.getAllUnits()));
	}

	public int countUnitsInRadius(MapPoint point, int tileRadius, Collection<Unit> units) {
		int result = 0;

		for (Unit unit : units) {
			if (getDistanceBetween(unit, point) <= tileRadius) {
				result++;
			}
		}

		return result;
	}

	public ArrayList<Unit> getArmyUnitsInRadius(int x, int y, int tileRadius, boolean onlyMyUnits) {
		ArrayList<Unit> resultList = new ArrayList<Unit>();

		for (Unit unit : (onlyMyUnits ? bwapi.getMyUnits() : bwapi.getAllUnits())) {
			if (unit.getType().isArmy() && getDistanceBetween(unit, x, y) <= tileRadius) {
				resultList.add(unit);
			}
		}

		return resultList;
	}

	public int countMineralsInRadiusOf(int tileRadius, int x, int y) {
		int result = 0;
		for (Unit unit : getMineralsUnits()) {
			if (getDistanceBetween(unit, x, y) <= tileRadius) {
				result++;
			}
		}
		return result;
	}

	public ArrayList<Unit> getIdleArmyUnitsInRadiusOf(int x, int y, int tileRadius) {
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
		if (location == null) {
			return null;
		}
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

	public ArrayList<Unit> getEnemyUnitsVisible(boolean includeGroundUnits, boolean includeAirUnits) {
		ArrayList<Unit> units = new ArrayList<Unit>();
		for (Unit unit : bwapi.getEnemyUnits()) {
			UnitType type = unit.getType();
			if (!type.isBuilding()) {
				if (!type.isFlyer() && includeGroundUnits) {
					units.add(unit);
				} else if (type.isFlyer() && includeAirUnits) {
					units.add(unit);
				}
			}
		}
		return units;
	}

	public ArrayList<Unit> getEnemyUnitsVisible() {
		return getEnemyUnitsVisible(true, true);
	}

	public Collection<Unit> getEnemyArmyUnits() {
		ArrayList<Unit> armyUnits = new ArrayList<Unit>();
		for (Unit enemy : MapExploration.getEnemyUnitsDiscovered()) {
			if (!enemy.isWorker() && !enemy.getType().isBuilding()
					&& enemy.getType().getGroundAttackNormalized() > 0) {
				armyUnits.add(enemy);
			}
		}
		return armyUnits;
	}

	public Collection<Unit> getEnemyUnitsOfType(UnitTypes... types) {

		// Create set object containing all allowed types of units to return
		ArrayList<UnitTypes> typesList = new ArrayList<UnitTypes>();
		for (UnitTypes unitTypes : types) {
			typesList.add(unitTypes);
		}

		// Iterate through enemy units and check if they're types match
		ArrayList<Unit> armyUnits = new ArrayList<Unit>();
		for (UnitTypes type : types) {
			for (Unit enemy : getBwapi().getEnemyUnits()) {
				// for (Unit enemy : MapExploration.getEnemyUnitsDiscovered()) {
				if (type.getID() == enemy.getType().getID()) {
					armyUnits.add(enemy);
				}
			}
		}
		return armyUnits;
	}

	public Collection<Unit> getEnemyBuildings() {
		return MapExploration.getEnemyBuildingsDiscovered();
	}

	public int getNumberOfUnitsInRadius(Unit unit, int tileRadius, ArrayList<Unit> unitsList) {
		return getNumberOfUnitsInRadius(unit.getX(), unit.getY(), tileRadius, unitsList);
	}

	public int getNumberOfUnitsInRadius(int x, int y, int tileRadius, ArrayList<Unit> unitsList) {
		int counter = 0;

		for (Unit unit : unitsList) {
			if (getDistanceBetween(unit, x, y) <= tileRadius) {
				counter++;
			}
		}

		return counter;
	}

	public ArrayList<Unit> getUnitsInRadius(int x, int y, int tileRadius) {
		return getUnitsInRadius(new MapPointInstance(x, y), tileRadius, getAllUnits());
	}

	private ArrayList<Unit> getAllUnits() {
		ArrayList<Unit> allUnits = new ArrayList<Unit>();
		allUnits.addAll(bwapi.getAllUnits());
		return allUnits;
	}

	/** @return List of units from unitsList sorted ascending by distance. */
	public ArrayList<Unit> getUnitsInRadius(MapPoint point, int tileRadius,
			Collection<Unit> unitsList) {
		HashMap<Unit, Double> unitToDistance = new HashMap<Unit, Double>();

		for (Unit unit : unitsList) {
			double distance = getDistanceBetween(unit, point.getX(), point.getY());
			if (distance <= tileRadius) {
				unitToDistance.put(unit, distance);
			}
		}

		// Return listed sorted by distance ascending.
		ArrayList<Unit> resultList = new ArrayList<Unit>();
		resultList.addAll(RUtilities.sortByValue(unitToDistance, true).keySet());
		return resultList;
	}

	public static String getEnemyRace() {
		return ENEMY_RACE;
	}

	public static void setEnemyRace(String enemyRaceString) {
		ENEMY_RACE = enemyRaceString;

		// String enemyBotName = ENEMY.getName().toLowerCase();
		// System.out.println("BOT: " + ENEMY.getName());

		// if (MapExploration.getNumberOfStartLocations(lastInstance.getBwapi()
		// .getMap().getStartLocations()) - 1 > 1) {
		// BotStrategyManager.setExpandWithCannons(true);
		// }

		// ============
		// Protoss
		if ("Protoss".equals(ENEMY_RACE)) {
			enemyProtoss = true;
			ProtossGateway.enemyIsProtoss();

			// boolean shouldExpandWithCannons =
			// enemyBotName.contains("alberta");
			// boolean shouldExpandWithCannons = true;
			// BotStrategyManager.setExpandWithCannons(true);
		}

		// ============
		// Zerg
		else if ("Zerg".equals(ENEMY_RACE)) {
			enemyZerg = true;
			ProtossGateway.enemyIsZerg();
		}

		// ============
		// Terran
		else if ("Terran".equals(ENEMY_RACE)) {
			enemyTerran = true;
			ProtossGateway.enemyIsTerran();
		}
	}

	public Unit getRandomWorker() {
		for (Unit unit : bwapi.getMyUnits()) {
			if (unit.getTypeID() == UnitManager.WORKER.ordinal() && !unit.isConstructing()) {
				return unit;
			}
		}
		return null;
	}

	public Unit getOptimalBuilder(MapPoint buildTile) {
		ArrayList<Unit> freeWorkers = new ArrayList<Unit>();
		for (Unit worker : getWorkers()) {
			if (worker.isCompleted() && !worker.isConstructing() && !worker.isRepairing()
					&& !worker.isUnderAttack() && !worker.equals(MapExploration.getExplorer())) {
				freeWorkers.add(worker);
			}
		}

		// Return the closest builder to the tile
		return getUnitNearestFromList(buildTile, freeWorkers);
	}

	public boolean isEnemyDetectorNear(int x, int y) {
		return getEnemyDetectorNear(x, y) != null;
	}

	public boolean isEnemyDetectorNear(MapPoint point) {
		return isEnemyDetectorNear(point.getX(), point.getY());
	}

	public Unit getEnemyDetectorNear(MapPoint point) {
		return getEnemyDetectorNear(point.getX(), point.getY());
	}

	public Unit getEnemyDetectorNear(int x, int y) {
		ArrayList<Unit> enemiesNearby = getUnitsInRadius(new MapPointInstance(x, y),
				WHAT_IS_NEAR_DISTANCE, bwapi.getEnemyUnits());
		for (Unit enemy : enemiesNearby) {
			if (enemy.isCompleted() && enemy.getType().isDetector()) {
				return enemy;
			}
		}
		return null;
	}

	public boolean isEnemyDefensiveGroundBuildingNear(MapPoint point) {
		return isEnemyDefensiveAirBuildingNear(point.getX(), point.getY());
	}

	public boolean isEnemyDefensiveGroundBuildingNear(int x, int y) {
		ArrayList<Unit> enemiesNearby = getUnitsInRadius(new MapPointInstance(x, y), 11,
				getEnemyBuildings());
		for (Unit enemy : enemiesNearby) {
			if (enemy.isCompleted() && enemy.getType().isAttackCapable()
					&& enemy.canAttackGroundUnits()) {
				return true;
			}
		}
		return false;
	}

	public boolean isEnemyDefensiveAirBuildingNear(int x, int y) {
		ArrayList<Unit> enemiesNearby = getUnitsInRadius(new MapPointInstance(x, y),
				WHAT_IS_NEAR_DISTANCE, getEnemyBuildings());
		for (Unit enemy : enemiesNearby) {
			if (enemy.isCompleted() && enemy.getType().isAttackCapable()
					&& enemy.canAttackAirUnits()) {
				return true;
			}
		}
		return false;
	}

	public Unit getEnemyDefensiveGroundBuildingNear(MapPoint point) {
		return getEnemyDefensiveGroundBuildingNear(point.getX(), point.getY());
	}

	public Unit getEnemyDefensiveGroundBuildingNear(int x, int y, int tileRadius) {
		ArrayList<Unit> enemiesNearby = getUnitsInRadius(new MapPointInstance(x, y),
				WHAT_IS_NEAR_DISTANCE, getEnemyBuildings());
		for (Unit enemy : enemiesNearby) {
			if (enemy.isCompleted() && enemy.getType().isAttackCapable()
					&& enemy.canAttackGroundUnits()) {
				return enemy;
			}
		}
		return null;
	}

	public Unit getEnemyDefensiveGroundBuildingNear(int x, int y) {
		return getEnemyDefensiveGroundBuildingNear(x, y, WHAT_IS_NEAR_DISTANCE);
	}

	public Unit getEnemyDefensiveAirBuildingNear(int x, int y) {
		ArrayList<Unit> enemiesNearby = getUnitsInRadius(new MapPointInstance(x, y),
				WHAT_IS_NEAR_DISTANCE, getEnemyBuildings());
		for (Unit enemy : enemiesNearby) {
			if (enemy.isCompleted() && enemy.getType().isAttackCapable()
					&& enemy.canAttackAirUnits()) {
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

	/** Returns Manhattan distance between two locations, expressed in tiles. */
	public int getDistanceSimple(MapPoint point1, MapPoint point2) {
		if (point1 == null || point2 == null) {
			return -1;
		}
		return (Math.abs(point1.getX() - point2.getX()) + Math.abs(point1.getY() - point2.getY())) / 32;
	}

	public Unit getBaseNearestToEnemy() {

		// Try to tell where may be some enemy base.
		Unit nearestEnemyBase = MapExploration.getNearestEnemyBase();
		if (nearestEnemyBase == null && !MapExploration.getEnemyBuildingsDiscovered().isEmpty()) {
			nearestEnemyBase = MapExploration.getEnemyBuildingsDiscovered().iterator().next();
		}

		// If we have no knowledge at all about enemy position, return the last
		// base.
		if (nearestEnemyBase == null) {
			return getLastBase();
		} else {
			Unit base = getUnitNearestFromList(nearestEnemyBase, ProtossNexus.getBases());
			if (base.equals(getFirstBase())) {
				base = getLastBase();
			}
			return base;
		}
	}

	public boolean isEnemyInRadius(MapPoint point, int tileRadius) {
		return getNumberOfUnitsInRadius(point.getX(), point.getY(), tileRadius,
				getEnemyUnitsVisible()) > 0;
	}

	public Unit getNearestEnemyInRadius(MapPoint point, int tileRadius) {
		Unit enemy = getUnitNearestFromList(point, bwapi.getEnemyUnits());
		if (enemy == null || getDistanceBetween(enemy, point) > tileRadius) {
			return null;
		}
		return enemy;
	}

	public Player getENEMY() {
		return ENEMY;
	}

	public static void setENEMY(Player eNEMY) {
		ENEMY = eNEMY;
	}

	public ArrayList<Unit> getUnitsNonBuilding() {
		ArrayList<Unit> objectsOfThisType = new ArrayList<Unit>();

		for (Unit unit : bwapi.getMyUnits()) {
			if (!unit.getType().isBuilding() && unit.isCompleted()) {
				objectsOfThisType.add(unit);
			}
		}

		return objectsOfThisType;
	}

	public ArrayList<Unit> getUnitsBuildings() {
		ArrayList<Unit> objectsOfThisType = new ArrayList<Unit>();

		for (Unit unit : bwapi.getMyUnits()) {
			if (unit.getType().isBuilding()) {
				objectsOfThisType.add(unit);
			}
		}

		return objectsOfThisType;
	}

	public Unit getEnemyWorkerInRadius(int tileRadius, Unit explorer) {
		for (Unit enemy : getBwapi().getEnemyUnits()) {
			if (enemy.getType().isWorker()) {
				if (getDistanceBetween(explorer, enemy) <= tileRadius) {
					return enemy;
				}
			}
		}
		return null;
	}

	public Collection<Unit> getEnemyWorkersInRadius(int tileRadius, Unit explorer) {
		ArrayList<Unit> result = new ArrayList<>();

		for (Unit enemy : getBwapi().getEnemyUnits()) {
			if (enemy.getType().isWorker()) {
				if (getDistanceBetween(explorer, enemy) <= tileRadius) {
					result.add(enemy);
				}
			}
		}
		return result;
	}

	public Collection<Unit> getEnemyUnitsInRadius(int tileRadius, Unit explorer) {
		ArrayList<Unit> result = new ArrayList<>();

		for (Unit enemy : getBwapi().getEnemyUnits()) {
			if (!enemy.getType().isBuilding()) {
				if (getDistanceBetween(explorer, enemy) <= tileRadius) {
					result.add(enemy);
				}
			}
		}
		return result;
	}

}
