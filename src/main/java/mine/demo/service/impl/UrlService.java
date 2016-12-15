package mine.demo.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import mine.demo.service.IUrlService;

@Service("excelService")
public class UrlService implements IUrlService {

	private static Logger logger = Logger.getLogger(UrlService.class);

	private String key = "baidu"; // 关键字

	private int delCount = 0; // 删除数量

	private int haskeycount = 0; // 有关键字数量

	private int nothaskeycount = 0; // 没有关键字数量

	private String excelFileName = "C:/Users/hh/Desktop/12.15/url.xls"; // url
																		// Excel文件

	private String delFileName = "C:/Users/hh/Desktop/12.15/del.txt"; // 保存删除链接文件

	private String haskeyFileName = "C:/Users/hh/Desktop/12.15/haskey.txt"; // 有关键字链接保存文件

	private String nothaskeyFileName = "C:/Users/hh/Desktop/12.15/nothaskey.txt"; // 没有关键字链接保存文件

	private List<String> getUrl() {
		jxl.Workbook readwb = null;
		List<String> list = new ArrayList<String>();
		try {
			// 构建Workbook对象, 只读Workbook对象
			// 直接从本地文件创建Workbook
			InputStream instream = new FileInputStream(excelFileName);
			readwb = Workbook.getWorkbook(instream);

			// Sheet的下标是从0开始
			// 获取第一张Sheet表
			Sheet readsheet = readwb.getSheet(0);
			// 获取Sheet表中所包含的总列数
			int rsColumns = readsheet.getColumns();
			// 获取Sheet表中所包含的总行数
			int rsRows = readsheet.getRows();
			// 获取指定单元格的对象引用

			for (int i = 0; i < rsRows; i++) {
				for (int j = 0; j < rsColumns; j++) {
					Cell cell = readsheet.getCell(j, i);
					list.add(cell.getContents());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			readwb.close();
		}
		return list;
	}

	private void getReturnData(String urlStr) throws Exception {
		/** 网络的url地址 */
		URL url = null;
		/** http连接 */
		/**//** 输入流 */
		BufferedReader in = null;
		StringBuffer sb = new StringBuffer();
		try {
			int status = 0;
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(urlStr);
			HttpResponse loginResponse = httpClient.execute(httpGet);
			status = loginResponse.getStatusLine().getStatusCode();
			logger.info("=========== url " + urlStr + " 调用结果： " + status);
			if (status == 200) {
				url = new URL(urlStr);
				in = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
				String str = null;
				while ((str = in.readLine()) != null) {
					sb.append(str);
				}
				String result = sb.toString();
				logger.info(result);
				write(result, urlStr);
			} else {
				delCount++;
				writeTxtFile(urlStr, new File(delFileName));
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.debug("send url failed: " + urlStr);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
			}
		}
		String result = sb.toString();
		logger.info(result);
	}

	private void writeTxtFile(String content, File fileName) {
		try {
			content += "\n";
			// 打开一个随机访问文件流，按读写方式
			RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
			// 文件长度，字节数
			long fileLength = randomFile.length();
			// 将写文件指针移到文件尾。
			randomFile.seek(fileLength);
			randomFile.writeBytes(content);
			randomFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void write(String result, String url) throws Exception {
		if (result.indexOf(key) > -1) {
			haskeycount++;
			writeTxtFile(url, new File(haskeyFileName));
		} else {
			writeTxtFile(url, new File(nothaskeyFileName));
			nothaskeycount++;
		}
	}

	@Override
	public void searchUrl() throws Exception {
		List<String> urls = getUrl();
		for (int i = 0; i < urls.size(); i++) {
			String url = urls.get(i);
			logger.info("#################### 第" + (i + 1) + "个链接");
			logger.info("==================访问url：" + url);
			getReturnData(url);
		}
		logger.info(
				"@@@@@@@@@  操作结果，删除链接数量：" + delCount + ", 有关键字链接数量：" + haskeycount + "，没有关键字链接数量：" + nothaskeycount);
	}

}
