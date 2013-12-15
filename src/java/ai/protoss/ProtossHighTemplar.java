package ai.protoss;

import java.util.ArrayList;

import jnibwapi.model.Unit;
import jnibwapi.types.TechType.TechTypes;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;
import ai.handling.army.TargetHandling;
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

		// ===================
		// Check if run from opponents
		boolean shouldRun = highTemplar.isUnderAttack();
		if (!shouldRun && xvr.isEnemyInRadius(highTemplar, 6)) {
			shouldRun = true;
		}

		if (shouldRun) {
			UnitActions.moveTo(highTemplar, xvr.getLastBase());
		}

		// =======================
		// Above all, use spells
		if (highTemplar.getEnergy() >= 75) {
			// tryUsingHallucination(highTemplar);
			tryUsingPsionicStorm(highTemplar);
		}
	}

	private static void tryUsingPsionicStorm(Unit highTemplar) {
		if (highTemplar.getEnergy() >= 75) {
			Unit unitToStrike = null;

			// Try to find top priority target.
			ArrayList<Unit> topPriorityTargets = TargetHandling
					.getTopPriorityTargetsNear(highTemplar, 13);
			if (!topPriorityTargets.isEmpty()) {
				for (Unit possibleTarget : topPriorityTargets) {
					if (!possibleTarget.isUnderStorm()
							&& !possibleTarget.isStasised()) {
						unitToStrike = possibleTarget;
						break;
					}
				}
			}

			if (unitToStrike == null) {

				// Find any target
				ArrayList<Unit> enemies = xvr.getUnitsInRadius(highTemplar, 13,
						xvr.getEnemyArmyUnits());
				for (Unit possibleTarget : enemies) {
					if (xvr.countUnitsInRadius(possibleTarget, 3, xvr
							.getBwapi().getEnemyUnits()) >= 4) {
						unitToStrike = possibleTarget;
						break;
					}

					// If target enemy isn't under other spells...
					if (!possibleTarget.isUnderStorm()
							&& !possibleTarget.isStasised()) {

						// ...and if there's at least one other enemy nearby.
						if (xvr.countUnitsInRadius(possibleTarget, 4, xvr
								.getBwapi().getEnemyUnits()) >= 2
								|| highTemplar.getEnergy() >= 150) {
							unitToStrike = possibleTarget;
							break;
						}
					}
				}
			}

			// If there's some target, use psionic storm
			if (unitToStrike != null) {
				UnitActions.useTech(highTemplar,
						TechnologyManager.PSIONIC_STORM, unitToStrike);
			}
		}
	}

	private static void tryUsingHallucination(Unit highTemplar) {
		if (highTemplar.getEnergy() >= 100) {
			Unit sheepDolly = null;

			if (sheepDolly == null
					|| xvr.getDistanceSimple(highTemplar, sheepDolly) > 20) {
				sheepDolly = xvr.getUnitOfTypeNearestTo(ARCHON, highTemplar);
			}

			if (sheepDolly == null
					|| xvr.getDistanceSimple(highTemplar, sheepDolly) > 20) {
				sheepDolly = xvr.getUnitOfTypeNearestTo(
						UnitTypes.Protoss_Dragoon, highTemplar);
			}

			if (sheepDolly == null
					|| xvr.getDistanceSimple(highTemplar, sheepDolly) > 20) {
				ArrayList<Unit> ourUnits = xvr
						.getUnitsOfType(UnitTypes.Protoss_Zealot);
				sheepDolly = xvr.getUnitNearestFromList(highTemplar, ourUnits);
			}

			if (sheepDolly != null) {
				UnitActions.useTech(highTemplar,
						TechnologyManager.HALLUCINATION, sheepDolly);
			}
		}
	}

	private static boolean tryWarpingArchon(Unit highTemplar) {
		if (UnitCounter.getNumberOfUnits(HIGH_TEMPLAR) >= 2) {

			// If we need to create archons (because there aren't enough)
			if (UnitCounter.getNumberOfUnits(ARCHON) < MINIMAL_ARCHONS) {
				Unit otherHighTemplar = getOtherHighTemplarNear(highTemplar);

				if (otherHighTemplar == null) {
					return false;
				}

				// Check if use existing energy of templars before warping
				if (highTemplar.getEnergy() >= 100) {
					tryUsingHallucination(highTemplar);
				} else if (otherHighTemplar.getEnergy() >= 100) {
					tryUsingHallucination(otherHighTemplar);
				}

				// If both templars don't have too much energy, warp an archon.
				else {
					if (otherHighTemplar != null) {
						xvr.getBwapi().useTech(highTemplar.getID(),
								TechTypes.Archon_Warp.ordinal(),
								otherHighTemplar.getID());
						return true;
					}
				}
			}
		}

		return false;
	}

	private static Unit getOtherHighTemplarNear(Unit highTemplar) {
		ArrayList<Unit> otherTemplars = xvr.getUnitsInRadius(highTemplar, 30,
				xvr.getUnitsOfType(HIGH_TEMPLAR));
		otherTemplars.remove(highTemplar);

		for (Unit otherUnit : otherTemplars) {
			if (otherUnit.getEnergy() < 140
					|| !TechnologyManager
							.isResearched(TechnologyManager.PSIONIC_STORM)) {
				return otherUnit;
			}
		}

		return null;
	}

}
