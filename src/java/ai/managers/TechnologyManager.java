package ai.managers;

import jnibwapi.model.Unit;
import jnibwapi.types.TechType.TechTypes;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.types.UpgradeType.UpgradeTypes;
import ai.core.XVR;
import ai.handling.units.UnitCounter;
import ai.protoss.ProtossArbiterTribunal;
import ai.protoss.ProtossCitadelOfAdun;
import ai.protoss.ProtossCyberneticsCore;
import ai.protoss.ProtossForge;
import ai.protoss.ProtossObservatory;
import ai.protoss.ProtossRoboticsSupportBay;
import ai.protoss.ProtossTemplarArchives;

public class TechnologyManager {

	public static final TechTypes HALLUCINATION = TechTypes.Hallucination;
	public static final TechTypes PSIONIC_STORM = TechTypes.Psionic_Storm;
	public static final TechTypes STASIS_FIELD = TechTypes.Stasis_Field;

	private static XVR xvr = XVR.getInstance();

	// private static HashMap<UpgradeTypes, Boolean> knownTechs = new
	// HashMap<UpgradeTypes, Boolean>();

	public static void act() {
		UpgradeTypes upgrade;
		TechTypes technology;

		int zealots = UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Zealot);
		int dragoons = UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Dragoon);
		int infantry = zealots + dragoons;

		// ======================================================
		// TOP PRIORITY
		// Technologies that are crucial and we don't need to have second base
		// in order to upgrade them

		// Protoss shield
		upgrade = UpgradeTypes.Protoss_Plasma_Shields;
		if ((infantry >= 8 || xvr.canAfford(300, 300))
				&& isUpgradePossible(upgrade)) {
			tryToUpgrade(ProtossForge.getOneNotBusy(), upgrade);
		}

		// Leg enhancement
		upgrade = UpgradeTypes.Leg_Enhancements;
		if (zealots >= 5 && isUpgradePossible(upgrade)) {
			tryToUpgrade(ProtossCitadelOfAdun.getOneNotBusy(), upgrade);
		}

		// Singularity charge
		upgrade = UpgradeTypes.Singularity_Charge;
		if (dragoons >= 2 && isUpgradePossible(upgrade)) {
			tryToUpgrade(ProtossCyberneticsCore.getOneNotBusy(), upgrade);
		}

		// Protoss ground weapon
		upgrade = UpgradeTypes.Protoss_Ground_Weapons;
		if (zealots >= 4 && xvr.canAfford(150 * getTechLevelOf(upgrade), 150)
				&& isUpgradePossible(upgrade)) {
			tryToUpgrade(ProtossForge.getOneNotBusy(), upgrade);
		}

		int gasInfantry = UnitCounter
				.getNumberOfUnits(UnitTypes.Protoss_Dragoon)
				+ UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Dark_Templar);

		// Observer speed
		upgrade = UpgradeTypes.Gravitic_Boosters;
		if (gasInfantry >= 9
				&& xvr.canAfford(400)
				&& isUpgradePossible(upgrade)
				&& UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Observer) >= 2) {
			tryToUpgrade(ProtossObservatory.getOneNotBusy(), upgrade);
		}

		// Psionic Storm
		technology = PSIONIC_STORM;
		if (gasInfantry >= 10
				&& UnitCounter
						.weHaveBuildingFinished(UnitTypes.Protoss_Templar_Archives)
				&& xvr.canAfford(200) && isResearchPossible(technology)) {
			// if (xvr.canAfford(200)
			// && isTechPossible(technology)
			// && UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Observer) >= 2)
			// {
			tryToResearch(ProtossTemplarArchives.getOneNotBusy(), technology);
		}

		// // Hallucination
		// technology = HALLUCINATION;
		// if (UnitCounter
		// .weHaveBuildingFinished(UnitTypes.Protoss_Templar_Archives)
		// && xvr.canAfford(200) && isResearchPossible(technology)) {
		// // if (xvr.canAfford(200)
		// // && isTechPossible(technology)
		// // && UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Observer) >= 2)
		// // {
		// tryToResearch(ProtossTemplarArchives.getOneNotBusy(), technology);
		// }

		// ======================================================
		// LOWER PRIORITY
		// To research technologies below we must have second base built.
		if (UnitCounter.getNumberOfUnits(UnitManager.BASE) <= 1
				|| gasInfantry < 8) {
			return;
		}

		// Observer range
		upgrade = UpgradeTypes.Sensor_Array;
		if (infantry >= 15 && xvr.canAfford(500) && isUpgradePossible(upgrade)) {
			tryToUpgrade(ProtossObservatory.getOneNotBusy(), upgrade);
		}

		// Protoss ground armor
		upgrade = UpgradeTypes.Protoss_Ground_Armor;
		if (zealots >= 5 && xvr.canAfford(150 * getTechLevelOf(upgrade), 150)
				&& isUpgradePossible(upgrade)) {
			tryToUpgrade(ProtossForge.getOneNotBusy(), upgrade);
		}

		// Scarab damage
		upgrade = UpgradeTypes.Scarab_Damage;
		if (UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Reaver) >= 2
				&& UnitCounter.weHaveBuilding(ProtossRoboticsSupportBay
						.getBuildingType()) && isUpgradePossible(upgrade)) {
			tryToUpgrade(ProtossRoboticsSupportBay.getOneNotBusy(), upgrade);
		}

		// Protoss stasis field
		technology = STASIS_FIELD;
		if (UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Arbiter) >= 1
				&& UnitCounter
						.weHaveBuildingFinished(UnitTypes.Protoss_Arbiter_Tribunal)
				&& xvr.canAfford(200) && isResearchPossible(technology)) {
			tryToResearch(ProtossArbiterTribunal.getOneNotBusy(), technology);
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

	private static boolean isResearchPossible(TechTypes technology) {
		return isNotResearched(technology) && canResearch(technology);
	}

	private static boolean isNotResearched(TechTypes tech) {
		return !XVR.SELF.hasResearched(tech.ordinal());
	}

	private static boolean canResearch(TechTypes tech) {
		return xvr.getBwapi().canResearch(tech.ordinal());
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

	private static void tryToResearch(Unit building, TechTypes technology) {
		if (building != null) {
			// Debug.message(xvr, "Researching " + technology.toString());
			xvr.getBwapi().research(building.getID(), technology.ordinal());
			// if (!building.isBuildingNotBusy()) {
			// knownTechs.put(upgrade, true);
			// }
		}
	}

	public static boolean isResearched(TechTypes tech) {
		return !isNotResearched(tech);
	}

}
