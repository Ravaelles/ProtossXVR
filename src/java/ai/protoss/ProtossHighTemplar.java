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

		if (highTemplar.getEnergy() >= 75) {
			// tryUsingHallucination(highTemplar);
			tryUsingPsionicStorm(highTemplar);
		}

		if (highTemplar.isUnderAttack()) {
			UnitActions.moveTo(highTemplar, xvr.getLastBase());
		}
	}

	private static void tryUsingPsionicStorm(Unit highTemplar) {
		if (highTemplar.getEnergy() >= 75) {
			Unit unitToStrike = null;

			// Try to find top priority target.
			ArrayList<Unit> topPriorityTargets = xvr.getUnitsInRadius(
					highTemplar, 20, xvr.getEnemyUnitsOfType(
							UnitTypes.Protoss_Carrier,
							UnitTypes.Terran_Siege_Tank_Siege_Mode,
							UnitTypes.Terran_Siege_Tank_Tank_Mode,
							UnitTypes.Zerg_Guardian, UnitTypes.Zerg_Lurker));
			if (!topPriorityTargets.isEmpty()) {
				unitToStrike = topPriorityTargets.get(0);
			}

			if (unitToStrike == null) {

				// Find any target
				ArrayList<Unit> enemies = xvr.getUnitsInRadius(highTemplar, 15,
						xvr.getEnemyArmyUnits());
				if (!enemies.isEmpty()) {
					unitToStrike = enemies.get(0);
				}
			}

			// If there's some target, use psionic storm
			if (unitToStrike != null) {
				UnitActions.useTech(highTemplar,
						TechnologyManager.PSIONIC_STORM, unitToStrike);
			}
		}
	}

//	private static void tryUsingHallucination(Unit highTemplar) {
//		if (highTemplar.getEnergy() >= 100) {
//			ArrayList<Unit> ourUnits = xvr
//					.getUnitsOfType(UnitTypes.Protoss_Zealot);
//			Unit sheepDolly = xvr.getUnitNearestFromList(highTemplar, ourUnits);
//			UnitActions.useTech(highTemplar, TechnologyManager.HALLUCINATION,
//					sheepDolly);
//		}
//	}

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
