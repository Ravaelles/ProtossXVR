package jnibwapi.protoss;

import java.util.ArrayList;

import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.xvr.Constructing;
import jnibwapi.xvr.ShouldBuildCache;
import jnibwapi.xvr.UnitCounter;

public class ProtossShieldBattery {

	private static final UnitTypes buildingType = UnitTypes.Protoss_Shield_Battery;
	private static XVR xvr = XVR.getInstance();

//	public static void act(Unit battery) {
//		if (battery.getEnergy() >= 10) {
//		}
//	}
	
	public static void buildIfNecessary() {
		if (shouldBuild()) {
			Constructing.construct(xvr, buildingType);
		}
	}

	public static boolean shouldBuild() {
		if (!UnitCounter.weHaveBuilding(buildingType)
				&& !Constructing.weAreBuilding(buildingType)) {
			if (UnitCounter.getNumberOfBattleUnits() >= 3) {
				ShouldBuildCache.cacheShouldBuildInfo(buildingType, true);
				return true;
			}
		}
		ShouldBuildCache.cacheShouldBuildInfo(buildingType, false);
		return false;
	}

	public static Unit getOneWithEnergy() {
		for (Unit unit : xvr.getUnitsOfType(buildingType)) {
			if (unit.getEnergy() >= 13) {
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
