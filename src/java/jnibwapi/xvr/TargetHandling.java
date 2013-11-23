package jnibwapi.xvr;

import java.util.Collection;

import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;

public class TargetHandling {

	private static XVR xvr = XVR.getInstance();

	public static Unit getImportantEnemyUnitTargetIfPossibleFor(MapPoint point) {
		Collection<Unit> enemyUnits = xvr.getEnemyBuildings();
		// ArrayList<Unit> enemyUnits = xvr.getUnitsInRadius(point.x, point.y,
		// 25,
		// xvr.getEnemyUnitsVisible());

		// Look for crucial units first
		for (Unit unit : enemyUnits) {
			UnitType type = unit.getType();
			if (unit.isExists()
					&& unit.getHitPoints() > 0
					&& (type.isLurker() || type.isTank() || type.isReaver()
							|| type.isHighTemplar() || (type.isDarkTemplar() && unit
							.isDetected()))
					&& xvr.getDistanceBetween(unit, point) <= 20) {
				if (isProperTarget(unit)) {
					return unit;
				}
			}
		}

		// // Look for standard units
		// for (Unit unit : enemyUnits) {
		// UnitType type = unit.getType();
		// if (unit.isExists() && unit.getHitPoints() > 0 && unit.isVisible()
		// && !type.isLarvaOrEgg()) {
		// return unit;
		// }
		// }

		return null;
	}

	public static Unit findTopPriorityTargetIfPossible(
			Collection<Unit> enemyBuildings) {
		for (Unit unit : enemyBuildings) {
			UnitType type = unit.getType();
			if (type.isBunker() || type.isPhotonCannon()
					|| type.isSunkenColony()) {
				if (isProperTarget(unit)) {
					return unit;
				}
			}
			if (unit.isRepairing() && isProperTarget(unit)) {
				return unit;
			}
		}
		return null;
	}

	public static boolean isProperTarget(Unit target) {
		if (target == null) {
			return false;
		}

		boolean isProper;

		if (target.getType().isBuilding()) {
			isProper = target.isExists() || !target.isVisible();
		} else {
			if (target.isHidden()) {
				return false;
			}

			isProper = target.isExists() || !target.isVisible();
		}

		// if (!isProper) {
		// System.out.println("INCORRECT TARGET = "
		// + (target != null ? target.toStringShort() : target));
		// }

		// if (target.getType().isBuilding()) {
		// isProper = (!target.isVisible() || (target.isVisible() && target
		// .getHitPoints() > 0) && !target.getType().isOnGeyser());
		// } else {
		// isProper = (!target.isVisible() || (target.isVisible()
		// && target.getHitPoints() > 0 && target.isExists()))
		// && !target.getType().isOnGeyser();
		// }

		// if (isProper) {
		// System.out.println("IS PROPER: " + target.toStringShort());
		// }

		return isProper;
	}

	public static Unit findHighPriorityTargetIfPossible(
			Collection<Unit> enemyBuildings) {
		for (Unit unit : enemyBuildings) {
			UnitType type = unit.getType();
			if (type.isSporeColony() || type.isMissileTurret() || type.isBase()) {
				if (isProperTarget(unit)) {
					return unit;
				}
			}
		}
		return null;
	}

	public static Unit findNormalPriorityTargetIfPossible(
			Collection<Unit> enemyBuildings) {
		for (Unit unit : enemyBuildings) {
			UnitType type = unit.getType();
			if (!type.isOnGeyser()) {
				if (isProperTarget(unit)) {
					return unit;
				}
			}
		}
		return null;
	}

	public static Unit getEnemyNearby(Unit unit, int maxTileDistance) {
		Unit nearestEnemy = xvr.getUnitNearestFromList(unit.getX(),
				unit.getY(), xvr.getEnemyUnitsVisible());
		if (nearestEnemy != null
				&& xvr.getDistanceBetween(unit, nearestEnemy) < maxTileDistance) {
			return nearestEnemy;
		} else {
			return null;
		}
	}

}
