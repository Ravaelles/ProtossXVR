package ai.protoss;

import java.util.ArrayList;

import jnibwapi.model.Unit;
import jnibwapi.types.TechType.TechTypes;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;
import ai.handling.units.UnitActions;
import ai.handling.units.UnitCounter;
import ai.managers.TechnologyManager;

public class ProtossHighTemplar {

	private static final int MINIMAL_ARCHONS = 5;

	private static final UnitTypes HIGH_TEMPLAR = UnitTypes.Protoss_High_Templar;
	private static final UnitTypes ARCHON = UnitTypes.Protoss_Archon;

	private static XVR xvr = XVR.getInstance();

	public static void act(Unit highTemplar) {

		// TOP PRIORITY:
		// Seriously damaged arbiter must go back to main base
		if (highTemplar.getShields() <= 25) {
			UnitActions.actWhenLowHitPointsOrShields(highTemplar, true);
			return;
		}

		// Try to create Archon. We need at least two high templars.
		if (tryWarpingArchon(highTemplar)) {
			return;
		}

		if (xvr.isEnemyInRadius(highTemplar, 20)) {
			tryUsingHallucination(highTemplar);
		}
	}

	private static void tryUsingHallucination(Unit highTemplar) {
		if (highTemplar.getEnergy() >= 100) {
			ArrayList<Unit> ourUnits = xvr
					.getUnitsOfType(UnitTypes.Protoss_Zealot);
			Unit sheepDolly = xvr.getUnitNearestFromList(highTemplar, ourUnits);
			xvr.getBwapi()
					.useTech(highTemplar.getID(),
							TechnologyManager.HALLUCINATION.getID(),
							sheepDolly.getID());
		}
	}

	private static boolean tryWarpingArchon(Unit highTemplar) {
		if (UnitCounter.getNumberOfUnits(HIGH_TEMPLAR) >= 2) {

			// If we need to create archons (because there aren't enough)
			if (UnitCounter.getNumberOfUnits(ARCHON) < MINIMAL_ARCHONS) {
				ArrayList<Unit> otherTemplars = xvr
						.getUnitsOfType(HIGH_TEMPLAR);
				otherTemplars.remove(highTemplar);
				Unit otherHighTemplar = xvr.getUnitNearestFromList(
						highTemplar.getX(), highTemplar.getY(), otherTemplars);
				if (otherHighTemplar != null) {
					xvr.getBwapi().useTech(highTemplar.getID(),
							TechTypes.Archon_Warp.ordinal(),
							otherHighTemplar.getID());
					return true;
				}
			}
		}

		return false;
	}
}
