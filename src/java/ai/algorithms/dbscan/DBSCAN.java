package ai.algorithms.dbscan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DBSCAN {

	private static List<DBSCANPoint> pointsList = new ArrayList<DBSCANPoint>();// 存储所有点的集合

	private static List<List<DBSCANPoint>> resultList = new ArrayList<List<DBSCANPoint>>();// 存储DBSCAN算法返回的结果集

	private static int e = 2;// e半径

	private static int minp = 3;// 密度阈值

	/**
	 * 
	 * 提取文本中的的所有点并存储在pointsList中
	 * 
	 * @throws IOException
	 */

	private static void display() {

		int index = 1;

		for (Iterator<List<DBSCANPoint>> it = resultList.iterator(); it.hasNext();) {

			List<DBSCANPoint> lst = it.next();

			if (lst.isEmpty()) {

				continue;

			}

			System.out.println("-----第" + index + "个聚类-----");

			for (Iterator<DBSCANPoint> it1 = lst.iterator(); it1.hasNext();) {

				DBSCANPoint p = it1.next();

				System.out.println(p.print());

			}

			index++;

		}

	}

	// 找出所有可以直达的聚类

	private static void applyDbscan() {

		try {

			pointsList = DBSCANUtility.getPointsList();

			for (Iterator<DBSCANPoint> it = pointsList.iterator(); it.hasNext();) {

				DBSCANPoint p = it.next();

				if (!p.isClassed()) {

					List<DBSCANPoint> tmpLst = new ArrayList<DBSCANPoint>();

					if ((tmpLst = DBSCANUtility.isKeyPoint(pointsList, p, e, minp)) != null) {

						// 为所有聚类完毕的点做标示

						DBSCANUtility.setListClassed(tmpLst);

						resultList.add(tmpLst);

					}

				}

			}

		} catch (IOException e) {

			// TODO Auto-generated catch block

			e.printStackTrace();

		}

	}

	// 对所有可以直达的聚类进行合并，即找出间接可达的点并进行合并

	private static List<List<DBSCANPoint>> getResult() {

		applyDbscan();// 找到所有直达的聚类

		int length = resultList.size();

		for (int i = 0; i < length; ++i) {

			for (int j = i + 1; j < length; ++j) {

				if (DBSCANUtility.mergeList(resultList.get(i), resultList.get(j))) {

					resultList.get(j).clear();

				}

			}

		}

		return resultList;

	}

	/**
	 * 
	 * 程序主函数
	 * 
	 * @param args
	 */

	public static void main(String[] args) {

		getResult();

		display();

		// System.out.println(Utility.getDistance(new Point(0,0), new
		// Point(0,2)));

	}

}