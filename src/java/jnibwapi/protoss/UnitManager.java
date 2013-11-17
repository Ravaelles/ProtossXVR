package jnibwapi.protoss;

import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;

public class UnitManager {

	public static final UnitTypes WORKER = UnitTypes.Protoss_Probe;
	public static final UnitTypes BASE = UnitTypes.Protoss_Nexus;

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
		// Specific actions for units, but don't override standard behavior
		
		// Reaver
		if (unitType.getID() == UnitTypes.Protoss_Reaver.ordinal()) {
			ProtossReaver.act(unit);
		}
		
		// ======================================

		// If unit is Building
		if (unitType.isBuilding()) {
			BuildingManager.act(unit);
		}

		// Observer
		else if (unitType.getID() == UnitTypes.Protoss_Observer.ordinal()) {
			ProtossObserver.act(unit);
		}

		// Standard army unit
		else {
			
			// ==================================================

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

			// // If there's an enemy close, attack him
			// actAttackEnemyIfCloseTo(unit);

			// Increase unit counter, so we can know which unit in order it was.
			_unitCounter++;
		}
	}

	public static void actWithArmyUnitsWhenEnemyNearby() {
		if (MassiveAttack.isRetreatNecessary()) {
			return;
		}
		
		// Act with non workers
		for (Unit unit : xvr.getUnitsNonWorker()) {
			actAttackEnemyIfCloseTo(unit);
		}
	}

	private static void actAttackEnemyIfCloseTo(Unit unit) {
		if (unit.getTargetUnitID() != -1 || unit.getOrderTargetID() != -1
				|| !unit.isMoving()) {
			return;
		}

		Unit nearestEnemy = xvr.getUnitNearestFromList(unit.getX(),
				unit.getY(), xvr.getEnemyUnitsVisible());
		if (nearestEnemy != null) {
			int distance = (int) xvr.getDistanceBetween(unit, nearestEnemy);
			if (distance < 12) {
				UnitActions.attackEnemyUnit(unit, nearestEnemy);
			}
		}
	}

	private static void actWhenOnCallForHelpMission(Unit unit) {
		Unit caller = unit.getCallForHelpMission().getCaller();

		// If already close to the point to be, cancel order.
		if (xvr.getDistanceBetween(unit, caller) <= 3) {
			System.out.println("xvr.getDistanceBetween(unit, caller) = " + xvr.getDistanceBetween(unit, caller));
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
			ArmyPlacing.spreadOutRandomly(unit);
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
			} else {
				// if (shouldSpreadOut(unit)) {
				// Debug.message(xvr, unit.getName() +
				// " spreads out (option 1)");
				ArmyPlacing.spreadOutRandomly(unit);
				// }
			}
		}

		// If no attack target is defined it probably means that the fog
		// of war is hiding from us other enemy buildings
		else {
			// Debug.message(xvr, unit.getName() +
			// " spreads out (option 2)");
			// if (shouldSpreadOut(unit)) {
			ArmyPlacing.spreadOutRandomly(unit);
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
