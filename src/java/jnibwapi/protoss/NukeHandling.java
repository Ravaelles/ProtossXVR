package jnibwapi.protoss;

import java.awt.Point;
import java.util.ArrayList;

import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitCommandType.UnitCommandTypes;

public class NukeHandling {

	public static Point nuclearDetectionPoint = null;
	
	private static XVR xvr = XVR.getInstance();

	public static void nukeDetected(int x, int y) {		
		// ######
		// ###### X AND Y ARE USUALLY JUST MISSILE SILOS !!!!!!
		// ######
		
		nuclearDetectionPoint = new Point(x, y);
		Point probableGhostLocation = null;
		
		// Only ghost can release nuke so get all enemy ghosts known
		ArrayList<Unit> enemyGhostsKnown = new ArrayList<Unit>();
		Unit motherfucker = null;
		for (Unit unit : xvr.getBwapi().getEnemyUnits()) {
			if (unit.getType().isGhost()) {
				enemyGhostsKnown.add(unit);
				if (unit.getLastCommandID() == UnitCommandTypes.Use_Tech_Position.ordinal()) {
					motherfucker = unit;
					System.out.println("## MOTHERFUCKER FOUND");
					break;
				}
			}
		}
		
		// Tough situation: we don't know of any enemy ghost; try to scan x,y... =/
		if (enemyGhostsKnown.isEmpty() && motherfucker == null) {
			ProtossObserver.tryToScanPoint(x, y);
			System.out.println("## HOPELESS NUKE CASE!");
		}
		else {
			if (motherfucker != null) {
				probableGhostLocation = new Point(motherfucker.getX(), motherfucker.getY());
			}
			else {
				Unit someGhost = enemyGhostsKnown.get(0);
				System.out.println("## TRYING TO GUESS THAT THIS IS: " + someGhost);
				probableGhostLocation = new Point(someGhost.getX(), someGhost.getY());
			}
			ProtossObserver.tryToScanPoint(x, y);
		}
		
		// Send all units from given radius to fight the bastard!
		if (probableGhostLocation != null) {
			ArrayList<Unit> armyUnitsNearby = xvr.getArmyUnitsInRadius(
					probableGhostLocation.x, probableGhostLocation.y, 40, true);
			System.out.println("## ATTACKING NUKE PLACE WITH: " + armyUnitsNearby.size()
					+ " SOLDIERS!");
			for (Unit unit : armyUnitsNearby) {
				UnitActions.attackTo(unit, probableGhostLocation.x, probableGhostLocation.y);
			}
		}
		else {
			System.out.println("## GHOST POSITION UNKNOWN");
		}
	}

}
