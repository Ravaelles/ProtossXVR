package ai.protoss;

import java.util.ArrayList;

import jnibwapi.model.Unit;
import ai.core.XVR;
import ai.handling.map.MapExploration;
import ai.handling.map.MapPoint;
import ai.handling.units.UnitActions;
import ai.managers.StrategyManager;
import ai.utils.RUtilities;

public class ProtossDarkTemplar {

	private static XVR xvr = XVR.getInstance();

	public static void act(Unit unit) {

		// TOP PRIORITY: Templar under attack must go back to base.
		// But if there's a massive attack and you have other units nearby DON'T
		// retreat
		boolean shouldConsiderRunningAway = true;
		if (StrategyManager.isAttackPending()) {
			if (xvr.countUnitsInRadius(unit.getX(), unit.getY(), 10, true) >= 4) {
				shouldConsiderRunningAway = false;
			}
		}
		if (shouldConsiderRunningAway
				&& UnitActions
						.runFromEnemyDetectorOrDefensiveBuildingIfNecessary(
								unit, true, true) || unit.getHitPoints() < 50) {
			return;
		}

		// // TOP PRIORITY: Act when enemy detector is nearby, just run away to
		// // base.
		// if (!MassiveAttack.isAttackPending()
		// && (xvr.isEnemyDetectorNear(unit.getX(), unit.getY()) || (unit
		// .isDetected() && xvr
		// .isEnemyDefensiveGroundBuildingNear(unit.getX(),
		// unit.getY())))) {
		// Unit goTo = xvr.getLastBase();
		// UnitActions.attackTo(unit, goTo.getX(), goTo.getY());
		// return;
		// }

		// Don't interrupt unit on march
		if ((unit.isAttacking() || unit.isMoving()) && !unit.isUnderAttack()) {
			return;
		}

		// ======== DEFINE NEXT MOVE =============================

		// Get 3 base locations near enemy, or buildings and try to go there.
		MapPoint pointToHarass = defineNeighborhoodToHarass(unit);

		ArrayList<MapPoint> pointForHarassmentNearEnemy = new ArrayList<>();
		pointForHarassmentNearEnemy.addAll(MapExploration.getBaseLocationsNear(
				pointToHarass, 30));
		pointForHarassmentNearEnemy.addAll(MapExploration.getChokePointsNear(
				pointToHarass, 30));

		MapPoint goTo = null;
		if (!pointForHarassmentNearEnemy.isEmpty()) {

			// Randomly choose one of them.
			goTo = (MapPoint) RUtilities
					.getRandomListElement(pointForHarassmentNearEnemy);
		}

		else {
			goTo = MapExploration.getNearestUnknownPointFor(unit.getX(),
					unit.getY(), true);
			if (goTo != null
					&& xvr.getBwapi()
							.getMap()
							.isConnected(unit, goTo.getX() / 32,
									goTo.getY() / 32)) {
			}
		}

		// Attack this randomly chosen base location.
		UnitActions.attackTo(unit, goTo.getX(), goTo.getY());
	}

	private static MapPoint defineNeighborhoodToHarass(Unit unit) {

		// Try to get random base
		MapPoint pointToHarass = MapExploration.getRandomKnownEnemyBase();

		// If we don't know any base, get random building
		if (pointToHarass == null) {
			pointToHarass = MapExploration.getNearestEnemyBuilding();
		}

		// If still nothing...
		if (pointToHarass == null) {
			pointToHarass = MapExploration.getRandomChokePoint();
		}

		return pointToHarass;
	}
}
