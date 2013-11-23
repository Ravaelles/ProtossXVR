package jnibwapi.protoss;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;

import jnibwapi.RUtilities;
import jnibwapi.XVR;
import jnibwapi.model.ChokePoint;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.xvr.Constructing;
import jnibwapi.xvr.MapExploration;
import jnibwapi.xvr.MapPoint;
import jnibwapi.xvr.MapPointInstance;
import jnibwapi.xvr.UnitCounter;
import jnibwapi.xvr.UnitManager;

public class ProtossPylon {

	private static int INITIAL_PYLON_MIN_DIST_FROM_BASE = 7;
	private static int INITIAL_PYLON_MAX_DIST_FROM_BASE = 25;
	private static int PYLON_FROM_PYLON_MIN_DISTANCE = 8;
	// private static int PYLON_FROM_PYLON_MIN_DISTANCE_RAND = 4;
	private static int PYLON_FROM_PYLON_MAX_DISTANCE = 20;

	private static final UnitTypes buildingType = UnitTypes.Protoss_Pylon;
	private static XVR xvr = XVR.getInstance();

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

		// !Constructing.weAreBuilding(buildingType)
		// &&
		return ((total <= 10 && free <= 2)
				|| (total > 10 && total <= 18 && free <= 4)
				|| (total > 18 && total <= 45 && free <= 7)
				|| (total > 45 && free <= 10) || (total > 90 && total < 200 && free <= 20));
	}

	public static Point findTileNearPylonForNewBuilding() {
		if (!UnitCounter.weHavePylonFinished()) {
			return null;
		}

		// Get the pylon that has fewest buildings nearby.
		// Unit pylonWithLeastBuildings = getPylonWithLeastBuildings();

		// Unit base = ProtossNexus.getRandomBase();
		Unit base = xvr.getFirstBase();
		if (RUtilities.rand(0, 4) == 0) {
			base = ProtossNexus.getRandomBase();
		}

		for (Unit pylon : xvr.getUnitsOfGivenTypeInRadius(buildingType, 60,
				base.getX(), base.getY(), true)) {
			if (pylon != null) {

				// return Constructing.getLegitTileToBuildNear(
				// xvr.getRandomWorker(), buildingType, pylon.getTileX(),
				// pylon.getTileY(), 0, PYLON_FROM_PYLON_MAX_DISTANCE);
				// return new Point(pylonWithLeastBuildings.getTileX(),
				// pylonWithLeastBuildings.getTileY());
				// int minDistanceBetweenPylons =
				// PYLON_FROM_PYLON_MIN_DISTANCE_RAND
				// + PYLON_FROM_PYLON_MIN_DISTANCE_RAND
				// - RUtilities.rand(0,
				// 2 * PYLON_FROM_PYLON_MIN_DISTANCE_RAND);

				Point tile = Constructing.getLegitTileToBuildNear(
						xvr.getRandomWorker(), buildingType, pylon.getTileX(),
						pylon.getTileY(), 0, 9, true);
				if (tile != null) {
					return tile;
				}
			}
		}
		return null;
	}

	public static Point findTileForPylon() {
		Unit builder = Constructing.getRandomWorker();

		// It's not the first pylon
		if (UnitCounter.weHavePylonFinished()) {
			return findTileForNextPylon(builder);
		}

		// It's the first pylon
		else {
			return findTileForFirstPylon(builder, xvr.getFirstBase());
		}
	}

	private static Point findTileForNextPylon(Unit builder) {

		// If we already have N pylons, with some luck try building it as normal
		// building.
		if ((UnitCounter.getNumberOfUnits(buildingType) >= 5 && RUtilities
				.rand(0, 3) == 0)
				|| (xvr.getSuppliesTotal() > 55 && xvr.getSuppliesFree() <= 5)) {
			return findTileNearPylonForNewBuilding();
		}

		// Either build randomly near base
		if (RUtilities.rand(0, 1) == 0) {

			// If we have more than one base make sure that every base has at
			// least
			// one pylon nearby
			if (UnitCounter.getNumberOfUnits(UnitManager.BASE) > 1) {
				for (Unit base : xvr.getUnitsOfType(UnitManager.BASE)) {

					// If this base has no pylons nearby, build one.
					if (xvr.countUnitsOfGivenTypeInRadius(buildingType,
							base.getX(), base.getY(), 10, true) < 1) {
						return findTileForFirstPylon(builder, base);
					}
				}
			}

			// Build normally, at random base.
			Point buildTile = findTileForPylonNearby(
					ProtossNexus.getRandomBase(),
					INITIAL_PYLON_MIN_DIST_FROM_BASE,
					INITIAL_PYLON_MAX_DIST_FROM_BASE);
			if (buildTile != null) {
				return buildTile;
			}
		}

		// or build near random pylon.
		return findTileForPylonNearby(getRandomPylon(),
				PYLON_FROM_PYLON_MIN_DISTANCE, PYLON_FROM_PYLON_MAX_DISTANCE);

		// if (RUtilities.rand(0, 6) == 0) {
		// return findTileForPylonNearbyBase(ProtossNexus.getRandomBase());
		// }

		// // If we have more than one base make sure that every base has at
		// least
		// // one pylon nearby
		// if (UnitCounter.getNumberOfUnits(UnitManager.BASE) > 1) {
		// for (Unit base : xvr.getUnitsOfType(UnitManager.BASE)) {
		// // Point point = findTileForPylonNearbyBase(base);
		// // if (point != null) {
		// // return point;
		// // }
		// if (xvr.countUnitsOfGivenTypeInRadius(buildingType,
		// base.getX(), base.getY(), 10, true) < 1) {
		// return findTileForFirstPylon(builder, base);
		// }
		// }
		// }
		//
		// // // For each
		// // for (Unit pylon : getPylonsInRadius(xvr.getFirstBase(),
		// // INITIAL_PYLON_MAX_DIST_FROM_BASE)) {
		// // Point tileForPylon = Constructing.getLegitTileToBuildNear(builder,
		// // buildingType, pylon, PYLON_FROM_PYLON_MIN_DISTANCE,
		// // PYLON_FROM_PYLON_MAX_DISTANCE);
		// // if (tileForPylon != null) {
		// // return tileForPylon;
		// // }
		// // }
		// // return null;
		//
		// return findTileForPylonNearbyBase(ProtossNexus.getRandomBase());
		// ArrayList<Unit> pylons = getPylonsInRadius(xvr.getFirstBase(), 100);
		// if (RUtilities.rand(0, 1) == 0) {
		// Collections.shuffle(pylons);
		// }
		// for (Unit pylon : pylons) {
		// Point tile = findTileForNextPylonIfPossible(pylon, builder);
		// if (tile != null) {
		// return tile;
		// }
		// }
		//
		// // If we're here it means no new place has been found. So just built
		// // randomly.
		// return findTileForPylonNearbyBase(builder);
	}

	private static Unit getRandomPylon() {
		ArrayList<Unit> pylons = getPylons();
		return (Unit) RUtilities.getRandomListElement(pylons);
	}

	private static Point findTileForPylonNearby(MapPoint point, int minDist,
			int maxDist) {
		// boolean existsPylonNearby = !xvr.getUnitsOfGivenTypeInRadius(
		// buildingType, 8, base.getX(), base.getY(), true).isEmpty();

		// System.out.println();
		// System.out.println("LOOKING FOR PYLON: " + existsPylonNearby);
		// System.out.println(xvr.getUnitsOfGivenTypeInRadius(buildingType, 8,
		// base.getX(), base.getY(), true).size());
		// System.out.println("BASE: " + base.toStringShort());
		// for (Unit pylon : xvr.getUnitsOfGivenTypeInRadius(buildingType, 8,
		// base.getX(), base.getY(), true)) {
		// System.out.println(" pylon: " + pylon.toStringShort());
		// }
		// System.out.println();
		// if (existsPylonNearby) {
		// return null;
		// } else {
		return findTileForNextPylonIfPossible(point,
				Constructing.getRandomWorker());
		// return Constructing.getLegitTileToBuildNear(xvr.getRandomWorker(),
		// buildingType, unit, INITIAL_PYLON_MIN_DIST_FROM_BASE,
		// INITIAL_PYLON_MAX_DIST_FROM_BASE, false);
		// }
	}

	private static Point findTileForNextPylonIfPossible(
			MapPoint nearToThisUnit, Unit builder) {
		int tileX = nearToThisUnit.getTx();
		int tileY = nearToThisUnit.getTy();

		int currentDist = PYLON_FROM_PYLON_MIN_DISTANCE;
		while (currentDist <= PYLON_FROM_PYLON_MAX_DISTANCE) {
			for (int i = tileX - currentDist; i <= tileX + currentDist; i++) {
				for (int j = tileY - currentDist; j <= tileY + currentDist; j++) {
					if (Constructing.canBuildHere(builder, buildingType, i, j)
							&& xvr.getUnitsOfGivenTypeInRadius(buildingType,
									PYLON_FROM_PYLON_MIN_DISTANCE - 1, i * 32,
									j * 32, true).isEmpty()) {
						ChokePoint choke = MapExploration
								.getNearestChokePointFor(i * 32, j * 32);

						// Damn, try NOT to build in the middle of narrow choke
						// point.
						if (choke.getRadius() < 6 * 32
								&& xvr.getDistanceBetween(i * 32, j * 32,
										choke.getCenterX(), choke.getCenterY()) > 5) {
							return new Point(i, j);
						}
					}
				}
			}

			currentDist++;
		}
		return null;
	}

	//
	// private static ArrayList<Unit> getPylonsInRadius(Unit base,
	// int iNITIAL_PYLON_MIN_DIST_FROM_BASE2) {
	// return xvr.getUnitsInRadius(base.getX(), base.getY(),
	// INITIAL_PYLON_MAX_DIST_FROM_BASE, getPylons());
	// }

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

	private static Point findTileForFirstPylon(Unit builder, Unit base) {
		if (base == null) {
			return null;
		}

		// Find point being in the middle of way base<->nearest choke point.
		ChokePoint choke = MapExploration.getNearestChokePointFor(base);
		MapPointInstance location = new MapPointInstance(
				(base.getX() + choke.getCenterX()) / 2,
				(base.getY() + choke.getCenterY()) / 2);

		return Constructing.getLegitTileToBuildNear(builder, buildingType,
				location, 0, 100, false);
	}

	public static UnitTypes getBuildingType() {
		return buildingType;
	}

}
