package ai.managers;

import java.util.ArrayList;

import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;
import ai.handling.army.ArmyPlacing;
import ai.handling.army.StrengthEvaluator;
import ai.handling.army.TargetHandling;
import ai.handling.map.MapExploration;
import ai.handling.units.CallForHelp;
import ai.handling.units.UnitActions;
import ai.protoss.ProtossArbiter;
import ai.protoss.ProtossDarkTemplar;
import ai.protoss.ProtossHighTemplar;
import ai.protoss.ProtossObserver;
import ai.protoss.ProtossReaver;
import ai.utils.RUtilities;

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

		// Flying unit
		if (unitType.isFlyer()) {

			// TOP PRIORITY: Act when enemy detector or some AA building is
			// nearby:
			// just run away, no matter what.
			if (UnitActions.runFromEnemyDetectorOrDefensiveBuildingIfNecessary(
					unit, false, true)) {
				return;
			}
		}

		// Wounded units should avoid being killed if possible
		handleWoundedUnitBehaviourIfNecessary(unit);

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
				if (StrategyManager.isAttackPending()) {
					actWhenMassiveAttackIsPending(unit);
				}

				// Standard situation
				else {
					actWhenNoMassiveAttack(unit);
				}
			}

			// If unit is still idle, try to do something
			actWhenUnitIsStillIdle(unit);

			// ==================================
			// Anti-HERO-One-fights-the-army code
			decideSkirmishIfToFightOrRetreat(unit);

			// ==================================

			// Increase unit counter, so we can know which unit in order it was.
			_unitCounter++;
		}

		// ======================================
		// SPECIFIC ACTIONS for units, but DON'T FULLY OVERRIDE standard
		// behavior

		// Reaver
		if (unitType.getID() == UnitTypes.Protoss_Reaver.ordinal()) {
			ProtossReaver.act(unit);
		}

		// Dark Templar
		else if (unit.getTypeID() == UnitTypes.Protoss_Dark_Templar.ordinal()) {
			ProtossDarkTemplar.act(unit);
			return;
		}

		// High Templar
		else if (unit.getTypeID() == UnitTypes.Protoss_High_Templar.ordinal()) {
			ProtossHighTemplar.act(unit);
			return;
		}

		// Arbiter
		else if (unit.getTypeID() == UnitTypes.Protoss_Arbiter.ordinal()) {
			ProtossArbiter.act(unit);
			return;
		}

		avoidSeriousSpellEffectsIfNecessary(unit);
	}

	private static void actWhenUnitIsStillIdle(Unit unit) {

		// get enemies "nearby"
		ArrayList<Unit> enemiesNearby = xvr.getUnitsInRadius(unit.getX(),
				unit.getY(), 25, xvr.getEnemyUnitsVisible());
		if (!enemiesNearby.isEmpty()) {
			Unit enemy = enemiesNearby.get(0);

			// If you have less than X to the enemy, attack him
			if (xvr.getDistanceBetween(unit, enemy) <= 20) {
				UnitActions.attackTo(unit, enemy);
			}
		}
	}

	private static void handleWoundedUnitBehaviourIfNecessary(Unit unit) {
		if (unit.getHitPoints() <= 30
				|| unit.getShields() <= unit.getType().getMaxShields() / 2) {

			// Now, it doesn't make sense to run away if we're close to some
			// bunker or cannon and we're lonely. In this case it's better to
			// attack, rather than retreat without causing any damage.
			if (isInSuicideShouldFightPosition(unit)) {
				return;
			}

			// If there are tanks nearby, DON'T RUN. Rather die first!
			if (xvr.countUnitsOfGivenTypeInRadius(
					UnitTypes.Terran_Siege_Tank_Siege_Mode, 15, unit.getX(),
					unit.getY(), false) > 0) {
				return;
			}

			UnitActions.actWhenLowHitPointsOrShields(unit, false);
		}
	}

	private static boolean isInSuicideShouldFightPosition(Unit unit) {

		// Only if we're only unit in this region it makes sense to die.
		int alliesNearby = -1 + xvr.countUnitsInRadius(unit, 4, true);
		if (alliesNearby <= 0) {
			if (xvr.isEnemyDefensiveGroundBuildingNear(unit)) {
				return true;
			}
		}

		return false;
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

		if (unit.getType().isDarkTemplar() || unit.getType().isObserver()) {
			if (!unit.isDetected()) {
				return;
			}
		}

		if (xvr.countUnitsInRadius(unit, 7, true) >= 2) {
			return;
		}

		double ourStrengthRatio = StrengthEvaluator
				.calculateOurStrengthRatio(unit);
		if (ourStrengthRatio < 0.6) {
			// System.out.println("RUN! " + unit.getName());
			UnitActions.moveTo(unit, xvr.getFirstBase().getX(), xvr
					.getFirstBase().getY());
		}
	}

	public static void actWithArmyUnitsWhenEnemyNearby() {
		if (StrategyManager.isRetreatNecessary()) {
			return;
		}

		// Act with non workers
		for (Unit unit : xvr.getUnitsNonWorker()) {
			UnitType type = unit.getType();
			if (!type.isObserver()) {
				actTryAttackingCloseEnemyUnits(unit);
			}
		}
	}

	private static void actTryAttackingCloseEnemyUnits(Unit unit) {
		// if (unit.getTargetUnitID() != -1 || unit.getOrderTargetID() != -1
		// || !unit.isMoving()) {
		// return;
		// }
		boolean groundAttackCapable = unit.canAttackGroundUnits();
		boolean airAttackCapable = unit.canAttackAirUnits();

		// Disallow wounded units to attack distant targets.
		if (unit.getShields() < 15
				|| (unit.getShields() < 40 && unit.getHitPoints() < 40)) {
			return;
		}

		Unit enemyToAttack;

		// Try selecting top priority units like lurkers, siege tanks.
		Unit importantEnemyUnitNearby = TargetHandling
				.getImportantEnemyUnitTargetIfPossibleFor(unit,
						groundAttackCapable, airAttackCapable);
		if (importantEnemyUnitNearby != null) {
			enemyToAttack = importantEnemyUnitNearby;
		}

		// If no such unit is nearby then attack the closest one.
		else {
			enemyToAttack = xvr.getUnitNearestFromList(unit.getX(),
					unit.getY(), xvr.getEnemyUnitsVisible(groundAttackCapable,
							airAttackCapable));
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
		if (StrategyManager.isSomethingToAttackDefined()) {
			if (isUnitAttackingSomeone(unit)) {
				return;
			}

			if (StrategyManager.getTargetPoint() != null) {
				// if (MassiveAttack.getTargetPoint() != null
				// && xvr.getDistanceBetween(unit,
				// MassiveAttack.getTargetPoint()) > 4) {
				// if (!unit.isIdle() && unit.isUnderAttack()) {
				if (!isUnitAttackingSomeone(unit)) {
					UnitActions.attackTo(unit,
							StrategyManager.getTargetPoint().x,
							StrategyManager.getTargetPoint().y);
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
