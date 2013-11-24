package ai.handling.map;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.BaseLocation;
import jnibwapi.model.ChokePoint;
import jnibwapi.model.Region;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;
import ai.core.Debug;
import ai.core.XVR;
import ai.handling.units.UnitActions;
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

	private static Unit explorer;
	private static boolean hasExplorerJustAttacked = false;

	public static Unit getExplorer() {
		return explorer;
	}

	public static void explore(Unit explorer) {

		// Define nearest enemy
		Unit nearestEnemy = xvr.getUnitNearestFromList(explorer.getX(),
				explorer.getY(), xvr.getEnemyUnitsVisible());

		// Act when enemy is nearby
		if (nearestEnemy != null
				&& xvr.getDistanceBetween(nearestEnemy, explorer) < 5) {

			// If we're worker and found hidden unit like dark templar, get the
			// hell out of there.
			if (explorer.isWorker()) {
				UnitActions.moveAwayFromUnitIfPossible(explorer, nearestEnemy,
						8);
				return;
			}

			// If we have trolled the enemy, RUN =]
			if (hasExplorerJustAttacked) {
				BaseLocation goTo = getMostDistantBaseLocation(xvr
						.getFirstBase());
				UnitActions.moveTo(explorer, goTo.getX(), goTo.getY());
			} else {
				UnitActions.attackEnemyUnit(explorer, nearestEnemy);
				hasExplorerJustAttacked = true;
			}
		}

		// No enemy is nearby
		else {
			hasExplorerJustAttacked = false;

			// If explorer is on its way, don't interrupt.
			if ((!explorer.isIdle() && !explorer.isGatheringMinerals() && !explorer
					.isGatheringGas()) || !explorer.isCompleted()) {
				return;
			}
			BaseLocation goTo = null;
			boolean initial = false;

			// If no base has been discovered try to
			if (baseLocationsDiscovered.isEmpty()) {
				initial = true;
				goTo = getMostDistantBaseLocation(xvr.getFirstBase());
				Debug.message(xvr, "Initial scouting: go to [" + goTo.getX()
						/ 32 + ", " + goTo.getY() / 32 + "]");
			}

			// Non-initial scout behavior
			else {
				goTo = nonInitialScouting(explorer);
			}

			if (goTo != null) {

				// Add info that we've visited this place.
				baseLocationsDiscovered.add(goTo);

				// Send unit to scout specified point.
				if (initial) {
					UnitActions.attackTo(explorer, goTo.getX(), goTo.getY());
				} else {
					UnitActions.moveTo(explorer, goTo.getX(), goTo.getY());
				}
			}
		}
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
		double nearestDistance = 2;
		BaseLocation nearestObject = null;

		for (BaseLocation object : xvr.getBwapi().getMap().getBaseLocations()) {
			double distance = xvr.getDistanceBetween(unit, object.getX(),
					object.getY());
			if (distance > nearestDistance) {
				nearestDistance = distance;
				nearestObject = object;
			}
		}

		return nearestObject;
	}

	private static BaseLocation nonInitialScouting(Unit explorer) {
		XVR xvr = XVR.getInstance();
		BaseLocation goTo = null;

		// Filter out visited bases.
		ArrayList<BaseLocation> possibleBases = new ArrayList<BaseLocation>();
		possibleBases.addAll(xvr.getBwapi().getMap().getBaseLocations());
		possibleBases.removeAll(baseLocationsDiscovered);

		// If there is any unvisited base- go there. If no- go to the random
		// base.

		if (possibleBases.isEmpty()) {
			goTo = (BaseLocation) RUtilities.getRandomListElement(xvr
					.getBwapi().getMap().getBaseLocations());
		} else {
			goTo = (BaseLocation) RUtilities
					.getRandomListElement(possibleBases);
		}

		return goTo;
	}

	public static ChokePoint getNearestChokePointFor(Unit unit) {
		return getNearestChokePointFor(unit.getX(), unit.getY());
	}

	public static ChokePoint getNearestChokePointFor(int x, int y) {
		double nearestDistance = 999999;
		ChokePoint nearestObject = null;

		for (ChokePoint object : chokePointsProcessed) {
			double distance = xvr.getDistanceBetween(x, y, object.getCenterX(),
					object.getCenterY());
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearestObject = object;
			}
		}

		return nearestObject;
	}

	public static ArrayList<ChokePoint> getNearestChokePointsFor(Unit unit) {
		HashMap<ChokePoint, Double> chokes = new HashMap<ChokePoint, Double>();
		if (unit == null) {
			return new ArrayList<ChokePoint>();
		}

		for (ChokePoint object : chokePointsProcessed) {
			double distance = xvr.getDistanceBetween(unit, object.getCenterX(),
					object.getCenterY()) / 32;
			chokes.put(object, distance);
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
			return (Unit) RUtilities.getRandomElement(enemyBasesDiscovered
					.values());
		}
	}

	private static BaseLocation getRandomBaseLocation() {
		ArrayList<BaseLocation> list = new ArrayList<BaseLocation>();
		for (BaseLocation base : baseLocationsDiscovered) {
			list.add(base);
		}
		return (BaseLocation) RUtilities.getRandomListElement(list);
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
				if (closestDistance > distance) {
					closestBuilding = building;
					closestDistance = distance;
				}
			}

			// return new Point(closestBase.getX(), closestBase.getY());
			return closestBuilding;
		}
	}

	public static MapPointInstance getNearestUnknownPointFor(int x, int y,
			boolean mustBeWalkable) {
		int tileX = x / 32;
		int tileY = y / 32;

		int currentDist = 3;
		int maximumDist = 90;
		while (currentDist < maximumDist) {
			for (int attempt = 0; attempt < currentDist; attempt++) {
				int i = tileX + currentDist
						- RUtilities.rand(0, 2 * currentDist);
				int j = tileY + currentDist
						- RUtilities.rand(0, 2 * currentDist);
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
				int i = tileX + currentDist
						- RUtilities.rand(0, 2 * currentDist);
				int j = tileY + currentDist
						- RUtilities.rand(0, 2 * currentDist);
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
		return (ChokePoint) RUtilities
				.getRandomListElement(chokePointsProcessed);
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

	private static synchronized void removeNonExistingUnitsFrom(
			HashMap<Integer, Unit> mapping) {
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

	public static ArrayList<? extends MapPoint> getBaseLocationsNear(Unit base,
			int tileRadius) {
		ArrayList<BaseLocation> bases = new ArrayList<BaseLocation>();
		if (base == null) {
			return bases;
		}

		for (BaseLocation object : xvr.getBwapi().getMap().getBaseLocations()) {
			if (object == null) {
				continue;
			}
			double distance = xvr.getDistanceBetween(base, object.getX(),
					object.getY());
			if (distance <= tileRadius) {
				bases.add(object);
			}
		}

		return bases;
	}

	public static void updateInfoAboutHiddenUnits() {
		_hiddenEnemyUnits.clear();
		for (Unit unit : enemyUnitsDiscovered.values()) {
			if (unit.isEnemy()
					&& (unit.isCloaked() || unit.isBurrowed() || !unit
							.isDetected())) {
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
			Unit nearestHiddenEnemy = xvr.getUnitNearestFromList(unit,
					_hiddenEnemyUnits);
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
					// Debug.message(xvr, "Skipped choke: " + choke.getTx() +
					// ","
					// + choke.getTy());
					// System.out.println("Skipped choke: " + choke.getTx() +
					// ","
					// + choke.getTy());
					continue;
				} else {
					chokePointsProcessed.add(choke);
				}
			}
		}
		int percentSkipped = 100
				* (initialChokes - chokePointsProcessed.size()) / initialChokes;
		if (percentSkipped > 0) {
			System.out.println("Skipped " + percentSkipped
					+ "% of initial choke points");
		}
	}

	public static ChokePoint getImportantChokePointNear(Unit base) {
		ArrayList<ChokePoint> nearestChokePoints = getChokePointsForRegion(xvr
				.getBwapi().getMap().getRegion(base));

		return nearestChokePoints.get(0);

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

	private static ArrayList<ChokePoint> getChokePointsForRegion(Region region) {
		ArrayList<ChokePoint> result = new ArrayList<ChokePoint>();
		for (ChokePoint choke : chokePointsProcessed) {
			if (choke.getFirstRegionID() == region.getID()
					|| choke.getSecondRegionID() == region.getID()) {
				result.add(choke);
			}
		}
		return result;
	}

	public static ArrayList<ChokePoint> getChokePoints() {
		return chokePointsProcessed;
	}
}
