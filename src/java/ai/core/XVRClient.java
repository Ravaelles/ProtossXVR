package ai.core;

import java.util.ArrayList;
import java.util.HashMap;

import jnibwapi.BWAPIEventListener;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.Player;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitDamages;
import jnibwapi.types.UnitType;
import ai.handling.map.MapExploration;
import ai.handling.other.NukeHandling;
import ai.managers.StrategyManager;
import ai.protoss.ProtossGateway;
import ai.protoss.ProtossNexus;
import ai.protoss.ProtossObserver;

public class XVRClient implements BWAPIEventListener {

	private JNIBWAPI bwapi;
	private XVR xvr;

	private ArrayList<Integer> historyOfOurUnits = new ArrayList<>(400);
	private HashMap<Integer, UnitType> historyOfOurUnitsObjects = new HashMap<>();

	// =========================================

	public JNIBWAPI getBwapi() {
		return bwapi;
	}

	public static void main(String[] args) {
		new XVRClient();
	}

	public XVRClient() {
		bwapi = new JNIBWAPI(this);
		xvr = new XVR(this);

		bwapi.start();
	}

	// =========================================

	@Override
	public void connected() {
		bwapi.loadTypeData();
	}

	@Override
	public void gameStarted() {

		// Game settings
		bwapi.enableUserInput();
		bwapi.setGameSpeed(XVR.GAME_SPEED);
		bwapi.loadMapData(true);

		// ========================================

		XVR.SELF = bwapi.getSelf();
		XVR.SELF_ID = bwapi.getSelf().getID();

		Player enemy = bwapi.getEnemies().get(0);
		XVR.setENEMY(enemy);
		XVR.ENEMY_ID = enemy.getID();

		// ========================================

		// Creates map where values of attacks of all unit types are stored.
		UnitDamages.rememberUnitDamageValues();

		// Removes some of initial choke points e.g. those on the edge of the
		// map.
		MapExploration.processInitialChokePoints();

		// ========================================

		// Enemy -> Protoss
		if (enemy.getRaceID() == 2) {
			XVR.setENEMY_RACE("Protoss");
			ProtossGateway.enemyIsProtoss();
		}
		// ENEMY -> Terran
		else if (enemy.getRaceID() == 1) {
			XVR.setENEMY_RACE("Terran");
			ProtossGateway.enemyIsTerran();
		}
		// ENEMY -> Zerg
		else if (enemy.getRaceID() == 0) {
			XVR.setENEMY_RACE("Zerg");
		}

		// ==========
		// HotFix
		ProtossNexus.initialMineralGathering();
		
		// =========
		
		Debug.message(xvr, "#########################", false);
		Debug.message(xvr, "## They see mee warping ###", false);
		Debug.message(xvr, "###### They hating ########", false);
		Debug.message(xvr, "#########################", false);
	}

	@Override
	public void gameUpdate() {
		// if (xvr.getTime() % 10 == 0) {
		// }
		Debug.drawDebug(xvr);

		xvr.act();
	}

	public void gameEnded() {
	}

	public void keyPressed(int keyCode) {
	}

	public void matchEnded(boolean winner) {
		Debug.message(xvr, "###############", false);
		Debug.message(xvr, "## For Adun! ##", false);
		Debug.message(xvr, "###############", false);
	}

	public void sendText(String text) {
	}

	public void receiveText(String text) {
		Debug.message(xvr, "sorry, can't talk right now");
		Debug.message(xvr, "i have to click very fast, u kno");
	}

	public void nukeDetect(int x, int y) {
		System.out.println("DETECTED NUKE AT: " + x + ", " + y);
		NukeHandling.nukeDetected(x, y);
	}

	public void nukeDetect() {
		System.out.println("DETECTED NUKE OVERALL");
	}

	public void playerLeft(int playerID) {
		Debug.message(xvr, "########################", false);
		Debug.message(xvr, "## Sayonara, gringo! ^_^ ##", false);
		Debug.message(xvr, "########################", false);
	}

	public void unitCreate(int unitID) {
		xvr.unitCreated(unitID);
		Unit unit = bwapi.getUnit(unitID);
		UnitType unitType = unit.getType();
		if (!unit.isEnemy()) {
			historyOfOurUnits.add(unitID);
			historyOfOurUnitsObjects.put(unitID, unit.getType());
//			if (unitType.isBuilding() && unitType.isBase()) {
//
//				// Build pylon nearby
//				Constructing.forceConstructingPylonNear(unit);
//			}
		}
		// if (unitType.isBuilding()) {
		// TerranConstructing.removeIsBeingBuilt(unitType);
		// }
		
		if (unit.isMyUnit() && unitType.isBase()) {
			ProtossNexus.updateNextBaseToExpand();
		}
	}

	public void unitDestroy(int unitID) {
		boolean wasOurUnit = historyOfOurUnits.contains(unitID);
		if (wasOurUnit) {
			Debug.ourDeaths++;
		} else {
			Debug.enemyDeaths++;
		}

		if (!wasOurUnit) {
			// System.out.println("DESTROYED: " + unitID);
			boolean removedSomething = MapExploration
					.enemyUnitDestroyed(unitID);

			// Check if massive attack target has just been destroyed; if so,
			// redefine it.
			if (removedSomething && StrategyManager.getTargetUnit() != null
					&& StrategyManager.getTargetUnit().getID() == unitID) {
				// System.out.println("REDIFINING... " +
				// MassiveAttack.getTargetUnit().toStringShort());
				StrategyManager.forceRedefinitionOfNextTarget();
			}
		}

		// =====================================
		// Check what type was the destroyed unit
		UnitType unitType = null;
		for (int historyUnitID : historyOfOurUnitsObjects.keySet()) {
			if (historyUnitID == unitID) {
				unitType = historyOfOurUnitsObjects.get(historyUnitID);
				break;
//				System.out.println();
//				System.out.println("Destroyed unit was " + unitType);
			}
		}
		
		if (unitType != null) {
			if (unitType.isBase() && wasOurUnit) {
				ProtossNexus.updateNextBaseToExpand();
			}
		}
		
//		 Unit unit = Unit.getByID(unitID);
//		 if (unit == null) {
//		 return;
//		 }
//		 System.out.println("Destroyed unit was " + unit.toStringShort());
		// UnitType unitType = UnitType.getUnitTypeByID(unit.getTypeID());
	}

	public void unitDiscover(int unitID) {
		Unit unit = Unit.getByID(unitID);
		if (unit == null || !unit.isEnemy()) {
			return;
		}

		// Add info that we discovered enemy unit
		MapExploration.enemyUnitDiscovered(unit);

		// System.out.println("Unit discover: " + (unit != null ? unit.getName()
		// : "null"));
	}

	public void unitEvade(int unitID) {
		Unit unit = Unit.getByID(unitID);
		if (unit == null || !unit.isEnemy()) {
			return;
		}
//		System.out.println("Unit evade: "
//				+ (unit != null ? unit.getName() : "null"));
	}

	public void unitHide(int unitID) {
		Unit unit = Unit.getByID(unitID);
		if (unit == null || !unit.isEnemy()) {
			return;
		}

		// System.out.println("Unit hide: "
		// + (unit != null ? unit.getName() : "null"));
		if (unit.isEnemy()
				&& (unit.isCloaked() || unit.isBurrowed() || !unit.isDetected())) {
			ProtossObserver.hiddenUnitDetected(unit);
		}
	}

	public void unitMorph(int unitID) {
	}

	public void unitShow(int unitID) {
		Unit unit = Unit.getByID(unitID);
		if (unit == null || !unit.isEnemy()) {
			return;
		}

		if (unit.isEnemy() && unit.isHidden()) {
			// Debug.message(xvr, "Hidden unit: " +
			// Unit.getByID(unitID).getName());
			ProtossObserver.hiddenUnitDetected(unit);
		}

		if (unit.getType().isCarrier() && !ProtossGateway.isPlanAntiAirActive()) {
			ProtossGateway.changePlanToAntiAir();
		}
	}

	public void unitRenegade(int unitID) {
		Unit unit = Unit.getByID(unitID);
		if (unit == null || !unit.isEnemy()) {
			return;
		}

		// Add info that we discovered enemy unit
		MapExploration.enemyUnitDiscovered(unit);
	}

	public void saveGame(String gameName) {
	}

	public void unitComplete(int unitID) {
		Unit unit = bwapi.getUnit(unitID);
		UnitType unitType = unit.getType();
		if (unit.isMyUnit() && unitType.isBase()) {
			ProtossNexus.updateNextBaseToExpand();
		}
	}

	public void playerDropped(int playerID) {
		Debug.message(xvr, "########################", false);
		Debug.message(xvr, "## Sayonara, gringo! ^_^ ##", false);
		Debug.message(xvr, "########################", false);
	}

}
