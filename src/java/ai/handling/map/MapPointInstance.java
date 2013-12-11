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

	@Override
	public String toString() {
		return "[x:" + _x + ", y:" + _y + "]";
	}

	public static MapPointInstance getMiddlePointBetween(MapPoint point1,
			MapPoint point2) {
		MapPointInstance point = new MapPointInstance(
				(point1.getX() + point2.getX()) / 2,
				(point1.getY() + point2.getY()) / 2);
		return point;
	}
	
	public static MapPointInstance getTwoThirdPointBetween(MapPoint point1,
			MapPoint point2) {
		MapPointInstance point = new MapPointInstance(
				(point1.getX() + 2 * point2.getX()) / 3,
				(point1.getY() + 2 * point2.getY()) / 3);
		return point;
	}

}
