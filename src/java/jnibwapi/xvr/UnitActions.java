package jnibwapi.xvr;

import jnibwapi.RUtilities;
import jnibwapi.XVR;
import jnibwapi.model.ChokePoint;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;

public class UnitActions {

	private static XVR xvr = XVR.getInstance();

	public static void loadUnitInto(Unit unit, Unit loadTo) {
		if (unit != null && loadTo != null) {
			XVR.getInstance().getBwapi().load(unit.getID(), loadTo.getID());
		}
	}

	public static void moveTo(Unit unit, Unit destination) {
		moveTo(unit, destination.getX(), destination.getY());
	}

	public static void moveTo(Unit unit, MapPoint point) {
		moveTo(unit, point.getX(), point.getY());
	}

	public static void moveTo(Unit unit, int x, int y) {
		XVR.getInstance().getBwapi().move(unit.getID(), x, y);
	}

	public static void attackTo(Unit ourUnit, Unit enemyUnit) {
		attackTo(ourUnit, enemyUnit.getX(), enemyUnit.getY());
	}

	public static void attackTo(Unit ourUnit, int x, int y) {
		if (ourUnit != null) {
			xvr.getBwapi().attack(ourUnit.getID(), x, y);
		}
	}

	public static void attackEnemyUnit(Unit ourUnit, Unit enemy) {
		if (ourUnit != null) {
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
			Unit unitToMoveAwayFrom, int howManyTiles) {
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

		// Act when enemy detector is nearby, run away
		if (!MassiveAttack.isAttackPending()
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
		Unit importantEnemyUnit = TargetHandling
				.getImportantEnemyUnitTargetIfPossibleFor(unit);
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
	}

	public static boolean runFromEnemyDetectorOrDefensiveBuildingIfNecessary(
			Unit unit, boolean isAirUnit) {
		boolean isEnemyDetector = xvr.isEnemyDetectorNear(unit.getX(),
				unit.getY());
		if (isEnemyDetector) {

			// Try to move away from this enemy detector on N tiles.
			UnitActions.moveAwayFromUnitIfPossible(unit,
					xvr.getEnemyDetectorNear(unit.getX(), unit.getY()), 7);
			return true;
		}

		boolean isEnemyBuilding = isAirUnit ? xvr
				.isEnemyDefensiveAirBuildingNear(unit.getX(), unit.getY())
				: xvr.isEnemyDefensiveGroundBuildingNear(unit.getX(),
						unit.getY());
		if (isEnemyBuilding) {
			Unit enemyBuilding = isAirUnit ? xvr
					.getEnemyDefensiveAirBuildingNear(unit.getX(), unit.getY())
					: xvr.getEnemyDefensiveGroundBuildingNear(unit.getX(),
							unit.getY());
			UnitActions.moveAwayFromUnitIfPossible(unit, enemyBuilding, 7);
			return true;
		}

		return false;
	}

}
