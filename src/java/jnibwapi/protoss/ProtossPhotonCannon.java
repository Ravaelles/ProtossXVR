package jnibwapi.protoss;

import java.awt.Point;

import jnibwapi.XVR;
import jnibwapi.model.ChokePoint;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

public class ProtossPhotonCannon {

	private static final UnitTypes buildingType = UnitTypes.Protoss_Photon_Cannon;
	private static XVR xvr = XVR.getInstance();

	private static final double MAX_DIST_FROM_CHOKE_POINT_MODIFIER = 1.9;
	// private static final int MAX_DIST_FROM_CHOKE_POINT = 1;
	// private static final int MAX_DIST_FROM_OTHER_CANNON = 5;
	private static final int MAX_CANNON_STACK = 4;

	private static ChokePoint _chokePointToReinforce = null;

	// private static final int MAX_DIST_FROM_BASE = 30;

	public static boolean shouldBuild() {
		if (UnitCounter.weHaveBuilding(UnitTypes.Protoss_Gateway)
				&& xvr.canAfford(100)) {

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
		int searchInDistance = (int) (1.3 * MAX_DIST_FROM_CHOKE_POINT_MODIFIER
				* chokePoint.getRadius() / 32);
		int numberOfCannonsNearby = xvr.getUnitsOfGivenTypeInRadius(
				buildingType, searchInDistance, chokePoint.getCenterX(),
				chokePoint.getCenterY(), true).size();

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

	private static Point findProperBuildTile(ChokePoint choke,
			boolean requiresPower) {

		// Define approximate bunker tile
		Point initialBuildTile = new Point(choke.getCenterX() / 32,
				choke.getCenterY() / 32);

		// Define initial worker
		Unit workerUnit = WorkerManager.findNearestWorkerTo(initialBuildTile.x,
				initialBuildTile.y);
		
		// Define maximum distance from a choke point for a cannon
		int maxDistanceBasedOnChokePointRadius = (int) (choke.getRadius()
				* MAX_DIST_FROM_CHOKE_POINT_MODIFIER / 32);
		int maxDistanceBasedOnDistanceFromChokePoint = 7;
		int maximumDistance = Math.max(maxDistanceBasedOnChokePointRadius,
				maxDistanceBasedOnDistanceFromChokePoint);

		// Get proper build tile
		Point properBuildTile = Constructing.getLegitTileToBuildNear(
				workerUnit, buildingType, initialBuildTile.x,
				initialBuildTile.y, 1, maximumDistance,
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

		// There's a pylon which can power this tile for cannon.
		if (tileForCannon != null) {
			return tileForCannon;
		}

		// FIX: Force constructing a pylon nearby first
		else {
			if (shouldBuildFor(_chokePointToReinforce)) {
				Point tileForPylon = findProperBuildTile(
						_chokePointToReinforce, false);
				if (tileForPylon != null) {
					Constructing.forceConstructionAt(
							ProtossPylon.getBuildingtype(), tileForPylon);
				}
			}
			return null;
		}
	}

	public static UnitTypes getBuildingType() {
		return buildingType;
	}

}
