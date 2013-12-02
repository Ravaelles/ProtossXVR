package ai.protoss;

import java.util.ArrayList;

import jnibwapi.model.Unit;
import ai.core.XVR;
import ai.handling.units.UnitActions;
import ai.managers.TechnologyManager;

public class ProtossArbiter {

	private static XVR xvr = XVR.getInstance();

	public static void act(Unit unit) {

		// TOP PRIORITY:
		// Seriously damaged arbiter must go back to main base
		if (unit.getShields() <= 15) {
			// UnitActions.moveTo(unit, xvr.getUnitOfTypeNearestTo(
			// UnitTypes.Protoss_Photon_Cannon, xvr.getLastBase()));
			UnitActions.actWhenLowHitPointsOrShields(unit, true);
			return;
		}

		// TOP PRIORITY: Arbiter under attack must go back to base.
		if (UnitActions.runFromEnemyDetectorOrDefensiveBuildingIfNecessary(
				unit, false, true, true) || unit.isUnderAttack()) {
			return;
		}

		// Try to use spells
		tryUsingStasisField(unit);
	}

	private static void tryUsingStasisField(Unit unit) {
		if (unit.getEnergy() >= 100) {
			ArrayList<Unit> enemies = xvr.getUnitsInRadius(unit, 15,
					xvr.getEnemyArmyUnits());
			if (!enemies.isEmpty()) {
				Unit target = null;
				for (Unit possibleTarget : enemies) {
					if (xvr.countUnitsInRadius(possibleTarget, 3, true) == 0) {
						target = possibleTarget;
						break;
					}
				}
				if (target != null) {
					UnitActions.useTech(unit, TechnologyManager.STASIS_FIELD, target);
				}
			}
		}
	}

}
