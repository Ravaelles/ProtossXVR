package jnibwapi.model;

import jnibwapi.xvr.MapPoint;

/**
 * Represents a StarCraft base location.
 * 
 * For a description of fields see:
 * http://code.google.com/p/bwta/wiki/BaseLocation
 */
public class BaseLocation extends MapPoint implements Comparable<BaseLocation> {

	public static final int numAttributes = 10;

	private int x;
	private int y;
	private int tx;
	private int ty;
	private int regionID;
	private int minerals;
	private int gas;
	private boolean island;
	private boolean mineralOnly;
	private boolean startLocation;

	public BaseLocation(int[] data, int index) {
		x = data[index++];
		y = data[index++];
		tx = data[index++];
		ty = data[index++];
		regionID = data[index++];
		minerals = data[index++];
		gas = data[index++];
		island = (data[index++] == 1);
		mineralOnly = (data[index++] == 1);
		startLocation = (data[index++] == 1);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getTx() {
		return tx;
	}

	public int getTy() {
		return ty;
	}

	public int getRegionID() {
		return regionID;
	}

	public int getMinerals() {
		return minerals;
	}

	public int getGas() {
		return gas;
	}

	public boolean isIsland() {
		return island;
	}

	public boolean isMineralOnly() {
		return mineralOnly;
	}

	public boolean isStartLocation() {
		return startLocation;
	}

	// ======================

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
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
		BaseLocation other = (BaseLocation) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public int compareTo(BaseLocation arg0) {
		BaseLocation otherBase = (BaseLocation) arg0;
		return (x + ";" + y).compareTo(otherBase.x + ";" + otherBase.y);
	}

}
