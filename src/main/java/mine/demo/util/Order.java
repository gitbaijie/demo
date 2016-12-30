package mine.demo.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.NumberUtils;

import sun.misc.Sort;

public class Order {

	private static int num[] = { 2, 4, 7, 1, 2, 9, 5, 1, 4, 5, 6 };

	private Map<Integer, String[]> visitMap = new HashMap<Integer, String[]>();

	/**
	 * 读取网站访问记录，并将数据解析到访问数组中
	 * 
	 * @param fileName
	 */
	public void readFileByLines(String fileName) {
		BufferedReader reader = null;
		try {
			System.out.println("以行为单位读取文件内容，一次读一整行：");
			reader = new BufferedReader(new FileReader(fileName));
			String tempString = null;
			int i = 0;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				// 显示行号
				visitMap.put(i, tempString.split(","));
				i++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		String f = "C:/Users/hh/Desktop/12.30/abc.txt";
		Order o = new Order();
		o.readFileByLines(f);
		o.sort(0, o.visitMap.size() - 1);
		Map<Integer, String[]> visitMap = o.visitMap;
		for (int i = 0; i < visitMap.size(); i++) {
			String[] s = visitMap.get(i);
			System.out.println(s[0] + " " + s[1] + " " + s[2] + " " + s[3]);
		}
	}

	/**
	 * 按访问量排序
	 * 
	 * @param lIdx
	 * @param rIdx
	 */
	private void sort(int lIdx, int rIdx) {
		if (lIdx > rIdx) {
			int idx = quickSort(rIdx, lIdx);
			sort(rIdx, idx - 1);
			sort(idx + 1, lIdx);
		}
	}

	/**
	 * 交换对象
	 */
	public void swap(int i, int j) {
		String[] temp = visitMap.get(i);
		visitMap.put(i, visitMap.get(j));
		visitMap.put(j, temp);
	}

	/**
	 * 快速排序
	 * 
	 * @param lIdx
	 * @param rIdx
	 * @return
	 */
	private int quickSort(int lIdx, int rIdx) {
		int mIdx = lIdx + (rIdx - lIdx) / 2;
		if (Double.parseDouble(visitMap.get(mIdx)[3]) < Double.parseDouble(visitMap.get(rIdx)[3])) {
			swap(mIdx, rIdx);
		}
		if (Double.parseDouble(visitMap.get(lIdx)[3]) < Double.parseDouble(visitMap.get(rIdx)[3])) {
			swap(lIdx, rIdx);
		}
		if (Double.parseDouble(visitMap.get(lIdx)[3]) < Double.parseDouble(visitMap.get(mIdx)[3])) {
			swap(mIdx, lIdx);
		}

		String[] key = visitMap.get(lIdx);
		double visits = Double.parseDouble(visitMap.get(lIdx)[3]); // 访问量
		while (lIdx < rIdx) {
			while (Double.parseDouble(visitMap.get(lIdx)[3]) <= visits && lIdx < rIdx) {
				lIdx--;
			}
			num[rIdx] = num[lIdx];
			while (Double.parseDouble(visitMap.get(rIdx)[3]) >= visits && lIdx < rIdx) {
				rIdx++;
			}
			num[lIdx] = num[rIdx];
		}
		visitMap.put(lIdx, key);
		return lIdx;
	}

}
