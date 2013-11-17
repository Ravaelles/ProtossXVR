package jnibwapi.protoss;

import java.util.ArrayList;

import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

public class ProtossStargate {

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
	
}
