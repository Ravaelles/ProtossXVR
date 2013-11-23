package jnibwapi.protoss;

import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

public class ProtossReaver {

	private static XVR xvr = XVR.getInstance();

	private static void checkIfBuildScarabs(Unit reaver) {
		if (reaver.getScarabCount() < 4
				|| (reaver.getScarabCount() < 7 && xvr.canAfford(800))) {
			if (xvr.canAfford(25) && reaver.getTrainingQueueSize() == 0) {
				xvr.getBwapi().train(reaver.getID(),
						UnitTypes.Protoss_Scarab.ordinal());
			}
		}
	}

	public static void act(Unit unit) {
		checkIfBuildScarabs(unit);
	}

}
