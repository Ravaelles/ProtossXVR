package jnibwapi.model;

import ai.handling.map.MapPoint;

/**
 * Represents a choke point in a StarCraft map.
 * 
 * For a description of fields see:
 * http://code.google.com/p/bwta/wiki/Chokepoint
 */
public class ChokePoint extends MapPoint {

	public static final int numAttributes = 9;
	public static final double fixedScale = 100.0;

	private static int firstFreeID = 1;
	private int ID;
	private int centerX;
	private int centerY;
	private double radius;
	private int firstRegionID;
	private int secondRegionID;
	private int firstSideX;
	private int firstSideY;
	private int secondSideX;
	private int secondSideY;
	private Region firstRegion;
	private Region secondRegion;
	
	private boolean disabled = false;

	public ChokePoint(int[] data, int index) {
		ID = firstFreeID++;

		centerX = data[index++];
		centerY = data[index++];
		radius = data[index++] / fixedScale;
		firstRegionID = data[index++];
		secondRegionID = data[index++];
		firstSideX = data[index++];
		firstSideY = data[index++];
		secondSideX = data[index++];
		secondSideY = data[index++];
	}

	public Region getOtherRegion(Region region) {
		return region.equals(firstRegion) ? secondRegion : firstRegion;
	}

	public Region getFirstRegion() {
		return firstRegion;
	}

	public void setFirstRegion(Region firstRegion) {
		this.firstRegion = firstRegion;
	}

	public Region getSecondRegion() {
		return secondRegion;
	}

	public void setSecondRegion(Region secondRegion) {
		this.secondRegion = secondRegion;
	}

	public int getCenterX() {
		return centerX;
	}

	public int getCenterY() {
		return centerY;
	}

	public double getRadius() {
		return radius;
	}

	public int getFirstRegionID() {
		return firstRegionID;
	}

	public int getSecondRegionID() {
		return secondRegionID;
	}

	public int getFirstSideX() {
		return firstSideX;
	}

	public int getFirstSideY() {
		return firstSideY;
	}

	public int getSecondSideX() {
		return secondSideX;
	}

	public int getSecondSideY() {
		return secondSideY;
	}

	@Override
	public int getX() {
		return getCenterX();
	}
	
	
	@Override
	public int getY() {
		return getCenterY();
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ID;
		return result;
	}
	

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChokePoint other = (ChokePoint) obj;
		if (ID != other.ID)
			return false;
		return true;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	
}
