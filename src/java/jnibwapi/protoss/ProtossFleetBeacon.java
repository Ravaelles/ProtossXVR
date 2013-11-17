package jnibwapi.protoss;

import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

public class ProtossFleetBeacon {

	private static final UnitTypes buildingType = UnitTypes.Protoss_Fleet_Beacon;
	private static XVR xvr = XVR.getInstance();

	public static void buildIfNecessary() {
		if (shouldBuild()) {
			Constructing.construct(xvr, buildingType);
		}
	}

	public static boolean shouldBuild() {
		if (UnitCounter.weHaveBuilding(UnitTypes.Protoss_Stargate)
				&& !UnitCounter.weHaveBuilding(buildingType)	
				&& xvr.canAfford(300, 200)
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
