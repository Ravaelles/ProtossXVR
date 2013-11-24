package ai.algorithms.dbscan;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DBSCANUtility {

	/**
	 * 
	 * 测试两个点之间的距离
	 * 
	 * @param p
	 *            点
	 * 
	 * @param q
	 *            点
	 * 
	 * @return 返回两个点之间的距离
	 */

	public static double getDistance(DBSCANPoint p, DBSCANPoint q) {

		int dx = p.getX() - q.getX();

		int dy = p.getY() - q.getY();

		double distance = Math.sqrt(dx * dx + dy * dy);

		return distance;

	}

	/**
	 * 
	 * 检查给定点是不是核心点
	 * 
	 * @param lst
	 *            存放点的链表
	 * 
	 * @param p
	 *            待测试的点
	 * 
	 * @param e
	 *            e半径
	 * 
	 * @param minp
	 *            密度阈值
	 * 
	 * @return 暂时存放访问过的点
	 */

	public static List<DBSCANPoint> isKeyPoint(List<DBSCANPoint> lst, DBSCANPoint p, int e,
			int minp) {

		int count = 0;

		List<DBSCANPoint> tmpLst = new ArrayList<DBSCANPoint>();

		for (Iterator<DBSCANPoint> it = lst.iterator(); it.hasNext();) {

			DBSCANPoint q = it.next();

			if (getDistance(p, q) <= e) {

				++count;

				if (!tmpLst.contains(q)) {

					tmpLst.add(q);

				}

			}

		}

		if (count >= minp) {

			p.setKey(true);

			return tmpLst;

		}

		return null;

	}

	public static void setListClassed(List<DBSCANPoint> lst) {

		for (Iterator<DBSCANPoint> it = lst.iterator(); it.hasNext();) {

			DBSCANPoint p = it.next();

			if (!p.isClassed()) {

				p.setClassed(true);

			}

		}

	}

	/**
	 * 
	 * 如果b中含有a中包含的元素，则把两个集合合并
	 * 
	 * @param a
	 * 
	 * @param b
	 * 
	 * @return a
	 */

	public static boolean mergeList(List<DBSCANPoint> a, List<DBSCANPoint> b) {

		boolean merge = false;

		for (int index = 0; index < b.size(); ++index) {

			if (a.contains(b.get(index))) {

				merge = true;

				break;

			}

		}

		if (merge) {

			for (int index = 0; index < b.size(); ++index) {

				if (!a.contains(b.get(index))) {

					a.add(b.get(index));

				}

			}

		}

		return merge;

	}

	/**
	 * 
	 * 返回文本中的点集合
	 * 
	 * @return 返回文本中点的集合
	 * 
	 * @throws IOException
	 */

	public static List<DBSCANPoint> getPointsList() throws IOException {

		List<DBSCANPoint> lst = new ArrayList<DBSCANPoint>();

		String txtPath = "points.txt";

		BufferedReader br = new BufferedReader(new FileReader(txtPath));

		String str = "";

		while ((str = br.readLine()) != null && str != "") {

			lst.add(new DBSCANPoint(str));

		}

		br.close();

		return lst;

	}

}
