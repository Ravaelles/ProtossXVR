package jnibwapi.protoss;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;

import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

public class ProtossPylon {

	private static int INITIAL_PYLON_MIN_DIST_FROM_BASE = 6;
	private static int INITIAL_PYLON_MAX_DIST_FROM_BASE = 70;
	private static int PYLON_FROM_PYLON_MIN_DISTANCE = 8;
	private static int PYLON_FROM_PYLON_MAX_DISTANCE = 70;

	private static final UnitTypes buildingType = UnitTypes.Protoss_Pylon;
	private static XVR xvr = XVR.getInstance();
//	private static JNIBWAPI bwapi = xvr.getBwapi();

	public static void buildIfNecessary() {
		if (xvr.canAfford(100)) {

			// It only makes sense to build Supply Depot if supplies less than
			// X.
			if (shouldBuild()) {
				Constructing.construct(xvr, buildingType);
			}
		}
	}

	public static boolean shouldBuild() {
		int free = xvr.getSuppliesFree();
		int total = xvr.getSuppliesTotal();

		return !Constructing.weAreBuilding(buildingType)
				&& ((total <= 10 && free <= 2)
						|| (total > 10 && total <= 18 && free <= 4)
						|| (total > 18 && total <= 45 && free <= 7)
						|| (total > 45 && free <= 10)
						|| (total > 90 && total < 200 && free <= 20));
	}

	public static Point findTileNearPylonForNewBuilding() {
		if (!UnitCounter.weHavePylonFinished()) {
			return null;
		}

		// Get the pylon that has fewest buildings nearby.
		// Unit pylonWithLeastBuildings = getPylonWithLeastBuildings();

		Unit firstBase = xvr.getFirstBase();

		// Get
		// for (Unit pylon : xvr.getUnitsOfGivenTypeInRadius(buildingType,
		// INITIAL_PYLON_MAX_DIST_FROM_BASE, firstBase.getX(),
		// firstBase.getY(), true)) {
		// if (pylon != null) {
		// return Constructing.getLegitTileToBuildNear(
		// xvr.getRandomWorker(), buildingType, pylon.getTileX(),
		// pylon.getTileY(), 0, PYLON_FROM_PYLON_MAX_DISTANCE);
		// return new Point(pylonWithLeastBuildings.getTileX(),
		// pylonWithLeastBuildings.getTileY());
		// }
		// }
		// return null;
		
		for (Unit pylon : xvr.getUnitsOfGivenTypeInRadius(buildingType,
				INITIAL_PYLON_MAX_DIST_FROM_BASE, firstBase.getX(),
				firstBase.getY(), true)) {
			if (pylon != null) {
				
//				return Constructing.getLegitTileToBuildNear(
//						xvr.getRandomWorker(), buildingType, pylon.getTileX(),
//						pylon.getTileY(), 0, PYLON_FROM_PYLON_MAX_DISTANCE);
//				return new Point(pylonWithLeastBuildings.getTileX(),
//						pylonWithLeastBuildings.getTileY());
				Point tile = Constructing.getLegitTileToBuildNear(xvr.getRandomWorker(),
						buildingType, pylon.getTileX(), pylon.getTileY(), 0,
						PYLON_FROM_PYLON_MAX_DISTANCE, true);
				if (tile != null) {
					return tile;
				}
			}
		}
		return null;
	}

	// private static Unit getPylonWithLeastBuildings() {
	//
	// // Create mapping of pylons to number of buildings in range.
	// Map<Unit, Integer> pylonsToBuildingsNumbers =
	// createMapPylonsToBuildingsNumber();
	//
	// if (!pylonsToBuildingsNumbers.isEmpty()) {
	//
	// // Return first pylon (the one with lowest buildings around)
	// for (Unit pylon : pylonsToBuildingsNumbers.keySet()) {
	// return pylon;
	// }
	// }
	// return null;
	// }

	public static Point findTileForPylon() {
		Unit builder = Constructing.getRandomWorker();

		// It's not the first pylon
		if (UnitCounter.weHavePylonFinished()) {
			return findTileForNextPylon(builder);
		}

		// It's the first pylon
		else {
			return findTileForFirstPylon(builder);
		}
	}

	private static Point findTileForNextPylon(Unit builder) {

		// // For each
		// for (Unit pylon : getPylonsInRadius(xvr.getFirstBase(),
		// INITIAL_PYLON_MAX_DIST_FROM_BASE)) {
		// Point tileForPylon = Constructing.getLegitTileToBuildNear(builder,
		// buildingType, pylon, PYLON_FROM_PYLON_MIN_DISTANCE,
		// PYLON_FROM_PYLON_MAX_DISTANCE);
		// if (tileForPylon != null) {
		// return tileForPylon;
		// }
		// }
		// return null;

		for (Unit pylon : getPylonsInRadius(xvr.getFirstBase(),
				INITIAL_PYLON_MAX_DIST_FROM_BASE)) {
			Point tile = findTileForNextPylonIfPossible(pylon, builder);
			if (tile != null) {
				return tile;
			}
		}
		return null;
	}

	private static Point findTileForNextPylonIfPossible(Unit nearToThisPylon,
			Unit builder) {
		int tileX = nearToThisPylon.getTileX();
		int tileY = nearToThisPylon.getTileY();

		int currentDist = PYLON_FROM_PYLON_MIN_DISTANCE;
		while (currentDist <= PYLON_FROM_PYLON_MAX_DISTANCE) {
			for (int i = tileX - currentDist; i <= tileX + currentDist; i++) {
				for (int j = tileY - currentDist; j <= tileY + currentDist; j++) {
					if (Constructing.canBuildHere(builder, buildingType,
									i, j)
							&& xvr.getUnitsOfGivenTypeInRadius(buildingType,
									PYLON_FROM_PYLON_MIN_DISTANCE - 1, i * 32,
									j * 32, true).isEmpty()) {
						return new Point(i, j);
					}
				}
			}

			currentDist++;
		}
		return null;
	}

//	private static Map<Unit, Integer> createMapPylonsToBuildingsNumber() {
//		Unit firstBase = xvr.getFirstBase();
//
//		// Get list of pylons being close to the base
//		ArrayList<Unit> pylons = getPylonsInRadius(firstBase,
//				INITIAL_PYLON_MIN_DIST_FROM_BASE);
//
//		// Sort the list
//		Map<Unit, Integer> result = new HashMap<Unit, Integer>();
//		for (Unit pylon : pylons) {
//			result.put(pylon,
//					xvr.countUnitsInRadius(pylon.getX(), pylon.getY(), 6, true));
//		}
//		return RUtilities.sortByValue(result, true);
//	}

	private static ArrayList<Unit> getPylonsInRadius(Unit firstBase,
			int iNITIAL_PYLON_MIN_DIST_FROM_BASE2) {
		return xvr.getUnitsInRadius(firstBase.getX(), firstBase.getY(),
				INITIAL_PYLON_MAX_DIST_FROM_BASE, getPylons());
	}

	private static ArrayList<Unit> getPylons() {
		ArrayList<Unit> pylons = xvr.getUnitsOfType(UnitTypes.Protoss_Pylon);
		for (Iterator<Unit> iterator = pylons.iterator(); iterator.hasNext();) {
			Unit unit = (Unit) iterator.next();
			if (!unit.isCompleted()) {
				iterator.remove();
			}
		}
		return pylons;
	}

	private static Point findTileForFirstPylon(Unit builder) {
		if (xvr.getFirstBase() == null) {
			return null;
		}
		return Constructing.getLegitTileToBuildNear(builder, buildingType,
				xvr.getFirstBase(), INITIAL_PYLON_MIN_DIST_FROM_BASE,
				INITIAL_PYLON_MAX_DIST_FROM_BASE, false);
	}

	public static UnitTypes getBuildingtype() {
		return buildingType;
	}
	
}
