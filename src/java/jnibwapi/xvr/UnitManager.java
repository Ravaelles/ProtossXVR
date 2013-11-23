package jnibwapi.xvr;

import jnibwapi.RUtilities;
import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.protoss.ProtossArbiter;
import jnibwapi.protoss.ProtossDarkTemplar;
import jnibwapi.protoss.ProtossNexus;
import jnibwapi.protoss.ProtossObserver;
import jnibwapi.protoss.ProtossReaver;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;

public class UnitManager {

	public static final UnitTypes WORKER = UnitTypes.Protoss_Probe;
	public static final UnitTypes BASE = UnitTypes.Protoss_Nexus;
	public static final UnitTypes GATEWAY = UnitTypes.Protoss_Gateway;

	private static XVR xvr = XVR.getInstance();

	private static int _unitCounter = 0;

	// private static final int UNIT_CLUSTER_WIDTH = 5;
	// private static final int UNIT_CLUSTER_MIN_UNITS = 2;

	public static void act() {
		Unit base = xvr.getFirstBase();
		if (base == null) {
			return;
		}

		// ===============================
		// Act with buildings
		ProtossNexus.act();
		ArmyCreationManager.act();

		// ===============================
		// Act with live units
		// ChokePoint beHere = TerranMapExploration.getNearestChokePointFor(xvr,
		// base);

		// Act with non workers
		for (Unit unit : xvr.getUnitsNonWorker()) {
			act(unit);
		}

		// ===============================
		// Reset variables
		_unitCounter = 0;
		CallForHelp.clearOldOnes();
	}

	private static void act(Unit unit) {
		UnitType unitType = UnitType.getUnitTypeByID(unit.getTypeID());
		if (unitType == null) {
			return;
		}

		// ======================================
		// OVERRIDE COMMANDS FOR SPECIFIC UNITS

		// If unit is Building
		if (unitType.isBuilding()) {
			BuildingManager.act(unit);
		}

		// Observer
		else if (unitType.getID() == UnitTypes.Protoss_Observer.ordinal()) {
			ProtossObserver.act(unit);
		}

		// ======================================
		// STANDARD ARMY UNIT COMMANDS
		else {

			// If unit has personalized order
			if (unit.getCallForHelpMission() != null) {
				actWhenOnCallForHelpMission(unit);
			}

			// Standard action for unit
			else {

				// If we're ready to total attack
				if (MassiveAttack.isAttackPending()) {
					actWhenMassiveAttackIsPending(unit);
				}

				// Standard situation
				else {
					actWhenNoMassiveAttack(unit);
				}
			}

			// ==================================
			// Anti-HERO-One-fights-the-army code
			decideSkirmishIfToFightOrRetreat(unit);

			// ==================================

			// Increase unit counter, so we can know which unit in order it was.
			_unitCounter++;
		}

		// ======================================
		// SPECIFIC ACTIONS for units, but DON'T FULLY OVERRIDE standard behavior

		// Reaver
		if (unitType.getID() == UnitTypes.Protoss_Reaver.ordinal()) {
			ProtossReaver.act(unit);
		}

		// Dark Templar
		else if (unit.getTypeID() == UnitTypes.Protoss_Dark_Templar.ordinal()) {
			ProtossDarkTemplar.act(unit);
			return;
		}
		
		// Arbiter
		else if (unit.getTypeID() == UnitTypes.Protoss_Arbiter.ordinal()) {
			ProtossArbiter.act(unit);
			return;
		}

		avoidSeriousSpellEffectsIfNecessary(unit);
	}

	private static void avoidHiddenUnitsIfNecessary(Unit unit) {
		Unit hiddenEnemyUnitNearby = MapExploration
				.getHiddenEnemyUnitNearbyTo(unit);
		if (hiddenEnemyUnitNearby != null) {
			UnitActions.moveAwayFromUnitIfPossible(unit, hiddenEnemyUnitNearby,
					5);
		}
	}

	private static void avoidSeriousSpellEffectsIfNecessary(Unit unit) {
		if (unit.isUnderStorm() || unit.isUnderDisruptionWeb()
				|| unit.isUnderDarkSwarm()) {
			UnitActions.moveTo(unit,
					unit.getX() + 4 * 32 - RUtilities.rand(0, 8 * 32),
					unit.getY() + 4 * 32 - RUtilities.rand(0, 8 * 32));
		}
	}

	private static void decideSkirmishIfToFightOrRetreat(Unit unit) {
		if (!unit.isAttacking() || !unit.isUnderAttack()
				|| xvr.getDistanceBetween(unit, xvr.getFirstBase()) < 15) {
			return;
		}

		double ourStrengthRatio = StrengthEvaluator
				.calculateOurStrengthRatio(unit);
		if (ourStrengthRatio < 0.9) {
			// System.out.println("RUN! " + unit.getName());
			UnitActions.moveTo(unit, xvr.getFirstBase().getX(), xvr
					.getFirstBase().getY());
		}
	}

	public static void actWithArmyUnitsWhenEnemyNearby() {
		if (MassiveAttack.isRetreatNecessary()) {
			return;
		}

		// Act with non workers
		for (Unit unit : xvr.getUnitsNonWorker()) {
			actTryAttackingCloseEnemyUnits(unit);
		}
	}

	private static void actTryAttackingCloseEnemyUnits(Unit unit) {
		// if (unit.getTargetUnitID() != -1 || unit.getOrderTargetID() != -1
		// || !unit.isMoving()) {
		// return;
		// }

		Unit enemyToAttack;

		// Try selecting top priority units like lurkers, siege tanks.
		Unit importantEnemyUnitNearby = TargetHandling
				.getImportantEnemyUnitTargetIfPossibleFor(unit);
		if (importantEnemyUnitNearby != null) {
			enemyToAttack = importantEnemyUnitNearby;
		}

		// If no such unit is nearby then attack the closest one.
		else {
			enemyToAttack = xvr.getUnitNearestFromList(unit.getX(),
					unit.getY(), xvr.getEnemyUnitsVisible());
		}

		// Attack selected target if it's not too far away.
		if (enemyToAttack != null) {
			int distance = (int) xvr.getDistanceSimple(unit, enemyToAttack);
			if (distance < 19) {
				UnitActions.attackTo(unit, enemyToAttack);
			}
		}
	}

	private static void actWhenOnCallForHelpMission(Unit unit) {
		Unit caller = unit.getCallForHelpMission().getCaller();

		// If already close to the point to be, cancel order.
		if (xvr.getDistanceBetween(unit, caller) <= 3) {
			unit.getCallForHelpMission().unitArrivedToHelp(unit);
		}

		// Still way to go!
		else {
			UnitActions.attackTo(unit, caller.getX(), caller.getY());
		}
	}

	private static boolean actMakeDecisionSomeoneCalledForHelp(Unit unit) {
		for (CallForHelp call : CallForHelp.getThoseInNeedOfHelp()) {
			boolean willAcceptCallForHelp = false;

			// Critical call for help must be accepted
			if (call.isCritical()) {
				willAcceptCallForHelp = true;
			}

			// No critical call, react only if we're not too far
			else if (xvr.getDistanceBetween(call.getCaller(), unit) < 30) {
				willAcceptCallForHelp = true;
			}

			if (willAcceptCallForHelp) {
				call.unitHasAcceptedIt(unit);
				return true;
			}
		}
		return false;
	}

	private static void actWhenNoMassiveAttack(Unit unit) {
		if (shouldUnitBeExplorer(unit)) {
			UnitActions.spreadOutRandomly(unit);
		} else {
			if (isUnitAttackingSomeone(unit)) {
				return;
			}

			// =====================================
			// Possible override of orders if some unit needs help

			// If some unit called for help
			boolean isOnCallForHelpMission = false;
			if (CallForHelp.isAnyCallForHelp()) {
				boolean accepted = actMakeDecisionSomeoneCalledForHelp(unit);
				if (!isOnCallForHelpMission && accepted) {
					isOnCallForHelpMission = true;
				}
			}

			// Call for help isn't active right now
			if (!isOnCallForHelpMission) {
				ArmyPlacing.goToSafePlaceIfNotAlreadyThere(unit);
			}
		}

		// // Now try to hide in nearby bunker if possible
		// if (unit.isNotMedic()) {
		// ProtossCannon.tryToLoadIntoBunker(unit);
		// }
		avoidHiddenUnitsIfNecessary(unit);
	}

	private static boolean shouldUnitBeExplorer(Unit unit) {
		return (_unitCounter == 1 || _unitCounter == 14)
				|| unit.getTypeID() == UnitTypes.Protoss_Dark_Templar.ordinal();
	}

	private static void actWhenMassiveAttackIsPending(Unit unit) {

		// If unit is surrounded by other units (doesn't attack alone)
		// if (isPartOfClusterOfMinXUnits(unit)) {

		// If there is attack target defined, go for it.
		if (MassiveAttack.isSomethingToAttackDefined()) {
			if (isUnitAttackingSomeone(unit)) {
				return;
			}

			if (MassiveAttack.getTargetPoint() != null) {
				// if (MassiveAttack.getTargetPoint() != null
				// && xvr.getDistanceBetween(unit,
				// MassiveAttack.getTargetPoint()) > 4) {
				// if (!unit.isIdle() && unit.isUnderAttack()) {
				if (!isUnitAttackingSomeone(unit)) {
					UnitActions.attackTo(unit,
							MassiveAttack.getTargetPoint().x,
							MassiveAttack.getTargetPoint().y);
				}
				// attackTo(unit, MassiveAttack.getTargetPoint().x,
				// MassiveAttack.getTargetPoint().y);
				// }

				if (isUnitFullyIdle(unit)) {
					// System.out.println("Spread out fully iddle");
					UnitActions.spreadOutRandomly(unit);
				}
			} else {
				// if (shouldSpreadOut(unit)) {
				// Debug.message(xvr, unit.getName() +
				// " spreads out (option 1)");
				UnitActions.spreadOutRandomly(unit);
				// }
			}
		}

		// If no attack target is defined it probably means that the fog
		// of war is hiding from us other enemy buildings
		else {
			// Debug.message(xvr, unit.getName() +
			// " spreads out (option 2)");
			// if (shouldSpreadOut(unit)) {
			UnitActions.spreadOutRandomly(unit);
			// }
		}
		// }
		//
		// // Unit is almost alone, retreat to base in hope to find some
		// // other units
		// else {
		// goToNearestUnitIfNotAlreadyThere(unit);
		// }
	}

	private static boolean isUnitFullyIdle(Unit unit) {
		return !unit.isAttacking() && !unit.isMoving() && !unit.isUnderAttack()
				&& unit.isIdle();
		// && unit.getGroundWeaponCooldown() == 0
	}

	private static boolean isUnitAttackingSomeone(Unit unit) {
		return unit.getOrderTargetID() != -1 || unit.getTargetUnitID() != -1;
	}

	// private static boolean isPartOfClusterOfMinXUnits(Unit unit) {
	// // System.out.println("SIZE CLUSTER = "
	// // + xvr.countUnitsInRadius(unit.getX(), unit.getY(),
	// // UNIT_CLUSTER_WIDTH, true));
	// // return xvr.countUnitsInRadius(unit.getX(), unit.getY(),
	// // UNIT_CLUSTER_WIDTH, true) >= UNIT_CLUSTER_MIN_UNITS;
	// return true;
	// }

}
