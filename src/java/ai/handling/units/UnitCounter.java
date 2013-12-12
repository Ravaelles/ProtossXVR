package ai.handling.units;

import java.util.HashMap;
import java.util.Set;

import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;

public class UnitCounter {

	private static HashMap<Integer, Integer> numberOfUnits = new HashMap<Integer, Integer>();
	private static XVR xvr = XVR.getInstance();

	// public static HashMap<Integer, Integer> getNumberOfUnits() {
	// return numberOfUnits;
	// }

	public static void recalculateUnits() {
		resetUnits();
		countUnits();
	}

	private static void resetUnits() {
		numberOfUnits.clear();
		// numberOfUnits.put(UnitTypes.Protoss_Command_Center.ordinal(), 0);
		// numberOfUnits.put(UnitManager.WORKER.ordinal(), 0);
		// numberOfUnits.put(UnitTypes.Protoss_Supply_Depot.ordinal(), 0);
		// numberOfUnits.put(UnitTypes.Protoss_Gateway.ordinal(), 0);
		// numberOfUnits.put(UnitTypes.Protoss_Marine.ordinal(), 0);
		// numberOfUnits.put(UnitTypes.Protoss_Medic.ordinal(), 0);
		// numberOfUnits.put(UnitTypes.Protoss_Firebat.ordinal(), 0);
		// numberOfUnits.put(UnitTypes.Protoss_Bunker.ordinal(), 0);
		// numberOfUnits.put(UnitTypes.Protoss_Cybernetics_Core.ordinal(), 0);
		// numberOfUnits.put(UnitTypes.Protoss_.ordinal(), 0);
		// numberOfUnits.put(UnitTypes.Protoss_.ordinal(), 0);

		// if (xvr != null) {
		//
		// }
	}

	private static void countUnits() {
		for (Unit unit : xvr.getBwapi().getMyUnits()) {
			numberOfUnits
					.put(unit.getTypeID(),
							(numberOfUnits.containsKey(unit.getTypeID()) ? numberOfUnits
									.get(unit.getTypeID()) + 1 : 1));
		}
	}

	public static int getNumberOfUnits(UnitTypes type) {
		return numberOfUnits.containsKey(type.ordinal()) ? numberOfUnits
				.get(type.ordinal()) : 0;
	}

	public static Set<Integer> getExistingUnitTypes() {
		return numberOfUnits.keySet();
	}

	public static int getNumberOfUnitsCompleted(UnitTypes type) {
		int result = 0;
		for (Unit unit : XVR.getInstance().getUnitsOfType(type)) {
			if (unit.isCompleted()) {
				result++;
			}
		}
		return result;
	}

	public static int getNumberOfBattleUnits() {
		return getNumberOfInfantryUnits() + 
				+ getNumberOfUnits(UnitTypes.Protoss_Reaver)
				+ getNumberOfUnits(UnitTypes.Protoss_Corsair)
				+ getNumberOfUnits(UnitTypes.Protoss_Scout)
				+ getNumberOfUnits(UnitTypes.Protoss_Carrier)
				+ getNumberOfUnits(UnitTypes.Protoss_Arbiter);
	}

	public static boolean weHaveBuilding(UnitTypes unitType) {
		return getNumberOfUnits(unitType) > 0;
	}

	public static boolean weHaveBuildingFinished(UnitTypes unitType) {
		return getNumberOfUnitsCompleted(unitType) > 0;
	}

	public static int getNumberOfInfantryUnits() {
		return getNumberOfUnits(UnitTypes.Protoss_Zealot)
				+ getNumberOfUnits(UnitTypes.Protoss_Dragoon)
				+ getNumberOfUnits(UnitTypes.Protoss_High_Templar)
				+ getNumberOfUnits(UnitTypes.Protoss_Dark_Templar);
	}

	public static boolean weHavePylonFinished() {
		return getNumberOfUnitsCompleted(UnitTypes.Protoss_Pylon) > 0;
	}

	public static int countAirUnitsNonCorsair() {
		return getNumberOfUnits(UnitTypes.Protoss_Observer)
				+ getNumberOfUnits(UnitTypes.Protoss_Arbiter)
				+ getNumberOfUnits(UnitTypes.Protoss_Carrier);
	}

	public static int getNumberOfPylons() {
		return getNumberOfUnits(UnitTypes.Protoss_Pylon);
	}
	
	public static int getNumberOfPylonsCompleted() {
		return getNumberOfUnitsCompleted(UnitTypes.Protoss_Pylon);
	}

}
