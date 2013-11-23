package jnibwapi.protoss;

import java.util.ArrayList;

import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.xvr.Constructing;
import jnibwapi.xvr.ShouldBuildCache;
import jnibwapi.xvr.UnitCounter;

public class ProtossRoboticsFacility {

	public static UnitTypes OBSERVER = UnitTypes.Protoss_Observer;
	public static UnitTypes REAVER = UnitTypes.Protoss_Reaver;
	public static UnitTypes SHUTTLE = UnitTypes.Protoss_Shuttle;

	private static final int MINIMUM_OBSERVERS = 6;
	private static final double REAVERS_TO_INFANTRY_RATIO = 0.2;
	
	private static final UnitTypes buildingType = UnitTypes.Protoss_Robotics_Facility;
	private static XVR xvr = XVR.getInstance();

	public static void buildIfNecessary() {
		if (shouldBuild()) {
			Constructing.construct(xvr, buildingType);
		}
	}

	public static boolean shouldBuild() {
		// UnitCounter.weHaveBuilding(UnitTypes.Protoss_Citadel_of_Adun)
		if (UnitCounter.getNumberOfUnits(ProtossGateway.getBuildingType()) >= 2
				&& UnitCounter.getNumberOfUnits(buildingType) <= 1
				&& !Constructing.weAreBuilding(buildingType)) {
			if (UnitCounter.getNumberOfBattleUnits() >= 3) {
				ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
				return true;
			}
		}
		ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
		return false;
	}

	public static Unit getOneNotBusy() {
		for (Unit unit : xvr.getUnitsOfType(buildingType)) {
			if (unit.isBuildingNotBusy()) {
				return unit;
			}
		}
		return null;
	}

	public static UnitTypes getBuildingtype() {
		return buildingType;
	}

	public static ArrayList<Unit> getAllObjects() {
		return xvr.getUnitsOfTypeCompleted(buildingType);
	}
	
	// ==========================
	// Unit creating

	public static void act(Unit facility) {
		if (facility == null) {
			return;
		}
		
		int[] buildingQueueDetails = Constructing.shouldBuildAnyBuilding();
		int freeMinerals = xvr.getMinerals();
		int freeGas = xvr.getGas();
		if (buildingQueueDetails != null) {
			freeMinerals -= buildingQueueDetails[0];
			freeGas -= buildingQueueDetails[1];
		}

		if (buildingQueueDetails == null || (freeMinerals >= 125 && freeGas >= 25)) {
			if (facility.getTrainingQueueSize() == 0) {
				xvr.buildUnit(facility,
						defineUnitToBuild(freeMinerals, freeGas));
			}
		}
	}

	private static UnitTypes defineUnitToBuild(int freeMinerals, int freeGas) {
		boolean observersAllowed = UnitCounter
				.weHaveBuildingFinished(ProtossObservatory.getBuildingtype())
				&& (freeMinerals >= 125 && freeGas >= 50);
		boolean reaversAllowed = UnitCounter
				.weHaveBuildingFinished(UnitTypes.Protoss_Templar_Archives)
				&& (freeMinerals >= 125 && freeGas >= 100);

		// OBSERVER
		if (observersAllowed) {
			if (UnitCounter.getNumberOfUnits(OBSERVER) < MINIMUM_OBSERVERS) {
				return OBSERVER;
			}
		}

		// REAVER
		if (reaversAllowed) {
			int totalInfantry = UnitCounter.getNumberOfInfantryUnits() + 1;
			int totalReavers = UnitCounter.getNumberOfUnits(REAVER);
			if ((double) totalReavers / totalInfantry <= REAVERS_TO_INFANTRY_RATIO) {
				return REAVER;
			}
		}

		return null;
	}

}
