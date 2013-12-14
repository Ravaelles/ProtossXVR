package ai.handling.units;

import java.util.ArrayList;

import jnibwapi.model.ChokePoint;
import jnibwapi.model.Unit;
import jnibwapi.types.TechType.TechTypes;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import ai.core.XVR;
import ai.handling.army.StrengthEvaluator;
import ai.handling.army.TargetHandling;
import ai.handling.map.MapExploration;
import ai.handling.map.MapPoint;
import ai.managers.StrategyManager;
import ai.managers.UnitManager;
import ai.protoss.ProtossShieldBattery;
import ai.utils.RUtilities;

public class UnitActions {

	private static XVR xvr = XVR.getInstance();

	public static void loadUnitInto(Unit unit, Unit loadTo) {
		if (unit != null && loadTo != null) {
			XVR.getInstance().getBwapi().load(unit.getID(), loadTo.getID());
		}
	}

	public static void moveTo(Unit unit, Unit destination) {
		if (unit == null || destination == null) {
			// System.err.println("moveTo # unit: " + unit + " # destination: "
			// + destination);
			return;
		}
		moveTo(unit, destination.getX(), destination.getY());
	}

	public static void moveTo(Unit unit, MapPoint point) {
		moveTo(unit, point.getX(), point.getY());
	}

	public static void moveTo(Unit unit, int x, int y) {
		XVR.getInstance().getBwapi().move(unit.getID(), x, y);
	}

	public static void attackTo(Unit ourUnit, MapPoint point) {
		if (ourUnit == null || point == null) {
			return;
		}
		attackTo(ourUnit, point.getX(), point.getY());
	}

	public static void attackTo(Unit ourUnit, int x, int y) {
		if (ourUnit != null) {
			xvr.getBwapi().attack(ourUnit.getID(), x, y);
		}
	}

	public static void attackEnemyUnit(Unit ourUnit, Unit enemy) {
		if (ourUnit != null && enemy != null) {
			xvr.getBwapi().attack(ourUnit.getID(), enemy.getID());
		}
	}

	public static void repair(Unit worker, Unit building) {
		if (worker != null && building != null) {
			xvr.getBwapi().repair(worker.getID(), building.getID());
		}
	}

	public boolean canBuildUnit(UnitType type) {
		return canBuildUnit(type.getUnitTypesObject());
	}

	public static boolean canBuildUnit(UnitTypes type) {
		switch (type) {
		case Protoss_Cybernetics_Core:
			return xvr.countUnitsOfType(UnitTypes.Protoss_Cybernetics_Core) > 0;
			// case Protoss_Observatory:
			// for (Unit unit : xvr
			// .getUnitsOfType(UnitManager.BASE)) {
			// if (unit.getAddOnID() <= 0) {
			// return true;
			// }
			// }
			// return false;
		default:
			System.out.println("canBuildUnit ERROR: " + type);
			return false;
		}
	}

	public static void callForHelp(Unit toRescue, boolean critical) {
		CallForHelp.issueCallForHelp(toRescue, critical);
	}

	public static void goToRandomChokePoint(Unit unit) {
		ChokePoint goTo = MapExploration.getRandomChokePoint();
		UnitActions.moveTo(unit, goTo.getCenterX(), goTo.getCenterY());
	}

	public static void moveAwayFromUnitIfPossible(Unit unit,
			MapPoint unitToMoveAwayFrom, int howManyTiles) {
		if (unit == null || unitToMoveAwayFrom == null) {
			return;
		}

		int xDirectionToUnit = unitToMoveAwayFrom.getX() - unit.getX();
		int yDirectionToUnit = unitToMoveAwayFrom.getY() - unit.getY();

		moveTo(unit, unit.getX() - xDirectionToUnit, unit.getY()
				- yDirectionToUnit);
	}

	public static boolean shouldSpreadOut(Unit unit) {
		return unit.isIdle() && !unit.isMoving() && !unit.isAttacking()
				&& !unit.isUnderAttack();
	}

	public static void spreadOutRandomly(Unit unit) {
		if (!StrengthEvaluator.isStrengthRatioFavorableFor(unit)) {
			UnitActions.moveToMainBase(unit);
			return;
		}

		// Act when enemy detector is nearby, run away
		if (!StrategyManager.isAttackPending()
				&& (xvr.isEnemyDetectorNear(unit.getX(), unit.getY()) || xvr
						.isEnemyDefensiveGroundBuildingNear(unit.getX(),
								unit.getY()))) {
			Unit goTo = xvr.getLastBase();
			UnitActions.attackTo(unit, goTo.getX(), goTo.getY());
			return;
		}

		// WORKER: Act when enemy is nearby, run away
		Unit enemyNearby = TargetHandling.getEnemyNearby(unit, 8);
		if (enemyNearby != null && unit.isWorker() && unit.getHitPoints() > 18) {
			Unit goTo = xvr.getFirstBase();
			UnitActions.attackTo(unit, goTo.getX(), goTo.getY());
			return;
		}

		// Look if there's really important unit nearby
		boolean groundAttackCapable = unit.canAttackGroundUnits();
		boolean airAttackCapable = unit.canAttackAirUnits();
		Unit importantEnemyUnit = TargetHandling
				.getImportantEnemyUnitTargetIfPossibleFor(unit,
						groundAttackCapable, airAttackCapable);
		if (importantEnemyUnit != null && importantEnemyUnit.isDetected()) {
			Unit goTo = importantEnemyUnit;
			UnitActions.attackTo(unit, goTo.getX(), goTo.getY());
		}

		// System.out.println("###### SPREAD OUT ########");
		if (!unit.isMoving() && !unit.isUnderAttack()
				&& unit.getHitPoints() > 18) {

			// If distance to current target is smaller than N it means that
			// unit can spread out and scout nearby grounds
			// if (xvr.getDistanceBetween(unit, unit.getTargetX(),
			// unit.getTargetY()) < 38) {
			MapPoint goTo = MapExploration.getNearestUnknownPointFor(
					unit.getX(), unit.getY(), true);
			if (goTo != null
					&& xvr.getBwapi()
							.getMap()
							.isConnected(unit, goTo.getX() / 32,
									goTo.getY() / 32)) {
				UnitActions.attackTo(unit, goTo.getX(), goTo.getY());
			} else {
				UnitActions.attackTo(unit,
						unit.getX() + 1000 - RUtilities.rand(0, 2000),
						unit.getY() + 1000 - RUtilities.rand(0, 2000));
			}
			// }
		} else if (unit.isWorker() && unit.isUnderAttack()) {
			UnitActions.moveTo(unit, xvr.getFirstBase());
		}

		if (unit.isAttacking()
				&& !StrengthEvaluator.isStrengthRatioFavorableFor(unit)) {
			UnitActions.moveToMainBase(unit);
		}
	}

	public static void moveToMainBase(Unit unit) {
		moveTo(unit, xvr.getFirstBase());
	}

	public static boolean runFromEnemyDetectorOrDefensiveBuildingIfNecessary(
			Unit unit, boolean tryAvoidingDetectors,
			boolean allowAttackingDetectorsIfSafe, boolean isAirUnit) {
		final int RUN_DISTANCE = 6;

		// If we should avoid detectors, look for one nearby.
		if (tryAvoidingDetectors) {
			boolean isEnemyDetectorNear = xvr.isEnemyDetectorNear(unit);

			// Okay, we know there's detector near.
			if (isEnemyDetectorNear) {
				boolean canDetectorShootAtThisUnit = false;
				boolean isAttackingDetectorSafe = false;

				// If unit can possibly attack detector safely, even if it's
				// supposed
				// to avoid them, define if this detector can shoot at our unit.
				if (allowAttackingDetectorsIfSafe) {
					Unit enemyDetector = xvr.getEnemyDetectorNear(unit);
					if (isAirUnit) {
						canDetectorShootAtThisUnit = enemyDetector
								.canAttackAirUnits();
					} else {
						canDetectorShootAtThisUnit = enemyDetector
								.canAttackGroundUnits();
					}

					// If detector cannot shoot at this unit, but is surrounded
					// by some enemies then do not attack
					if (!canDetectorShootAtThisUnit) {
						ArrayList<Unit> enemyUnitsNearDetector = xvr
								.getUnitsInRadius(enemyDetector, 9,
										xvr.getEnemyArmyUnits());
						if (enemyUnitsNearDetector.size() <= 1) {
							isAttackingDetectorSafe = true;
						}
					}
				}

				// If unit isn't allowed to attack detectors OR if the detector
				// cannot be attack safely, then run away from it.
				if (!allowAttackingDetectorsIfSafe || !isAttackingDetectorSafe) {

					// Try to move away from this enemy detector on N tiles.
					UnitActions.moveAwayFromUnitIfPossible(unit,
							xvr.getEnemyDetectorNear(unit.getX(), unit.getY()),
							RUN_DISTANCE);
					return true;
				}
			}
		}

		boolean isEnemyBuildingNear = isAirUnit ? xvr
				.isEnemyDefensiveAirBuildingNear(unit.getX(), unit.getY())
				: xvr.isEnemyDefensiveGroundBuildingNear(unit.getX(),
						unit.getY());
		if (isEnemyBuildingNear) {
			Unit enemyBuilding = isAirUnit ? xvr
					.getEnemyDefensiveAirBuildingNear(unit.getX(), unit.getY())
					: xvr.getEnemyDefensiveGroundBuildingNear(unit.getX(),
							unit.getY());
			UnitActions.moveAwayFromUnitIfPossible(unit, enemyBuilding,
					RUN_DISTANCE);
			return true;
		}

		return false;
	}

	public static void actWhenLowHitPointsOrShields(Unit unit,
			boolean isImportantUnit) {
		UnitType type = unit.getType();
		Unit goTo = null;

		if (xvr.getTimeSecond() < 500
				&& UnitCounter
						.getNumberOfUnits(UnitTypes.Protoss_Photon_Cannon) < 2) {
			return;
		}

		if (xvr.getTimeSecond() < 300
				&& xvr.getTimeSecond() < 300
				&& UnitCounter
						.getNumberOfUnits(UnitTypes.Protoss_Photon_Cannon) == 0) {
			return;
		}

		int currShields = unit.getShields();
		int maxShields = type.getMaxShields();
		int currHP = unit.getHitPoints();
		int maxHP = type.getMaxHitPoints();

		// If there's massive attack and unit has more than 60% of initial
		// shields, we treat it as healthy, as there's nothing to do about it.
		if (StrategyManager.isAttackPending()
				&& currShields >= 0.13 * maxShields) {
			if (!isImportantUnit && currHP >= 0.6 * maxHP) {
				return;
			}
		}

		// Unit has almost all shields
		if (currShields >= maxShields / 2) {
			return;
		}

		// If there's bunker
		if (xvr.getUnitsOfGivenTypeInRadius(UnitTypes.Terran_Bunker, 3, unit,
				false).size() > 0) {
			return;
		}

		// If there's bunker
		if (xvr.getUnitsOfGivenTypeInRadius(UnitTypes.Protoss_Photon_Cannon, 3,
				unit, false).size() > 0) {
			return;
		}

		// If there's bunker
		if (xvr.getUnitsOfGivenTypeInRadius(UnitTypes.Zerg_Sunken_Colony, 3,
				unit, false).size() > 0) {
			return;
		}

		// =====================================================================
		// If unit is close to base then run away only if critically wounded.
		if (xvr.countUnitsOfGivenTypeInRadius(UnitManager.BASE, 13, unit, true) >= 1) {
			if (unit.getHitPoints() > (type.getMaxHitPoints() / 3 + 3)) {
				return;
			}
		}

		// =====================================================================

		// First try to go to the nearest shield battery, if exists.
		goTo = ProtossShieldBattery.getOneWithEnergy();
		if (goTo != null && goTo.isUnpowered()) {
			goTo = null;
		}

		if (goTo != null) {

			// We can heal at this point! Right click *should* do it.
			if (goTo.getEnergy() >= 13) {
				UnitActions.rightClick(unit, goTo);
				return;
			}

			// We cannot heal, just go there and hope it will heal the poor unit
			// ;_;
		}

		// Then try to go to cannon nearest to the last base, if exists.
		else {
			goTo = xvr.getUnitOfTypeNearestTo(UnitTypes.Protoss_Photon_Cannon,
					xvr.getLastBase());
		}

		// If not, go to the first base.
		if (goTo == null) {
			goTo = xvr.getFirstBase();
		}

		if (goTo != null) {
			UnitActions.moveTo(unit, goTo);
			// UnitActions.attackTo(unit, goTo);
		}
	}

	private static void rightClick(Unit unit, Unit clickTo) {
		if (unit == null || clickTo == null) {
			System.err.println("rightClick # unit: " + unit + " # clickTo: "
					+ clickTo);
			return;
		}
		xvr.getBwapi().rightClick(unit.getID(), clickTo.getID());
	}

	public static void useTech(Unit wizard, TechTypes tech, Unit useOn) {
		xvr.getBwapi().useTech(wizard.getID(), tech.getID(), useOn.getID());
	}

}
