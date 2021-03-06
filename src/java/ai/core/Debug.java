package ai.core;

import java.awt.Point;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.ChokePoint;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitCommandType.UnitCommandTypes;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.util.BWColor;
import ai.handling.army.StrengthEvaluator;
import ai.handling.constructing.ShouldBuildCache;
import ai.handling.map.MapExploration;
import ai.handling.map.MapPoint;
import ai.handling.other.NukeHandling;
import ai.handling.units.UnitCounter;
import ai.managers.ArmyCreationManager;
import ai.managers.StrategyManager;
import ai.managers.UnitManager;
import ai.protoss.ProtossNexus;

public class Debug {

	public static final boolean FULL_DEBUG = true;

	private static int messageCounter = 1;
	private static int mainMessageRowCounter = 0;

	public static int ourDeaths = 0;
	public static int enemyDeaths = 0;

	public static void drawDebug(XVR xvr) {
		int oldMainMessageRowCounter = mainMessageRowCounter;
		mainMessageRowCounter = 0;

		// MapPoint assimilator = Constructing.findTileForAssimilator();
		// if (assimilator != null) {
		// xvr.getBwapi().drawBox(assimilator.getX(), assimilator.getY(),
		// 3 * 32, 2 * 32, BWColor.TEAL, false, false);
		// }

		if (FULL_DEBUG) {
			paintNextBuildingsPosition(xvr);
		}
		paintUnitsDetails(xvr);

		if (FULL_DEBUG) {
			paintValuesOverUnits(xvr);
		}

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

		// // Draw next building place
		// MapPoint buildTile = ProtossPylon.findTileNearPylonForNewBuilding();
		// if (buildTile != null) {
		// xvr.getBwapi().drawCircle(buildTile.getX() - 64,
		// buildTile.getX() - 48, 50, BWColor.TEAL, false, false);
		// }

		// xvr.getBwapi()
		// .drawText(
		// region.getCenterX(),
		// region.getCenterY(),
		// String.format("Region [%d]", region
		// .getChokePoints().size()), false);

		// Draw choke points
		paintChokePoints(xvr);

		// Draw where to attack
		paintAttackLocation(xvr);

		// Statistics
		paintStatistics(xvr);

		// ========
		mainMessageRowCounter = oldMainMessageRowCounter;
	}

	private static void paintValuesOverUnits(XVR xvr) {
		JNIBWAPI bwapi = xvr.getBwapi();
		String text;
		double strength;
		for (Unit unit : bwapi.getMyUnits()) {
			strength = StrengthEvaluator.calculateStrengthRatioFor(unit);

			if (strength != -1) {
				strength -= 1; // make +/- values display
				if (strength < 99998) {
					text = (strength > 0 ? "+" : "") + String.format("%.1f", strength);
					bwapi.drawText(unit.getX() - 7, unit.getY() + 30, text, false);
				}
			}
		}
	}

	private static void paintAttackLocation(XVR xvr) {
		JNIBWAPI bwapi = xvr.getBwapi();
		if (StrategyManager.getTargetUnit() != null) {
			bwapi.drawCircle(StrategyManager.getTargetUnit().getX(), StrategyManager
					.getTargetUnit().getY(), 33, BWColor.RED, false, false);
			bwapi.drawCircle(StrategyManager.getTargetUnit().getX(), StrategyManager
					.getTargetUnit().getY(), 32, BWColor.RED, false, false);
			bwapi.drawCircle(StrategyManager.getTargetUnit().getX(), StrategyManager
					.getTargetUnit().getY(), 3, BWColor.RED, true, false);
		}

		if (NukeHandling.nuclearDetectionPoint != null) {
			Point nuclearPoint = NukeHandling.nuclearDetectionPoint;
			bwapi.drawCircle(nuclearPoint.x, nuclearPoint.y, 20, BWColor.RED, false, false);
			bwapi.drawCircle(nuclearPoint.x, nuclearPoint.y, 18, BWColor.RED, false, false);
			bwapi.drawCircle(nuclearPoint.x, nuclearPoint.y, 16, BWColor.RED, false, false);
			bwapi.drawCircle(nuclearPoint.x, nuclearPoint.y, 14, BWColor.RED, false, false);
		}
	}

	private static void paintNextBuildingsPosition(XVR xvr) {
		MapPoint building;

		// Paint next NEXUS position
		building = ProtossNexus.getTileForNextBase(false);
		if (building != null) {
			xvr.getBwapi().drawBox(building.getX(), building.getY(), building.getX() + 4 * 32,
					building.getY() + 4 * 32, BWColor.TEAL, false, false);
			// xvr.getBwapi().drawText(building.getX() + 10, building.getY() +
			// 30,
			// "Nexus", false);
		}

		// // Paint next CANNON position
		// building = ProtossPhotonCannon.findTileForCannon();
		// if (building != null) {
		// xvr.getBwapi().drawBox(building.getX(), building.getY(),
		// building.getX() + 2 * 32, building.getY() + 2 * 32,
		// BWColor.TEAL, false, false);
		// xvr.getBwapi().drawText(building.getX() + 10, building.getY() + 30,
		// "Cannon", false);
		// }
		//
		// // Paint next PYLON position
		// building = ProtossPylon.findTileForPylon();
		// if (building != null) {
		// xvr.getBwapi().drawBox(building.getX(), building.getY(),
		// building.getX() + 2 * 32, building.getY() + 2 * 32,
		// BWColor.TEAL, false, false);
		// xvr.getBwapi().drawText(building.getX() + 10, building.getY() + 30,
		// "Pylon", false);
		// }
		//
		// // Paint GATEWAY position
		// building = ProtossPylon
		// .findTileNearPylonForNewBuilding(UnitTypes.Protoss_Gateway);
		// if (building != null) {
		// xvr.getBwapi().drawBox(building.getX(), building.getY(),
		// building.getX() + 2 * 32, building.getY() + 2 * 32,
		// BWColor.TEAL, false, false);
		// xvr.getBwapi().drawText(building.getX() + 10, building.getY() + 30,
		// "Gateway", false);
		// }
	}

	private static void paintChokePoints(XVR xvr) {
		for (ChokePoint choke : MapExploration.getChokePoints()) {
			// xvr.getBwapi().drawBox(bounds[0], bounds[1],
			// bounds[2], bounds[3], BWColor.TEAL, false, false);
			// xvr.getBwapi().drawCircle(chokePoint.getFirstSideX(),
			// chokePoint.getFirstSideY(), 3, BWColor.RED, true, false);
			// xvr.getBwapi().drawCircle(chokePoint.getSecondSideX(),
			// chokePoint.getSecondSideY(), 3, BWColor.BLUE, true, false);
			xvr.getBwapi().drawCircle(choke.getCenterX(), choke.getCenterY(),
					(int) choke.getRadius(), BWColor.BLACK, false, false);
			// xvr.getBwapi().drawText(
			// chokePoint.getCenterX(),
			// chokePoint.getCenterY(),
			// String.format("Choke [%d,%d]",
			// chokePoint.getCenterX() / 32,
			// chokePoint.getCenterY() / 32), false);

			// Region ourRegion =
			// xvr.getBwapi().getMap().getRegion(xvr.getFirstBase());
			// boolean onlyOurRegion = false;
			// if (choke.getSecondRegion().getConnectedRegions().size() == 1) {
			// Region region = (Region) RUtilities.getSetElement(choke
			// .getSecondRegion().getConnectedRegions(), 0);
			// if (region.equals(ourRegion)) {
			// onlyOurRegion = true;
			// }
			// }
			// if (choke.getFirstRegion().getConnectedRegions().size() == 1) {
			// Region region = (Region) RUtilities.getSetElement(choke
			// .getFirstRegion().getConnectedRegions(), 0);
			// if (region.equals(ourRegion)) {
			// onlyOurRegion = true;
			// }
			// }
			//
			// String string = choke.getFirstRegionID() + " ("
			// + choke.getFirstRegion().getConnectedRegions().size()
			// + "), " + choke.getSecondRegionID() + "("
			// + choke.getSecondRegion().getConnectedRegions().size()
			// + "), " + (onlyOurRegion ? "YESSSSS" : "no");
			//
			// xvr.getBwapi().drawText(choke.getCenterX() - 40,
			// choke.getCenterY(),
			// string, false);
		}

	}

	private static void paintUnitsDetails(XVR xvr) {
		// Draw circles over workers (blue if they're gathering minerals, green
		// if gas, yellow if they're constructing).
		JNIBWAPI bwapi = xvr.getBwapi();
		for (Unit u : bwapi.getMyUnits()) {
			if (FULL_DEBUG) {
				// if (u.isMoving()) {
				// continue;
				// }

				if (u.isGatheringMinerals()) {
					bwapi.drawCircle(u.getX(), u.getY(), 12, BWColor.BLUE, false, false);
				} else if (u.isGatheringGas()) {
					bwapi.drawCircle(u.getX(), u.getY(), 12, BWColor.GREEN, false, false);
				} else if (u.isMoving()) {
					bwapi.drawLine(u.getX(), u.getY(), u.getTargetX(), u.getTargetY(),
							BWColor.WHITE, false);
				} else if (u.isAttacking()) {
					bwapi.drawCircle(u.getX(), u.getY(), 12, BWColor.RED, false, false);
					bwapi.drawCircle(u.getX(), u.getY(), 11, BWColor.RED, false, false);

					bwapi.drawLine(u.getX(), u.getY(), u.getTargetX(), u.getTargetY(), BWColor.RED,
							false);
				} else if (u.isRepairing()) {
					bwapi.drawCircle(u.getX(), u.getY(), 12, BWColor.PURPLE, false, false);
					bwapi.drawCircle(u.getX(), u.getY(), 11, BWColor.PURPLE, false, false);
					bwapi.drawCircle(u.getX(), u.getY(), 10, BWColor.PURPLE, false, false);
				} else if (u.isConstructing()
						|| u.getLastCommandID() == UnitCommandTypes.Build.ordinal()) {
					if (u.getBuildTypeID() == UnitManager.BASE.ordinal()) {
						bwapi.drawCircle(u.getX(), u.getY(), 16, BWColor.ORANGE, false, false);
						bwapi.drawCircle(u.getX(), u.getY(), 14, BWColor.ORANGE, false, false);
						bwapi.drawCircle(u.getX(), u.getY(), 12, BWColor.ORANGE, false, false);
						bwapi.drawCircle(u.getX(), u.getY(), 10, BWColor.ORANGE, false, false);
						bwapi.drawCircle(u.getX(), u.getY(), 8, BWColor.ORANGE, false, false);
						bwapi.drawCircle(u.getX(), u.getY(), 6, BWColor.ORANGE, false, false);
					}
					else {
						bwapi.drawCircle(u.getX(), u.getY(), 12, BWColor.ORANGE, false, false);
						bwapi.drawCircle(u.getX(), u.getY(), 11, BWColor.ORANGE, false, false);
					}
				} else if (u.isStuck()) {
					bwapi.drawCircle(u.getX(), u.getY(), 12, BWColor.TEAL, false, false);
					bwapi.drawCircle(u.getX(), u.getY(), 11, BWColor.TEAL, false, false);
					bwapi.drawCircle(u.getX(), u.getY(), 10, BWColor.TEAL, false, false);
					bwapi.drawCircle(u.getX(), u.getY(), 9, BWColor.TEAL, false, false);
				}
				// } else if (u.isIdle()) {
				// bwapi.drawCircle(u.getX(), u.getY(), 12, BWColor.WHITE,
				// false, false);
				// bwapi.drawCircle(u.getX(), u.getY(), 11, BWColor.WHITE,
				// false, false);
				// bwapi.drawCircle(u.getX(), u.getY(), 10, BWColor.WHITE,
				// false, false);
				// }
			}

			if (u.isConstructing()) {
				String name = (UnitType.getUnitTypesByID(u.getBuildTypeID()) + "").replace(
						"Protoss_", "");
				bwapi.drawText(u.getX() - 30, u.getY(), "-> " + name, false);
			} else if (u.isTraining()) {
				String name = (bwapi.getUnitCommandType(u.getLastCommandID()).getName() + "")
						.replace("Protoss_", "");
				bwapi.drawText(u.getX() - 30, u.getY(), "-> " + name, false);
			}

			// Paint Observers
			if (u.getTypeID() == UnitTypes.Protoss_Observer.ordinal()) {
				bwapi.drawCircle(u.getX(), u.getY(), 13, BWColor.BLUE, false, false);
				bwapi.drawCircle(u.getX(), u.getY(), 12, BWColor.BLUE, false, false);
			}
		}
		// } else if ((u.isMoving() || u.isAttacking()) && !u.isSCV()) {
		// // xvr.getBwapi().drawCircle(u.getX(), u.getY(), 12,
		// // BWColor.GREEN, false, false);
		// xvr.getBwapi().drawText(u.getX(), u.getY(),
		// String.format("->[%d,%d]",u.getTargetX() / 32, u.getTargetY() /
		// 32), false);
		// }
	}

	@SuppressWarnings("static-access")
	private static void paintStatistics(XVR xvr) {
		if (xvr.getFirstBase() == null) {
			return;
		}

		int time = xvr.getTime();
		paintMainMessage(xvr, "Time: " + (time / 30) + "s"); // (" + time + ")"
		paintMainMessage(xvr, "Killed: " + enemyDeaths);
		paintMainMessage(xvr, "Lost: " + ourDeaths);
		if (StrategyManager.getTargetUnit() != null) {
			Unit attack = StrategyManager.getTargetUnit();
			paintMainMessage(xvr,
					"Attack target: " + attack.getName() + " ## visible:" + attack.isVisible()
							+ ", exists:" + attack.isExists() + ", HP:" + attack.getHitPoints());
		}

		if (FULL_DEBUG) {
			paintMainMessage(xvr, "--------------------");
			paintMainMessage(xvr, "Enemy: " + xvr.getEnemyRace());
			paintMainMessage(xvr,
					"Nexus: " + UnitCounter.getNumberOfUnitsCompleted(UnitTypes.Protoss_Nexus));
			// if
			// (UnitCounter.getNumberOfUnitsCompleted(UnitTypes.Protoss_Assimilator)
			// >
			// 0)
			// paintMainMessage(
			// xvr,
			// "Assimilators: "
			// + UnitCounter
			// .getNumberOfUnitsCompleted(UnitTypes.Protoss_Assimilator));
			// if
			// (UnitCounter.getNumberOfUnitsCompleted(UnitTypes.Protoss_Pylon) >
			// 0)
			// paintMainMessage(
			// xvr,
			// "Pylons: "
			// + UnitCounter
			// .getNumberOfUnitsCompleted(UnitTypes.Protoss_Pylon));
			if (UnitCounter.getNumberOfUnitsCompleted(UnitTypes.Protoss_Gateway) > 0)
				paintMainMessage(
						xvr,
						"Gateway: "
								+ UnitCounter.getNumberOfUnitsCompleted(UnitTypes.Protoss_Gateway));
			// if
			// (UnitCounter.getNumberOfUnitsCompleted(UnitTypes.Protoss_Cybernetics_Core)
			// > 0)
			// paintMainMessage(
			// xvr,
			// "Cybernetics: "
			// + UnitCounter
			// .getNumberOfUnitsCompleted(UnitTypes.Protoss_Cybernetics_Core));
			if (UnitCounter.getNumberOfUnitsCompleted(UnitTypes.Protoss_Photon_Cannon) > 0)
				paintMainMessage(
						xvr,
						"Cannons: "
								+ UnitCounter
										.getNumberOfUnitsCompleted(UnitTypes.Protoss_Photon_Cannon));

			paintMainMessage(xvr, "--------------------");

			paintMainMessage(xvr,
					"Probes: (" + UnitCounter.getNumberOfUnitsCompleted(UnitManager.WORKER) + " / "
							+ ProtossNexus.getOptimalMineralGatherersAtBase(xvr.getFirstBase())
							+ ")");

			paintMainMessage(
					xvr,
					"Gath. gas: ("
							+ ProtossNexus.getNumberofGasGatherersForBase(xvr.getFirstBase()) + ")");

			if (UnitCounter.getNumberOfUnitsCompleted(UnitTypes.Protoss_Zealot) > 0)
				paintMainMessage(
						xvr,
						"Zealots: "
								+ UnitCounter.getNumberOfUnitsCompleted(UnitTypes.Protoss_Zealot));
			if (UnitCounter.getNumberOfUnitsCompleted(UnitTypes.Protoss_Dragoon) > 0)
				paintMainMessage(
						xvr,
						"Dragoons: "
								+ UnitCounter.getNumberOfUnitsCompleted(UnitTypes.Protoss_Dragoon));
			if (UnitCounter.getNumberOfUnitsCompleted(UnitTypes.Protoss_Dark_Templar) > 0)
				paintMainMessage(
						xvr,
						"D. Templars: "
								+ UnitCounter
										.getNumberOfUnitsCompleted(UnitTypes.Protoss_Dark_Templar));
			if (UnitCounter.getNumberOfUnitsCompleted(UnitTypes.Protoss_High_Templar) > 0)
				paintMainMessage(
						xvr,
						"H. Templars: "
								+ UnitCounter
										.getNumberOfUnitsCompleted(UnitTypes.Protoss_High_Templar));
			if (UnitCounter.getNumberOfUnitsCompleted(UnitTypes.Protoss_Observer) > 0)
				paintMainMessage(
						xvr,
						"Observers: "
								+ UnitCounter.getNumberOfUnitsCompleted(UnitTypes.Protoss_Observer));

			paintMainMessage(xvr, "--------------------");

			String minUnitsString = "";
			if (StrategyManager.getMinBattleUnits() > 0) {
				minUnitsString += " (min. " + StrategyManager.getMinBattleUnits() + ")";
			}
			paintMainMessage(xvr, "Battle units: " + UnitCounter.getNumberOfBattleUnits()
					+ minUnitsString);

			String buildArmy = "";
			if (ArmyCreationManager.weShouldBuildBattleUnits()) {
				buildArmy = "true";
			} else {
				buildArmy = "FALSE";
			}
			paintMainMessage(xvr, "Build army: " + buildArmy);
			paintMainMessage(xvr, "Attack ready: "
					+ (StrategyManager.isAttackPending() ? "YES" : "no"));

			paintMainMessage(xvr, "--------------------");

		}

		if (xvr.getTime() % 10 == 0) {

		}

		for (UnitTypes type : ShouldBuildCache.getBuildingsThatShouldBeBuild()) {
			paintMainMessage(xvr,
					"Build "
							+ type.name().substring(0, Math.min(15, type.name().length()))
									.toUpperCase() + ": true");
		}
	}

	private static void paintMainMessage(XVR xvr, String string) {
		xvr.getBwapi().drawText(new Point(5, 12 * mainMessageRowCounter++), string, true);
	}

	public static void message(XVR xvr, String txt, boolean displayCounter) {
		xvr.getBwapi().printText((displayCounter ? ("(" + messageCounter++ + ".) ") : "") + txt);
	}

	public static void message(XVR xvr, String txt) {
		message(xvr, txt, true);
	}

	public static void messageBuild(XVR xvr, UnitTypes type) {
		String building = "#" + UnitType.getUnitTypesByID(type.ordinal()).name();

		message(xvr, "Trying to build " + building);
	}

}
