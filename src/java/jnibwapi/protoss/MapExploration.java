package jnibwapi.protoss;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import jnibwapi.Debug;
import jnibwapi.JNIBWAPI;
import jnibwapi.RUtilities;
import jnibwapi.XVR;
import jnibwapi.model.BaseLocation;
import jnibwapi.model.ChokePoint;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;

public class MapExploration {

	private static final int MAXIMUM_CHOKE_POINT_DISTANCE_FROM_BASE = 30;

	private static XVR xvr = XVR.getInstance();
	private static JNIBWAPI bwapi = XVR.getInstance().getBwapi();
	public static ArrayList<ChokePoint> chokePointsInitial = new ArrayList<ChokePoint>();

	private static TreeSet<BaseLocation> baseLocationsDiscovered = new TreeSet<BaseLocation>();
	private static ArrayList<Unit> enemyBasesDiscovered = new ArrayList<Unit>();
	private static ArrayList<Unit> enemyBuildingsDiscovered = new ArrayList<Unit>();
	private static ArrayList<Unit> enemyUnitsDiscovered = new ArrayList<Unit>();

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

	public static void enemyUnitDiscovered(Unit enemyUnit) {
		UnitType type = enemyUnit.getType();
		if (type.isBase()) {
			enemyBasesDiscovered.add(enemyUnit);
		}
		if (type.isBuilding()) {
			enemyBuildingsDiscovered.add(enemyUnit);
		} else {
			enemyUnitsDiscovered.add(enemyUnit);
		}
	}

	public static BaseLocation getMostDistantBaseLocation(Unit unit) {
		XVR xvr = XVR.getInstance();

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
		double nearestDistance = 999999;
		ChokePoint nearestObject = null;

		for (ChokePoint object : chokePointsInitial) {
			double distance = xvr.getDistanceBetween(unit, object.getCenterX(),
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

		for (ChokePoint object : chokePointsInitial) {
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
			return (Unit) RUtilities.getRandomListElement(enemyBasesDiscovered);
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
		for (Iterator<Unit> iterator = enemyBasesDiscovered.iterator(); iterator
				.hasNext();) {
			Unit base = (Unit) iterator.next();
			if (base == null) {
				iterator.remove();
			}
		}

		if (enemyBasesDiscovered.isEmpty()) {
			return null;
		} else {
			Unit ourBase = xvr.getFirstBase();

			Unit closestBase = null;
			double closestDistance = 99999;

			for (Unit base : enemyBasesDiscovered) {
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

			for (Unit building : enemyBuildingsDiscovered) {
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

	public static Point getNearestUnknownPointFor(int x, int y,
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
					return new Point(i * 32, j * 32);
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
					return new Point(i * 32, j * 32);
				}
			}
			currentDist += RUtilities.rand(1, maximumDist - currentDist);
		}

		return null;
	}

	public static ArrayList<Unit> getEnemyBuildingsDiscovered() {
		return enemyBuildingsDiscovered;
	}

	public static ArrayList<Unit> getEnemyUnitsDiscovered() {
		return enemyUnitsDiscovered;
	}

	
	public static ChokePoint getRandomChokePoint() {
		return (ChokePoint) RUtilities.getRandomListElement(chokePointsInitial);
	}

	// xvr.getBwapi().getMap().getRegions().get(0).
	// xvr.getBwapi().isExplored(tx, ty)
	// for (int x = tx - currDist; x < tx + currDist; x++) {
	//
	// }
}
