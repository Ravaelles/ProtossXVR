package jnibwapi.protoss;

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

	public static void moveTo(Unit unit, int x, int y) {
		XVR.getInstance().getBwapi().move(unit.getID(), x, y);
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

		moveTo(unit, unit.getX() + xDirectionToUnit, unit.getY()
				+ yDirectionToUnit);
	}

}
