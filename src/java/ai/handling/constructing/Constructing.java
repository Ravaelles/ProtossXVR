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
import ai.utils.RUtilities;

public class Constructing {

	// private static final int MIN_DIST_FROM_CHOKE_POINT = 5;
	private static final int PROLONGATED_CONSTRUCTION_TIME = 300; // in fps

	private static XVR xvr = XVR.getInstance();

	private static HashMap<UnitTypes, Unit> _recentConstructionsInfo = new HashMap<>();
	private static HashMap<UnitTypes, MapPoint> _recentConstructionsPlaces = new HashMap<>();
	private static HashMap<Unit, UnitTypes> _recentConstructionsUnitToType = new HashMap<>();
	private static HashMap<Unit, Integer> _recentConstructionsTimes = new HashMap<>();
	private static int _recentConstructionsCounter = 0;
	private static int _actCounter = 0;

	// private static int iIncStep;
	// private static int jIncStep;

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

		// Check only every N frames
		boolean shouldBuildNexus = ProtossNexus.shouldBuild();
		boolean canBuildOtherThingThanNexus = !shouldBuildNexus || xvr.canAfford(550);

		if (_actCounter == 0) {
			ProtossNexus.buildIfNecessary();
		} else if (_actCounter == 1 && canBuildOtherThingThanNexus) {
			ProtossPhotonCannon.buildIfNecessary();
			ProtossRoboticsFacility.buildIfNecessary();
			ProtossCyberneticsCore.buildIfNecessary();
			ProtossRoboticsSupportBay.buildIfNecessary();
			ProtossTemplarArchives.buildIfNecessary();
			ProtossGateway.buildIfNecessary();
			ProtossObservatory.buildIfNecessary();
			ProtossAssimilator.buildIfNecessary();
			ProtossCitadelOfAdun.buildIfNecessary();
		} else if (canBuildOtherThingThanNexus) {
			ProtossPhotonCannon.buildIfNecessary();
			ProtossPylon.buildIfNecessary();
			ProtossStargate.buildIfNecessary();
			ProtossForge.buildIfNecessary();
			ProtossShieldBattery.buildIfNecessary();
			ProtossArbiterTribunal.buildIfNecessary();
			ProtossGateway.buildIfNecessary();
		}

		// It can happen that damned worker will stuck somewhere (what a retard)
		if (RUtilities.rand(0, 20) == 0) {
			checkForProlongatedConstructions();
		}
	}

	private static void checkForProlongatedConstructions() {
		int now = xvr.getTime();
		for (Unit builder : _recentConstructionsTimes.keySet()) {
			if (!builder.isConstructing()) {
				continue;
			}

			if (now - _recentConstructionsTimes.get(builder) > PROLONGATED_CONSTRUCTION_TIME) {
				MapPoint buildTile = _recentConstructionsPlaces.get(builder);
				UnitTypes building = _recentConstructionsUnitToType.get(builder);

				// Issue new construction order
				// Constructing.build(builder, buildTile, building);
				constructBuilding(xvr, building, buildTile);

				// Cancel previous construction
				// xvr.getBwapi().cancelConstruction(builder.getID());
				// And to make sure move unit
				UnitActions.moveTo(builder, xvr.getFirstBase());
			}
		}
	}

	private static MapPoint getTileAccordingToBuildingType(UnitTypes building) {

		// Pylon
		if (UnitTypes.Protoss_Pylon.ordinal() == building.ordinal()) {
			return ProtossPylon.findTileForPylon();
		}

		// Photon Cannon
		else if (UnitTypes.Protoss_Photon_Cannon.ordinal() == building.ordinal()) {
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
			return new int[] { mineralsRequired + 8, gasRequired, buildingsToBuildTypesNumber };
		}
		if (ProtossTemplarArchives.shouldBuild()) {
			mineralsRequired += 150;
			gasRequired += 200;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired, buildingsToBuildTypesNumber };
		}
		if (ProtossRoboticsFacility.shouldBuild()) {
			mineralsRequired += 200;
			gasRequired += 200;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired, buildingsToBuildTypesNumber };
		}
		if (ProtossRoboticsSupportBay.shouldBuild()) {
			mineralsRequired += 150;
			gasRequired += 100;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired, buildingsToBuildTypesNumber };
		}
		if (ProtossGateway.shouldBuild()) {
			mineralsRequired += 150;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired, buildingsToBuildTypesNumber };
		}
		// if (TerranBunker.shouldBuild()
		// && UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Bunker) < 2) {
		// mineralsRequired += 100;
		// buildingsToBuildTypesNumber++;
		// }
		if (ProtossAssimilator.shouldBuild()) {
			mineralsRequired += 100;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired, buildingsToBuildTypesNumber };
		}
		if (ProtossPylon.shouldBuild()) {
			mineralsRequired += 100;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired, buildingsToBuildTypesNumber };
		}
		if (ProtossForge.shouldBuild()) {
			mineralsRequired += 150;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired, buildingsToBuildTypesNumber };
		}
		if (ProtossCyberneticsCore.shouldBuild()) {
			mineralsRequired += 150;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired, buildingsToBuildTypesNumber };
		}
		if (ProtossCitadelOfAdun.shouldBuild()) {
			mineralsRequired += 150;
			gasRequired += 100;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired, buildingsToBuildTypesNumber };
		}
		if (ProtossObservatory.shouldBuild()) {
			mineralsRequired += 50;
			gasRequired += 100;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired, buildingsToBuildTypesNumber };
		}
		if (ProtossArbiterTribunal.shouldBuild()) {
			mineralsRequired += 200;
			gasRequired += 150;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired, buildingsToBuildTypesNumber };
		}
		if (ProtossFleetBeacon.shouldBuild()) {
			mineralsRequired += 300;
			gasRequired += 200;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired, buildingsToBuildTypesNumber };
		}
		if (ProtossShieldBattery.shouldBuild()) {
			mineralsRequired += 100;
			buildingsToBuildTypesNumber++;
			return new int[] { mineralsRequired + 8, gasRequired, buildingsToBuildTypesNumber };
		}

		if (buildingsToBuildTypesNumber > 0) {
			return new int[] { mineralsRequired + 8, gasRequired, buildingsToBuildTypesNumber };
		} else {
			return null;
		}
	}

	private static void resetInfoAboutConstructions() {
		_recentConstructionsCounter = 0;
		_recentConstructionsInfo.clear();
	}

	private static void addInfoAboutConstruction(UnitTypes building, Unit builder,
			MapPoint buildTile) {
		_recentConstructionsCounter = 0;
		_recentConstructionsInfo.put(building, builder);
		_recentConstructionsPlaces.put(building, buildTile);
		_recentConstructionsUnitToType.put(builder, building);
		_recentConstructionsTimes.put(builder, xvr.getTime());
		ShouldBuildCache.cacheShouldBuildInfo(building, false);
	}

	public static MapPoint findBuildTile(XVR xvr, int builderID, UnitTypes type, MapPoint place) {
		return findBuildTile(xvr, builderID, type.ordinal(), place.getX(), place.getY());
	}

	public static MapPoint findBuildTile(XVR xvr, int builderID, int buildingTypeID, int x, int y) {

		// Point pointToBuild = getLegitTileToBuildNear(builderID,
		// buildingTypeID, tileX, tileY, minDist, maxDist);
		MapPoint tileToBuild = ProtossPylon.findTileNearPylonForNewBuilding(UnitType
				.getUnitTypesByID(buildingTypeID));

		if (tileToBuild == null) {
			JNIBWAPI bwapi = xvr.getBwapi();
			bwapi.printText("Unable to find tile for new "
					+ bwapi.getUnitType(buildingTypeID).getName());
		}
		return tileToBuild;
	}

	public static MapPoint findTileForAssimilator() {
		Unit nearestGeyser = xvr.getUnitNearestFromList(xvr.getFirstBase(), xvr.getGeysersUnits());
		if (nearestGeyser != null
				&& xvr.getUnitsOfGivenTypeInRadius(UnitManager.BASE, 15, nearestGeyser, true)
						.isEmpty()) {
			return null;
		}

		// return new MapPointInstance(nearestGeyser.getX(),
		// nearestGeyser.getY());
		if (nearestGeyser != null) {
			return new MapPointInstance(nearestGeyser.getX() - 64, nearestGeyser.getY() - 32);
		} else {
			return null;
		}
	}

	public static MapPoint getLegitTileToBuildNear(Unit worker, UnitTypes type, MapPoint nearTo,
			int minimumDist, int maximumDist, boolean requiresPower) {
		if (worker == null || type == null) {
			return null;
		}
		return getLegitTileToBuildNear(worker.getID(), type.ordinal(), nearTo.getTx(),
				nearTo.getTy(), minimumDist, maximumDist, requiresPower);
	}

	public static MapPoint getLegitTileToBuildNear(Unit worker, UnitTypes type, int tileX,
			int tileY, int minimumDist, int maximumDist, boolean requiresPower) {
		if (worker == null || type == null) {
			return null;
		}
		return getLegitTileToBuildNear(worker.getID(), type.ordinal(), tileX, tileY, minimumDist,
				maximumDist, requiresPower);
	}

	public static MapPoint getLegitTileToBuildNear(int builderID, int buildingTypeID, int tileX,
			int tileY, int minimumDist, int maximumDist, boolean requiresPower) {
		JNIBWAPI bwapi = XVR.getInstance().getBwapi();
		UnitType type = UnitType.getUnitTypeByID(buildingTypeID);
		boolean isBase = type.isBase();
		boolean isCannon = type.isPhotonCannon();
		boolean isPylon = type.isPylon();

		int currentDist = minimumDist;
		while (currentDist <= maximumDist) {
			for (int i = tileX - currentDist; i <= tileX + currentDist; i++) {
				for (int j = tileY - currentDist; j <= tileY + currentDist; j++) {
					if ((!requiresPower || bwapi.hasPower(i, j))
							&& bwapi.canBuildHere(builderID, i, j, buildingTypeID, false)) {
						// && isBuildTileFullyBuildableFor(builderID, i, j,
						// buildingTypeID)
						int x = i * 32;
						int y = j * 32;
						MapPointInstance place = new MapPointInstance(x, y);
						Unit optimalBuilder = xvr.getOptimalBuilder(place);
						if (optimalBuilder != null
								&& (isCannon || isBase || isBuildTileFreeFromUnits(
										optimalBuilder.getID(), i, j))) {
							if (!isTooNearMineralAndBase(place)
									&& (isPylon || isEnoughPlaceToOtherBuildings(place, type))
									&& (isBase || !isOverlappingNextNexus(place, type))
									&& (isBase || !isTooCloseToAnyChokePoint(place))) {

								// if (type.isPhotonCannon()) {
								// System.out.println("@@@@@@@ "
								// + xvr.getDistanceBetween(choke, place) +
								// "/"
								// + choke.getRadius());
								// }
								return place;
							}
						}
					}
				}
			}

			currentDist++;
		}

		return null;
	}

	public static boolean isTooCloseToAnyChokePoint(MapPointInstance place) {
		// for (ChokePoint choke : MapExploration.getChokePoints()) {
		// if (choke.getRadius() < 210
		// && (xvr.getDistanceBetween(choke, place) - choke.getRadius() / 32) <=
		// MIN_DIST_FROM_CHOKE_POINT) {
		// return true;
		// }
		// }
		return false;
	}

	private static boolean isOverlappingNextNexus(MapPoint place, UnitType type) {
		if (!type.isBase() && UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Pylon) >= 1) {
			return xvr.getDistanceSimple(place, ProtossNexus.getTileForNextBase(false)) <= 4;
		} else {
			return false;
		}
	}

	private static boolean isEnoughPlaceToOtherBuildings(MapPoint place, UnitType type) {
		// type.isPhotonCannon() ||
		if (type.isBase() || type.isOnGeyser() || type.isPylon()) {
			return true;
		}

		int wHalf = type.getTileWidth();
		int hHalf = type.getTileHeight();
		int maxDimension = wHalf > hHalf ? wHalf : hHalf;
		// Map map = xvr.getBwapi().getMap();

		// Define center of the building
		// MapPoint center = new MapPointInstance(place.getX(), place.getY());
		MapPoint center = new MapPointInstance(place.getX() + wHalf, place.getY() + hHalf);

		ArrayList<Unit> buildingsNearby = xvr.getUnitsInRadius(center, maxDimension + 1,
				xvr.getUnitsBuildings());

		// System.out.println("FOR: " + type.getName());
		// for (Unit unit : buildingsNearby) {
		// System.out.println("   " + unit.getName() + ": " +
		// unit.distanceTo(center));
		// }
		// System.out.println();

		for (Unit unit : buildingsNearby) {
			if (unit.getType().isBuilding() && unit.distanceTo(center) <= maxDimension + 1) {
				return false;
			}
		}
		return true;

		// if (buildingsNearby.size() == 0) {
		// return true;
		// } else {
		// return false;
		// }
	}

	public static boolean isTooNearMineralAndBase(MapPoint point) {
		Unit nearestMineral = xvr.getUnitNearestFromList(point, xvr.getMineralsUnits());
		double distToMineral = xvr.getDistanceBetween(nearestMineral, point);
		if (distToMineral <= 3) {
			return true;
		}

		if (distToMineral <= 5) {
			Unit nearestBase = xvr.getUnitOfTypeNearestTo(UnitManager.BASE, point);
			double distToBase = xvr.getDistanceBetween(nearestBase, point);
			if (distToBase < distToMineral) {
				return true;
			}
		}
		return false;
	}

	public static boolean isBuildTileFullyBuildableFor(int builderID, int i, int j,
			int buildingTypeID) {
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

	public static boolean isBuildTileFreeFromUnits(int builderID, int tileX, int tileY) {
		JNIBWAPI bwapi = XVR.getInstance().getBwapi();
		MapPointInstance point = new MapPointInstance(tileX * 32, tileY * 32);

		// Check if units are blocking this tile
		boolean unitsInWay = false;
		for (Unit u : bwapi.getAllUnits()) {
			if (u.getID() == builderID) {
				continue;
			}
			if (xvr.getDistanceBetween(u, point) <= 3) {
				// for (Unit unit : xvr.getUnitsInRadius(point, 4,
				// xvr.getBwapi().getMyUnits())) {
				// UnitActions.moveAwayFromUnitIfPossible(unit, point, 6);
				// }
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
					&& UnitCounter.weHaveBuildingFinished(UnitTypes.Protoss_Forge)) {
				handleBaseConstruction(building, buildTile);
			} else {

				// Proper construction order
				constructBuilding(xvr, building, buildTile);
			}

		}
	}

	/** The idea is to build pylon and cannon first, just then build Nexus. */
	private static void handleBaseConstruction(UnitTypes building, MapPoint buildTile) {
		boolean baseInterrupted = false;

//		System.out.println("Base build: " + buildTile);

		// ==============================
		// Ensure there's pylon nearby

		// if (UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Nexus) > 2) {

		// Try to find proper choke to reinforce
		ChokePoint choke = MapExploration.getImportantChokePointNear(buildTile);

		// Get point in between choke and base
		MapPointInstance point = MapPointInstance.getMiddlePointBetween(buildTile, choke);

		ArrayList<Unit> pylons = xvr.getUnitsOfGivenTypeInRadius(UnitTypes.Protoss_Pylon, 10,
				choke, true);
		int cannonsNearby = xvr.countUnitsOfGivenTypeInRadius(UnitTypes.Protoss_Photon_Cannon, 16,
				point, true);

		boolean pylonIsOkay = !pylons.isEmpty() && pylons.get(0).isCompleted();
		if (!pylonIsOkay) {
			baseInterrupted = true;
			building = UnitTypes.Protoss_Pylon;

			// Get the base location
			// MapPoint base = ProtossNexus.getTileForNextBase(false);

			// Refind proper place for pylon
			buildTile = getLegitTileToBuildNear(xvr.getRandomWorker(), building, point, 0, 10, true);
		}

		// ==============================
		// Ensure there's at least some cannon nearby
		if (pylonIsOkay && cannonsNearby < 0) {
			baseInterrupted = true;
			building = UnitTypes.Protoss_Photon_Cannon;

			buildTile = ProtossPhotonCannon.findTileForCannon();
			// System.out.println("------------- FORCE CANNON FOR BASE");
		}
		// }

		// ==============================
		// We can build the base
		if (!baseInterrupted) {
			if (buildTile == null || !Constructing.canBuildAt(buildTile, UnitManager.BASE)) {
//				System.out.println("TEST cant Build At: " + buildTile);
				buildTile = ProtossNexus.getTileForNextBase(true);
			}
			// // System.out.println("BASE READY # pylonsNearby = " +
			// pylons.size()
			// // + ", cannonsNearby = " + cannonsNearby);
		}

		// System.out.println((buildTile != null ? buildTile.toStringLocation()
		// : buildTile) + " : "
		// + Constructing.canBuildAt(buildTile, UnitManager.BASE));

		constructBuilding(xvr, building, buildTile);
	}

	private static boolean canBuildAt(MapPoint point, UnitTypes type) {
		Unit randomWorker = xvr.getRandomWorker();
		if (randomWorker == null || point == null) {
			return false;
		}
		return xvr.getBwapi().canBuildHere(randomWorker.getID(), point.getTx(), point.getTy(),
				type.getID(), false);
	}

	private static boolean constructBuilding(XVR xvr, UnitTypes building, MapPoint buildTile) {
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

	public static int ifWeAreBuildingItCountHowManyWorkersIsBuildingIt(UnitTypes type) {
		int result = 0;

		// if (_recentConstructionsInfo.containsKey(type)) {
		// result++;
		// }
		for (Unit unit : xvr.getWorkers()) {
			if (unit.getBuildTypeID() == type.ordinal()) {
				result++;
			}
		}
		return result;
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

	private static void build(Unit builder, MapPoint buildTile, UnitTypes building) {
		boolean canProceed = false;

		// Disallow multiple building of all buildings, except cannons.
		if (building.getType().isPhotonCannon() || building.getType().isGateway()) {
			int builders = ifWeAreBuildingItCountHowManyWorkersIsBuildingIt(building);
			canProceed = builders == 0;
			//
			// || builders == 1
			// && builder.getID() == _recentConstructionsInfo.get(builder)
			// .getID()
		} else {
			canProceed = !weAreBuilding(building);
		}

		if (canProceed) {
			xvr.getBwapi().build(builder.getID(), buildTile.getTx(), buildTile.getTy(),
					building.ordinal());
			addInfoAboutConstruction(building, builder, buildTile);
			// removeDuplicateConstructionsPending(builder);
			// }
		}
	}

	public static Unit getRandomWorker() {
		return xvr.getRandomWorker();
	}

	public static boolean canBuildHere(Unit builder, UnitTypes buildingType, int tx, int ty) {
		return xvr.getBwapi().canBuildHere(builder.getID(), tx, ty, buildingType.ordinal(), false);
		// && isBuildTileFreeFromUnits(builder.getID(), tx, ty)
	}

	// /** This method shouldn't be used other than in very, very specific
	// cases. */
	// public static void forceConstructingPylonNear(MapPoint
	// tryBuildingAroundHere) {
	//
	// // First find proper place for building.
	// MapPoint tileForBuilding = getLegitTileToBuildNear(getRandomWorker(),
	// ProtossPylon.getBuildingType(), tryBuildingAroundHere, 5, 13, false);
	//
	// // Construct building here.
	// constructBuilding(xvr, ProtossPylon.getBuildingType(), tileForBuilding);
	// // if (!operation) {
	// // Debug.message(xvr, "Forced constr: no place for " + buildingtype);
	// // }
	// }

}
