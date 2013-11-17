package jnibwapi.protoss;

import jnibwapi.XVR;
import jnibwapi.types.UnitType.UnitTypes;

public class ProtossObservatory {

	private static final UnitTypes buildingType = UnitTypes.Protoss_Observatory;
	private static XVR xvr = XVR.getInstance();

	public static void buildIfNecessary() {
		if (shouldBuild()) {
			Constructing.construct(xvr, buildingType);
		}
	}

	public static boolean shouldBuild() {
		if (xvr.canAfford(50, 100)
				&& UnitCounter
						.weHaveBuilding(UnitTypes.Protoss_Robotics_Facility)) {
			if (!UnitCounter.weHaveBuilding(buildingType)
					&& !Constructing.weAreBuilding(buildingType)) {
				return true;
			}
		}
		return false;
	}

	public static UnitTypes getBuildingtype() {
		return buildingType;
	}
	
}
