package jnibwapi.protoss;

import jnibwapi.XVR;
import jnibwapi.types.UnitType.UnitTypes;

public class ProtossAssimilator {

	private static XVR xvr = XVR.getInstance();

	private static final UnitTypes buildingType = UnitTypes.Protoss_Assimilator;

	public static boolean shouldBuild() {
		if (!Constructing.weAreBuilding(buildingType)
				&& xvr.canAfford(100)
				&& (UnitCounter
						.weHaveBuilding(UnitTypes.Protoss_Cybernetics_Core) || UnitCounter
						.getNumberOfUnits(UnitTypes.Protoss_Gateway) >= 2)
				&& UnitCounter.getNumberOfUnits(buildingType) < UnitCounter
						.getNumberOfUnitsCompleted(UnitManager.BASE)) {
			if (UnitCounter.getNumberOfInfantryUnits() >= 4) {
				return true;
			}
		}
		return false;
	}

	public static void buildIfNecessary() {
		if (shouldBuild()) {
			Constructing.construct(xvr, buildingType);
		}
	}

	public static UnitTypes getBuildingtype() {
		return buildingType;
	}

}
