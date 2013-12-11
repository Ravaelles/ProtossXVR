package ai.handling.constructing;

import java.util.ArrayList;
import java.util.HashMap;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.ChokePoint;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;
import ai.handling.map.MapExploration;
import ai.handling.map.MapPoint;
import ai.handling.map.MapPointInstance;
import ai.handling.units.UnitActions;
import ai.handling.units.UnitCounter;
import ai.managers.UnitManager;
import ai.protoss.ProtossArbiterTribunal;
import ai.protoss.ProtossAssimilator;
import ai.protoss.ProtossCitadelOfAdun;
import ai.protoss.ProtossCyberneticsCore;
import ai.protoss.ProtossFleetBeacon;
import ai.protoss.ProtossForge;
import ai.protoss.ProtossGateway;
import ai.protoss.ProtossNexus;
import ai.protoss.ProtossObservatory;
import ai.protoss.ProtossPhotonCannon;
import ai.protoss.ProtossPylon;
import ai.protoss.ProtossRoboticsFacility;
import ai.protoss.ProtossRoboticsSupportBay;
import ai.protoss.ProtossShieldBattery;
import ai.protoss.ProtossStargate;
import ai.protoss.ProtossTemplarArchives;

public class Constructing {

	private static final int MIN_DIST_FROM_CHOKE_POINT = 5;

	private static XVR xvr = XVR.getInstance();

	private static HashMap<UnitTypes, Unit> _recentConstructionsInfo = new HashMap<>();
	private static int _recentConstructionsCounter = 0;
	private static int _actCounter = 0;

	private static int iIncStep;
	private static int jIncStep;

	// private static final int minDist = 7;

	// private static final int maxDist = 70;

	public static void act() {
		_actCounter++;
		if (_actCounter >= 3) {
			_actCounter = 0;
		}

		// Store info about constructing given building for 3 acts, then
		// remove all data
		if (_recentConstructionsCounter++ >= 3) {
			resetInfoAboutConstructions();
		}

		// Check only every N seconds
		if (_actCounter == 0) {
			ProtossNexus.buildIfNecessary();
			ProtossRoboticsFacility.buildIfNecessary();
			ProtossCyberneticsCore.buildIfNecessary();
			ProtossRoboticsSupportBay.buildIfNecessary();
			ProtossArbiterTribunal.buildIfNecessary();
			ProtossGateway.buildIfNecessary();
		} else if (_actCounter == 1) {
			ProtossPhotonCannon.buildIfNecessary();
			ProtossTemplarArchives.buildIfNecessary();
			ProtossGateway.buildIfNecessary();
			ProtossObservatory.buildIfNecessary();
			ProtossAssimilator.buildIfNecessary();
			ProtossCitadelOfAdun.buildIfNecessary();
		} else {
			ProtossPhotonCannon.buildIfNecessary();
			ProtossPylon.buildIfNecessary();
			ProtossStargate.buildIfNecessary();
			ProtossForge.buildIfNecessary();
			ProtossShieldBattery.buildIfNecessary();
		}
	}

	private static MapPoint getTileAccordingToBuildingType(UnitTypes building) {

		// Pylon
		if (UnitTypes.Protoss_Pylon.ordinal() == building.ordinal()) {
			return ProtossPylon.findTileForPylon();
		}

		// Photon Cannon
		else if (UnitTypes.Protoss_Photon_Cannon.ordinal() == building
				.ordinal()) {
			return ProtossPhotonCannon.findTileForCannon();
		}

		// Assimilator
		else if (UnitTypes.Protoss_Assimilator.ordinal() == building.ordinal()) {
			return findTileForAssimilator();
		}

		// Base
		else if (UnitManager.BASE.ordinal() == building.ordinal()) {
			return ProtossNexus.getTileForNextBase(false);
		}

		// Standard building
		else {

			// Let the Pylon class decide where to put buildings like Gateway,
			// Stargate etc.
			return ProtossPylon.findTileNearPylonForNewBuilding(building);

			// // Get random worker, it doesn't matter now which one, because we
			// // will decide later. But for now we have to specify something.
			// Unit workerUnit = xvr.getRandomWorker();
			// if (workerUnit == null) {
			// return null;
			// } else {
			// return Constructing.findBuildTile(xvr, workerUnit.getID(),
			// building.ordinal(), buildInNeighborhoodOf.getX(),
			// buildInNeighborhoodOf.getY());
			// }
		}
	}

	/**
	 * @return if we need to build some building it will return non-null value,
	 *         being int array containing three elements: first is total amount
	 *         of minerals required all buildings that we need to build, while
	 *         second is total amount of gas required and third returns total
	 *         number of building types that we want to build. If we don't need
	 *         to build anything right now it returns null
	 * */
	public static int[] shouldBuildAnyBuilding() {
		int mineralsRequired = 0;
		int gasRequired = 0;
		int buildingsToBuildTypesNumber = 0;
		if (ProtossNexus.shouldBuild()) {
			mineralsRequired += 400;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired,
					buildingsToBuildTypesNumber };
		}
		if (ProtossTemplarArchives.shouldBuild()) {
			mineralsRequired += 150;
			gasRequired += 200;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired,
					buildingsToBuildTypesNumber };
		}
		if (ProtossRoboticsFacility.shouldBuild()) {
			mineralsRequired += 200;
			gasRequired += 200;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired,
					buildingsToBuildTypesNumber };
		}
		if (ProtossRoboticsSupportBay.shouldBuild()) {
			mineralsRequired += 150;
			gasRequired += 100;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired,
					buildingsToBuildTypesNumber };
		}
		if (ProtossGateway.shouldBuild()) {
			mineralsRequired += 150;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired,
					buildingsToBuildTypesNumber };
		}
		// if (TerranBunker.shouldBuild()
		// && UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Bunker) < 2) {
		// mineralsRequired += 100;
		// buildingsToBuildTypesNumber++;
		// }
		if (ProtossAssimilator.shouldBuild()) {
			mineralsRequired += 100;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired,
					buildingsToBuildTypesNumber };
		}
		if (ProtossPylon.shouldBuild()) {
			mineralsRequired += 100;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired,
					buildingsToBuildTypesNumber };
		}
		if (ProtossForge.shouldBuild()) {
			mineralsRequired += 150;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired,
					buildingsToBuildTypesNumber };
		}
		if (ProtossCyberneticsCore.shouldBuild()) {
			mineralsRequired += 150;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired,
					buildingsToBuildTypesNumber };
		}
		if (ProtossCitadelOfAdun.shouldBuild()) {
			mineralsRequired += 150;
			gasRequired += 100;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired,
					buildingsToBuildTypesNumber };
		}
		if (ProtossObservatory.shouldBuild()) {
			mineralsRequired += 50;
			gasRequired += 100;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired,
					buildingsToBuildTypesNumber };
		}
		if (ProtossArbiterTribunal.shouldBuild()) {
			mineralsRequired += 200;
			gasRequired += 150;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired,
					buildingsToBuildTypesNumber };
		}
		if (ProtossFleetBeacon.shouldBuild()) {
			mineralsRequired += 300;
			gasRequired += 200;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired,
					buildingsToBuildTypesNumber };
		}
		if (ProtossShieldBattery.shouldBuild()) {
			mineralsRequired += 100;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired,
					buildingsToBuildTypesNumber };
		}

		if (buildingsToBuildTypesNumber > 0) {
			return new int[] { mineralsRequired + 8, gasRequired,
					buildingsToBuildTypesNumber };
		} else {
			return null;
		}
	}

	private static void resetInfoAboutConstructions() {
		_recentConstructionsCounter = 0;
		_recentConstructionsInfo.clear();
	}

	private static void addInfoAboutConstruction(UnitTypes building,
			Unit builder) {
		_recentConstructionsCounter = 0;
		_recentConstructionsInfo.put(building, builder);
		ShouldBuildCache.cacheShouldBuildInfo(building, false);
	}

	public static MapPoint findBuildTile(XVR xvr, int builderID,
			UnitTypes type, MapPoint place) {
		return findBuildTile(xvr, builderID, type.ordinal(), place.getX(),
				place.getY());
	}

	public static MapPoint findBuildTile(XVR xvr, int builderID,
			int buildingTypeID, int x, int y) {

		// Point pointToBuild = getLegitTileToBuildNear(builderID,
		// buildingTypeID, tileX, tileY, minDist, maxDist);
		MapPoint tileToBuild = ProtossPylon
				.findTileNearPylonForNewBuilding(UnitType
						.getUnitTypesByID(buildingTypeID));

		if (tileToBuild == null) {
			JNIBWAPI bwapi = xvr.getBwapi();
			bwapi.printText("Unable to find tile for new "
					+ bwapi.getUnitType(buildingTypeID).getName());
		}
		return tileToBuild;
	}

	public static MapPoint findTileForAssimilator() {
		Unit nearestGeyser = xvr.getUnitNearestFromList(xvr.getFirstBase(),
				xvr.getGeysersUnits());
		if (nearestGeyser != null
				&& xvr.getUnitsOfGivenTypeInRadius(UnitManager.BASE, 15,
						nearestGeyser, true).isEmpty()) {
			return null;
		}

		// return new MapPointInstance(nearestGeyser.getX(),
		// nearestGeyser.getY());
		if (nearestGeyser != null) {
			return new MapPointInstance(nearestGeyser.getX() - 64,
					nearestGeyser.getY() - 32);
		} else {
			return null;
		}
	}

	public static MapPoint getLegitTileToBuildNear(Unit worker, UnitTypes type,
			MapPoint nearTo, int minimumDist, int maximumDist,
			boolean requiresPower) {
		if (worker == null || type == null) {
			return null;
		}
		return getLegitTileToBuildNear(worker.getID(), type.ordinal(),
				nearTo.getTx(), nearTo.getTy(), minimumDist, maximumDist,
				requiresPower);
	}

	public static MapPoint getLegitTileToBuildNear(Unit worker, UnitTypes type,
			int tileX, int tileY, int minimumDist, int maximumDist,
			boolean requiresPower) {
		if (worker == null || type == null) {
			return null;
		}
		return getLegitTileToBuildNear(worker.getID(), type.ordinal(), tileX,
				tileY, minimumDist, maximumDist, requiresPower);
	}

	public static MapPoint getLegitTileToBuildNear(int builderID,
			int buildingTypeID, int tileX, int tileY, int minimumDist,
			int maximumDist, boolean requiresPower) {
		JNIBWAPI bwapi = XVR.getInstance().getBwapi();
		boolean canTemperWithIJ = true;
		if (!UnitType.getUnitTypeByID(buildingTypeID).isOnGeyser()) {
			canTemperWithIJ = false;
		} else {
			UnitType type = UnitType.getUnitTypeByID(buildingTypeID);
			if (type.isPhotonCannon()) {
				iIncStep = 6;
				jIncStep = 7;
			} else {
				iIncStep = 1;
				jIncStep = 1;
			}
		}

		// If already can build here return this tile
		// if (bwapi.canBuildHere(builderID, tileX, tileY, buildingTypeID,
		// false)) {
		// if ((!requiresPower || bwapi.hasPower(tileX, tileY))
		// && isBuildTileFreeFromUnits(builderID, tileX, tileY)) {
		// return new Point(tileX, tileY);
		// }
		// }

		// int currentDist = minimumDist;
		// while (currentDist < maximumDist) {
		//
		// for (int attempt = 0; attempt < currentDist * 5; attempt++) {
		// int i = tileX + currentDist - RUtilities.rand(0, 2 * currentDist);
		// int j = tileY + currentDist - RUtilities.rand(0, 2 * currentDist);
		// if (bwapi.canBuildHere(builderID, i, j, buildingTypeID, false)) {
		// if (isBuildTileFreeFromUnits(builderID, i, j)) {
		// return new Point(i, j);
		// }
		// }
		// currentDist++;
		// }
		// }

		int currentDist = minimumDist;
		while (currentDist <= maximumDist) {
			for (int i = tileX - currentDist; i <= tileX + currentDist; i++) {
				for (int j = tileY - currentDist; j <= tileY + currentDist; j++) {
					if ((!requiresPower || bwapi.hasPower(i, j))
							&& bwapi.canBuildHere(builderID, i, j,
									buildingTypeID, false)
					// && isBuildTileFullyBuildableFor(builderID, i, j,
					// buildingTypeID)
					) {
						// && bwapi.canBuildHere(builderID, i, j,
						// buildingTypeID, false)
						// && bwapi.isBuildable(i, j, true)) {
						// if (isBuildTileFreeFromUnits(builderID, i, j)) {
						Unit optimalBuilder = xvr
								.getOptimalBuilder(new MapPointInstance(i * 32,
										j * 32));
						if (optimalBuilder != null
								&& isBuildTileFreeFromUnits(
										optimalBuilder.getID(), i, j)) {
							int x = i * 32;
							int y = j * 32;
							if (xvr.getDistanceBetween(MapExploration
									.getNearestChokePointFor(x, y), x, y) >= MIN_DIST_FROM_CHOKE_POINT) {
								return new MapPointInstance(x, y);
							}
						}
						// }
					}

					// End of j
					if (canTemperWithIJ && j % jIncStep == 0) {
						j++;
					}
				}

				// End of i
				if (canTemperWithIJ && i % iIncStep == 0) {
					i++;
				}
			}

			currentDist++;
		}

		return null;
	}

	private static boolean isBuildTileFullyBuildableFor(int builderID, int i,
			int j, int buildingTypeID) {
		UnitType buildingType = UnitType.getUnitTypeByID(buildingTypeID);
		int wHalf = buildingType.getTileWidth() / 2;
		int hHalf = buildingType.getTileHeight() / 2;
		for (int tx = i - wHalf; tx < i + wHalf; tx++) {
			for (int ty = j - hHalf; ty < j + hHalf; ty++) {
				if (!xvr.getBwapi().isBuildable(tx, ty, true)) {
					return false;
				}
			}
		}

		// if (UnitCounter.weHaveBuildingFinished(UnitTypes.Protoss_Pylon)) {
		// MapPoint tileForNextBase = ProtossNexus.getTileForNextBase(false);
		// if (tileForNextBase != null
		// && xvr.getDistanceBetween(tileForNextBase,
		// new MapPointInstance(i * 32, j * 32)) < 3) {
		// return false;
		// }
		// }

		return true;
	}

	// public static Point getBuildableTileNearestTo(int minimumDist,
	// int maximumDist, int tileX, int tileY, int builderID,
	// UnitTypes buildingType) {
	// boolean canTemperWithIJ = !buildingType.getType().isOnGeyser();
	//
	// int currentDist = minimumDist;
	// while (currentDist <= maximumDist) {
	// for (int i = tileX - currentDist; i <= tileX + currentDist; i++) {
	// for (int j = tileY - currentDist; j <= tileY + currentDist; j++) {
	// if (xvr.getBwapi().canBuildHere(builderID, i, j,
	// buildingType.ordinal(), false)) {
	// if (Constructing.isBuildTileFreeFromUnits(builderID, i,
	// j)) {
	// return new Point(i, j);
	// }
	//
	// // End of j
	// if (canTemperWithIJ && j % jStep == 0) {
	// j++;
	// }
	// }
	//
	// // End of i
	// if (canTemperWithIJ && i % iStep == 0) {
	// i++;
	// }
	// }
	// }
	//
	// if (maximumDist < 15) {
	// currentDist++;
	// } else {
	// if (currentDist > 4 && RUtilities.rand(0, 3) == 0) {
	// currentDist += 2;
	// } else {
	// currentDist++;
	// }
	// }
	// }
	// return null;
	// }

	public static boolean isBuildTileFreeFromUnits(int builderID, int tileX,
			int tileY) {
		JNIBWAPI bwapi = XVR.getInstance().getBwapi();
		MapPointInstance point = new MapPointInstance(tileX * 32, tileY * 32);

		// Check if units are blocking this tile
		boolean unitsInWay = false;
		for (Unit u : bwapi.getAllUnits()) {
			if (u.getID() == builderID) {
				continue;
			}
			if (xvr.getDistanceBetween(u, point) <= 3) {
				for (Unit unit : xvr.getUnitsInRadius(point, 4, xvr.getBwapi()
						.getMyUnits())) {
					UnitActions.moveAwayFromUnitIfPossible(unit, point, 6);
				}
				unitsInWay = true;
			}
		}
		if (!unitsInWay) {
			return true;
		}

		return false;
	}

	public static void construct(XVR xvr, UnitTypes building) {

		// Define tile where to build according to type of building.
		MapPoint buildTile = getTileAccordingToBuildingType(building);
		// System.out.println("buildTile FOR: " + building + " = " + buildTile);
		// Debug.message(xvr, "buildTile FOR: " + building + " = " + buildTile);

		// Check if build tile is okay.
		if (buildTile != null) {

			// If this building is base, make sure there's a pylon and at least
			// two cannons build nearby
			if (building.getType().isBase()
					&& UnitCounter
							.weHaveBuildingFinished(UnitTypes.Protoss_Forge)) {
				handleBaseConstruction(building, buildTile);
			} else {

				// Proper construction order
				constructBuilding(xvr, building, buildTile);
			}

		}
	}

	/** The idea is to build pylon and cannon first, just then build Nexus. */
	private static void handleBaseConstruction(UnitTypes building,
			MapPoint buildTile) {
		boolean baseInterrupted = false;

		// ==============================
		// Ensure there's pylon nearby
		ArrayList<Unit> pylons = xvr.getUnitsOfGivenTypeInRadius(
				UnitTypes.Protoss_Pylon, 13, buildTile, true);
		boolean pylonIsOkay = !pylons.isEmpty() && pylons.get(0).isCompleted();
		if (!pylonIsOkay) {
			baseInterrupted = true;
			building = UnitTypes.Protoss_Pylon;

			// Get the base location
			MapPoint base = ProtossNexus.getTileForNextBase(false);

			// Try to find proper choke to reinforce
			ChokePoint choke = MapExploration.getImportantChokePointNear(base);

			// Get point in between choke and base
			MapPointInstance point = MapPointInstance.getMiddlePointBetween(
					base, choke);

			// Refind proper place for pylon
			buildTile = getLegitTileToBuildNear(xvr.getRandomWorker(),
					building, point, 0, 15, true);
		}

		// ==============================
		// Ensure there's at least some cannon nearby
		int cannonsNearby = xvr.countUnitsOfGivenTypeInRadius(
				UnitTypes.Protoss_Photon_Cannon, 13, buildTile, true);
		if (pylonIsOkay && cannonsNearby < 0) {
			baseInterrupted = true;
			building = UnitTypes.Protoss_Photon_Cannon;

			buildTile = ProtossPhotonCannon.findTileForCannon();
		}

		// ==============================
		// We can build the base
		if (!baseInterrupted) {
			if (!Constructing.canBuildAt(buildTile, UnitManager.BASE)) {
				// System.out.println("TEST canBuildAt");
				buildTile = ProtossNexus.getTileForNextBase(true);
			}
			// System.out.println("BASE READY # pylonsNearby = " +
			// pylonsNearby
			// + ", cannonsNearby = " + cannonsNearby);
		}

		constructBuilding(xvr, building, buildTile);
	}

	private static boolean canBuildAt(MapPoint point, UnitTypes type) {
		return xvr.getBwapi().canBuildHere(xvr.getRandomWorker().getID(),
				point.getTx(), point.getTy(), type.getID(), false);
	}

	private static boolean constructBuilding(XVR xvr, UnitTypes building,
			MapPoint buildTile) {
		if (buildTile == null) {
			return false;
		}

		Unit workerUnit = xvr.getOptimalBuilder(buildTile);
		if (workerUnit != null) {

			// if we found a good build position, and we aren't already
			// constructing this building order our worker to build it
			// && (!xvr.weAreBuilding(building))
			if (buildTile != null) {
				// Debug.messageBuild(xvr, building);
				build(workerUnit, buildTile, building);

				// // If it's base then build pylon for new base
				// if (UnitType.getUnitTypeByID(building.getID()).isBase()) {
				// forceConstructionOfPylonNear(buildTile);
				// }
				return true;
			}
		}
		return false;
	}

	public static boolean weAreBuilding(UnitTypes type) {
		if (_recentConstructionsInfo.containsKey(type)) {
			return true;
		}
		for (Unit unit : xvr.getBwapi().getMyUnits()) {
			if ((!unit.isCompleted() && unit.getTypeID() == type.ordinal())
					|| unit.getBuildTypeID() == type.ordinal()) {
				return true;
			}
		}
		return false;
		// for (Unit unit : xvr.getWorkers()) {
		// if (unit.getBuildTypeID() == type.ordinal()) {
		// return true;
		// }
		// }
		// return false;

		// ### Version ### working fine (I guess) for Terran, Protoss Probes
		// almost
		// never construct

		// if (_recentConstructionsInfo.containsKey(type)) {
		// return true;
		// }
		// for (Unit unit : xvr.getBwapi().getMyUnits()) {
		// if ((unit.getTypeID() == type.ordinal()) && (!unit.isCompleted())) {
		// return true;
		// }
		// if (unit.getBuildTypeID() == type.ordinal()) {
		// return true;
		// }
		// }
		// return false;
	}

	private static void build(Unit builder, MapPoint buildTile,
			UnitTypes building) {
		// building.getType().isPhotonCannon() ||
		if (!weAreBuilding(building)) {
			xvr.getBwapi().build(builder.getID(), buildTile.getTx(),
					buildTile.getTy(), building.ordinal());
			// if (Unit.getByID(worker).isConstructing()) {
			addInfoAboutConstruction(building, builder);
			// removeDuplicateConstructionsPending(builder);
			// }

			// System.out.println("DISTANCE UNITS: " + xvr.getDistanceBetween(
			// xvr.getFirstBase(), buildTile.x * 32, buildTile.y * 32));
		}
	}

	// public static void removeDuplicateConstructionsPending(
	// Unit onlyAllowedBuilder) {
	// // UnitType buildingType =
	// // UnitType.getUnitTypeByID(onlyAllowedBuilder.getBuildTypeID());
	// for (Unit otherBuilder : xvr.getWorkers()) {
	// if (otherBuilder.isConstructing()
	// && onlyAllowedBuilder.getBuildTypeID() == otherBuilder
	// .getBuildTypeID()
	// && !otherBuilder.equals(onlyAllowedBuilder)) {
	// xvr.getBwapi().cancelConstruction(otherBuilder.getID());
	// }
	// }
	// }

	public static Unit getRandomWorker() {
		return xvr.getRandomWorker();
	}

	public static boolean canBuildHere(Unit builder, UnitTypes buildingType,
			int tx, int ty) {
		return xvr.getBwapi().canBuildHere(builder.getID(), tx, ty,
				buildingType.ordinal(), false)
				&& isBuildTileFreeFromUnits(builder.getID(), tx, ty);
	}

	/** This method shouldn't be used other than in very, very specific cases. */
	public static void forceConstructingPylonNear(MapPoint tryBuildingAroundHere) {

		// First find proper place for building.
		MapPoint tileForBuilding = getLegitTileToBuildNear(getRandomWorker(),
				ProtossPylon.getBuildingType(), tryBuildingAroundHere, 5, 13,
				false);

		// Construct building here.
		constructBuilding(xvr, ProtossPylon.getBuildingType(), tileForBuilding);
		// if (!operation) {
		// Debug.message(xvr, "Forced constr: no place for " + buildingtype);
		// }
	}

}
