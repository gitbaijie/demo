package mine.demo.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import mine.demo.service.IUrlService;

@Service("excelService")
public class UrlServiceImpl implements IUrlService {

	private static Logger logger = Logger.getLogger(UrlServiceImpl.class);

	private String key = "elemax"; // 关键字

	private int delCount = 0; // 删除数量

	private int content_haskeycount = 0; // 内容有关键字数量

	private int link_haskeycount = 0; // 链接有关键字数量

	private int not_haskeycount = 0; // 没有关键字数量

	private int error_404count = 0; // 页面不存在数量

	private String excelFileName = "C:/Users/hh/Desktop/1.17/ELEMAX.xls"; // url
																		// Excel文件
	private String DEL_TXT = "Deleted"; // 删除

	private String CONTENT_HAS_TXT = "Not Deleted"; // 内容关键字

	private String LINK_HAS_TXT = "Not Deleted/Kanekalon in url"; // 链接关键字

	private String NOT_HAS_TXT = "Removed"; // 其他产品

	private String ERROR404_TXT = "404"; // 页面不存在

	private void getUrl() throws WriteException, IOException {
		jxl.Workbook readwb = null;
		WritableWorkbook wwb = null;
		try {
			// 构建Workbook对象, 只读Workbook对象
			// 直接从本地文件创建Workbook
			InputStream instream = new FileInputStream(excelFileName);
			readwb = Workbook.getWorkbook(instream);
			wwb = Workbook.createWorkbook(new File(excelFileName), readwb);

			// Sheet的下标是从0开始
			// 获取第一张Sheet表
			Sheet readsheet = readwb.getSheet(0);
			WritableSheet ws = wwb.getSheet(0);
			// 读取第一张工作表
			// 获取Sheet表中所包含的总行数
			int rsRows = readsheet.getRows();
			logger.info("================ 导入 " + rsRows + " ===============");
			for (int i = 0; i < rsRows; i++) {
				Thread.sleep(1 * 1000); // 休息1秒
				Cell cell = readsheet.getCell(0, i);
				String url = cell.getContents();
				if (!url.isEmpty()) {
					writeCell(ws, i, url);
				}
			}
			wwb.write();
		} catch (Exception e) {
			logger.debug(e);
		} finally {
			readwb.close();
			wwb.close();
		}
	}

	/**
	 * 写入Sheet
	 * 
	 * @param ws
	 * @param row
	 * @param content
	 * @throws Exception
	 */
	private void writeCell(WritableSheet ws, int row, String url) throws Exception {
		String content = searchUrl(url, row);
		if (content != null) {
			Label label = new Label(1, row, content);
			ws.addCell(label);
		}
	}

	/**
	 * search url
	 * 
	 * @param url
	 * @param row
	 * @return
	 * @throws Exception
	 */
	private String searchUrl(String url, int row) throws Exception {
		Document doc;
		try {
			doc = Jsoup.connect(url).get();
			logger.info("=========== " + (row + 1) + " url " + url);
			if (isDel(doc)) {
				// 删除
				logger.info("============== " + url + " 删除");
				delCount++;
				return DEL_TXT;
			} else {
				// 未删除
				if (isHasKeyOfContent(doc)) {
					// 内容有key
					logger.info("============== " + url + " 内容关键字");
					content_haskeycount++;
					return CONTENT_HAS_TXT;
				} else if (isHasKeyOfLink(doc, url)) {
					// 链接有key
					logger.info("============== " + url + " 链接关键字");
					link_haskeycount++;
					return LINK_HAS_TXT;
				} else {
					// 没有key
					logger.info("============== " + url + " 其他产品");
					not_haskeycount++;
					return NOT_HAS_TXT;
				}
			}
		} catch (Exception e) {
			logger.debug(e);
			logger.debug("send url failed: " + url);
			error_404count++;
			return ERROR404_TXT;
		}
	}

	/**
	 * 判断内容里是否有关键字
	 * 
	 * @param doc
	 * @return
	 */
	private boolean isHasKeyOfContent(Document doc) {
		// 内容要排除a标签
		Elements ListDiv = doc.getElementsByTag("div");
		for (Element element : ListDiv) {
			Elements links = element.getElementsByTag("a");
			for (Element link : links) {
				link.remove();
			}
			String context = element.text().toLowerCase();
			if (context.indexOf(key) > -1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断链接里是否有关键字
	 * 
	 * @param doc
	 * @return
	 * @throws IOException
	 */
	private boolean isHasKeyOfLink(Document doc, String url) throws IOException {
		doc = Jsoup.connect(url).get(); // 上一步已删除a标签，现在需要重新加载url
		Elements links = doc.getElementsByTag("a");
		for (Element link : links) {
			String linkTitle = link.attr("title").toLowerCase();
			String linkText = link.text().toLowerCase();
			if (linkTitle.indexOf(key) > -1 || linkText.indexOf(key) > -1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 是否被删除
	 * 
	 * @param doc
	 * @return
	 */
	private boolean isDel(Document doc) {
		Elements element = doc.getElementsByAttributeValue("class", "ma-title-wrap");
		if (element.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void searchUrl() throws Exception {
		Date sDate = new Date();
		// delFile(); // 删除记录文件
		getUrl();
		Date eDate = new Date();
		long time = (eDate.getTime() - sDate.getTime()) / 1000;
		logger.info("##########  操作结果，删除url数量：" + delCount + ", 内容有关键字url数量：" + content_haskeycount + ", 链接有关键字url数量："
				+ link_haskeycount + "，没有关键字url数量：" + not_haskeycount + "，页面不存在数量：" + error_404count + " 用时" + time
				+ "秒");
	}
	
	public static void main(String[] args) throws Exception {
		IUrlService service = new UrlServiceImpl();
		service.searchUrl();
	}

}
