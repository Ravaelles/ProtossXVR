package ai.handling.map;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.BaseLocation;
import jnibwapi.model.ChokePoint;
import jnibwapi.model.Region;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.Debug;
import ai.core.XVR;
import ai.handling.units.UnitActions;
import ai.handling.units.UnitCounter;
import ai.managers.WorkerManager;
import ai.protoss.ProtossNexus;
import ai.utils.RUtilities;

public class MapExploration {

	private static final int MAXIMUM_CHOKE_POINT_DISTANCE_FROM_BASE = 30;

	private static XVR xvr = XVR.getInstance();
	private static JNIBWAPI bwapi = XVR.getInstance().getBwapi();
	private static ArrayList<ChokePoint> chokePointsProcessed = new ArrayList<ChokePoint>();

	private static TreeSet<BaseLocation> baseLocationsDiscovered = new TreeSet<BaseLocation>();
	private static HashMap<Integer, Unit> enemyBasesDiscovered = new HashMap<Integer, Unit>();
	private static HashMap<Integer, Unit> enemyBuildingsDiscovered = new HashMap<Integer, Unit>();
	private static HashMap<Integer, Unit> enemyUnitsDiscovered = new HashMap<Integer, Unit>();
	private static ArrayList<Unit> _hiddenEnemyUnits = new ArrayList<Unit>();

	public static Unit explorer;
	private static boolean _disabledChokePointsNearMainBase = false;

	public static Unit getExplorer() {
		return explorer;
	}

	private static boolean _exploredSecondBase = false;
	private static boolean _isDiscoveringEnemyBase = false;

	public static void explore(Unit explorer) {
		if (!explorer.isCompleted()) {
			return;
		}
		MapExploration.explorer = explorer;

		// If explorer is marked to be discovering enemy base, but he's
		// attacked, then unmark him from his task, thus allowing him to run.
		if (_isDiscoveringEnemyBase && explorer.isUnderAttack() && explorer.getShields() < 14) {
			if (xvr.getEnemyBuildings().size() > 0) {
				_isDiscoveringEnemyBase = false;
			}
		}

		boolean isWounded = isExplorerWounded();
		double distToEnemy = getDistanceToNearestEnemy();

		// ===========================
		// Don't interfere if explorer is building or attacking etc.
		boolean shouldBeConstructing = explorer.isConstructing();
		boolean shouldContinueAttacking = explorer.isAttacking() && !isWounded;
		boolean shouldBeDiscoveringEnemyBase = _isDiscoveringEnemyBase
				|| enemyBuildingsDiscovered.isEmpty();
		boolean isEnemyClose = distToEnemy > 0 && distToEnemy < 3;
		boolean shouldBeMoving = explorer.isMoving() && isEnemyClose;
		if (!explorer.isIdle()
				&& (shouldBeDiscoveringEnemyBase || shouldBeMoving || shouldBeConstructing || shouldContinueAttacking)) {
			return;
		}

		// ===========================
		// If unit is WOUNDED, then retreat to the main base
		if (tryRunningFromEnemiesIfNecessary()) {
			return;
		}

		// ===========================
		// Explorer ACTIONS

		// If we need to scout next base location (Nexus construction can screw
		// otherwise), do it.
		if (tryScoutingNextBaseLocation()) {
			return;
		}

		// Discover base location of the enemy
		if (tryDiscoveringEnemyBaseLocation()) {
			return;
		}

		// Always when possible, try to trololo the enemy
		if (tryAttackingEnemyIfPossible()) {
			return;
		}

		// Gather minerals if idle
		gatherResourcesIfIdle();
	}

	private static void gatherResourcesIfIdle() {
		if (explorer.isIdle()) {
			WorkerManager.gatherResources(explorer, xvr.getFirstBase());
		}
	}

	private static double getDistanceToNearestEnemy() {
		Unit nearestEnemy = xvr.getUnitNearestFromList(explorer.getX(), explorer.getY(),
				xvr.getEnemyUnitsVisible());
		return explorer.distanceTo(nearestEnemy);
	}

	private static boolean isExplorerWounded() {
		return (explorer.getShields()) < 16
				|| (explorer.getShields() < 20 && (explorer.getShields() + explorer.getHitPoints() < 30));
	}

	private static boolean tryRunningFromEnemiesIfNecessary() {
		boolean isWounded = isExplorerWounded();

		Unit nearestEnemy = xvr.getUnitNearestFromList(explorer.getX(), explorer.getY(),
				xvr.getEnemyUnitsVisible());
		double distToNearestEnemy = explorer.distanceTo(nearestEnemy);

		boolean isUnitUnderAttack = explorer.isUnderAttack();
		boolean isEnemyArmyUnitClose = isEnemyArmyUnitCloseToExplorer();
		boolean isEnemyCloseAndUnitIsWounded = distToNearestEnemy > 0 && distToNearestEnemy <= 5
				&& isWounded;
		boolean isAttackingAndIsOverwhelmed = explorer.isAttacking()
				&& xvr.getEnemyUnitsInRadius(6, explorer).size() >= 2;

		// System.out.println("#### " + explorer.getID());
		// System.out.println(isEnemyArmyUnitClose);
		// System.out.println(isEnemyCloseAndUnitIsWounded);
		// System.out.println(isAttackingAndIsOverwhelmed);

		if (isUnitUnderAttack || isEnemyArmyUnitClose
				|| (isEnemyCloseAndUnitIsWounded && isAttackingAndIsOverwhelmed)) {
			double distToMainBase = explorer.distanceTo(xvr.getFirstBase());

			// If already at base, run to the most distant base
			if (distToMainBase < 13) {
				UnitActions.moveTo(explorer, getMostDistantBaseLocation(explorer));
				return true;
			}

			// Not yet at base, still go there.
			else {
				UnitActions.moveToMainBase(explorer);
				return true;
			}
		}

		return false;
	}

	private static boolean isEnemyArmyUnitCloseToExplorer() {
		Unit nearestArmyUnit = xvr.getUnitNearestFromList(explorer, xvr.getEnemyArmyUnits());
		if (nearestArmyUnit == null) {
			return false;
		} else {
			double dist = nearestArmyUnit.distanceTo(explorer);
			return dist < 12 && dist > 0 && !nearestArmyUnit.isWorker();
		}
	}

	private static boolean tryAttackingEnemyIfPossible() {
		Unit enemyUnit = null;

		// if (XVR.isEnemyProtoss()) {
		// Collection<Unit> pylons =
		// xvr.getEnemyUnitsOfType(ProtossPylon.getBuildingType());
		// if (!pylons.isEmpty()) {
		// nearestEnemyBuilding = (Unit) RUtilities.getRandomElement(pylons);
		// }
		// } else if (XVR.isEnemyZerg()) {
		// Collection<Unit> pools =
		// xvr.getEnemyUnitsOfType(UnitTypes.Zerg_Spawning_Pool);
		// if (!pools.isEmpty()) {
		// nearestEnemyBuilding = (Unit) RUtilities.getRandomElement(pools);
		// }
		// } else if (XVR.isEnemyTerran()) {
		// nearestEnemyBuilding = xvr.getEnemyWorkerInRadius(300, explorer);
		// }

		enemyUnit = xvr.getEnemyWorkerInRadius(300, explorer);
		if (enemyUnit != null && enemyUnit.distanceTo(xvr.getFirstBase()) < 20) {
			enemyUnit = null;
		}

		// boolean isWounded = isExplorerWounded();
		boolean isWounded = isExplorerWounded();
		boolean isProperTargetSelected = enemyUnit != null;
		boolean isNeighborhoodQuiteSafe = (xvr.getEnemyUnitsInRadius(3, explorer).size() == 0 && xvr
				.getEnemyUnitsInRadius(6, explorer).size() <= 1);
		boolean isUnitVeryMuchAlive = explorer.getShields() >= 19;

		if (isProperTargetSelected && (!isWounded || isNeighborhoodQuiteSafe)
				&& (isNeighborhoodQuiteSafe || isUnitVeryMuchAlive)) {
			UnitActions.attackEnemyUnit(explorer, enemyUnit);
			return true;
		}

		if (enemyUnit == null) {
			enemyUnit = xvr.getUnitNearestFromList(explorer, xvr.getEnemyBuildings());
		}

		if (enemyUnit != null) {
			UnitActions.attackTo(explorer, enemyUnit);
			return true;
		}

		// boolean isProperTargetSelected = nearestEnemyBuilding != null;
		// boolean isNormalBuilding = isProperTargetSelected
		// && (!nearestEnemyBuilding.isDefensiveGroundBuilding() ||
		// !nearestEnemyBuilding
		// .isCompleted());
		// boolean isNeighborhoodQuiteSafe = (xvr.getEnemyUnitsInRadius(3,
		// explorer).size() == 0 && xvr
		// .getEnemyUnitsInRadius(6, explorer).size() <= 1);
		// boolean isUnitVeryMuchAlive = explorer.getShields() >= 19;
		//
		// if (isProperTargetSelected && isNormalBuilding
		// && (isNeighborhoodQuiteSafe || isUnitVeryMuchAlive)) {
		// UnitActions.attackTo(explorer, nearestEnemyBuilding);
		// return true;
		// }
		//
		// // System.out.println(isNormalBuilding + " / " +
		// isNeighborhoodQuiteSafe
		// // + " / "
		// // + isUnitVeryMuchAlive);
		//
		// if (explorer.isIdle() || (!_isDiscoveringEnemyBase &&
		// nearestEnemyBuilding == null)) {
		// Unit nearestEnemyUnit = xvr.getUnitNearestFromList(explorer,
		// xvr.getBwapi()
		// .getEnemyUnits());
		// if (nearestEnemyUnit != null && xvr.getEnemyUnitsInRadius(6,
		// explorer).size() <= 1) {
		// UnitActions.attackTo(explorer, nearestEnemyUnit);
		// return true;
		// }
		// }

		return false;
	}

	private static boolean tryDiscoveringEnemyBaseLocation() {
		boolean hasDiscoveredBaseLocation = !enemyBuildingsDiscovered.isEmpty();
		if (!hasDiscoveredBaseLocation) {
			BaseLocation goTo = null;

			// Filter out visited bases.
			ArrayList<BaseLocation> possibleBases = new ArrayList<BaseLocation>();
			possibleBases.addAll(xvr.getBwapi().getMap().getStartLocations());
			possibleBases.removeAll(baseLocationsDiscovered);
			possibleBases.remove(getOurBaseLocation());

			// If there is any unvisited base- go there. If no- go to the random
			// base.
			if (possibleBases.isEmpty()) {
				goTo = (BaseLocation) RUtilities.getRandomListElement(xvr.getBwapi().getMap()
						.getStartLocations());
			} else {
				goTo = (BaseLocation) RUtilities.getRandomListElement(possibleBases);
			}

			if (goTo != null) {
				_isDiscoveringEnemyBase = true;
				UnitActions.moveTo(explorer, goTo);
				baseLocationsDiscovered.add(goTo);
				return true;
			}
		}
		return false;
	}

	private static boolean tryScoutingNextBaseLocation() {
		if (!_exploredSecondBase) {
			MapPoint secondBase = ProtossNexus.getSecondBaseLocation();
			UnitActions.moveTo(explorer, secondBase);
			Debug.message(xvr, "Scout next base 1");
			_exploredSecondBase = true;
			return true;
		}

		if (UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Nexus) >= 2) {
			MapPoint tileForNextBase = ProtossNexus.getTileForNextBase(false);
			if (!xvr.getBwapi().isVisible(tileForNextBase.getTx(), tileForNextBase.getTy())) {
				Debug.message(xvr, "Scout next base 2");
				UnitActions.moveTo(explorer, tileForNextBase);
				return true;
			}
		}
		return false;
	}

	public static synchronized void enemyUnitDiscovered(Unit enemyUnit) {
		UnitType type = enemyUnit.getType();
		if (type.isBase()) {
			synchronized (enemyBasesDiscovered) {
				enemyBasesDiscovered.put(enemyUnit.getID(), enemyUnit);
			}
		}
		if (type.isBuilding()) {
			synchronized (enemyBuildingsDiscovered) {
				enemyBuildingsDiscovered.put(enemyUnit.getID(), enemyUnit);
			}
		} else {
			synchronized (enemyUnitsDiscovered) {
				enemyUnitsDiscovered.put(enemyUnit.getID(), enemyUnit);
			}
		}
	}

	public static BaseLocation getMostDistantBaseLocation(Unit unit) {
		double mostFarDistance = 2;
		BaseLocation nearestObject = null;

		ArrayList<BaseLocation> baseLocations = new ArrayList<>();
		baseLocations.addAll(xvr.getBwapi().getMap().getBaseLocations());
		baseLocations.remove(getOurBaseLocation());
		Collections.shuffle(baseLocations);

		boolean onlyStartLocations = baseLocationsDiscovered.size() < getNumberOfStartLocations(baseLocations);

		for (BaseLocation object : baseLocations) {
			if (onlyStartLocations && !object.isStartLocation()) {
				continue;
			}

			double distance = xvr.getDistanceBetween(unit, object.getX(), object.getY());
			if (distance > mostFarDistance) {
				mostFarDistance = distance;
				nearestObject = object;
			}
		}

		return nearestObject;
	}

	public static int getNumberOfStartLocations(Collection<BaseLocation> baseLocations) {
		int result = 0;
		for (BaseLocation object : baseLocations) {
			if (object.isStartLocation()) {
				result++;
			}
		}
		return result;
	}

	private static BaseLocation getOurBaseLocation() {
		List<BaseLocation> baseLocations = xvr.getBwapi().getMap().getBaseLocations();
		Unit ourBase = xvr.getFirstBase();

		for (BaseLocation object : baseLocations) {
			if (object.isStartLocation()) {
				double distance = xvr.getDistanceSimple(ourBase, object);
				if (distance < 5) {
					return object;
				}
			}
		}

		return null;
	}

	// private static BaseLocation nonInitialScouting(Unit explorer) {
	// XVR xvr = XVR.getInstance();
	// BaseLocation goTo = null;
	//
	// // Filter out visited bases.
	// ArrayList<BaseLocation> possibleBases = new ArrayList<BaseLocation>();
	// possibleBases.addAll(xvr.getBwapi().getMap().getStartLocations());
	// possibleBases.removeAll(baseLocationsDiscovered);
	// possibleBases.remove(getOurBaseLocation());
	//
	// // If there is any unvisited base- go there. If no- go to the random
	// // base.
	//
	// if (possibleBases.isEmpty()) {
	// goTo = (BaseLocation) RUtilities.getRandomListElement(xvr
	// .getBwapi().getMap().getBaseLocations());
	// } else {
	// goTo = (BaseLocation) RUtilities
	// .getRandomListElement(possibleBases);
	// }
	//
	// return goTo;
	// }

	public static ChokePoint getNearestChokePointFor(MapPoint point) {
		return getNearestChokePointFor(point.getX(), point.getY());
	}

	public static ChokePoint getNearestChokePointFor(int x, int y) {
		double nearestDistance = 999999;
		ChokePoint nearestObject = null;

		for (ChokePoint object : chokePointsProcessed) {
			double distance = xvr
					.getDistanceBetween(x, y, object.getCenterX(), object.getCenterY())
					- object.getRadius() / 33;
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearestObject = object;
			}
		}

		return nearestObject;
	}

	public static ArrayList<ChokePoint> getNearestChokePointsFor(MapPoint point) {
		HashMap<ChokePoint, Double> chokes = new HashMap<ChokePoint, Double>();
		if (point == null) {
			return new ArrayList<ChokePoint>();
		}

		for (ChokePoint choke : chokePointsProcessed) {
			double distance = xvr.getDistanceBetween(point, choke.getCenterX(), choke.getCenterY())
					- choke.getRadius() / 32;
			chokes.put(choke, distance);
		}

		ArrayList<ChokePoint> result = new ArrayList<ChokePoint>();
		int counter = 0;
		int limitTo = 2;
		Map<ChokePoint, Double> byValue = RUtilities.sortByValue(chokes, true);
		for (ChokePoint chokePoint : byValue.keySet()) {
			if (byValue.get(chokePoint) < MAXIMUM_CHOKE_POINT_DISTANCE_FROM_BASE
					|| counter < limitTo) {
				result.add(chokePoint);
			} else {
				if (counter >= limitTo) {
					break;
				}
			}
			counter++;
		}
		return result;
	}

	public static Point getBaseNearEnemy() {

		// Either an existing enemy base...
		if (!enemyBasesDiscovered.isEmpty() && RUtilities.rand(0, 100) < 50) {
			Unit randomEnemyBase = getRandomKnownEnemyBase();
			return new Point(randomEnemyBase.getX(), randomEnemyBase.getY());
		}
		// Or fully random base
		else {
			BaseLocation baseLocation = getRandomBaseLocation();
			return new Point(baseLocation.getX(), baseLocation.getY());
		}
	}

	public static int getNumberOfKnownEnemyBases() {
		return enemyBasesDiscovered.size();
	}

	public static Unit getRandomKnownEnemyBase() {
		if (enemyBasesDiscovered.isEmpty()) {
			return null;
		} else {
			return (Unit) RUtilities.getRandomElement(enemyBasesDiscovered.values());
		}
	}

	private static BaseLocation getRandomBaseLocation() {
		ArrayList<BaseLocation> list = new ArrayList<BaseLocation>();
		for (BaseLocation base : baseLocationsDiscovered) {
			list.add(base);
		}
		if (list.isEmpty()) {
			return null;
		} else {
			return (BaseLocation) RUtilities.getRandomListElement(list);
		}
	}

	public static Unit getNearestEnemyBase() {
		// for (Iterator<Unit> iterator = enemyBasesDiscovered.iterator();
		// iterator
		// .hasNext();) {
		// Unit base = (Unit) iterator.next();
		// if (base == null) {
		// iterator.remove();
		// }
		// }

		if (enemyBasesDiscovered.isEmpty()) {
			return null;
		} else {
			Unit ourBase = xvr.getFirstBase();

			Unit closestBase = null;
			double closestDistance = 99999;

			for (Unit base : enemyBasesDiscovered.values()) {
				double distance = xvr.getDistanceBetween(base, ourBase);
				if (closestDistance > distance) {
					closestBase = base;
					closestDistance = distance;
				}
			}

			// return new Point(closestBase.getX(), closestBase.getY());
			return closestBase;
		}
	}

	public static Unit getRandomEnemyBuilding() {
		if (enemyBuildingsDiscovered.isEmpty()) {
			return null;
		} else {
			return (Unit) RUtilities.getRandomElement(enemyBuildingsDiscovered.values());
		}
	}

	public static Unit getNearestEnemyBuilding() {
		if (enemyBuildingsDiscovered.isEmpty()) {
			return null;
		} else {
			Unit ourBase = xvr.getFirstBase();

			Unit closestBuilding = null;
			double closestDistance = 99999;

			for (Unit building : enemyBuildingsDiscovered.values()) {
				if (building.getType().isOnGeyser()) {
					continue;
				}

				double distance = xvr.getDistanceBetween(building, ourBase);
				if (closestDistance > distance && closestDistance != 0) {
					closestBuilding = building;
					closestDistance = distance;
				}
			}

			// return new Point(closestBase.getX(), closestBase.getY());
			return closestBuilding;
		}
	}

	public static MapPointInstance getNearestUnknownPointFor(int x, int y, boolean mustBeWalkable) {
		int tileX = x / 32;
		int tileY = y / 32;

		int currentDist = 3;
		int maximumDist = 90;
		while (currentDist < maximumDist) {
			for (int attempt = 0; attempt < currentDist; attempt++) {
				int i = tileX + currentDist - RUtilities.rand(0, 2 * currentDist);
				int j = tileY + currentDist - RUtilities.rand(0, 2 * currentDist);
				if (!bwapi.isExplored(i, j)
				// unit.get
						&& (!mustBeWalkable || (bwapi.getMap().isWalkable(i, j)))) {
					// System.out.println("currentDist = " + currentDist);
					return new MapPointInstance(i * 32, j * 32);
				}
			}
			currentDist += RUtilities.rand(1, maximumDist - currentDist);
		}

		// If we reach here it means we didn't find proper unexplored point.
		// Just go to explored.
		currentDist = 10;
		maximumDist = 90;
		while (currentDist < maximumDist) {
			for (int attempt = 0; attempt < currentDist; attempt++) {
				int i = tileX + currentDist - RUtilities.rand(0, 2 * currentDist);
				int j = tileY + currentDist - RUtilities.rand(0, 2 * currentDist);
				if (!mustBeWalkable || (bwapi.getMap().isWalkable(i, j))) {
					return new MapPointInstance(i * 32, j * 32);
				}
			}
			currentDist += RUtilities.rand(1, maximumDist - currentDist);
		}

		return null;
	}

	public static Collection<Unit> getEnemyBuildingsDiscovered() {
		return enemyBuildingsDiscovered.values();
	}

	public static Collection<Unit> getEnemyUnitsDiscovered() {
		return enemyUnitsDiscovered.values();
	}

	public static ChokePoint getRandomChokePoint() {
		return (ChokePoint) RUtilities.getRandomListElement(chokePointsProcessed);
	}

	public static synchronized void removeNonExistingEnemyUnits() {
		// for (Iterator<Unit> iterator = enemyBuildingsDiscovered.iterator();
		// iterator
		// .hasNext();) {
		// for (Entry<Integer, Unit> entry :
		// enemyBuildingsDiscovered.entrySet()) {
		// Unit unit = entry.getValue();
		// if (unit == null || !unit.isExists() || unit.getHitPoints() < 1) {
		// // System.out.println("REMOVE " + unit.toStringShort());
		// enemyBuildingsDiscovered.remove(unit.getID());
		// }
		// }
		//
		// for (Iterator<Unit> iterator = enemyBasesDiscovered.iterator();
		// iterator
		// .hasNext();) {
		// Unit unit = (Unit) iterator.next();
		// if (unit == null || !unit.isExists() || unit.getHitPoints() < 1) {
		// iterator.remove();
		// }
		// }
		//
		// for (Iterator<Unit> iterator = enemyUnitsDiscovered.iterator();
		// iterator
		// .hasNext();) {
		// Unit unit = (Unit) iterator.next();
		// if (unit == null || !unit.isExists() || unit.getHitPoints() < 1) {
		// iterator.remove();
		// }
		// }

		synchronized (enemyBasesDiscovered) {
			removeNonExistingUnitsFrom(enemyBasesDiscovered);
		}
		synchronized (enemyBuildingsDiscovered) {
			removeNonExistingUnitsFrom(enemyBuildingsDiscovered);
		}
		synchronized (enemyUnitsDiscovered) {
			removeNonExistingUnitsFrom(enemyUnitsDiscovered);
		}
	}

	private static synchronized void removeNonExistingUnitsFrom(HashMap<Integer, Unit> mapping) {
		for (Entry<Integer, Unit> entry : mapping.entrySet()) {
			Unit unit = entry.getValue();
			if (unit == null || !unit.isExists() || unit.getHitPoints() < 1) {
				// System.out.println("REMOVE " + unit.toStringShort());
				mapping.remove(unit.getID());
			}
		}
	}

	public static boolean enemyUnitDestroyed(int unitID) {
		boolean result = false;
		if (enemyBasesDiscovered.remove(unitID) != null) {
			result = true;
		}
		if (enemyBuildingsDiscovered.remove(unitID) != null) {
			result = true;
		}
		if (enemyUnitsDiscovered.remove(unitID) != null) {
			result = true;
		}
		return result;
	}

	public static ArrayList<? extends MapPoint> getBaseLocationsNear(MapPoint point, int tileRadius) {
		ArrayList<BaseLocation> bases = new ArrayList<BaseLocation>();
		if (point == null) {
			return bases;
		}

		for (BaseLocation object : xvr.getBwapi().getMap().getBaseLocations()) {
			if (object == null) {
				continue;
			}
			double distance = xvr.getDistanceBetween(point, object.getX(), object.getY());
			if (distance <= tileRadius) {
				bases.add(object);
			}
		}

		return bases;
	}

	public static void updateInfoAboutHiddenUnits() {
		_hiddenEnemyUnits.clear();
		for (Unit unit : xvr.getEnemyArmyUnits()) {
			if (unit.isEnemy() && (unit.isCloaked() || unit.isBurrowed() || !unit.isDetected())) {
				_hiddenEnemyUnits.add(unit);
			}
		}
	}

	public static ArrayList<Unit> getEnemyUnitsHidden() {
		return _hiddenEnemyUnits;
	}

	public static Unit getHiddenEnemyUnitNearbyTo(Unit unit) {
		if (_hiddenEnemyUnits.isEmpty()) {
			return null;
		} else {
			Unit nearestHiddenEnemy = xvr.getUnitNearestFromList(unit, _hiddenEnemyUnits);
			if (xvr.getDistanceBetween(unit, nearestHiddenEnemy) < 12) {
				return nearestHiddenEnemy;
			}
			return null;
		}
	}

	public static void processInitialChokePoints() {
		int initialChokes = bwapi.getMap().getChokePoints().size();
		int mapWidth = xvr.getBwapi().getMap().getWidth();
		int mapHeight = xvr.getBwapi().getMap().getHeight();

		final int MIN_DIST_FROM_BORDER = 6;

		// Store initial choke points
		for (ChokePoint choke : bwapi.getMap().getChokePoints()) {

			// Filter out gigantic choke points
			if (choke.getRadius() / 32 <= 15) {

				// Check whether this choke isn't too close to map borders
				if (choke.getTx() <= MIN_DIST_FROM_BORDER
						|| choke.getTx() >= (mapWidth - MIN_DIST_FROM_BORDER)
						|| choke.getTy() <= MIN_DIST_FROM_BORDER
						|| choke.getTy() >= (mapHeight - MIN_DIST_FROM_BORDER)) {
					continue;
				} else {
					chokePointsProcessed.add(choke);
				}
			}
		}
		int percentSkipped = 100 * (initialChokes - chokePointsProcessed.size()) / initialChokes;
		if (percentSkipped > 0) {
			System.out.println("Skipped " + percentSkipped + "% of initial choke points");
		}
	}

	public static ChokePoint getImportantChokePointNear(MapPoint point) {
		ArrayList<ChokePoint> nearestChokePoints = getChokePointsForRegion(xvr.getBwapi().getMap()
				.getRegion(point), point);

		if (!nearestChokePoints.isEmpty()) {
			MapPoint secondBase = ProtossNexus.getSecondBaseLocation();

			// We're at second base
			if (xvr.getDistanceBetween(secondBase, point) < 13) {
				ChokePoint chokeNearMainBase = MapExploration.getImportantChokePointNear(xvr
						.getFirstBase());
				nearestChokePoints.remove(chokeNearMainBase);
				return nearestChokePoints.isEmpty() ? chokeNearMainBase : nearestChokePoints.get(0);
			} else {
				return nearestChokePoints.get(0);
			}
		} else {
			return getNearestChokePointFor(point);
		}

		// int mapWidth = xvr.getBwapi().getMap().getWidth();
		// int mapHeight = xvr.getBwapi().getMap().getHeight();
		// System.out.println("width: " + mapWidth);
		// System.out.println("height: " + mapHeight);
		//
		// // If there's only one choke point just return it.
		// if (nearestChokePoints.size() == 1) {
		// return nearestChokePoints.get(0);
		// }
		//
		// // If there're 2 choke points or more, return the first not being
		// close
		// // to the edge of map
		// else if (nearestChokePoints.size() >= 2) {
		// for (int i = 0; i < nearestChokePoints.size(); i++) {
		// ChokePoint choke = nearestChokePoints.get(i);
		//
		// if (choke.getCenterX() / 32 <= 4
		// || choke.getCenterX() / 32 >= (mapWidth - 4)
		// || choke.getCenterY() / 32 <= 4
		// || choke.getCenterY() / 32 >= (mapHeight - 4)) {
		// continue;
		// } else {
		// return choke;
		// }
		// }
		// System.err.println("getImportantChokePointNear (1): null");
		// return null;
		// }
		//
		// // // If there're 2 choke points or more, return the choke point with
		// // // highest radius.
		// // else if (nearestChokePoints.size() >= 2) {
		// // int bestIndex = 0;
		// // for (int i = 1; i < nearestChokePoints.size(); i++) {
		// // if (nearestChokePoints.get(i).getRadius() > nearestChokePoints
		// // .get(bestIndex).getRadius()) {
		// // bestIndex = i;
		// // }
		// // }
		// //
		// // return nearestChokePoints.get(bestIndex);
		// // }
		//
		// // No choke points found
		// else {
		// System.err.println("getImportantChokePointNear (2): null");
		// return null;
		// }

	}

	private static ArrayList<ChokePoint> getChokePointsForRegion(Region region, final MapPoint point) {
		ArrayList<ChokePoint> result = new ArrayList<ChokePoint>();
		if (region == null) {
			return result;
		}
		for (ChokePoint choke : chokePointsProcessed) {
			if (choke.getFirstRegionID() == region.getID()
					|| choke.getSecondRegionID() == region.getID()) {
				result.add(choke);
			}
		}

		Collections.sort(result, new Comparator<ChokePoint>() {
			@Override
			public int compare(ChokePoint o1, ChokePoint o2) {
				return Double.compare(o1.distanceTo(point) - o1.getRadius() / 32,
						o2.distanceTo(point) - o2.getRadius() / 32);
			}
		});

		return result;
	}

	public static ArrayList<ChokePoint> getChokePoints() {
		return chokePointsProcessed;
	}

	public static Collection<ChokePoint> getChokePointsNear(MapPoint near, int tileRadius) {
		ArrayList<ChokePoint> chokes = new ArrayList<>();
		if (near == null) {
			return chokes;
		}

		for (ChokePoint object : chokePointsProcessed) {
			if (object == null) {
				continue;
			}
			double distance = xvr.getDistanceSimple(near, object);
			if (distance <= tileRadius) {
				chokes.add(object);
			}
		}

		return chokes;
	}

	public static void disableChokePointsNearFirstBase() {
		if (!_disabledChokePointsNearMainBase) {
			Collection<ChokePoint> chokes = MapExploration.getChokePointsNear(
					ProtossNexus.getSecondBaseLocation(), 20);
			Region baseRegion = xvr.getBwapi().getMap().getRegion(xvr.getFirstBase());
			for (ChokePoint choke : chokes) {
				if (baseRegion.getChokePoints().contains(choke)) {
					// chokePointsProcessed.remove(choke);
					choke.setDisabled(true);
					;
				}
			}
			_disabledChokePointsNearMainBase = true;
		}
	}
}
