package ai.protoss;

import java.awt.Point;

import jnibwapi.model.ChokePoint;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;
import ai.handling.constructing.Constructing;
import ai.handling.constructing.ShouldBuildCache;
import ai.handling.map.MapExploration;
import ai.handling.map.MapPoint;
import ai.handling.units.UnitCounter;
import ai.managers.UnitManager;
import ai.managers.WorkerManager;
import ai.utils.RUtilities;

public class ProtossPhotonCannon {

	private static final UnitTypes buildingType = UnitTypes.Protoss_Photon_Cannon;
	private static XVR xvr = XVR.getInstance();

	private static final double MAX_DIST_FROM_CHOKE_POINT_MODIFIER = 1.8;
	// private static final int MAX_DIST_FROM_CHOKE_POINT = 1;
	// private static final int MAX_DIST_FROM_OTHER_CANNON = 5;
	private static final int MAX_CANNON_STACK = 4;

	private static MapPoint _placeToReinforceWithCannon = null;

	// private static boolean _forcedPylonConstruction = false;

	// private static final int MAX_DIST_FROM_BASE = 30;

	public static boolean shouldBuild() {
		if (UnitCounter.weHaveBuilding(UnitTypes.Protoss_Forge)) {

			int cannons = UnitCounter.getNumberOfUnits(buildingType);
			int bases = UnitCounter.getNumberOfUnits(UnitManager.BASE);
			int battleUnits = UnitCounter.getNumberOfBattleUnits();

			if (cannons == 2 && battleUnits <= 6) {
				return false;
			}

			if (cannons >= 4 * bases) {
				return xvr.canAfford(900) && cannons <= 7 * bases;
			}

			for (Unit base : ProtossNexus.getBases()) {

				// Dont check always, it consumes lot of calculations
				if (RUtilities.rand(0, 1) == 0
						|| UnitCounter.getNumberOfUnits(UnitManager.BASE) == 1) {
					if (shouldBuildFor(base)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean shouldBuildFor(Unit base) {
		if (base == null) {
			return false;
		}

		// Get the nearest choke point to base
		ChokePoint chokePoint = MapExploration.getImportantChokePointNear(base);

		// // If this is new base, try to force building of cannon here.
		// if (!base.equals(xvr.getFirstBase())) {
		// _placeToReinforceWithCannon = base;
		// return true;
		// }

		// If in the neighborhood of choke point there's too many cannons, don't
		// build next one.
		if (shouldBuildFor(chokePoint)) {
			_placeToReinforceWithCannon = chokePoint;
			return true;
		} else {
			return false;
		}
	}

	public static void buildIfNecessary() {
		if (shouldBuild()) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			for (Unit base : ProtossNexus.getBases()) {
				tryToBuildFor(base);
			}
		}
		ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
	}

	private static void tryToBuildFor(Unit base) {
		if (shouldBuildFor(base)) {
			Constructing.construct(xvr, buildingType);
		}
	}

	private static boolean shouldBuildFor(ChokePoint chokePoint) {
		int numberOfCannonsNearby = calculateCannonsNearby(chokePoint);

		// If there isn't too many cannons defending this choke point
		if (numberOfCannonsNearby < MAX_CANNON_STACK) {
			return true;
		}

		// No, there's too many cannons. Don't build next one.
		else {
			// System.out.println("TOO MANY CANNONS NEARBY");
			return false;
		}
	}

	private static int calculateCannonsNearby(MapPoint mapPoint) {
		int radius;

		ChokePoint choke = null;
		if (mapPoint instanceof ChokePoint) {
			choke = (ChokePoint) mapPoint;
			radius = (int) choke.getRadius() / 32;
		} else {
			radius = 8;
		}

		int searchInDistance = (int) (1.7 * MAX_DIST_FROM_CHOKE_POINT_MODIFIER * radius);
		if (searchInDistance < 9) {
			searchInDistance = 9;
		}
		
		int numberOfCannonsNearby = xvr.getUnitsOfGivenTypeInRadius(
				buildingType, searchInDistance, mapPoint, true).size();
		return numberOfCannonsNearby;
	}

	private static MapPoint findProperBuildTile(MapPoint mapPoint,
			boolean requiresPower) {

		// Define approximate bunker tile
		Point initialBuildTile = new Point(mapPoint.getX() / 32,
				mapPoint.getY() / 32);

		// Define initial worker
		Unit workerUnit = WorkerManager.findNearestWorkerTo(initialBuildTile.x,
				initialBuildTile.y);

		// Define maximum distance from a choke point for a cannon
		int minimumDistance = 6;
		int numberOfCannonsNearby = calculateCannonsNearby(mapPoint);

		if (mapPoint instanceof ChokePoint) {
			ChokePoint choke = (ChokePoint) mapPoint;
			if (choke.getRadius() / 32 >= 8) {
				minimumDistance -= 5;
			}
		}
		int maximumDistance = minimumDistance
				+ (12 / (numberOfCannonsNearby + 1));
		// int maxDistanceBasedOnChokePointRadius = (int) (choke.getRadius()
		// * MAX_DIST_FROM_CHOKE_POINT_MODIFIER / 32);
		// int maxDistanceBasedOnDistanceFromChokePoint = 7;
		// int maximumDistance = Math.max(maxDistanceBasedOnChokePointRadius,
		// maxDistanceBasedOnDistanceFromChokePoint);

		// Get proper build tile
		MapPoint properBuildTile = Constructing.getLegitTileToBuildNear(
				workerUnit, buildingType, initialBuildTile.x,
				initialBuildTile.y, minimumDistance, maximumDistance,
				requiresPower);

		return properBuildTile;
	}

	public static MapPoint findTileForCannon() {
		// return findProperBuildTile(_chokePointToReinforce, true);
		if (_placeToReinforceWithCannon == null) {
			_placeToReinforceWithCannon = MapExploration
					.getNearestChokePointFor(xvr.getFirstBase());
		}

		// Try to find normal tile.
		MapPoint tileForCannon = findProperBuildTile(_placeToReinforceWithCannon,
				true);
		if (tileForCannon != null) {
			return tileForCannon;
		}

		return null;
	}

	public static UnitTypes getBuildingType() {
		return buildingType;
	}

}
