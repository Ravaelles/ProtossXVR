package ai.handling.map;

public abstract class MapPoint {
	
	public abstract int getX();

	public abstract int getY();

	public int getTx() {
		return getX() / 32;
	}
	
	public int getTy() {
		return getY() / 32;
	}

}
