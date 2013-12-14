package ai.protoss;

import java.util.ArrayList;

import jnibwapi.model.ChokePoint;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;
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

public class ProtossPhotonCannon {

	private static final UnitTypes buildingType = UnitTypes.Protoss_Photon_Cannon;
	private static XVR xvr = XVR.getInstance();

	private static final double MAX_DIST_FROM_CHOKE_POINT_MODIFIER = 1.8;
	public static final int MAX_CANNON_STACK = 4;

	private static MapPoint _placeToReinforceWithCannon = null;

	public static boolean shouldBuild() {
		if (UnitCounter.weHaveBuilding(UnitTypes.Protoss_Forge)) {
			int maxCannonStack = calculateMaxCannonStack();

			int cannons = UnitCounter.getNumberOfUnits(buildingType);
			// int bases = UnitCounter.getNumberOfUnits(UnitManager.BASE);
			int pylons = UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Pylon);
			// int battleUnits = UnitCounter.getNumberOfBattleUnits();

			if (pylons == 1) {
				if (!xvr.canAfford(300)) {
					return false;
				}
			}

			if (cannons <= maxCannonStack
					&& ProtossPylon.calculateExistingPylonsStrength() >= 1.35
					&& calculateExistingCannonsStrength() < maxCannonStack) {
//				 System.out.println("FIRST CASE");
				return true;
			}

			// if (cannons == 2 && battleUnits <= 6) {
			// return false;
			// }

			// if (cannons >= 4 * bases) {
			// return xvr.canAfford(1200) && cannons <= 7 * bases;
			// }

			// Select one place to reinforce
			for (MapPoint base : getPlacesToReinforce()) {
				if (UnitCounter.getNumberOfUnits(UnitManager.BASE) == 1) {
					if (shouldBuildFor((MapPoint) base)) {
//						 System.out.println("#SECOND");
						return true;
					}
				}
			}

			// If main base isn't protected at all, build some cannons
			// if (RUtilities.rand(0, 10) == 0) {
			// int cannonsNearMainBase = xvr.countUnitsOfGivenTypeInRadius(
			// UnitTypes.Protoss_Photon_Cannon, 10,
			// xvr.getFirstBase(), true);
			// if (cannons >= ProtossPhotonCannon.MAX_CANNON_STACK * 1.5
			// && UnitCounter
			// .getNumberOfUnits(UnitTypes.Protoss_Gateway) >= 2
			// && cannonsNearMainBase <= 2) {
			// return true;
			// }
			// }

			// If reached here, then check if build cannon at next base
			MapPoint tileForNextBase = ProtossNexus.getTileForNextBase(false);
			if (shouldBuildFor(tileForNextBase)) {
				if (xvr.countUnitsOfGivenTypeInRadius(UnitTypes.Protoss_Pylon,
						12, tileForNextBase, true) > 0) {
					return true;
				}
			}
		}
		return false;
	}

	private static double calculateExistingCannonsStrength() {
		double result = 0;
		UnitType type = UnitType
				.getUnitTypeByID(UnitTypes.Protoss_Photon_Cannon.ordinal());
		int maxHitPoints = type.getMaxShields() + type.getMaxHitPoints();

		for (Unit cannon : xvr.getUnitsOfType(buildingType)) {
			double cannonTotalHP = (double) (cannon.getShields() + cannon
					.getHitPoints()) / maxHitPoints;
			if (!cannon.isCompleted()) {
				cannonTotalHP = Math.sqrt(cannonTotalHP);
			}
			result += cannonTotalHP;
		}

		return result;
	}

	private static boolean shouldBuildFor(MapPoint base) {
		if (base == null) {
			return false;
		}

		// Build just at second base
		if (base.equals(xvr.getFirstBase())) {
			return false;
		}

		// Build at first base
		// if (UnitCounter.getNumberOfUnits(UnitManager.BASE) >= 2) {
		// if (base.equals(xvr.getFirstBase())) {
		// return false;
		// }
		// }

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
			for (MapPoint base : getPlacesToReinforce()) {
				tryToBuildFor(base);
			}
		}
		ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
	}

	private static ArrayList<MapPoint> getPlacesToReinforce() {
		ArrayList<MapPoint> placesToReinforce = new ArrayList<>();

		// Second base should be one huge defensive bunker.
		placesToReinforce.add(ProtossNexus.getSecondBaseLocation());

		// Add bases from newest, to the oldest (I guess?)
		ArrayList<Unit> bases = ProtossNexus.getBases();
		for (int i = bases.size() - 1; i >= 0; i--) {
			placesToReinforce.add(bases.get(i));
		}

		return placesToReinforce;
	}

	private static void tryToBuildFor(MapPoint base) {
		if (shouldBuildFor(base)) {
			Constructing.construct(xvr, buildingType);
		}
	}

	private static boolean shouldBuildFor(ChokePoint chokePoint) {
		// return findTileForCannon() != null;
		if (chokePoint.isDisabled()) {
			return false;
		}

		int numberOfCannonsNearby = calculateCannonsNearby(chokePoint);

		int bonus = 0;
		if (xvr.getDistanceBetween(ProtossNexus.getSecondBaseLocation(),
				chokePoint) < 14) {
			// if (!xvr.getFirstBase().equals(
			// ProtossNexus.getNearestBaseForUnit(chokePoint))) {
			bonus = 1;
		}

		// If there isn't too many cannons defending this choke point
		if (numberOfCannonsNearby < calculateMaxCannonStack() + bonus) {
			return true;
		}

		// No, there's too many cannons. Don't build next one.
		else {
			// System.out.println("TOO MANY CANNONS NEARBY");
			return false;
		}
	}

	public static int calculateMaxCannonStack() {
		return BotStrategyManager.isExpandWithCannons() ? MAX_CANNON_STACK
				: (UnitCounter.getNumberOfBattleUnits() >= 8 ? 1 : MAX_CANNON_STACK);
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

		int searchInDistance = (int) (1.5 * MAX_DIST_FROM_CHOKE_POINT_MODIFIER * radius);
		if (searchInDistance < 9) {
			searchInDistance = 9;
		}

		ArrayList<Unit> cannonsNearby = xvr.getUnitsOfGivenTypeInRadius(
				buildingType, searchInDistance, mapPoint, true);

		double result = 0;
		double maxCannonHP = 200;
		for (Unit cannon : cannonsNearby) {
			// if (!cannon.isCompleted()) {
			// result -= 1;
			// }
			result += (cannon.getHitPoints() + cannon.getShields())
					/ maxCannonHP;
		}

		return (int) result;
	}

	private static MapPoint findProperBuildTile(MapPoint mapPoint,
			boolean requiresPower) {

		// Define approximate tile for cannon
		MapPointInstance initialBuildTile = new MapPointInstance(
				mapPoint.getX(), mapPoint.getY());

		// Define random worker, for technical reasons
		Unit workerUnit = xvr.getRandomWorker();

		// ================================
		// Define maximum distance from a choke point for a cannon
		int minimumDistance = 1;
		int numberOfCannonsNearby = calculateCannonsNearby(mapPoint);
		if (mapPoint instanceof ChokePoint) {
			ChokePoint choke = (ChokePoint) mapPoint;
			if (choke.getRadius() / 32 >= 8) {
				minimumDistance = 3;
			}
		}
		int maximumDistance = minimumDistance
				+ (10 / Math.max(1, numberOfCannonsNearby));

		// ================================
		// Find proper build tile
		MapPoint properBuildTile = Constructing.getLegitTileToBuildNear(
				workerUnit, buildingType, initialBuildTile, minimumDistance,
				maximumDistance, requiresPower);

		return properBuildTile;
	}

	public static MapPoint findTileForCannon() {

		// return findProperBuildTile(_chokePointToReinforce, true);
		if (_placeToReinforceWithCannon == null) {
			_placeToReinforceWithCannon = MapExploration
					.getNearestChokePointFor(getInitialPlaceToReinforce());
		}

		// Try to find normal tile.
		MapPoint tileForCannon = findProperBuildTile(
				_placeToReinforceWithCannon, true);
		if (tileForCannon != null) {
			return tileForCannon;
		}

		// ===================
		// If main base isn't protected at all, build some cannons
		// if (UnitCounter.getNumberOfUnits(buildingType) <= 1) {
		// Unit firstBase = xvr.getFirstBase();
		// int cannonsNearMainBase = xvr.countUnitsOfGivenTypeInRadius(
		// UnitTypes.Protoss_Photon_Cannon, 12, firstBase, true);
		// if (cannonsNearMainBase < 1) {
		//
		// MapPoint point = MapPointInstance.getTwoThirdPointBetween(
		// firstBase,
		// MapExploration.getImportantChokePointNear(firstBase));
		//
		// tileForCannon = Constructing
		// .getLegitTileToBuildNear(xvr.getRandomWorker(),
		// buildingType, point, 0, 10, true);
		// if (tileForCannon != null) {
		// return tileForCannon;
		// }
		// }
		// }

		// ===================
		// If we're here it can mean we should build cannons at position of the
		// next base
		MapPoint tileForNextBase = ProtossNexus.getTileForNextBase(false);
		if (shouldBuildFor(tileForNextBase)) {
			tileForCannon = findProperBuildTile(tileForNextBase, true);
			if (tileForCannon != null) {
				return tileForCannon;
			}
		}

		return null;
	}

	private static MapPoint getInitialPlaceToReinforce() {
		return ProtossNexus.getSecondBaseLocation();
		// return xvr.getFirstBase();
	}

	public static UnitTypes getBuildingType() {
		return buildingType;
	}

}
