package ai.protoss;

import java.util.ArrayList;

import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;
import ai.handling.constructing.Constructing;
import ai.handling.constructing.ShouldBuildCache;
import ai.handling.units.UnitCounter;

public class ProtossStargate {

	public static UnitTypes ARBITER = UnitTypes.Protoss_Arbiter;
	public static UnitTypes CORSAIR = UnitTypes.Protoss_Corsair;
	public static UnitTypes SCOUT = UnitTypes.Protoss_Scout;

	private static final int MINIMUM_ARBITERS = 2;
	private static final int MINIMUM_CORSAIRS = 3;
	private static final int MINIMUM_SCOUTS = 2;
	private static final int CORSAIRS_PER_OTHER_AIR_UNIT = 2;

	private static final UnitTypes buildingType = UnitTypes.Protoss_Stargate;
	private static XVR xvr = XVR.getInstance();

	public static void buildIfNecessary() {
		if (shouldBuild()) {
			Constructing.construct(xvr, buildingType);
		}
	}

	public static boolean shouldBuild() {
		// UnitCounter.weHaveBuilding(UnitTypes.Protoss_Cybernetics_Core)
		if (UnitCounter.weHaveBuilding(UnitTypes.Protoss_Templar_Archives)
				&& UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Reaver) >= 4
				&& UnitCounter.weHaveBuilding(UnitTypes.Protoss_Observatory)
				&& UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Observer) >= 3
				&& !UnitCounter.weHaveBuilding(buildingType)
				&& !Constructing.weAreBuilding(buildingType)) {
			// if (UnitCounter.getNumberOfBattleUnitsCompleted() > 15) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			return true;
			// }
		}

		if (UnitCounter.getNumberOfUnits(buildingType) == 1 && xvr.canAfford(800, 500)) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			return true;
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
		return xvr.getUnitsOfType(buildingType);
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

		if (buildingQueueDetails == null || (freeMinerals >= 200 && freeGas >= 400)) {
			if (facility.getTrainingQueueSize() == 0) {
				xvr.buildUnit(facility, defineUnitToBuild(freeMinerals, freeGas));
			}
		}
	}

	private static UnitTypes defineUnitToBuild(int freeMinerals, int freeGas) {
		boolean arbiterAllowed = UnitCounter.weHaveBuilding(UnitTypes.Protoss_Arbiter_Tribunal);

		// ARBITER
		if (arbiterAllowed && xvr.countUnitsOfType(ARBITER) < MINIMUM_ARBITERS) {
			return ARBITER;
		}

		// SCOUT
		if (UnitCounter.getNumberOfUnits(SCOUT) < MINIMUM_SCOUTS) {
			return SCOUT;
		}

		// CORSAIR
		if (UnitCounter.getNumberOfUnits(CORSAIR) < MINIMUM_CORSAIRS
				|| (UnitCounter.countAirUnitsNonCorsair() * CORSAIRS_PER_OTHER_AIR_UNIT < UnitCounter
						.getNumberOfUnits(CORSAIR))) {
			return CORSAIR;
		}

		return null;
	}

}
