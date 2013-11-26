package ai.managers;

import java.util.ArrayList;

import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;
import ai.handling.units.UnitCounter;
import ai.protoss.ProtossGateway;
import ai.protoss.ProtossNexus;
import ai.protoss.ProtossRoboticsFacility;
import ai.protoss.ProtossStargate;

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
			ArrayList<Unit> roboticsFacilitiesList = ProtossRoboticsFacility
					.getAllObjects();
			if (!roboticsFacilitiesList.isEmpty()) {
				for (Unit roboticsFacility : roboticsFacilitiesList) {
					ProtossRoboticsFacility.act(roboticsFacility);
				}
			}

			// STARGATE
			ArrayList<Unit> buildingsList = ProtossStargate.getAllObjects();
			if (!buildingsList.isEmpty()) {
				for (Unit stargate : buildingsList) {
					ProtossStargate.act(stargate);
				}
			}
		}
	}

	public static boolean weShouldBuildBattleUnits() {
		// if (Constructing.shouldBuildAnyBuilding() != null) {
		// return false;
		// }
		//
		// int numberOfSoldiers = UnitCounter.getNumberOfBattleUnits();
		//
		// if (numberOfSoldiers <= 7) {
		// return true;
		// }
		// else if (
		// !UnitCounter.weHaveBuilding(UnitTypes.Protoss_Cybernetics_Core)
		// || ProtossAssimilator.shouldBuild()) {
		// return false;
		// }
		// // else if (numberOfSoldiers <= 7
		// // && !UnitCounter.weHaveBuilding(UnitTypes.Protoss_Bunker)
		// // ) {
		// // return false;
		// // }

		int battleUnits = UnitCounter.getNumberOfBattleUnits();
		
		// If enemy is Protoss, force more units
		if (xvr.getENEMY().isProtoss()) {
			if (battleUnits <= 9) {
				return true;
			}
		}
		
		return xvr.canAfford(700)
				|| (!ProtossNexus.shouldBuild() || xvr.canAfford(525) || battleUnits < 10)
				&& (UnitCounter
						.weHaveBuilding(UnitTypes.Protoss_Cybernetics_Core) || battleUnits < 5);
	}

}
