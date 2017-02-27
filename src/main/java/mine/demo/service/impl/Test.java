package mine.demo.service.impl;

import java.io.IOException;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import mine.demo.util.CookieUtil;

public class Test {

	/**
	 * http://www.alibaba.com/product-detail/elektronik-sigara-electronic-cigarette-kamry-cassiel_60599087821.html
	 * http://www.alibaba.com/product-detail/Kamry-Cassiel-e-cigs-vape-pen_60589452246.html
	 * http://www.alibaba.com/product-detail/cigarette-liquid-vapor-refills-Kamry-Cassiel_60585450687.html
	 * http://www.alibaba.com/product-detail/Wholesale-china-accept-paypal-510thread-kamry_60599080662.html
	 */
	// https://login.alibaba.com

	public static void main(String[] args) throws IOException {

		int i = 0;
		while (i < 50) {
			String url = "//pin.aliyun.com/get_img?sessionid=8ba722c3a2db5b52fd10877fb49ed1cf&identity=sm-aisn-detail&type=default";

//			Map<String, String> cookies = CookieUtil.getCookie();

			Document doc = Jsoup.connect(url).get();
			Element e = doc.getElementById("checkcodeImg");
			System.out.println("e:" + e);
			if (e != null) {
				System.out.println("src: " + e.attr("src"));
			}
			i++;
		}

	}

}
