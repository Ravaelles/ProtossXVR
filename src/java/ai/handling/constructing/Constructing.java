package ai.handling.constructing;

import java.awt.Point;
import java.util.HashMap;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;
import ai.handling.map.MapExploration;
import ai.handling.map.MapPoint;
import ai.handling.map.MapPointInstance;
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
import ai.utils.RUtilities;

public class Constructing {

	private static final int MIN_DIST_FROM_CHOKE_POINT = 5;

	private static XVR xvr = XVR.getInstance();

	private static HashMap<UnitTypes, Unit> _recentConstructionsInfo = new HashMap<>();
	private static int _recentConstructionsCounter = 0;
	private static int _actCounter = 0;

	// private static final int minDist = 7;

	// private static final int maxDist = 70;

	public static void act() {
		_actCounter++;
		if (_actCounter >= 3) {
			_actCounter = 0;
		}

		// Store info about constructing given building for 3 seconds, then
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
		} else if (_actCounter == 1) {
			ProtossTemplarArchives.buildIfNecessary();
			ProtossGateway.buildIfNecessary();
			ProtossObservatory.buildIfNecessary();
			ProtossAssimilator.buildIfNecessary();
			ProtossCitadelOfAdun.buildIfNecessary();
			ProtossPhotonCannon.buildIfNecessary();
		} else {
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
			return ProtossNexus.findTileForBase();
		}

		// Standard building
		else {

			// Let the Pylon class decide where to put buildings like Gateway,
			// Stargate etc.
			return ProtossPylon.findTileNearPylonForNewBuilding();

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
		MapPoint tileToBuild = ProtossPylon.findTileNearPylonForNewBuilding();

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
		}
		else {
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
									buildingTypeID, false)) {
						// if (isBuildTileFreeFromUnits(builderID, i, j)) {
						int x = i * 32;
						int y = j * 32;
						if (xvr.getDistanceBetween(
								MapExploration.getNearestChokePointFor(x, y),
								x, y) >= MIN_DIST_FROM_CHOKE_POINT) {
							return new MapPointInstance(x, y);
						}
						// }
					}
				}
			}

			// if (maximumDist < 15) {
			currentDist++;
			// } else {
			// if (currentDist > (minDist + 2) && RUtilities.rand(0, 3) == 0) {
			// currentDist += 2;
			// } else {
			// currentDist++;
			// }
			// }
		}

		return null;
	}

	public static Point getBuildableTileNearestTo(int minimumDist,
			int maximumDist, int tileX, int tileY, int builderID,
			UnitTypes buildingType) {
		int currentDist = minimumDist;
		while (currentDist <= maximumDist) {
			for (int i = tileX - currentDist; i <= tileX + currentDist; i++) {
				for (int j = tileY - currentDist; j <= tileY + currentDist; j++) {
					if (xvr.getBwapi().canBuildHere(builderID, i, j,
							buildingType.ordinal(), false)) {
						if (Constructing.isBuildTileFreeFromUnits(builderID, i,
								j)) {
							return new Point(i, j);
						}
					}
				}
			}

			if (maximumDist < 15) {
				currentDist++;
			} else {
				if (currentDist > 4 && RUtilities.rand(0, 3) == 0) {
					currentDist += 2;
				} else {
					currentDist++;
				}
			}
		}
		return null;
	}

	public static boolean isBuildTileFreeFromUnits(int builderID, int tileX,
			int tileY) {
		JNIBWAPI bwapi = XVR.getInstance().getBwapi();

		// Check if units are blocking this tile
		boolean unitsInWay = false;
		for (Unit u : bwapi.getAllUnits()) {
			if (u.getID() == builderID) {
				continue;
			}
			if (xvr.getDistanceBetween(u, tileX * 32, tileY * 32) <= 3) {
				unitsInWay = true;
			}
		}
		if (!unitsInWay) {
			return true;
		}

		return false;
	}

	public static void construct(XVR xvr, UnitTypes building) {

		// Define neighborhood where to build.
		// Unit nearbyBase = xvr.getFirstBase();
		// if (nearbyBase == null) {
		// return;
		// }

		// Define tile where to build according to type of building.
		MapPoint buildTile = getTileAccordingToBuildingType(building);
		// System.out.println("buildTile FOR: " + building + " = " + buildTile);

		// Check if build tile is okay.
		if (buildTile != null) {

			// Proper construction order/
			constructBuilding(xvr, building, buildTile);
		} else {
			// if (building.ordinal() !=
			// ProtossPhotonCannon.getBuildingType().ordinal()) {
			// System.out.println("### NO TILE FOUND FOR: " + building
			// + " ###");
			// }
		}
	}

	// public static void construct(XVR xvr, UnitTypes building, Point
	// buildTile) {
	//
	// // try to find the worker near our home position
	// Unit workerUnit = xvr.getRandomWorker();
	// if (workerUnit == null || buildTile == null) {
	// return;
	// }
	//
	// // Make sure we can build there
	// buildTile = getLegitTileToBuildNear(workerUnit.getID(),
	// building.ordinal(), buildTile.x, buildTile.y, 0, maxDist);
	// workerUnit = WorkerManager.findNearestWorkerTo(buildTile.x * 32,
	// buildTile.y * 32);
	//
	// // Proper construction
	// constructWithWorker(xvr, workerUnit, building, buildTile);
	// }

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

	// private static void forceConstructionOfPylonNear(Point buildTile) {
	// Point tileForPylon = findBuildTile(xvr, xvr.getRandomWorker().getID(),
	// ProtossPylon.getBuildingType().ordinal(), buildTile.x,
	// buildTile.y);
	// constructBuilding(xvr, UnitTypes.Protoss_Pylon, tileForPylon);
	// }

	public static boolean weAreBuilding(UnitTypes type) {
		if (_recentConstructionsInfo.containsKey(type)) {
			return true;
		}
		for (Unit unit : xvr.getBwapi().getMyUnits()) {
			if ((unit.getTypeID() == type.ordinal()) && (!unit.isCompleted())) {
				return true;
			}
			if (unit.getBuildTypeID() == type.ordinal()) {
				return true;
			}
		}
		return false;
	}

	private static void build(Unit builder, MapPoint buildTile,
			UnitTypes building) {
		if (!weAreBuilding(building)) {
			xvr.getBwapi().build(builder.getID(), buildTile.getTx(),
					buildTile.getTy(), building.ordinal());
			// if (Unit.getByID(worker).isConstructing()) {
			addInfoAboutConstruction(building, builder);
			removeDuplicateConstructionsPending(builder);
			// }

			// System.out.println("DISTANCE UNITS: " + xvr.getDistanceBetween(
			// xvr.getFirstBase(), buildTile.x * 32, buildTile.y * 32));
		}
	}

	public static void removeDuplicateConstructionsPending(
			Unit onlyAllowedBuilder) {
		// UnitType buildingType =
		// UnitType.getUnitTypeByID(onlyAllowedBuilder.getBuildTypeID());
		for (Unit otherBuilder : xvr.getWorkers()) {
			if (otherBuilder.isConstructing()
					&& onlyAllowedBuilder.getBuildTypeID() == otherBuilder
							.getBuildTypeID()
					&& !otherBuilder.equals(onlyAllowedBuilder)) {
				xvr.getBwapi().cancelConstruction(otherBuilder.getID());
			}
		}
	}

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
