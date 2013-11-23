package jnibwapi.protoss;

import java.util.ArrayList;

import jnibwapi.RUtilities;
import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.xvr.MapExploration;
import jnibwapi.xvr.MapPoint;
import jnibwapi.xvr.MassiveAttack;
import jnibwapi.xvr.UnitActions;

public class ProtossDarkTemplar {

	private static XVR xvr = XVR.getInstance();

	public static void act(Unit unit) {

		// TOP PRIORITY: Templar under attack must go back to base.
		// But if there's a massive attack and you have other units nearby DON'T
		// retreat
		boolean shouldConsiderRunningAway = true;
		if (MassiveAttack.isAttackPending()) {
			if (xvr.countUnitsInRadius(unit.getX(), unit.getY(), 10, true) >= 4) {
				shouldConsiderRunningAway = false;
			}
		}
		if (shouldConsiderRunningAway
				&& UnitActions
						.runFromEnemyDetectorOrDefensiveBuildingIfNecessary(
								unit, true) || unit.getHitPoints() < 50) {
			return;
		}

//		// TOP PRIORITY: Act when enemy detector is nearby, just run away to
//		// base.
//		if (!MassiveAttack.isAttackPending()
//				&& (xvr.isEnemyDetectorNear(unit.getX(), unit.getY()) || (unit
//						.isDetected() && xvr
//						.isEnemyDefensiveGroundBuildingNear(unit.getX(),
//								unit.getY())))) {
//			Unit goTo = xvr.getLastBase();
//			UnitActions.attackTo(unit, goTo.getX(), goTo.getY());
//			return;
//		}

		// Don't interrupt unit on march
		if ((unit.isAttacking() || unit.isMoving()) && !unit.isUnderAttack()) {
			return;
		}

		// ======== DEFINE NEXT MOVE =============================

		// Get 3 base locations near enemy
		ArrayList<? extends MapPoint> pointForHarassmentNearEnemy = MapExploration
				.getBaseLocationsNear(MapExploration.getRandomKnownEnemyBase(),
						3);
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

}
