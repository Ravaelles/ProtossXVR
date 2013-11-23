package jnibwapi.protoss;

import java.util.ArrayList;

import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.xvr.Constructing;
import jnibwapi.xvr.UnitCounter;

public class ProtossStargate {

	public static UnitTypes ARBITER = UnitTypes.Protoss_Arbiter;
	public static UnitTypes CORSAIR = UnitTypes.Protoss_Corsair;

	private static final int MINIMUM_ARBITERS = 2;
	private static final int MINIMUM_CORSAIRS = 3;
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
				&& UnitCounter.weHaveBuilding(UnitTypes.Protoss_Observatory)
				&& !UnitCounter.weHaveBuilding(buildingType)
				&& xvr.canAfford(150, 150)
				&& !Constructing.weAreBuilding(buildingType)) {
			// if (UnitCounter.getNumberOfBattleUnits() >= 15) {
			return true;
			// }
		}
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

		if (buildingQueueDetails == null
				|| (freeMinerals >= 200 && freeGas >= 400)) {
			if (facility.getTrainingQueueSize() == 0) {
				xvr.buildUnit(facility,
						defineUnitToBuild(freeMinerals, freeGas));
			}
		}
	}

	private static UnitTypes defineUnitToBuild(int freeMinerals, int freeGas) {
		boolean arbiterAllowed = UnitCounter
				.weHaveBuilding(UnitTypes.Protoss_Arbiter_Tribunal);

		// ARBITER
		if (arbiterAllowed && xvr.countUnitsOfType(ARBITER) < MINIMUM_ARBITERS) {
			return ARBITER;
		}

		// CORSAIR
		if (UnitCounter.getNumberOfUnits(CORSAIR) < MINIMUM_CORSAIRS
				|| (UnitCounter.countAirUnitsNonCorsair()
						* CORSAIRS_PER_OTHER_AIR_UNIT < UnitCounter
							.getNumberOfUnits(CORSAIR))) {
			return CORSAIR;
		}

		return null;
	}

}
