package ai.managers;

import java.util.ArrayList;

import jnibwapi.model.Unit;
import ai.core.XVR;
import ai.handling.units.UnitCounter;
import ai.protoss.ProtossGateway;
import ai.protoss.ProtossNexus;
import ai.protoss.ProtossPhotonCannon;
import ai.protoss.ProtossRoboticsFacility;
import ai.protoss.ProtossStargate;

public class ArmyCreationManager {

	private static XVR xvr = XVR.getInstance();

	public static void act() {
		if (weShouldBuildBattleUnits()) {

			// ROBOTICS FACILITY
			ArrayList<Unit> roboticsFacilitiesList = ProtossRoboticsFacility
					.getAllObjects();
			if (!roboticsFacilitiesList.isEmpty()) {
				for (Unit roboticsFacility : roboticsFacilitiesList) {
					ProtossRoboticsFacility.act(roboticsFacility);
				}
			}
			
			// GATEWAY
			ArrayList<Unit> gatewayList = ProtossGateway.getAllObjects();
			if (!gatewayList.isEmpty()) {
				for (Unit gateway : gatewayList) {
					ProtossGateway.act(gateway);
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
		int battleUnits = UnitCounter.getNumberOfBattleUnits();
		
		if (battleUnits <= 6) {
			return true;
		}
		if (!xvr.canAfford(125)) {
			return false;
		}
		if (ProtossNexus.shouldBuild() && !xvr.canAfford(525)) {
			return false;
		}
		if (ProtossPhotonCannon.shouldBuild() && !xvr.canAfford(250)) {
			return false;
		}
		
		return true;
	}

}
