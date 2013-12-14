package ai.managers;

public class BotStrategyManager {

	private static boolean expandWithCannons = false;
	private static boolean expandWithGateways = true;
	private static int minBattleUnits = 0;

	// =============

	/** Build initially forge, then some cannons, only then some gateways. */
	public static boolean isExpandWithCannons() {
		return expandWithCannons;
	}

	public static void setExpandWithCannons(boolean expandWithCannons) {
		BotStrategyManager.expandWithCannons = expandWithCannons;
		BotStrategyManager.expandWithGateways = false;
	}

	/** Build initially gateways and make zealot push. */
	public static boolean isExpandWithGateways() {
		return expandWithGateways;
	}

	public static void setExpandWithGateways(boolean expandWithGateways) {
		BotStrategyManager.expandWithGateways = expandWithGateways;
		BotStrategyManager.expandWithCannons = false;
	}
	
	public static void waitUntilMinBattleUnits(int minUnits) {
		minBattleUnits = minUnits;
	}

	public static int getMinBattleUnits() {
		return minBattleUnits;
	}

}
