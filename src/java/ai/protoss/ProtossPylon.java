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
import ai.managers.BotStrategyManager;
import ai.managers.UnitManager;
import ai.utils.RUtilities;

public class ProtossPylon {

	private static int INITIAL_PYLON_MIN_DIST_FROM_BASE = 5;
	private static int INITIAL_PYLON_MAX_DIST_FROM_BASE = 18;
	private static int PYLON_FROM_PYLON_MIN_DISTANCE = 8;
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
		int pylons = UnitCounter.getNumberOfUnits(buildingType);
		int workers = UnitCounter.getNumberOfUnits(UnitManager.WORKER);
		int forges = UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Forge);

		// ### VERSION ### Expansion with cannons
		if (BotStrategyManager.isExpandWithCannons()) {
			if (pylons == 0 && (workers >= 7 || xvr.canAfford(87))) {
				ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
				return true;
			}
		}

		if (BotStrategyManager.isExpandWithCannons()) {
			if (pylons == 1
					&& ((forges == 1 && xvr.canAfford(54)) || (forges == 0 && xvr
							.canAfford(194)))) {
				ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
				return true;
			}
		} else {
			if (pylons == 1
					&& ((forges == 1 && xvr.canAfford(54)) || (forges == 0 && xvr
							.canAfford(216)))) {
				ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
				return true;
			}
		}

		if (total == 200) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
			return false;
		}

		// int gateways =
		// UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Gateway);
		// int cannonsAll = UnitCounter
		// .getNumberOfUnits(UnitTypes.Protoss_Photon_Cannon);
		// int cannonsCompleted = UnitCounter
		// .getNumberOfUnitsCompleted(UnitTypes.Protoss_Photon_Cannon);

		// ### VERSION ### cannons
		// if (pylons == 1 && (cannonsCompleted <= 1 || cannonsAll <= 3)
		// && (gateways <= 1 && gateways != 0) && !xvr.canAfford(260)) {
		// ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
		// return false;
		// }

		if (ProtossPylon
				.findTileNearPylonForNewBuilding(UnitTypes.Protoss_Gateway) == null) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			return true;
		}

		if (total < 80 && Constructing.weAreBuilding(buildingType)) {
			if (!(total >= 10 && total <= 20 && free == 0)) {
				ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
				return false;
			}
		}

		// !Constructing.weAreBuilding(buildingType)
		// &&
		boolean shouldBuild = ((pylons == 0 && total <= 9 && free <= 3)
				|| (total >= 10 && total <= 17 && free <= 4 && pylons <= 1)
				|| (total >= 18 && total <= 25 && free <= 7)
				|| (total > 25 && total <= 45 && free <= 11)
				|| (total > 45 && free <= 16) || (total > 90 && total < 200 && free <= 20));

		ShouldBuildCache.cacheShouldBuildInfo(buildingType, shouldBuild);
		return shouldBuild;
	}

	public static double calculateExistingPylonsStrength() {
		double result = 0;

		for (Unit pylon : xvr.getUnitsOfType(buildingType)) {
			result += (double) (pylon.getShields() + pylon.getHitPoints()) / 600;
		}

		return result;
	}

	public static MapPoint findTileNearPylonForNewBuilding(UnitTypes typeToBuild) {
		if (!UnitCounter.weHavePylonFinished()) {
			return null;
		}
		int pylons = UnitCounter.getNumberOfPylons();

		// Get the pylon that has fewest buildings nearby.
		// Unit pylonWithLeastBuildings = getPylonWithLeastBuildings();

		// Unit base = ProtossNexus.getRandomBase();
		Unit base = xvr.getFirstBase();
		if (base == null) {
			return null;
		}

		// if (UnitCounter.getNumberOfUnits(UnitManager.BASE) >= 3
		// && RUtilities.rand(0, 4) == 0) {
		// base = ProtossNexus.getRandomBase();
		// }

		int searchRadiusOfPylons = 14;
		if (pylons > 10 || pylons == 1) {
			searchRadiusOfPylons = 50;
		}

		for (Unit pylon : xvr.getUnitsInRadius(base, searchRadiusOfPylons,
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

				int minDistFromPylon = 5;
//				MapPoint secondBase = ProtossNexus.getSecondBaseLocation();
//				if (secondBase != null
//						&& xvr.getDistanceBetween(pylon, secondBase) < 12) {
//					minDistFromPylon = 0;
//				}

				MapPoint tile = Constructing.getLegitTileToBuildNear(
						xvr.getRandomWorker(), typeToBuild, pylon,
						minDistFromPylon, 15, true);
				if (tile != null) {
					return tile;
				}
			}
		}
		return null;
	}

	public static MapPoint findTileForPylon() {
		Unit builder = Constructing.getRandomWorker();

		// // ### VERSION ### Expansion with cannons
		// if (BotStrategyManager.isExpandWithCannons()) {
		// if (UnitCounter.getNumberOfUnits(buildingType) == 1) {
		// return findTileForFirstPylonAtBase(builder,
		// ProtossNexus.getSecondBaseLocation());
		// }
		// }

		// // It's not the first pylon
		// if (UnitCounter.weHavePylonFinished()) {
		// if (UnitCounter.getNumberOfPylons() == 1 && XVR.isEnemyProtoss()) {
		// MapPoint tile = findTileForFirstPylonAtBase(builder,
		// ProtossNexus.getSecondBaseLocation());
		// if (tile != null) {
		// return tile;
		// }
		// }
		// return findTileForNextPylon(builder);
		// }
		if (UnitCounter.weHavePylonFinished()) {
			if (UnitCounter.getNumberOfPylons() == 1) {
				MapPoint tile = findTileForFirstPylonAtBase(builder,
						ProtossNexus.getSecondBaseLocation());
				if (tile != null) {
					return tile;
				}
			}
			return findTileForNextPylon(builder);
		}

		// It's the first pylon
		else {
			return findTileForFirstPylon(builder, xvr.getFirstBase());
			// return findTileForFirstPylonAtBase(builder,
			// ProtossNexus.getSecondBaseLocation());
		}
	}

	private static MapPoint findTileForNextPylon(Unit builder) {

		// If we have more than one base make sure that every base has at
		// least one pylon nearby
		for (Unit base : xvr.getUnitsOfType(UnitManager.BASE)) {
			int nearbyPylons = xvr.countUnitsOfGivenTypeInRadius(buildingType,
					14, base.getX(), base.getY(), true);

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
			return findTileNearPylonForNewBuilding(buildingType);
		}

		// Either build randomly near base
		if (UnitCounter.getNumberOfPylons() >= 10 && RUtilities.rand(0, 3) == 0) {

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
		Unit pylon = null;
		if (UnitCounter.getNumberOfPylons() > 5) {
			pylon = getRandomPylon();
		} else {
			ArrayList<Unit> pylonsNearMainBase = xvr
					.getUnitsOfGivenTypeInRadius(buildingType, 14,
							xvr.getFirstBase(), true);
			if (!pylonsNearMainBase.isEmpty()) {
				pylon = (Unit) RUtilities.getRandomElement(pylonsNearMainBase);
			}
			if (pylon == null) {
				pylon = getRandomPylon();
			}
		}
		
		MapPoint tile = findTileForPylonNearby(pylon,
				PYLON_FROM_PYLON_MIN_DISTANCE, PYLON_FROM_PYLON_MAX_DISTANCE);
		if (tile != null) {
			return tile;
		} else {
			return findTileNearPylonForNewBuilding(buildingType);
		}

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
						//
						// && Constructing.isBuildTileFullyBuildableFor(
						// builder.getID(), i, j,
						// buildingType.ordinal()))
						MapPointInstance point = new MapPointInstance(x, y);
						if (!Constructing.isTooNearMineralAndBase(point)) {

							// Damn, try NOT to build in the middle of narrow
							// choke
							// point.
							if (!Constructing.isTooCloseToAnyChokePoint(point)) {
								return point;
							}
						}
					}
				}
			}

			currentDist++;
		}
		return null;
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

	// ### Version ### build cannons at second base
	private static MapPoint findTileForFirstPylonAtBase(Unit builder,
			MapPoint base) {
		if (base == null) {
			return null;
		}

		// Change first base to second base.
		// base = ProtossNexus.findTileForBase(true);
		base = ProtossNexus.getSecondBaseLocation();
		if (base == null) {
			return null;
		}

		// Find point being in the middle of way second base<->nearest choke
		// point.
		ChokePoint choke = MapExploration.getNearestChokePointFor(base);
		if (choke == null) {
			return null;
		}

		// MapPointInstance location = new MapPointInstance(
		// (base.getX() + 2 * choke.getX()) / 3,
		// (base.getY() + 2 * choke.getY()) / 3);
		MapPointInstance location = MapPointInstance.getMiddlePointBetween(
				base, choke);

		// Find place for pylon between choke point and the second base.
		return Constructing.getLegitTileToBuildNear(builder, buildingType,
				location, 0, 100, false);
	}

	// ### Version ### normal
	private static MapPoint findTileForFirstPylon(Unit builder, Unit base) {
		if (base == null) {
			return null;
		}

		// Find point being in the middle of way base<->nearest choke point.
		ChokePoint choke = MapExploration.getNearestChokePointFor(base);
		MapPointInstance location = new MapPointInstance(
				(2 * base.getX() + choke.getCenterX()) / 3,
				(2 * base.getY() + choke.getCenterY()) / 3);
		// System.out.println();
		// System.out.println(choke.toStringLocation());
		// System.out.println(location.toStringLocation());

		return Constructing.getLegitTileToBuildNear(builder, buildingType,
				location, 0, 100, false);
	}

	public static UnitTypes getBuildingType() {
		return buildingType;
	}

}
