package jnibwapi.protoss;

import java.util.ArrayList;

import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

public class ArmyCreationManager {
	
	private static XVR xvr = XVR.getInstance();

	public static void act() {
		if (weShouldBuildBattleUnits()) {
			
			// GATEWAY
			ArrayList<Unit> gatewayList = ProtossGateway.getAllObjects();
			if (!gatewayList.isEmpty()) {
				for (Unit gateway : gatewayList) {
					ProtossGateway.act(gateway);
				}
			}
			
			// ROBOTICS FACILITY
			ArrayList<Unit> roboticsFacilitiesList = ProtossRoboticsFacility.getAllObjects();
			if (!roboticsFacilitiesList.isEmpty()) {
				for (Unit roboticsFacility : roboticsFacilitiesList) {
					ProtossRoboticsFacility.act(roboticsFacility);
				}
			}
		}
	}

	public static boolean weShouldBuildBattleUnits() {
//		if (Constructing.shouldBuildAnyBuilding() != null) {
//			return false;
//		}
//		
//		int numberOfSoldiers = UnitCounter.getNumberOfBattleUnits();
//		
//		if (numberOfSoldiers <= 7) {
//			return true;
//		}
//		else if (
//				!UnitCounter.weHaveBuilding(UnitTypes.Protoss_Cybernetics_Core)
//				|| ProtossAssimilator.shouldBuild()) {
//			return false;
//		}
////		else if (numberOfSoldiers <= 7
////				&& !UnitCounter.weHaveBuilding(UnitTypes.Protoss_Bunker)
////				) {
////			return false;
////		}
		
		return (!ProtossNexus.shouldBuild() || xvr.canAfford(525))
				&& (UnitCounter.weHaveBuilding(UnitTypes.Protoss_Cybernetics_Core)
						|| UnitCounter.getNumberOfBattleUnits() < 3);
	}

}
