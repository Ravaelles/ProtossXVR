package ai.handling.constructing;

import java.util.HashMap;
import java.util.Set;

import jnibwapi.types.UnitType.UnitTypes;

public class ShouldBuildCache {

	private static HashMap<UnitTypes, Boolean> shouldBuildMap = new HashMap<UnitTypes, Boolean>();

	public static void cacheShouldBuildInfo(UnitTypes buildingType,
			boolean shouldBuild) {
		if (shouldBuild) {
			shouldBuildMap.put(buildingType, true);
		} else {
			shouldBuildMap.remove(buildingType);
		}
	}

	public static boolean getCachedValueOfShouldBuild(UnitTypes buildingType) {
		return shouldBuildMap.get(buildingType);
	}

	public static Set<UnitTypes> getBuildingsThatShouldBeBuild() {
		return shouldBuildMap.keySet();
	}

	// if (ProtossPylon.shouldBuild())
	// paintMainMessage(xvr, "Build PYLON: true");
	// if (ProtossPhotonCannon.shouldBuild())
	// paintMainMessage(xvr, "Build CANNON: true");
	// if (ProtossGateway.shouldBuild())
	// paintMainMessage(xvr, "Build GATEWAY: true");
	// if (ProtossNexus.shouldBuild())
	// paintMainMessage(xvr, "Build NEXUS: true");
	// if (ProtossAssimilator.shouldBuild())
	// paintMainMessage(xvr, "Build ASSIMILATOR: true");
	// if (ProtossObservatory.shouldBuild())
	// paintMainMessage(xvr, "Build OBSERVATORY: true");
	// if (ProtossCybernetics.shouldBuild())
	// paintMainMessage(xvr, "Build CYBERNETICS: true");

}
