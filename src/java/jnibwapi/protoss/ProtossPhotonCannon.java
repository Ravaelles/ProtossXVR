package jnibwapi.protoss;

import java.awt.Point;

import jnibwapi.XVR;
import jnibwapi.model.ChokePoint;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.xvr.Constructing;
import jnibwapi.xvr.MapExploration;
import jnibwapi.xvr.UnitCounter;
import jnibwapi.xvr.UnitManager;
import jnibwapi.xvr.WorkerManager;

public class ProtossPhotonCannon {

	private static final UnitTypes buildingType = UnitTypes.Protoss_Photon_Cannon;
	private static XVR xvr = XVR.getInstance();

	private static final double MAX_DIST_FROM_CHOKE_POINT_MODIFIER = 1.9;
	// private static final int MAX_DIST_FROM_CHOKE_POINT = 1;
	// private static final int MAX_DIST_FROM_OTHER_CANNON = 5;
	private static final int MAX_CANNON_STACK = 4;

	private static ChokePoint _chokePointToReinforce = null;

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
				if (shouldBuildFor(base)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean shouldBuildFor(Unit base) {

		// Get the nearest choke point to base
		ChokePoint chokePoint = MapExploration.getNearestChokePointFor(base);

		// If in the neighborhood of choke point there's too many cannons, don't
		// build next one.
		if (shouldBuildFor(chokePoint)) {
			_chokePointToReinforce = chokePoint;
			return true;
		} else {
			return false;
		}
	}

	public static void buildIfNecessary() {
		if (shouldBuild()) {
			for (Unit base : ProtossNexus.getBases()) {
				tryToBuildFor(base);
			}
		}
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

	private static int calculateCannonsNearby(ChokePoint chokePoint) {
		int searchInDistance = (int) (1.7 * MAX_DIST_FROM_CHOKE_POINT_MODIFIER
				* chokePoint.getRadius() / 32);
		int numberOfCannonsNearby = xvr.getUnitsOfGivenTypeInRadius(
				buildingType, searchInDistance, chokePoint.getCenterX(),
				chokePoint.getCenterY(), true).size();
		return numberOfCannonsNearby;
	}

	private static Point findProperBuildTile(ChokePoint choke,
			boolean requiresPower) {

		// Define approximate bunker tile
		Point initialBuildTile = new Point(choke.getCenterX() / 32,
				choke.getCenterY() / 32);

		// Define initial worker
		Unit workerUnit = WorkerManager.findNearestWorkerTo(initialBuildTile.x,
				initialBuildTile.y);

		// Define maximum distance from a choke point for a cannon
		int minimumDistance = 6;
		int numberOfCannonsNearby = calculateCannonsNearby(choke);

		if (choke.getRadius() / 32 >= 8) {
			minimumDistance -= 3;
		}
		int maximumDistance = minimumDistance
				+ (12 / (numberOfCannonsNearby + 1));
		// int maxDistanceBasedOnChokePointRadius = (int) (choke.getRadius()
		// * MAX_DIST_FROM_CHOKE_POINT_MODIFIER / 32);
		// int maxDistanceBasedOnDistanceFromChokePoint = 7;
		// int maximumDistance = Math.max(maxDistanceBasedOnChokePointRadius,
		// maxDistanceBasedOnDistanceFromChokePoint);

		// Get proper build tile
		Point properBuildTile = Constructing.getLegitTileToBuildNear(
				workerUnit, buildingType, initialBuildTile.x,
				initialBuildTile.y, minimumDistance, maximumDistance,
				requiresPower);

		return properBuildTile;
	}

	public static Point findTileForCannon() {
		// return findProperBuildTile(_chokePointToReinforce, true);
		if (_chokePointToReinforce == null) {
			_chokePointToReinforce = MapExploration.getNearestChokePointFor(xvr
					.getFirstBase());
		}

		// Try to find normal tile.
		Point tileForCannon = findProperBuildTile(_chokePointToReinforce, true);
		if (tileForCannon != null) {
			return tileForCannon;
		}

		return null;
	}

	public static UnitTypes getBuildingType() {
		return buildingType;
	}

}
