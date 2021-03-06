package mine.demo.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import mine.demo.pojo.UrlInfo;
import mine.demo.service.IAlibabaUrlService;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import mine.demo.util.CookieUtil;

@Service("alibabaService")
public class AlibabaUrlServiceImpl implements IAlibabaUrlService {

	private static Logger logger = Logger.getLogger(IAlibabaUrlService.class);

	private String baseurl = "https://www.alibaba.com/products/F0/kamry_cassiel/"; // 搜索的基础url

	private int successCount = 0; // 成功的url数量

	private int pages = 24; // 总页数

	private String searchName = "kamry cassiel"; // search关键字

	private String excelFileName = "C:/Users/yue06/Desktop/2.17/" + searchName + ".xls"; // url
	private int row = 1; // 行

	private Map<String, String> cookies;

	public AlibabaUrlServiceImpl() {
		cookies = CookieUtil.getCookie();
	}

	/**
	 * 写入excel
	 * 
	 * @param path
	 */
	private void exportExcel() {
		WritableWorkbook book = null;
		String info[] = { "序号", "url", "产品名称", "公司名称", "原产地", "商标", "金额" };
		try {
			book = Workbook.createWorkbook(new File(excelFileName));
			// 生成名为searchName的工作表，参数0表示第一页
			WritableSheet sheet = book.createSheet(searchName, 0);
			// 表头导航
			for (int i = 0; i < info.length; i++) {
				Label label = new Label(i, 0, info[i]);
				sheet.addCell(label);
			}
			for (int i = 1; i <= pages; i++) {
				String url = baseurl + i + ".html";
				logger.info("=================== 第" + i + "页");
				List<UrlInfo> list = searchList(url);
				for (int j = 0; j < list.size(); j++) {
					UrlInfo obj = list.get(j);
					int col = 0;
					int row = obj.getNum();
					sheet.addCell(new Label(col++, row, obj.getNum() + ""));
					sheet.addCell(new Label(col++, row, obj.getUrl()));
					sheet.addCell(new Label(col++, row, obj.getProName()));
					sheet.addCell(new Label(col++, row, obj.getComName()));
					sheet.addCell(new Label(col++, row, obj.getOriplace()));
					sheet.addCell(new Label(col++, row, obj.getBrandName()));
					sheet.addCell(new Label(col++, row, obj.getPrice()));
				}
			}
			// 写入数据并关闭文件
			book.write();
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(e);
		} finally {
			if (book != null) {
				try {
					book.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			File f = new File("excelFileName");
			f.delete();
		}
	}

	/**
	 * 查询列表
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 * @throws IOException
	 */
	private List<UrlInfo> searchList(String url) throws Exception {
		List<UrlInfo> list = new ArrayList<UrlInfo>();
		Document doc;
		logger.info("========= search list：" + url);
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			logger.debug("======== search list 报错：" + url, e);
			throw new Exception();
		}
		String str = doc.data();
		String[] arr = str.split("productHref\":\"");
		for (int i = 1; i < arr.length; i++) {
			String proUrl = arr[i].split("\",")[0];
			proUrl = proUrl.replace("\\u002f", "/");
			proUrl = proUrl.replace("\\u002d", "-");
			UrlInfo obj = new UrlInfo();
			obj.setUrl("http:" + proUrl);
			searchPro(obj);
			list.add(obj);
		}
		return list;
	}

	/**
	 * search 产品页面
	 * 
	 * @param obj
	 * @throws Exception
	 */
	private void searchPro(UrlInfo obj) throws Exception {
		Thread.sleep(1000); // sleep 1.5s 防止被禁
		Document doc;
		String url = obj.getUrl();

		logger.info("============ " + row + " search pro：" + url);
		try {
			Connection con = Jsoup.connect(
					"http://www.alibaba.com/product-detail/2016-kamry-cigarros-electronicos-vapor-Cassiel_60595723922.html");
			con.header("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
			// 设置cookie和post上面的map数据
			Response response = con.ignoreContentType(true).method(Method.POST).cookies(CookieUtil.getCookie())
					.execute();
			doc = response.parse();
		} catch (IOException e) {
			logger.debug(" search list 报错：" + url, e);
			throw new Exception();
		}
		// 处理验证码
		checkCode(doc);

		// 产品名称
		Elements elements = doc.getElementsByAttributeValue("class", "ma-title-text");
		for (Element e : elements) {
			obj.setProName(e.text());
			break;
		}
		if (obj.getProName() == null || obj.getProName().equals("")) {
			elements = doc.getElementsByAttributeValue("class", "ma-title");
			for (Element e : elements) {
				obj.setProName(e.text());
				break;
			}
		}
		// 公司名称
		elements = doc.getElementsByAttributeValue("class", "company-name link-default");
		for (Element e : elements) {
			obj.setComName(e.text());
			break;
		}
		// 原产地、商标
		elements = doc.getElementsByAttributeValue("class", "ellipsis");
		int n = 0;
		for (Element e : elements) {
			if (n == 0) {
				obj.setOriplace(e.text()); // 原产地
				n++;
			} else {
				obj.setBrandName(e.text()); // 商标
				break;
			}
		}

		// 金额
		searchPrice(doc, obj);

		obj.setNum(row); // 序号
		row++;
		successCount++; // 成功计数
	}

	private void searchPrice(Document doc, UrlInfo obj) throws Exception {
		String priceCurrency = null;
		String lowPrice = null;
		String highPrice = null;
		// // div 里 title <div class="ma-spec-price" title="US $76.00">US $<span
		// class="pre-inquiry-price">76.00</span></div>
		// Elements elements = doc.getElementsByAttributeValue("class",
		// "ma-spec-price");
		// for (Element e : elements) {
		// if (e.attr("title") != null && !e.attr("title").equals("")) {
		// logger.info("============ 区间金额 title");
		// obj.setPrice(e.text());
		// return;
		// }
		// }
		//
		// 有区间金额： US $97.00 US $95.00
		Elements elements = doc.getElementsByAttributeValue("class", "ma-spec-price");
		if (!elements.isEmpty()) {
			logger.info("============ 区间金额");
			for (Element e : elements) {
				obj.setPrice(e.text());
			}
			return;
		}

		// 没区间金额 US：$15 - 150
		if (priceCurrency == null || lowPrice == null || highPrice == null) {
			elements = doc.getElementsByTag("span");
			for (Element e : elements) {
				if (e.attr("itemprop").equals("priceCurrency")) {
					priceCurrency = e.text();
					if (priceCurrency != null && lowPrice != null && highPrice != null) {
						obj.setPrice(priceCurrency + lowPrice + highPrice);
						return;
					}
				} else if (e.attr("itemprop").equals("lowPrice")) {
					lowPrice = e.text();
					if (priceCurrency != null && lowPrice != null && highPrice != null) {
						obj.setPrice(priceCurrency + lowPrice + highPrice);
						return;
					}
				} else if (e.attr("itemprop").equals("highPrice")) {
					highPrice = e.text();
					if (priceCurrency != null && lowPrice != null && highPrice != null) {
						obj.setPrice(priceCurrency + lowPrice + " - " + highPrice);
						return;
					}
				}
			}
		}
		// 直接写的金额： 128 Piece/Pieces
		elements = doc.getElementsByAttributeValue("class", "ma-min-order");
		if (!elements.isEmpty()) {
			for (Element e : elements) {
				obj.setPrice(e.text());
				return;
			}
		}
		logger.info("============  没有金额");
	}

	/**
	 * @Title: checkCode @Description: 处理验证码 @param @param doc @return
	 * void @throws
	 */
	private void checkCode(Document doc) {
		Element e = doc.getElementById("checkcodeImg");
		logger.debug("需要输入验证码");
	}

	public void searchUrl() throws Exception {
		Date sDate = new Date();
		// delFile(); // 删除记录文件
		exportExcel();
		Date eDate = new Date();
		long time = (eDate.getTime() - sDate.getTime()) / 1000;
		logger.info("##########  操作结果，search数量：" + successCount + "" + " 用时" + time + "秒");
	}

	public static void main(String[] args) throws Exception {
		new AlibabaUrlServiceImpl().searchUrl();
	}

}
