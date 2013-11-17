package jnibwapi.protoss;

import java.util.ArrayList;
import java.util.Iterator;

import jnibwapi.XVR;
import jnibwapi.model.Unit;

public class CallForHelp {

	private static XVR xvr = XVR.getInstance();

	private static ArrayList<CallForHelp> callsForHelp = new ArrayList<CallForHelp>();

	private Unit caller;
	private int time;
	private ArrayList<Unit> enemiesNearby;
	private boolean critical;
	private int callsAccepted = 0;

	public CallForHelp(Unit caller, int time, ArrayList<Unit> enemiesNearby,
			boolean critical) {
		super();
		this.caller = caller;
		this.time = time;
		this.enemiesNearby = enemiesNearby;
		this.critical = critical;
	}

	public Unit getCaller() {
		return caller;
	}

	public void setCaller(Unit caller) {
		this.caller = caller;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public boolean isCritical() {
		return critical;
	}

	public void setCritical(boolean critical) {
		this.critical = critical;
	}

	public ArrayList<Unit> getEnemiesNearby() {
		return enemiesNearby;
	}

	public void setEnemiesNearby(ArrayList<Unit> enemiesNearby) {
		this.enemiesNearby = enemiesNearby;
	}

	public static void issueCallForHelp(Unit toRescue, boolean critical) {
		ArrayList<Unit> enemies = xvr.getUnitsInRadius(toRescue.getX(),
				toRescue.getX(), 15, xvr.getEnemyUnitsVisible());
		CallForHelp call = new CallForHelp(toRescue, xvr.getTime(), enemies,
				critical);
		callsForHelp.add(call);
	}

	public static boolean isAnyCallForHelp() {
		return !callsForHelp.isEmpty();
	}

	public static ArrayList<CallForHelp> getThoseInNeedOfHelp() {
		return callsForHelp;
	}

	public void unitHasAcceptedIt(Unit unit) {
		unit.setCallForHelpMission(this);
		callsAccepted++;
	}

	public int getCallsAccepted() {
		return callsAccepted;
	}

	public void unitArrivedToHelp(Unit unit) {
		callsAccepted--;
		unit.setCallForHelpMission(null);
		if (callsAccepted == 0) {
			callsForHelp.remove(this);
		}
	}

	public static void clearOldOnes() {
		for (Iterator<CallForHelp> iterator = callsForHelp.iterator(); iterator
				.hasNext();) {
			CallForHelp call = (CallForHelp) iterator.next();
			if (xvr.getTimeDifferenceBetweenNowAnd(call.time) > 12) {
				iterator.remove();
			}
		}
	}

}
