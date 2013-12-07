package ai.protoss;

import java.util.ArrayList;
import java.util.Iterator;

import jnibwapi.model.ChokePoint;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;
import ai.handling.constructing.Constructing;
import ai.handling.constructing.ShouldBuildCache;
import ai.handling.map.MapExploration;
import ai.handling.map.MapPoint;
import ai.handling.map.MapPointInstance;
import ai.handling.units.UnitCounter;
import ai.managers.UnitManager;
import ai.utils.RUtilities;

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

		if (total == 200) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
			return false;
		}

		// !Constructing.weAreBuilding(buildingType)
		// &&
		boolean shouldBuild = ((total <= 10 && free <= 2)
				|| (total > 10 && total <= 18 && free <= 4)
				|| (total > 18 && total <= 45 && free <= 7)
				|| (total > 45 && free <= 10) || (total > 90 && total < 200 && free <= 20));

		ShouldBuildCache.cacheShouldBuildInfo(buildingType, shouldBuild);
		return shouldBuild;
	}

	public static MapPoint findTileNearPylonForNewBuilding() {
		if (!UnitCounter.weHavePylonFinished()) {
			return null;
		}

		// Get the pylon that has fewest buildings nearby.
		// Unit pylonWithLeastBuildings = getPylonWithLeastBuildings();

		// Unit base = ProtossNexus.getRandomBase();
		Unit base = xvr.getFirstBase();
		if (base == null) {
			return null;
		}

		if (UnitCounter.getNumberOfUnits(UnitManager.BASE) >= 3
				&& RUtilities.rand(0, 4) == 0) {
			base = ProtossNexus.getRandomBase();
		}

		for (Unit pylon : xvr.getUnitsInRadius(base, 100,
				xvr.getUnitsOfType(buildingType))) {
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

				MapPoint tile = Constructing
						.getLegitTileToBuildNear(xvr.getRandomWorker(),
								buildingType, pylon, 0, 15, true);
				if (tile != null) {
					return tile;
				}
			}
		}
		return null;
	}

	public static MapPoint findTileForPylon() {
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

	private static MapPoint findTileForNextPylon(Unit builder) {

		// If we have more than one base make sure that every base has at
		// least one pylon nearby
		for (Unit base : xvr.getUnitsOfType(UnitManager.BASE)) {
			int nearbyPylons = xvr.countUnitsOfGivenTypeInRadius(buildingType,
					9, base.getX(), base.getY(), true);

			// If this base has no pylons nearby, build one.
			// if (nearbyPylons == 0) {
			// return findTileForFirstPylon(builder, base);
			// } else {
			// Point buildTile = findTileForPylonNearby(base,
			// INITIAL_PYLON_MIN_DIST_FROM_BASE,
			// INITIAL_PYLON_MAX_DIST_FROM_BASE);
			// if (buildTile != null) {
			// return buildTile;
			// }
			// }

			if (nearbyPylons == 0) {
				MapPoint buildTile = findTileForPylonNearby(base,
						INITIAL_PYLON_MIN_DIST_FROM_BASE,
						INITIAL_PYLON_MAX_DIST_FROM_BASE);
				if (buildTile != null) {
					return buildTile;
				}
			}
		}

		// If we already have N pylons, with some luck try building it as normal
		// building.
		if ((UnitCounter.getNumberOfUnits(buildingType) >= 5 && RUtilities
				.rand(0, 5) == 0)
				|| (xvr.getSuppliesTotal() > 55 && xvr.getSuppliesFree() <= 5)) {
			return findTileNearPylonForNewBuilding();
		}

		// Either build randomly near base
		if (RUtilities.rand(0, 1) == 0) {

			// Build normally, at random base.
			MapPoint buildTile = findTileForPylonNearby(
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

	private static MapPoint findTileForPylonNearby(MapPoint point, int minDist,
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
		return findLegitTileForPylon(point, Constructing.getRandomWorker());
		// return Constructing.getLegitTileToBuildNear(xvr.getRandomWorker(),
		// buildingType, unit, INITIAL_PYLON_MIN_DIST_FROM_BASE,
		// INITIAL_PYLON_MAX_DIST_FROM_BASE, false);
		// }
	}

	private static MapPoint findLegitTileForPylon(MapPoint buildNearToHere,
			Unit builder) {
		int tileX = buildNearToHere.getTx();
		int tileY = buildNearToHere.getTy();

		int currentDist = PYLON_FROM_PYLON_MIN_DISTANCE;
		while (currentDist <= PYLON_FROM_PYLON_MAX_DISTANCE) {
			for (int i = tileX - currentDist; i <= tileX + currentDist; i++) {
				for (int j = tileY - currentDist; j <= tileY + currentDist; j++) {
					int x = i * 32;
					int y = j * 32;
					if (Constructing.canBuildHere(builder, buildingType, i, j)
							&& xvr.getUnitsOfGivenTypeInRadius(buildingType,
									PYLON_FROM_PYLON_MIN_DISTANCE - 1, x, y,
									true).isEmpty()) {
						ChokePoint choke = MapExploration
								.getNearestChokePointFor(x, y);

						// Damn, try NOT to build in the middle of narrow choke
						// point.
						if (choke.getRadius() < 6 * 32
								&& xvr.getDistanceBetween(x, y,
										choke.getCenterX(), choke.getCenterY()) > 5) {
							return new MapPointInstance(x, y);
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

	private static MapPoint findTileForFirstPylon(Unit builder, Unit base) {
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
