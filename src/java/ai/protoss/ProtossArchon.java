package ai.protoss;

import jnibwapi.model.Unit;

public class ProtossArchon {
	
	public static boolean isSeriouslyWounded(Unit archon) {
		if (archon.getShields() < 80) {
			return true;
		}
		return false;
	}

}
