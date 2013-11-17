package jnibwapi.protoss;

import jnibwapi.Debug;
import jnibwapi.XVR;
import jnibwapi.model.Unit;
import jnibwapi.types.UpgradeType.UpgradeTypes;

public class TechnologyManager {

	private static XVR xvr = XVR.getInstance();

	// private static HashMap<UpgradeTypes, Boolean> knownTechs = new
	// HashMap<UpgradeTypes, Boolean>();

	public static void act() {

		// Leg enhancement
		if (isUpgradePossible(UpgradeTypes.Leg_Enhancements)) {
			tryToUpgrade(ProtossCitadelOfAdun.getOneNotBusy(),
					UpgradeTypes.Leg_Enhancements);
		}
		
		// Protoss shield
		if (isUpgradePossible(UpgradeTypes.Protoss_Plasma_Shields)) {
			tryToUpgrade(ProtossForge.getOneNotBusy(),
					UpgradeTypes.Protoss_Plasma_Shields);
		}
	}

	private static boolean isUpgradePossible(UpgradeTypes legEnhancements) {
		return isNotUpgraded(UpgradeTypes.Leg_Enhancements)
				&& canUpgrade(UpgradeTypes.Leg_Enhancements);
	}

	private static boolean isNotUpgraded(UpgradeTypes tech) {
		return !XVR.SELF.hasResearched(tech.ordinal());
	}

	private static boolean canUpgrade(UpgradeTypes tech) {
		return xvr.getBwapi().canUpgrade(tech.ordinal());
	}

	private static void tryToUpgrade(Unit building, UpgradeTypes upgrade) {
		if (building != null) {
			Debug.message(xvr, "Researching " + upgrade.toString());
			xvr.getBwapi().upgrade(building.getID(), upgrade.ordinal());
			// if (!building.isBuildingNotBusy()) {
			// knownTechs.put(upgrade, true);
			// }
		}
	}

}
