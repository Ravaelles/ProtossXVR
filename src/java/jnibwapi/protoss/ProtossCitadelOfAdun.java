package jnibwapi.protoss;

import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.xvr.Constructing;
import jnibwapi.xvr.UnitCounter;

public class ProtossCitadelOfAdun {

	private static final UnitTypes buildingType = UnitTypes.Protoss_Citadel_of_Adun;
	private static XVR xvr = XVR.getInstance();

	public static void buildIfNecessary() {
		if (shouldBuild()) {
			Constructing.construct(xvr, buildingType);
		}
	}

	public static boolean shouldBuild() {
		if (!UnitCounter.weHaveBuilding(buildingType)
				&& UnitCounter
						.weHaveBuilding(UnitTypes.Protoss_Cybernetics_Core)
				&& UnitCounter.getNumberOfUnits(ProtossGateway
						.getBuildingType()) >= 2
				&& !Constructing.weAreBuilding(buildingType)
				&& xvr.canAfford(150, 100)) {
			if (UnitCounter.getNumberOfBattleUnits() >= 2) {
				return true;
			}
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
