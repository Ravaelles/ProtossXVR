package jnibwapi.protoss;

import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.xvr.Constructing;
import jnibwapi.xvr.UnitCounter;

public class ProtossTemplarArchives {

	private static final UnitTypes buildingType = UnitTypes.Protoss_Templar_Archives;
	private static XVR xvr = XVR.getInstance();

	public static void buildIfNecessary() {
		if (shouldBuild()) {
			Constructing.construct(xvr, buildingType);
		}
	}

	public static boolean shouldBuild() {
		if (!UnitCounter.weHaveBuilding(buildingType)
				&& UnitCounter.weHaveBuilding(ProtossCitadelOfAdun.getBuildingtype())
				&& !Constructing.weAreBuilding(buildingType)) {
//			if (UnitCounter.getNumberOfBattleUnits() >= 15) {
				return true;
//			}
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

}
