package jnibwapi.protoss;

import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.xvr.Constructing;
import jnibwapi.xvr.ShouldBuildCache;
import jnibwapi.xvr.UnitCounter;

public class ProtossObservatory {

	private static final UnitTypes buildingType = UnitTypes.Protoss_Observatory;
	private static XVR xvr = XVR.getInstance();

	public static void buildIfNecessary() {
		if (shouldBuild()) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			Constructing.construct(xvr, buildingType);
		}
		ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
	}

	public static boolean shouldBuild() {
		if (UnitCounter.weHaveBuilding(UnitTypes.Protoss_Robotics_Facility)) {
			if (!UnitCounter.weHaveBuilding(buildingType)) {
//					&& !Constructing.weAreBuilding(buildingType)) {
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
