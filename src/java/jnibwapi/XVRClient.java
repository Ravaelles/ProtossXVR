package jnibwapi;

import jnibwapi.model.ChokePoint;
import jnibwapi.model.Player;
import jnibwapi.model.Unit;
import jnibwapi.protoss.MapExploration;
import jnibwapi.protoss.MassiveAttack;
import jnibwapi.protoss.NukeHandling;
import jnibwapi.protoss.ProtossGateway;
import jnibwapi.protoss.ProtossNexus;
import jnibwapi.protoss.ProtossObserver;

public class XVRClient implements BWAPIEventListener {

	private JNIBWAPI bwapi;
	private XVR xvr;

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
		XVR.ENEMY = enemy;
		XVR.ENEMY_ID = enemy.getID();

		// Enemy -> Protoss
		if (enemy.getRaceID() == 2) {
			XVR.setENEMY_RACE("Protoss");
			ProtossGateway.enemyIsProtoss();
		}
		// ENEMY -> Terran
		else if (enemy.getRaceID() == 1) {
			XVR.setENEMY_RACE("Terran");
		}
		// ENEMY -> Zerg
		else if (enemy.getRaceID() == 0) {
			XVR.setENEMY_RACE("Zerg");
		}

		// Store initial choke points
		for (ChokePoint choke : bwapi.getMap().getChokePoints()) {
			MapExploration.chokePointsInitial.add(choke);
		}

		// ==========
		// HotFix
		ProtossNexus.initialMineralGathering();
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
	}

	public void sendText(String text) {
	}

	public void receiveText(String text) {
	}

	public void nukeDetect(int x, int y) {
		System.out.println("DETECTED NUKE AT: " + x + ", " + y);
		NukeHandling.nukeDetected(x, y);
	}

	public void nukeDetect() {
		System.out.println("DETECTED NUKE OVERALL");
	}

	public void playerLeft(int playerID) {
	}

	public void unitCreate(int unitID) {
		xvr.unitCreated(unitID);
		// Unit unit = bwapi.getUnit(unitID);
		// UnitType unitType = UnitType.getUnitTypeByID(unit.getTypeID());
		// if (unitType.isBuilding()) {
		// TerranConstructing.removeIsBeingBuilt(unitType);
		// }
	}

	public void unitDestroy(int unitID) {
		// System.out.println("DESTROYED: " + unitID);
		// Unit unit = bwapi.getUnit(unitID);
		// UnitType unitType = UnitType.getUnitTypeByID(unit.getTypeID());

		// Check if massive attack target has just been destroyed; if so,
		// redefine it.
		if (MassiveAttack.getTargetUnit() != null
				&& MassiveAttack.getTargetUnit().getID() == unitID) {
			// System.out.println("REDIFINING... " +
			// MassiveAttack.getTargetUnit().toStringShort());
			MassiveAttack.forceRedefinitionOfNextTarget();
		}
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
		System.out.println("Unit evade: "
				+ (unit != null ? unit.getName() : "null"));
	}

	public void unitHide(int unitID) {
		Unit unit = Unit.getByID(unitID);
		if (unit == null || !unit.isEnemy()) {
			return;
		}

		System.out.println("Unit hide: "
				+ (unit != null ? unit.getName() : "null"));
		if (unit.isEnemy() && (unit.isCloaked() || unit.isBurrowed())) {
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

		if (unit.isEnemy() && (unit.isCloaked() || unit.isBurrowed())) {
			Debug.message(xvr, "Hidden unit: " + Unit.getByID(unitID).getName());
			ProtossObserver.hiddenUnitDetected(unit);
		}
	}

	public void unitRenegade(int unitID) {
	}

	public void saveGame(String gameName) {
	}

	public void unitComplete(int unitID) {
	}

	public void playerDropped(int playerID) {
	}

}
