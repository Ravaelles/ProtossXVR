package jnibwapi.xvr;

import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.protoss.ProtossCitadelOfAdun;
import jnibwapi.protoss.ProtossCybernetics;
import jnibwapi.protoss.ProtossForge;
import jnibwapi.protoss.ProtossObservatory;
import jnibwapi.protoss.ProtossRoboticsSupportBay;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.types.UpgradeType.UpgradeTypes;

public class TechnologyManager {

	private static XVR xvr = XVR.getInstance();

	// private static HashMap<UpgradeTypes, Boolean> knownTechs = new
	// HashMap<UpgradeTypes, Boolean>();

	public static void act() {
		UpgradeTypes technology;

		// ======================================================
		// TOP PRIORITY
		// Technologies that are crucial and we don't need to have second base
		// in order to upgrade them

		// Leg enhancement
		technology = UpgradeTypes.Leg_Enhancements;
		if (isUpgradePossible(technology)) {
			tryToUpgrade(ProtossCitadelOfAdun.getOneNotBusy(), technology);
		}

		// Leg enhancement
		technology = UpgradeTypes.Singularity_Charge;
		if (UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Dragoon) >= 4 
				&& xvr.canAfford(250) && isUpgradePossible(technology)) {
			tryToUpgrade(ProtossCybernetics.getOneNotBusy(), technology);
		}

		// ======================================================
		// LOWER PRIORITY
		// To research technologies below we must have second base built.
		if (UnitCounter.getNumberOfUnits(UnitManager.BASE) <= 1) {
			return;
		}

		// Observer speed
		technology = UpgradeTypes.Gravitic_Boosters;
		if (xvr.canAfford(500) && isUpgradePossible(technology)) {
			tryToUpgrade(ProtossObservatory.getOneNotBusy(), technology);
		}

		// Observer range
		technology = UpgradeTypes.Sensor_Array;
		if (xvr.canAfford(500) && isUpgradePossible(technology)) {
			tryToUpgrade(ProtossObservatory.getOneNotBusy(), technology);
		}

		// Protoss shield
		technology = UpgradeTypes.Protoss_Plasma_Shields;
		if (isUpgradePossible(technology)) {
			tryToUpgrade(ProtossForge.getOneNotBusy(), technology);
		}

		// Protoss ground weapon
		technology = UpgradeTypes.Protoss_Ground_Weapons;
		if (xvr.canAfford(600 * getTechLevelOf(technology), 300)
				&& isUpgradePossible(technology)) {
			tryToUpgrade(ProtossForge.getOneNotBusy(), technology);
		}

		// Protoss ground armor
		technology = UpgradeTypes.Protoss_Ground_Armor;
		if (xvr.canAfford(600 * getTechLevelOf(technology), 300)
				&& isUpgradePossible(technology)) {
			tryToUpgrade(ProtossForge.getOneNotBusy(), technology);
		}

		// Scarab damage
		technology = UpgradeTypes.Scarab_Damage;
		if (UnitCounter.weHaveBuilding(ProtossRoboticsSupportBay
				.getBuildingType()) && isUpgradePossible(technology)) {
			tryToUpgrade(ProtossRoboticsSupportBay.getOneNotBusy(), technology);
		}
	}

	private static int getTechLevelOf(UpgradeTypes technology) {
		return XVR.SELF.upgradeLevel(technology.getID());
	}

	private static boolean isUpgradePossible(UpgradeTypes upgrade) {
		return isNotUpgraded(upgrade) && canUpgrade(upgrade);
	}

	private static boolean isNotUpgraded(UpgradeTypes tech) {
		return !XVR.SELF.hasResearched(tech.ordinal());
	}

	private static boolean canUpgrade(UpgradeTypes tech) {
		return xvr.getBwapi().canUpgrade(tech.ordinal());
	}

	private static void tryToUpgrade(Unit building, UpgradeTypes upgrade) {
		if (building != null) {
			// Debug.message(xvr, "Researching " + upgrade.toString());
			xvr.getBwapi().upgrade(building.getID(), upgrade.ordinal());
			// if (!building.isBuildingNotBusy()) {
			// knownTechs.put(upgrade, true);
			// }
		}
	}

}
