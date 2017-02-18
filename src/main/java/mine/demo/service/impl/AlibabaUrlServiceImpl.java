package mine.demo.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

@Service("alibabaService")
public class AlibabaUrlServiceImpl implements IAlibabaUrlService {

	private static Logger logger = Logger.getLogger(IAlibabaUrlService.class);

	private String baseurl = "https://www.alibaba.com/products/F0/kamry_cassiel/"; // 搜索的基础url

	private int successCount = 0; // 成功的url数量

	private int pages = 24; // 总页数

	private String searchName = "kamry cassiel"; // search关键字

	private String excelFileName = "C:/Users/yue06/Desktop/2.17/" + searchName + ".xls"; // url

	private int row = 1; // 行

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
			Response response = con.ignoreContentType(true).method(Method.POST).cookies(getCookie()).execute();
			doc = response.parse();
		} catch (IOException e) {
			logger.debug(" search list 报错：" + url, e);
			throw new Exception();
		}
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
	
	Map<String, String> getCookie() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("ali_apache_id", "11.251.145.233.1486290926404.130538.4");
		map.put("ali_beacon_id", "121.71.50.22.1486290927433.992858.2");
		map.put("cna", "oq4dEaTXb28CAXlHMhY5YIvI");
		map.put("t", "f268bfe692a070006bec0c03b7498fca");
		map.put("_uab_collina", "148724658330570282212876");
		map.put("history", "product_selloffer%5E%0A60512867458%24%0A60209693219%24%0A60618065670");
		map.put("gangesweb-buckettest", "121.71.50.22.1486290929013.8");
		map.put("ali_ab", "121.71.50.22.1486290928749.5");
		map.put("_umdata", "535523100CBE37C3EE813EA379A6CA8FA3EB9D0892EF0CB79455BCA8FBA16E7A34055F7D9109BBF4CD43AD3E795C914C38A903F3794CA60E63DF901E058B42A1");
		map.put("acs_usuc_t", "acs_rt=3ce18f4e43f34190847059c90584842f");
		map.put("v", "0");
		map.put("cookie2", "1941d019d6b93df57935da1fe0cc4995");
		map.put("_tb_token_", "5b17709434063");
		map.put("csg", "ea058ab3");
		map.put("xman_us_f", "x_l=1&x_locale=en_US&no_popup_today=n&x_user=HK|Nancy|Wang|ifm|230223677&last_popup_time=1487344384767");
		map.put("xman_us_t", "x_lid=    hk1520143162kbmc&sign=y&x_user=pVCo9dKOubqQVQfFQ59/KTpcIs4w+8eYOHLfFu9fUuw=&ctoken=m883av6s2nsq&need_popup=y&l_source=alibaba");
		map.put("intl_locale", "en_US");
		map.put("intl_common_forever", "agSoCGpT8yh1sLNJXdevHRvdiuxeGxAFjJXbipS/3OdwdMrrLES7tA==");
		map.put("xman_f", "tCn9i1eKfSQqChcKvd41+rEPZ9UR1z/wQmLU0rA1UMFACd4rc7BUAmZ6RRcJDbJZd8ALtqLFrFqb906QxTNXUjhYANCjxgiEGaPeJV6qfKFtgZP5u8agYhPkXRQLbQ9Q4bFCzSxOAGPaIvWD1yAsCUw5rvNm5mD48evx7a2VD/zMcV7OHmQ3DyZLTuoiAtR7OqrCsEnvdjhblLjolFonr1xbez5E7c0mTzFe3Ai4qPjSbS6Po2BgqXR51NtrHK0L1fs7Xj14WsGpvN9gE1p3L5OHyt4PNG7iHKtu3OHjkbLOTByduU34APH0YJo3ZH3nR1OmzGl/rsmym8Z4IM6GDbQDavAr3kLz9L6iRwyYipZtXOL3H0WyIL4i6LVBGdnyEr+J1SWGJiHktTgKtU74zUUpObs0N8m0kGsbauu+IbA=");
		map.put("JSESSIONID", "PLqE11Rb95wrHDj-wxz5qxER");
		map.put("l", "AsXFIT4r1HxnsA00-SIpi8CUVQv/gnkU");
		map.put("isg", "AhYWvZS1UkZn8WbTTwA50ZnNZ8whUVrx7FdArYB_AvmUQ7bd6EeqAXw5rWhV");
		map.put("xman_t", "zBxKjLW+xwdLSGTLrsvr+btRotCgi3Rr6Pgz8q0emdyEXkTBVoBF2cCHDeKWOlzI/8L3FjqYyavT3DWJU0WA713s9W4aPe7gX/9dmMyaZRrmToQIo+EQBA6B8O1+R+rfbtaENssxLqL3DlMOrhAgKTGEswSHFOMW0zVjeHiCeTQDl3t4EuYR3+qMjRqaXCxXuqfGHAcX/H+jTGFmKUXCMxIdWXs/koE1s5ODy688l+RzI0FITEXXJrTXZHVHqHtgv/lbMY3dcPd8xM7J8XjgR1uWnPSk6La1ZWzslK1GrRRLqkaEohSO1N0TD21dZP2KQG/rjHqfLy+4WKNSxaeBcdATdwst0qIio/IIxkHDCFrHHa+21fmOHbz8t+AP/Ns4gWxNrCMzkOuoqWbVQ+FNhDlZZPTa8xVuHb1dXhW96rvLlIKS6TbERUH0ZVimp5rG8GRRO+aSwXkJyoxgpnfkU7CNoNhuJh3mIiuEWbllwPOgXgXPzbxw7/aAsD6I6IYPOuy/gwj5sqlgtzsBsdPt0cUDf7PEHi5syDHUUItph4IN4OZdGcC1hWj2XmiAa98l5arXmlUw55mlNL0f4qU0CDrzUTsAa4S4aXuqe0GU1Cig6qBrlobVHixHqpP6lPRFNT5iSO6bwiBgFAtiWSkk0tgwTjvYDWTV");
				map.put("ali_apache_track", "mt=1|mid=hk1520143162kbmc");
		map.put("ali_apache_tracktmp", "W_signed=Y");
		map.put("acs_rt", "2682efec27f048a8b81c140ea10c28c0");
		return map;
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
