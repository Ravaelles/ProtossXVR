package jnibwapi.model;

import jnibwapi.XVR;
import jnibwapi.protoss.CallForHelp;
import jnibwapi.protoss.UnitManager;
import jnibwapi.types.UnitCommandType.UnitCommandTypes;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;

/**
 * Represents a StarCraft unit.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/Unit
 */
public class Unit implements Comparable<Unit> {

	public static final int numAttributes = 118;
	public static final double TO_DEGREES = 180.0 / Math.PI;
	public static final double fixedScale = 100.0;

	// ========
	private Unit mineralGathered = null;
	private CallForHelp callForHelpMission = null;
	// ========

	private int ID;
	private int replayID;
	private int playerID;
	private int typeID;
	private int x;
	private int y;
	private int tileX;
	private int tileY;
	private double angle;
	private double velocityX;
	private double velocityY;
	private int hitPoints;
	private int shield;
	private int energy;
	private int resources;
	private int resourceGroup;
	private int lastCommandFrame;
	private int lastCommandID;
	private int initialTypeID;
	private int initialX;
	private int initialY;
	private int initialTileX;
	private int initialTileY;
	private int initialHitPoints;
	private int initialResources;
	private int killCount;
	private int acidSporeCount;
	private int interceptorCount;
	private int scarabCount;
	private int spiderMineCount;
	private int groundWeaponCooldown;
	private int airWeaponCooldown;
	private int spellCooldown;
	private int defenseMatrixPoints;
	private int defenseMatrixTimer;
	private int ensnareTimer;
	private int irradiateTimer;
	private int lockdownTimer;
	private int maelstromTimer;
	private int orderTimer;
	private int plagueTimer;
	private int removeTimer;
	private int stasisTimer;
	private int stimTimer;
	private int buildTypeID;
	private int trainingQueueSize;
	private int researchingTechID;
	private int upgradingUpgradeID;
	private int remainingBuildTimer;
	private int remainingTrainTime;
	private int remainingResearchTime;
	private int remainingUpgradeTime;
	private int buildUnitID;
	private int targetUnitID;
	private int targetX;
	private int targetY;
	private int orderID;
	private int orderTargetID;
	private int secondaryOrderID;
	private int rallyX;
	private int rallyY;
	private int rallyUnitID;
	private int addOnID;
	private int transportID;
	private int numLoadedUnits;
	private int numLarva;
	private boolean exists;
	private boolean nukeReady;
	private boolean accelerating;
	private boolean attacking;
	private boolean attackFrame;
	private boolean beingConstructed;
	private boolean beingGathered;
	private boolean beingHealed;
	private boolean blind;
	private boolean braking;
	private boolean burrowed;
	private boolean carryingGas;
	private boolean carryingMinerals;
	private boolean cloaked;
	private boolean completed;
	private boolean constructing;
	private boolean defenseMatrixed;
	private boolean detected;
	private boolean ensnared;
	private boolean following;
	private boolean gatheringGas;
	private boolean gatheringMinerals;
	private boolean hallucination;
	private boolean holdingPosition;
	private boolean idle;
	private boolean interruptable;
	private boolean invincible;
	private boolean irradiated;
	private boolean lifted;
	private boolean loaded;
	private boolean lockedDown;
	private boolean maelstrommed;
	private boolean morphing;
	private boolean moving;
	private boolean parasited;
	private boolean patrolling;
	private boolean plagued;
	private boolean repairing;
	private boolean selected;
	private boolean sieged;
	private boolean startingAttack;
	private boolean stasised;
	private boolean stimmed;
	private boolean stuck;
	private boolean training;
	private boolean underAttack;
	private boolean underDarkSwarm;
	private boolean underDisruptionWeb;
	private boolean underStorm;
	private boolean unpowered;
	private boolean upgrading;
	private boolean visible;

	public Unit(int ID) {
		this.ID = ID;
	}

	public void setDestroyed() {
		exists = false;
	}

	public void update(int[] data, int index) {
		index++; // ID = data[index++];
		replayID = data[index++];
		playerID = data[index++];
		typeID = data[index++];
		x = data[index++];
		y = data[index++];
		tileX = data[index++];
		tileY = data[index++];
		angle = data[index++] / TO_DEGREES;
		velocityX = data[index++] / fixedScale;
		velocityY = data[index++] / fixedScale;
		hitPoints = data[index++];
		shield = data[index++];
		energy = data[index++];
		resources = data[index++];
		resourceGroup = data[index++];
		lastCommandFrame = data[index++];
		lastCommandID = data[index++];
		initialTypeID = data[index++];
		initialX = data[index++];
		initialY = data[index++];
		initialTileX = data[index++];
		initialTileY = data[index++];
		initialHitPoints = data[index++];
		initialResources = data[index++];
		killCount = data[index++];
		acidSporeCount = data[index++];
		interceptorCount = data[index++];
		scarabCount = data[index++];
		spiderMineCount = data[index++];
		groundWeaponCooldown = data[index++];
		airWeaponCooldown = data[index++];
		spellCooldown = data[index++];
		defenseMatrixPoints = data[index++];
		defenseMatrixTimer = data[index++];
		ensnareTimer = data[index++];
		irradiateTimer = data[index++];
		lockdownTimer = data[index++];
		maelstromTimer = data[index++];
		orderTimer = data[index++];
		plagueTimer = data[index++];
		removeTimer = data[index++];
		stasisTimer = data[index++];
		stimTimer = data[index++];
		buildTypeID = data[index++];
		trainingQueueSize = data[index++];
		researchingTechID = data[index++];
		upgradingUpgradeID = data[index++];
		remainingBuildTimer = data[index++];
		remainingTrainTime = data[index++];
		remainingResearchTime = data[index++];
		remainingUpgradeTime = data[index++];
		buildUnitID = data[index++];
		targetUnitID = data[index++];
		targetX = data[index++];
		targetY = data[index++];
		orderID = data[index++];
		orderTargetID = data[index++];
		secondaryOrderID = data[index++];
		rallyX = data[index++];
		rallyY = data[index++];
		rallyUnitID = data[index++];
		addOnID = data[index++];
		transportID = data[index++];
		numLoadedUnits = data[index++];
		numLarva = data[index++];
		exists = data[index++] == 1;
		nukeReady = data[index++] == 1;
		accelerating = data[index++] == 1;
		attacking = data[index++] == 1;
		attackFrame = data[index++] == 1;
		beingConstructed = data[index++] == 1;
		beingGathered = data[index++] == 1;
		beingHealed = data[index++] == 1;
		blind = data[index++] == 1;
		braking = data[index++] == 1;
		burrowed = data[index++] == 1;
		carryingGas = data[index++] == 1;
		carryingMinerals = data[index++] == 1;
		cloaked = data[index++] == 1;
		completed = data[index++] == 1;
		constructing = data[index++] == 1;
		defenseMatrixed = data[index++] == 1;
		detected = data[index++] == 1;
		ensnared = data[index++] == 1;
		following = data[index++] == 1;
		gatheringGas = data[index++] == 1;
		gatheringMinerals = data[index++] == 1;
		hallucination = data[index++] == 1;
		holdingPosition = data[index++] == 1;
		idle = data[index++] == 1;
		interruptable = data[index++] == 1;
		invincible = data[index++] == 1;
		irradiated = data[index++] == 1;
		lifted = data[index++] == 1;
		loaded = data[index++] == 1;
		lockedDown = data[index++] == 1;
		maelstrommed = data[index++] == 1;
		morphing = data[index++] == 1;
		moving = data[index++] == 1;
		parasited = data[index++] == 1;
		patrolling = data[index++] == 1;
		plagued = data[index++] == 1;
		repairing = data[index++] == 1;
		selected = data[index++] == 1;
		sieged = data[index++] == 1;
		startingAttack = data[index++] == 1;
		stasised = data[index++] == 1;
		stimmed = data[index++] == 1;
		stuck = data[index++] == 1;
		training = data[index++] == 1;
		underAttack = data[index++] == 1;
		underDarkSwarm = data[index++] == 1;
		underDisruptionWeb = data[index++] == 1;
		underStorm = data[index++] == 1;
		unpowered = data[index++] == 1;
		upgrading = data[index++] == 1;
		visible = data[index++] == 1;
	}

	public int getID() {
		return ID;
	}

	public int getReplayID() {
		return replayID;
	}

	public int getPlayerID() {
		return playerID;
	}

	public int getTypeID() {
		return typeID;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getTileX() {
		return tileX;
	}

	public int getTileY() {
		return tileY;
	}

	public double getAngle() {
		return angle;
	}

	public double getVelocityX() {
		return velocityX;
	}

	public double getVelocityY() {
		return velocityY;
	}

	public int getHitPoints() {
		return hitPoints;
	}

	public int getShields() {
		return shield;
	}

	public int getEnergy() {
		return energy;
	}

	public int getResources() {
		return resources;
	}

	public int getResourceGroup() {
		return resourceGroup;
	}

	public int getLastCommandFrame() {
		return lastCommandFrame;
	}

	public int getLastCommandID() {
		return lastCommandID;
	}

	public int getInitialTypeID() {
		return initialTypeID;
	}

	public int getInitialX() {
		return initialX;
	}

	public int getInitialY() {
		return initialY;
	}

	public int getInitialTileX() {
		return initialTileX;
	}

	public int getInitialTileY() {
		return initialTileY;
	}

	public int getInitialHitPoints() {
		return initialHitPoints;
	}

	public int getInitialResources() {
		return initialResources;
	}

	public int getKillCount() {
		return killCount;
	}

	public int getAcidSporeCount() {
		return acidSporeCount;
	}

	public int getInterceptorCount() {
		return interceptorCount;
	}

	public int getScarabCount() {
		return scarabCount;
	}

	public int getSpiderMineCount() {
		return spiderMineCount;
	}

	public int getGroundWeaponCooldown() {
		return groundWeaponCooldown;
	}

	public int getAirWeaponCooldown() {
		return airWeaponCooldown;
	}

	public int getSpellCooldown() {
		return spellCooldown;
	}

	public int getDefenseMatrixPoints() {
		return defenseMatrixPoints;
	}

	public int getDefenseMatrixTimer() {
		return defenseMatrixTimer;
	}

	public int getEnsnareTimer() {
		return ensnareTimer;
	}

	public int getIrradiateTimer() {
		return irradiateTimer;
	}

	public int getLockdownTimer() {
		return lockdownTimer;
	}

	public int getMaelstromTimer() {
		return maelstromTimer;
	}

	public int getOrderTimer() {
		return orderTimer;
	}

	public int getPlagueTimer() {
		return plagueTimer;
	}

	public int getRemoveTimer() {
		return removeTimer;
	}

	public int getStasisTimer() {
		return stasisTimer;
	}

	public int getStimTimer() {
		return stimTimer;
	}

	public int getBuildTypeID() {
		return buildTypeID;
	}

	public int getTrainingQueueSize() {
		return trainingQueueSize;
	}

	public int getResearchingTechID() {
		return researchingTechID;
	}

	public int getUpgradingUpgradeID() {
		return upgradingUpgradeID;
	}

	public int getRemainingBuildTimer() {
		return remainingBuildTimer;
	}

	public int getRemainingTrainTime() {
		return remainingTrainTime;
	}

	public int getRemainingResearchTime() {
		return remainingResearchTime;
	}

	public int getRemainingUpgradeTime() {
		return remainingUpgradeTime;
	}

	public int getBuildUnitID() {
		return buildUnitID;
	}

	public int getTargetUnitID() {
		return targetUnitID;
	}

	public int getTargetX() {
		return targetX;
	}

	public int getTargetY() {
		return targetY;
	}

	public int getOrderID() {
		return orderID;
	}

	public int getOrderTargetID() {
		return orderTargetID;
	}

	public int getSecondaryOrderID() {
		return secondaryOrderID;
	}

	public int getRallyX() {
		return rallyX;
	}

	public int getRallyY() {
		return rallyY;
	}

	public int getRallyUnitID() {
		return rallyUnitID;
	}

	public int getAddOnID() {
		return addOnID;
	}

	public int getTransportID() {
		return transportID;
	}

	public int getNumLoadedUnits() {
		return numLoadedUnits;
	}

	public int getNumLarva() {
		return numLarva;
	}

	public boolean isExists() {
		return exists;
	}

	public boolean isNukeReady() {
		return nukeReady;
	}

	public boolean isAccelerating() {
		return accelerating;
	}

	public boolean isAttacking() {
		return attacking;
	}

	public boolean isAttackFrame() {
		return attackFrame;
	}

	public boolean isBeingConstructed() {
		return beingConstructed;
	}

	public boolean isBeingGathered() {
		return beingGathered;
	}

	public boolean isBeingHealed() {
		return beingHealed;
	}

	public boolean isBlind() {
		return blind;
	}

	public boolean isBraking() {
		return braking;
	}

	public boolean isBurrowed() {
		return burrowed;
	}

	public boolean isCarryingGas() {
		return carryingGas;
	}

	public boolean isCarryingMinerals() {
		return carryingMinerals;
	}

	public boolean isCloaked() {
		return cloaked;
	}

	public boolean isCompleted() {
		return completed;
	}

	public boolean isConstructing() {
		return constructing;
	}

	public boolean isDefenseMatrixed() {
		return defenseMatrixed;
	}

	public boolean isDetected() {
		return detected;
	}

	public boolean isEnsnared() {
		return ensnared;
	}

	public boolean isFollowing() {
		return following;
	}

	public boolean isGatheringGas() {
		return gatheringGas;
	}

	public boolean isGatheringMinerals() {
		return gatheringMinerals;
	}

	public boolean isHallucination() {
		return hallucination;
	}

	public boolean isHoldingPosition() {
		return holdingPosition;
	}

	public boolean isIdle() {
		return idle;
	}

	public boolean isInterruptable() {
		return interruptable;
	}

	public boolean isInvincible() {
		return invincible;
	}

	public boolean isIrradiated() {
		return irradiated;
	}

	public boolean isLifted() {
		return lifted;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public boolean isLockedDown() {
		return lockedDown;
	}

	public boolean isMaelstrommed() {
		return maelstrommed;
	}

	public boolean isMorphing() {
		return morphing;
	}

	public boolean isMoving() {
		return moving;
	}

	public boolean isParasited() {
		return parasited;
	}

	public boolean isPatrolling() {
		return patrolling;
	}

	public boolean isPlagued() {
		return plagued;
	}

	public boolean isRepairing() {
		return repairing;
	}

	public boolean isSelected() {
		return selected;
	}

	public boolean isSieged() {
		return sieged;
	}

	public boolean isStartingAttack() {
		return startingAttack;
	}

	public boolean isStasised() {
		return stasised;
	}

	public boolean isStimmed() {
		return stimmed;
	}

	public boolean isStuck() {
		return stuck;
	}

	public boolean isTraining() {
		return training;
	}

	public boolean isUnderAttack() {
		return underAttack;
	}

	public boolean isUnderDarkSwarm() {
		return underDarkSwarm;
	}

	public boolean isUnderDisruptionWeb() {
		return underDisruptionWeb;
	}

	public boolean isUnderStorm() {
		return underStorm;
	}

	public boolean isUnpowered() {
		return unpowered;
	}

	public boolean isUpgrading() {
		return upgrading;
	}

	public boolean isVisible() {
		return visible;
	}

	// =================================================

	public boolean isWorker() {
		return typeID == UnitManager.WORKER.ordinal();
	}

	public static Unit getMyUnitByID(int unitID) {
		for (Unit unit : XVR.getInstance().getBwapi().getMyUnits()) {
			if (unit.getID() == unitID) {
				return unit;
			}
		}
		return null;
	}

	public static Unit getByID(int unitID) {
		for (Unit unit : XVR.getInstance().getBwapi().getAllUnits()) {
			if (unit.getID() == unitID) {
				return unit;
			}
		}
		return null;
	}

	public boolean isNotMedic() {
		return typeID != UnitTypes.Terran_Medic.ordinal();
	}

	public UnitType getType() {
		return UnitType.getUnitTypeByID(getTypeID());
	}

	public String getName() {
		return getType().getName();
	}

	public boolean isEnemy() {
		return Player.isEnemy(playerID);
	}

	public boolean isMyUnit() {
		return Player.isMyself(playerID);
	}

	public UnitCommandTypes getLastCommand() {
		for (UnitCommandTypes type : UnitCommandTypes.values()) {
			if (lastCommandID == type.ordinal()) {
				return type;
			}
		}
		return null;
	}

	@Override
	public int compareTo(Unit otherUnit) {
		return Integer.compare(ID, otherUnit.ID);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Unit other = (Unit) obj;
		if (ID != other.ID)
			return false;
		return true;
	}

	public Unit getMineralGathered() {
		return mineralGathered;
	}

	public void setMineralGathered(Unit mineralGathered) {
		this.mineralGathered = mineralGathered;
	}

	public String toStringShort() {
		return "Unit [type=" + getType().getName() + ", exists=" + isExists()
				+ ", tileX=" + tileX + ", tileY=" + tileY + ", hitPoints="
				+ hitPoints + ", visible=" + visible + ", underAttack="
				+ underAttack + "]";
	}

	@Override
	public String toString() {
		return "Unit [type=" + getType() + ", mineralGathered="
				+ mineralGathered + ", callForHelpMission="
				+ callForHelpMission + ", ID=" + ID + ", replayID=" + replayID
				+ ", playerID=" + playerID + ", typeID=" + typeID + ", x=" + x
				+ ", y=" + y + ", tileX=" + tileX + ", tileY=" + tileY
				+ ", angle=" + angle + ", velocityX=" + velocityX
				+ ", velocityY=" + velocityY + ", hitPoints=" + hitPoints
				+ ", shield=" + shield + ", energy=" + energy + ", resources="
				+ resources + ", resourceGroup=" + resourceGroup
				+ ", lastCommandFrame=" + lastCommandFrame + ", lastCommandID="
				+ lastCommandID + ", initialTypeID=" + initialTypeID
				+ ", initialX=" + initialX + ", initialY=" + initialY
				+ ", initialTileX=" + initialTileX + ", initialTileY="
				+ initialTileY + ", initialHitPoints=" + initialHitPoints
				+ ", initialResources=" + initialResources + ", killCount="
				+ killCount + ", acidSporeCount=" + acidSporeCount
				+ ", interceptorCount=" + interceptorCount + ", scarabCount="
				+ scarabCount + ", spiderMineCount=" + spiderMineCount
				+ ", groundWeaponCooldown=" + groundWeaponCooldown
				+ ", airWeaponCooldown=" + airWeaponCooldown
				+ ", spellCooldown=" + spellCooldown + ", defenseMatrixPoints="
				+ defenseMatrixPoints + ", defenseMatrixTimer="
				+ defenseMatrixTimer + ", ensnareTimer=" + ensnareTimer
				+ ", irradiateTimer=" + irradiateTimer + ", lockdownTimer="
				+ lockdownTimer + ", maelstromTimer=" + maelstromTimer
				+ ", orderTimer=" + orderTimer + ", plagueTimer=" + plagueTimer
				+ ", removeTimer=" + removeTimer + ", stasisTimer="
				+ stasisTimer + ", stimTimer=" + stimTimer + ", buildTypeID="
				+ buildTypeID + ", trainingQueueSize=" + trainingQueueSize
				+ ", researchingTechID=" + researchingTechID
				+ ", upgradingUpgradeID=" + upgradingUpgradeID
				+ ", remainingBuildTimer=" + remainingBuildTimer
				+ ", remainingTrainTime=" + remainingTrainTime
				+ ", remainingResearchTime=" + remainingResearchTime
				+ ", remainingUpgradeTime=" + remainingUpgradeTime
				+ ", buildUnitID=" + buildUnitID + ", targetUnitID="
				+ targetUnitID + ", targetX=" + targetX + ", targetY="
				+ targetY + ", orderID=" + orderID + ", orderTargetID="
				+ orderTargetID + ", secondaryOrderID=" + secondaryOrderID
				+ ", rallyX=" + rallyX + ", rallyY=" + rallyY
				+ ", rallyUnitID=" + rallyUnitID + ", addOnID=" + addOnID
				+ ", transportID=" + transportID + ", numLoadedUnits="
				+ numLoadedUnits + ", numLarva=" + numLarva + ", exists="
				+ exists + ", nukeReady=" + nukeReady + ", accelerating="
				+ accelerating + ", attacking=" + attacking + ", attackFrame="
				+ attackFrame + ", beingConstructed=" + beingConstructed
				+ ", beingGathered=" + beingGathered + ", beingHealed="
				+ beingHealed + ", blind=" + blind + ", braking=" + braking
				+ ", burrowed=" + burrowed + ", carryingGas=" + carryingGas
				+ ", carryingMinerals=" + carryingMinerals + ", cloaked="
				+ cloaked + ", completed=" + completed + ", constructing="
				+ constructing + ", defenseMatrixed=" + defenseMatrixed
				+ ", detected=" + detected + ", ensnared=" + ensnared
				+ ", following=" + following + ", gatheringGas=" + gatheringGas
				+ ", gatheringMinerals=" + gatheringMinerals
				+ ", hallucination=" + hallucination + ", holdingPosition="
				+ holdingPosition + ", idle=" + idle + ", interruptable="
				+ interruptable + ", invincible=" + invincible
				+ ", irradiated=" + irradiated + ", lifted=" + lifted
				+ ", loaded=" + loaded + ", lockedDown=" + lockedDown
				+ ", maelstrommed=" + maelstrommed + ", morphing=" + morphing
				+ ", moving=" + moving + ", parasited=" + parasited
				+ ", patrolling=" + patrolling + ", plagued=" + plagued
				+ ", repairing=" + repairing + ", selected=" + selected
				+ ", sieged=" + sieged + ", startingAttack=" + startingAttack
				+ ", stasised=" + stasised + ", stimmed=" + stimmed
				+ ", stuck=" + stuck + ", training=" + training
				+ ", underAttack=" + underAttack + ", underDarkSwarm="
				+ underDarkSwarm + ", underDisruptionWeb=" + underDisruptionWeb
				+ ", underStorm=" + underStorm + ", unpowered=" + unpowered
				+ ", upgrading=" + upgrading + ", visible=" + visible + "]";
	}

	public boolean isBuildingNotBusy() {
		return !isTraining() && !isUpgrading();
	}

	public CallForHelp getCallForHelpMission() {
		return callForHelpMission;
	}

	public void setCallForHelpMission(CallForHelp call) {
		this.callForHelpMission = call;
	}

}
