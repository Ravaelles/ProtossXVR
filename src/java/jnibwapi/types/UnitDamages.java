package jnibwapi.types;

import java.util.HashMap;
import java.util.Locale;

import jnibwapi.types.UnitType.UnitTypes;

public class UnitDamages {

	private static HashMap<UnitType, Integer> groundAttackValues = new HashMap<>();
	private static HashMap<UnitType, Double> groundAttackValuesPerSec = new HashMap<>();

	public static void rememberUnitDamageValues() {
		for (UnitTypes unitTypes : UnitTypes.values()) {
			if (!unitTypes.name().startsWith("Terran")
					&& !unitTypes.name().startsWith("Protoss")
					&& !unitTypes.name().startsWith("Zerg")) {
				continue;
			}

			UnitType type = UnitType.getUnitTypeByID(unitTypes.ordinal());

			try {
				int groundAttackValue = type.getGroundWeapon()
						.getDamageAmount();
				
				if (unitTypes.equals(UnitTypes.Protoss_Zealot)
						|| unitTypes.equals(UnitTypes.Terran_Firebat)) {
					groundAttackValue *= 2;
				}
				
				if (unitTypes.equals(UnitTypes.Zerg_Sunken_Colony)) {
					groundAttackValue /= 2;
				}

				groundAttackValues.put(type, groundAttackValue);

				if (groundAttackValue == 0) {
					groundAttackValuesPerSec.put(type, (double) 0);
				} else {
					double groundAttackValueNormalized = ((double) 30
							* groundAttackValue / type.getGroundWeapon()
							.getDamageCooldown());
					groundAttackValueNormalized = Double.parseDouble(String
							.format("%.1f", groundAttackValueNormalized,
									Locale.ENGLISH).replace(',', '.'));
					groundAttackValuesPerSec.put(type,
							groundAttackValueNormalized);
				}

			} catch (Exception e) {
				System.err.println(unitTypes.name() + ": " + e.getMessage());
			}
		}

//		displayUnitDamages();
	}

//	private static void displayUnitDamages() {
//		System.out.println("");
//		System.out.println("=====================================");
//		System.out.println("ATTACK VALUES OF UNITS:");
//
//		Map<UnitType, Integer> sorted = RUtilities.sortByValue(
//				groundAttackValues, false);
//		for (UnitType type : sorted.keySet()) {
//			System.out.println(type.getName() + ":  " + sorted.get(type));
//		}
//
//		System.out.println("");
//
//		Map<UnitType, Double> sorted2 = RUtilities.sortByValue(
//				groundAttackValuesPerSec, false);
//		System.out.println("ATTACK VALUES OF UNITS PER SEC:");
//		for (UnitType type : sorted2.keySet()) {
//			System.out.println(type.getName() + ":  " + sorted2.get(type));
//		}
//
//		System.out.println("");
//
//		System.out.println("RATIO DAMAGE / COST:");
//		HashMap<UnitType, Double> pricesMap = new HashMap<>(); 
//		for (UnitType type : sorted2.keySet()) {
//			double cost = type.getMineralPrice() + type.getGasPrice() * 1.5;
//			double value = sorted2.get(type) < 1 ? 0 : ((double) 1000
//					* sorted2.get(type) / cost);
//			pricesMap.put(type, value);
//		}
//		
//		Map<UnitType, Double> sorted3 = RUtilities.sortByValue(
//				pricesMap, false);
//		for (UnitType type : sorted3.keySet()) {
//			System.out.println(type.getName() + ":  " + sorted3.get(type));
//		}
//	}

	public static int getGroundAttackUnnormalized(UnitType type) {
		return groundAttackValues.get(type);
	}

	public static double getGroundAttackNormalized(UnitType type) {
		return groundAttackValuesPerSec.get(type);
	}

}
