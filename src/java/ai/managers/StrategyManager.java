package ai.managers;

import java.awt.Point;
import java.util.Collection;
import java.util.Iterator;

import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.Debug;
import ai.core.XVR;
import ai.handling.army.ArmyPlacing;
import ai.handling.army.TargetHandling;
import ai.handling.map.MapExploration;
import ai.handling.units.UnitCounter;

public class StrategyManager {

	private static XVR xvr = XVR.getInstance();

//	private static final int MINIMUM_INITIAL_ARMY_TO_PUSH_ONE_TIME = 5;
//	private static final int MINIMUM_NON_INITIAL_ARMY_TO_PUSH = 25;
//	private static final int MINIMUM_THRESHOLD_ARMY_TO_PUSH = 41;
//	private static final int MINIMUM_ARMY_PSI_USED_THRESHOLD = 75;

	/**
	 * It means we are NOT ready to attack the enemy, because we suck pretty
	 * badly.
	 */
	private static final int STATE_PEACE = 5;

	/** We have approved attack plan, now we should define targets. */
	private static final int STATE_NEW_ATTACK = 7;

	/** Attack is currently pending. */
	private static final int STATE_ATTACK_PENDING = 9;

	/** Attack has failed, we're taking too much losses, retreat somewhere. */
	private static final int STATE_RETREAT = 11;

	// =====================================================

	/**
	 * Current state of attack. Only allowed values are constants of this class
	 * that are prefixed with STATE_XXX.
	 */
	private static int currentState = STATE_PEACE;

	/**
	 * If we are ready to attack it represents pixel coordinates of place, where
	 * our units will move with "Attack" order. It represents place, not the
	 * specific Unit. Units are supposed to attack the neighborhood of this
	 * point.
	 */
	private static Point _attackPoint;

	/**
	 * If we are ready to attack it represents the unit that is the focus of our
	 * armies. Based upon this variable _attackPoint will be defined. If this
	 * value is null it means that we have destroyed this unit/building and
	 * should find next target, so basically it should be almost always
	 * non-null.
	 */
	private static Unit _attackTargetUnit;

	@SuppressWarnings("unused")
	private static int retreatsCounter = 0;
//	private static boolean pushedInitially = false;

	// ====================================================

	private static boolean decideIfWeAreReadyToAttack(boolean forceMinimum) {
		int battleUnits = UnitCounter.getNumberOfBattleUnitsCompleted();
		int minUnits = BotStrategyManager.getMinBattleUnits();
		int dragoons = UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Dragoon);

		if (battleUnits >= minUnits) {
			return true;
		} else {
			return minUnits != 0 && (dragoons >= minUnits * 0.18
					&& isAnyAttackFormPending());
		}

		// if (battleUnits >= MINIMUM_INITIAL_ARMY_TO_PUSH_ONE_TIME
		// && !pushedInitially) {
		// pushedInitially = true;
		// return true;
		// }
		//
		// // If there's more than threshold of psi used, attack. Always.
		// if (xvr.getSuppliesUsed() >= (ProtossNexus.MAX_WORKERS +
		// MINIMUM_ARMY_PSI_USED_THRESHOLD)) {
		// return true;
		// }
		//
		// // If there's more than threshold value of battle units
		// if (battleUnits > MINIMUM_THRESHOLD_ARMY_TO_PUSH) {
		// return true;
		// }
		//
		// int minimumArmyToPush;
		// if (!pushedInitially) {
		// minimumArmyToPush = MINIMUM_INITIAL_ARMY_TO_PUSH_ONE_TIME;
		// pushedInitially = true;
		// return true;
		// } else {
		// minimumArmyToPush = MINIMUM_NON_INITIAL_ARMY_TO_PUSH + 5
		// * retreatsCounter;
		// }
		// boolean weAreReadyToAttack = (battleUnits >= (forceMinimum ?
		// minimumArmyToPush
		// : minimumArmyToPush));
		//
		// if (minimumArmyToPush == MINIMUM_INITIAL_ARMY_TO_PUSH_ONE_TIME
		// || battleUnits >= (1.5 * minimumArmyToPush)
		// * Math.max(1,
		// MapExploration.getNumberOfKnownEnemyBases())) {
		// weAreReadyToAttack = true;
		// }

		// if ((MapExploration.getNumberOfKnownEnemyBases() > 0 && (battleUnits
		// >= (1.5 * MINIMUM_NON_INITIAL_ARMY_TO_PUSH)
		// * MapExploration.getNumberOfKnownEnemyBases()))) {
		// weAreReadyToAttack = true;
		// }

		// // Check if enemy isn't attacking our base; if so, then go back.
		// if (weAreReadyToAttack) {
		// Unit bunker =
		// xvr.getUnitOfTypeNearestTo(UnitTypes.Protoss_Photon_Cannon,
		// xvr.getFirstBase());
		// if (bunker != null) {
		//
		// // Calculate enemy units near our bunker
		// int enemyUnits = xvr.getNumberUnitsInRadius(bunker.getX(),
		// bunker.getY(), 25, xvr.getEnemyUnits());
		// if (enemyUnits >= 5) {
		// return false;
		// }
		// }
		// }

		// return weAreReadyToAttack;
	}

	/**
	 * Decide if full attack makes sense or if we're already attacking decide
	 * whether to retreat, continue attack or to change target.
	 */
	public static void evaluateMassiveAttackOptions() {

		// Currently we are nor attacking, nor retreating.
		if (!isAnyAttackFormPending()) {
			decisionWhenNotAttacking();
		}

		// We are either attacking or retreating.
		if (isAnyAttackFormPending()) {
			decisionWhenAttacking();
		}
	}

	private static void decisionWhenNotAttacking() {

		// According to many different factors decide if we should attack
		// enemy.
		boolean shouldAttack = decideIfWeAreReadyToAttack(true);

		// If we should attack, change the status correspondingly.
		if (shouldAttack) {
			changeStateTo(STATE_NEW_ATTACK);
		} else {
			armyIsNotReadyToAttack();
		}
	}

	private static void decisionWhenAttacking() {

		// If our army is ready to attack the enemy...
		if (isNewAttackState()) {
			changeStateTo(STATE_ATTACK_PENDING);

			// We will try to define place for our army where to attack. It
			// probably will be center around a crucial building like Command
			// Center. But what we really need is the point where to go, not the
			// unit. As long as the point is defined we can attack the enemy.

			// If we don't have defined point where to attack it means we
			// haven't yet decided where to go. So it's the war's very start.
			// Define this assault point now. It would be reasonable to relate
			// it to a particular unit.
			// if (!isPointWhereToAttackDefined()) {
			StrategyManager.defineInitialAttackTarget();
			// }
		}

		// Attack is pending, it's quite "regular" situation.
		if (isAttackPending()) {

			// Now we surely have defined our point where to attack, but it can
			// be so, that the unit which was the target has been destroyed
			// (e.g. just a second ago), so we're standing in the middle of
			// wasteland.
			// In this case define next target.
			// if (!isSomethingToAttackDefined()) {
			defineNextTarget();
			// }

			// Check again if continue attack or to retreat.
			boolean shouldAttack = decideIfWeAreReadyToAttack(false);
			if (!shouldAttack) {
				retreatsCounter++;
				changeStateTo(STATE_RETREAT);
			}
		}

		// If we should retreat... fly you fools!
		if (isRetreatNecessary()) {
			retreat();
		}
	}

	public static void forceRedefinitionOfNextTarget() {
		_attackPoint = null;
		_attackTargetUnit = null;
		defineNextTarget();
	}

	private static void defineNextTarget() {
		Unit target = TargetHandling.getImportantEnemyUnitTargetIfPossibleFor(
				ArmyPlacing.getArmyCenterPoint(), true, true);
		Collection<Unit> enemyBuildings = xvr.getEnemyBuildings();

		// Remove refineries, geysers etc
		for (Iterator<Unit> iterator = enemyBuildings.iterator(); iterator
				.hasNext();) {
			Unit unit = (Unit) iterator.next();
			if (unit.getType().isOnGeyser()) {
				iterator.remove();
			}
		}

		// Try to target some crucial building
		if (!TargetHandling.isProperTarget(target)) {
			target = TargetHandling
					.findTopPriorityTargetIfPossible(enemyBuildings);
		}
		if (!TargetHandling.isProperTarget(target)) {
			target = TargetHandling
					.findHighPriorityTargetIfPossible(enemyBuildings);
		}
		if (!TargetHandling.isProperTarget(target)) {
			target = TargetHandling
					.findNormalPriorityTargetIfPossible(enemyBuildings);
		}

		// If not target found attack the nearest building
		if (!TargetHandling.isProperTarget(target)) {
			Unit base = xvr.getFirstBase();
			if (base == null) {
				return;
			}
			target = xvr.getUnitNearestFromList(base.getX(), base.getY(),
					enemyBuildings);
		}

		// Update the target.
		if (target != null) {
			if (_attackTargetUnit != target) {
				changeNextTargetTo(target);
			} else {
				updateTargetPosition();
			}
		} else {
			_attackPoint = null;
			if (_attackTargetUnit != target && _attackTargetUnit == null) {
				Debug.message(xvr, "Next target is null... =/");
			}
		}

		// System.out.println("_attackTargetUnit = " +
		// _attackTargetUnit.toStringShort());
		// System.out.println("_attackPoint = " + _attackPoint);
		// System.out.println("isProperTarget(target) = " +
		// TargetHandling.isProperTarget(target));
		// System.out.println();
	}

	private static void updateTargetPosition() {
		Point point = new Point(_attackTargetUnit.getX(),
				_attackTargetUnit.getY());
		_attackPoint = point;
	}

	private static void retreat() {
		changeStateTo(STATE_PEACE);
	}

	private static void changeStateTo(int newState) {
		currentState = newState;
		if (currentState == STATE_PEACE || currentState == STATE_NEW_ATTACK) {
			armyIsNotReadyToAttack();
		}
	}

	private static boolean isAnyAttackFormPending() {
		return currentState != STATE_PEACE;
	}

	private static boolean isNewAttackState() {
		return currentState == STATE_NEW_ATTACK;
	}

	public static boolean isAttackPending() {
		return currentState == STATE_ATTACK_PENDING;
	}

	public static boolean isRetreatNecessary() {
		return currentState == STATE_RETREAT;
	}

	public static boolean isSomethingToAttackDefined() {
		// return _attackUnitNeighbourhood != null
		// && _attackUnitNeighbourhood.isExists();
		return _attackTargetUnit != null
				&& !_attackTargetUnit.getType().isOnGeyser();
	}

	private static void armyIsNotReadyToAttack() {
		_attackPoint = null;
		_attackTargetUnit = null;
	}

	private static void defineInitialAttackTarget() {
		// Unit buildingToAttack = MapExploration.getNearestEnemyBase();
		Unit buildingToAttack = MapExploration.getNearestEnemyBuilding();

		// We know some building of CPU that we can attack.
		if (buildingToAttack != null) {
			changeNextTargetTo(buildingToAttack);
		}

		// No building to attack found, safely decide not to attack.
		else {
			changeStateTo(STATE_PEACE);
		}
	}

	private static void changeNextTargetTo(Unit attackTarget) {
		if (attackTarget == null) {
			Debug.message(xvr, "ERROR! ATTACK TARGET UNKNOWN!");
			return;
		}
		// Debug.message(xvr, "Next to attack: "
		// + attackTarget.getType().getName());

		_attackTargetUnit = attackTarget;
		updateTargetPosition();
	}

	public static Unit getTargetUnit() {
		return _attackTargetUnit;
	}

	public static Point getTargetPoint() {
		return _attackPoint;
	}

	
	public static void forcePeace() {
		changeStateTo(STATE_PEACE);
	}

}
