package ai.handling.map;

public class MapPointInstance extends MapPoint {

	private int _x;
	private int _y;

	public MapPointInstance(int x, int y) {
		this._x = x;
		this._y = y;
	}

	public int getX() {
		return _x;
	}

	public int getY() {
		return _y;
	}

	public int getTx() {
		return getX() / 32;
	}

	public int getTy() {
		return getY() / 32;
	}

}
