package jnibwapi.protoss;

import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.xvr.UnitActions;

public class ProtossArbiter {

	private static XVR xvr = XVR.getInstance();

	public static void act(Unit unit) {

		// TOP PRIORITY:
		// Seriously damaged arbiter must go back to main base
		if (unit.getShields() < 10) {
			UnitActions.moveTo(unit, xvr.getUnitOfTypeNearestTo(
					UnitTypes.Protoss_Photon_Cannon, xvr.getLastBase()));
			return;
		}

		// TOP PRIORITY: Arbiter under attack must go back to base.
		if (UnitActions.runFromEnemyDetectorOrDefensiveBuildingIfNecessary(
				unit, true) || unit.isUnderAttack()) {
			return;
		}

	}

}
