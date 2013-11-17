package jnibwapi;

import java.awt.Point;

import jnibwapi.model.ChokePoint;
import jnibwapi.model.Unit;
import jnibwapi.protoss.ArmyCreationManager;
import jnibwapi.protoss.MassiveAttack;
import jnibwapi.protoss.NukeHandling;
import jnibwapi.protoss.ProtossAssimilator;
import jnibwapi.protoss.ProtossCybernetics;
import jnibwapi.protoss.ProtossGateway;
import jnibwapi.protoss.ProtossNexus;
import jnibwapi.protoss.ProtossObservatory;
import jnibwapi.protoss.ProtossPhotonCannon;
import jnibwapi.protoss.ProtossPylon;
import jnibwapi.protoss.UnitCounter;
import jnibwapi.protoss.UnitManager;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.util.BWColor;

public class Debug {

	private static int messageCounter = 1;
	private static int mainMessageRowCounter = 0;

	public static void drawDebug(XVR xvr) {
		int oldMainMessageRowCounter = mainMessageRowCounter;
		mainMessageRowCounter = 0;

		paintNextBuildingsPosition(xvr);

		paintWorkers(xvr);

		// // Draw regions
		// for (Region region : xvr.getBwapi().getMap().getRegions()) {
		// int[] bounds = region.getCoordinates();
		// xvr.getBwapi().drawBox(bounds[0] - bounds[2],
		// bounds[1] - bounds[3], 2 * bounds[2], 2 * bounds[3],
		// BWColor.TEAL, false, false);
		// xvr.getBwapi()
		// .drawText(
		// region.getCenterX(),
		// region.getCenterY(),
		// String.format("Region [%d]", region
		// .getChokePoints().size()), false);
		// }

		// Draw choke points
		paintChokePoints(xvr);

		// Statistics
		paintStatistics(xvr);

		// ========
		mainMessageRowCounter = oldMainMessageRowCounter;
	}

	private static void paintNextBuildingsPosition(XVR xvr) {

//		// Paint next PHOTON CANNON position
//		Point building = ProtossPhotonCannon.findTileForCannon();
//		if (building != null) {
//			xvr.getBwapi().drawBox(building.x * 32, building.y * 32,
//					(building.x + 2) * 32, (building.y + 2) * 32,
//					BWColor.PURPLE, false, false);
//			xvr.getBwapi().drawText(building.x * 32 + 10, building.y * 32 + 30,
//					"Cannon", false);
//		}
//
//		// Paint next PYLON position
//		building = ProtossPylon.findTileForPylon();
//		if (building != null) {
//			xvr.getBwapi().drawBox(building.x * 32, building.y * 32,
//					(building.x + 2) * 32, (building.y + 2) * 32,
//					BWColor.PURPLE, false, false);
//			xvr.getBwapi().drawText(building.x * 32 + 10, building.y * 32 + 30,
//					"Pylon", false);
//		}
//
//		// Paint next building position, next to some pylon
//		building = ProtossPylon.findTileNearPylonForNewBuilding();
//		if (building != null) {
//			xvr.getBwapi().drawBox(building.x * 32, building.y * 32,
//					(building.x + 3) * 32, (building.y + 2) * 32,
//					BWColor.PURPLE, false, false);
//			xvr.getBwapi().drawText(building.x * 32 + 10, building.y * 32 + 30,
//					"Building", false);
//		}
	}

	// private static void paintStatistics(XVR xvr) {
	// if (xvr.getFirstBase() == null) {
	// return;
	// }
	//
	// // for (Integer typeId : UnitCounter.getExistingUnitTypes()) {
	// // UnitTypes type = UnitType.getUnitTypesByID(typeId);
	// //
	// // }
	// }

	private static void paintChokePoints(XVR xvr) {
		for (ChokePoint chokePoint : xvr.getBwapi().getMap().getChokePoints()) {
			// xvr.getBwapi().drawBox(bounds[0], bounds[1],
			// bounds[2], bounds[3], BWColor.TEAL, false, false);
			// xvr.getBwapi().drawCircle(chokePoint.getFirstSideX(),
			// chokePoint.getFirstSideY(), 3, BWColor.RED, true, false);
			// xvr.getBwapi().drawCircle(chokePoint.getSecondSideX(),
			// chokePoint.getSecondSideY(), 3, BWColor.BLUE, true, false);
			xvr.getBwapi().drawCircle(chokePoint.getCenterX(),
					chokePoint.getCenterY(), (int) chokePoint.getRadius(),
					BWColor.BLACK, false, false);
			// xvr.getBwapi().drawText(
			// chokePoint.getCenterX(),
			// chokePoint.getCenterY(),
			// String.format("Choke [%d,%d]",
			// chokePoint.getCenterX() / 32,
			// chokePoint.getCenterY() / 32), false);
		}

	}

	private static void paintWorkers(XVR xvr) {
		// Draw circles over workers (blue if they're gathering minerals, green
		// if gas, yellow if they're constructing).
		JNIBWAPI bwapi = xvr.getBwapi();
		for (Unit u : bwapi.getMyUnits()) {
			if (u.isMoving()) {
				continue;
			}

			if (u.isGatheringMinerals()) {
				bwapi.drawCircle(u.getX(), u.getY(), 12, BWColor.BLUE, false,
						false);
			} else if (u.isGatheringGas()) {
				bwapi.drawCircle(u.getX(), u.getY(), 12, BWColor.GREEN, false,
						false);
			} else if (u.isAttacking()) {
				bwapi.drawCircle(u.getX(), u.getY(), 12, BWColor.RED, false,
						false);
				bwapi.drawCircle(u.getX(), u.getY(), 11, BWColor.RED, false,
						false);
			} else if (u.isRepairing()) {
				bwapi.drawCircle(u.getX(), u.getY(), 12, BWColor.PURPLE, false,
						false);
				bwapi.drawCircle(u.getX(), u.getY(), 11, BWColor.PURPLE, false,
						false);
				bwapi.drawCircle(u.getX(), u.getY(), 10, BWColor.PURPLE, false,
						false);
			} else if (u.isConstructing()) {
				bwapi.drawCircle(u.getX(), u.getY(), 12, BWColor.ORANGE, false,
						false);
				bwapi.drawCircle(u.getX(), u.getY(), 11, BWColor.ORANGE, false,
						false);
			} else if (u.isStuck()) {
				bwapi.drawCircle(u.getX(), u.getY(), 12, BWColor.TEAL, false,
						false);
				bwapi.drawCircle(u.getX(), u.getY(), 11, BWColor.TEAL, false,
						false);
				bwapi.drawCircle(u.getX(), u.getY(), 10, BWColor.TEAL, false,
						false);
				bwapi.drawCircle(u.getX(), u.getY(), 9, BWColor.TEAL, false,
						false);
			}

			if (u.isConstructing()) {
				String name = (UnitType.getUnitTypesByID(u.getBuildTypeID()) + "")
						.replace("Protoss_", "");
				bwapi.drawText(u.getX() - 30, u.getY(), "-> " + name, false);
			} else if (u.isTraining()) {
				String name = (bwapi.getUnitCommandType(u.getLastCommandID())
						.getName() + "").replace("Protoss_", "");
				bwapi.drawText(u.getX() - 30, u.getY(), "-> " + name, false);
			}
		}
		// } else if ((u.isMoving() || u.isAttacking()) && !u.isSCV()) {
		// // xvr.getBwapi().drawCircle(u.getX(), u.getY(), 12,
		// // BWColor.GREEN, false, false);
		// xvr.getBwapi().drawText(u.getX(), u.getY(),
		// String.format("->[%d,%d]",u.getTargetX() / 32, u.getTargetY() /
		// 32), false);
		// }

		if (NukeHandling.nuclearDetectionPoint != null) {
			Point nuclearPoint = NukeHandling.nuclearDetectionPoint;
			bwapi.drawCircle(nuclearPoint.x, nuclearPoint.y, 20, BWColor.RED,
					false, false);
			bwapi.drawCircle(nuclearPoint.x, nuclearPoint.y, 18, BWColor.RED,
					false, false);
			bwapi.drawCircle(nuclearPoint.x, nuclearPoint.y, 16, BWColor.RED,
					false, false);
			bwapi.drawCircle(nuclearPoint.x, nuclearPoint.y, 14, BWColor.RED,
					false, false);
		}
	}

	@SuppressWarnings("static-access")
	private static void paintStatistics(XVR xvr) {
		if (xvr.getFirstBase() == null) {
			return;
		}

		paintMainMessage(xvr, "Enemy: " + xvr.getENEMY_RACE());
		paintMainMessage(
				xvr,
				"Nexus: "
						+ UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Nexus));
		if (UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Assimilator) > 0)
			paintMainMessage(
					xvr,
					"Assimilators: "
							+ UnitCounter
									.getNumberOfUnits(UnitTypes.Protoss_Assimilator));
		if (UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Pylon) > 0)
			paintMainMessage(
					xvr,
					"Pylons: "
							+ UnitCounter
									.getNumberOfUnits(UnitTypes.Protoss_Pylon));
		if (UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Gateway) > 0)
			paintMainMessage(
					xvr,
					"Gateway: "
							+ UnitCounter
									.getNumberOfUnits(UnitTypes.Protoss_Gateway));
		if (UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Cybernetics_Core) > 0)
			paintMainMessage(
					xvr,
					"Cybernetics: "
							+ UnitCounter
									.getNumberOfUnits(UnitTypes.Protoss_Cybernetics_Core));
		if (UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Photon_Cannon) > 0)
			paintMainMessage(
					xvr,
					"Cannons: "
							+ UnitCounter
									.getNumberOfUnits(UnitTypes.Protoss_Photon_Cannon));

		paintMainMessage(xvr, "--------------------");

		paintMainMessage(
				xvr,
				"Probes: ("
						+ UnitCounter.getNumberOfUnits(UnitManager.WORKER)
						+ " / "
						+ ProtossNexus.getOptimalMineralGatherersAtBase(xvr
								.getFirstBase()) + ")");

		paintMainMessage(
				xvr,
				"Gath. gas: ("
						+ ProtossNexus.getNumberofGasGatherersForBase(xvr
								.getFirstBase()) + ")");

		if (UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Zealot) > 0)
			paintMainMessage(
					xvr,
					"Zealots: "
							+ UnitCounter
									.getNumberOfUnits(UnitTypes.Protoss_Zealot));
		if (UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Dragoon) > 0)
			paintMainMessage(
					xvr,
					"Dragoons: "
							+ UnitCounter
									.getNumberOfUnits(UnitTypes.Protoss_Dragoon));
		if (UnitCounter.getNumberOfUnits(UnitTypes.Protoss_Dark_Templar) > 0)
			paintMainMessage(
					xvr,
					"D. Templars: "
							+ UnitCounter
									.getNumberOfUnits(UnitTypes.Protoss_Dark_Templar));

		paintMainMessage(xvr, "--------------------");

		paintMainMessage(xvr,
				"Battle units: " + UnitCounter.getNumberOfBattleUnits());

		String buildArmy = "";
		if (ArmyCreationManager.weShouldBuildBattleUnits()) {
			buildArmy = "true";
		} else {
			buildArmy = "FALSE";
		}
		paintMainMessage(xvr, "Build army: " + buildArmy);
		paintMainMessage(xvr,
				"Attack ready: "
						+ (MassiveAttack.isAttackPending() ? "YES" : "no"));

		paintMainMessage(xvr, "--------------------");

		if (ProtossPylon.shouldBuild())
			paintMainMessage(xvr, "Build PYLON: true");
		if (ProtossPhotonCannon.shouldBuild())
			paintMainMessage(xvr, "Build CANNON: true");
		if (ProtossGateway.shouldBuild())
			paintMainMessage(xvr, "Build GATEWAY: true");
		if (ProtossNexus.shouldBuild())
			paintMainMessage(xvr, "Build NEXUS: true");
		if (ProtossAssimilator.shouldBuild())
			paintMainMessage(xvr, "Build ASSIMILATOR: true");
		if (ProtossObservatory.shouldBuild())
			paintMainMessage(xvr, "Build OBSERVATORY: true");
		if (ProtossCybernetics.shouldBuild())
			paintMainMessage(xvr, "Build CYBERNETICS: true");
		// if (Terran.shouldBuild())
		// paintMainMessage(xvr, "Build : ");
		// if (Terran.shouldBuild())
		// paintMainMessage(xvr, "Build : ");

		// paintMainMessage(xvr, ": " +
		// UnitCounter.getNumberOfUnits(UnitTypes.Protoss_));
		// paintMainMessage(xvr, ": " +
		// UnitCounter.getNumberOfUnits(UnitTypes.Protoss_));
		// paintMainMessage(xvr, ": " +
		// UnitCounter.getNumberOfUnits(UnitTypes.Protoss_));
	}

	private static void paintMainMessage(XVR xvr, String string) {
		xvr.getBwapi().drawText(new Point(5, 12 * mainMessageRowCounter++),
				string, true);
	}

	public static void message(XVR xvr, String txt) {
		xvr.getBwapi().printText("(" + messageCounter++ + ".) " + txt);
	}

	public static void messageBuild(XVR xvr, UnitTypes type) {
		String building = "#"
				+ UnitType.getUnitTypesByID(type.ordinal()).name();

		message(xvr, "Trying to build " + building);
	}

}
