package ai.protoss;

import java.util.ArrayList;

import ai.core.XVR;
import ai.handling.constructing.Constructing;
import ai.handling.constructing.ShouldBuildCache;
import ai.handling.units.UnitCounter;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

public class ProtossArbiterTribunal {

	private static final UnitTypes buildingType = UnitTypes.Protoss_Arbiter_Tribunal;
	private static XVR xvr = XVR.getInstance();

	public static void buildIfNecessary() {
		if (shouldBuild()) {
			ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
			Constructing.construct(xvr, buildingType);
		}
		ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
	}

	public static boolean shouldBuild() {
		if (UnitCounter.weHaveBuilding(UnitTypes.Protoss_Templar_Archives)
				&& !UnitCounter.weHaveBuilding(buildingType)
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

	public static UnitTypes getBuildingType() {
		return buildingType;
	}

	public static ArrayList<Unit> getAllObjects() {
		return xvr.getUnitsOfTypeCompleted(buildingType);
	}

}
