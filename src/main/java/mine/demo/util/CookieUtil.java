package mine.demo.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

public class CookieUtil {

	public static void main(String[] args) {
		System.out.println(getCookie());
	}

	private static String filePath = "C:/Users/hh/Desktop/2.27/cookie.txt";

	public static Map<String, String> getCookie() {
		String cookie = readTxtFile();
		Map<String, String> cookieMap = new HashMap<String, String>();
		if (!StringUtils.isEmpty(cookie)) {
			String[] cookieArray = cookie.split("; ");
			for (String code : cookieArray) {
				int i = code.indexOf("=");
				cookieMap.put(code.substring(0, i), code.substring((i+1), code.length()));
			}
		}
		return cookieMap;
	}
	
	private static String readTxtFile() {
		String cookie = "";
		try {
			String encoding = "GBK";
			File file = new File(filePath);
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					cookie = lineTxt;
				}
				read.close();
			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		return cookie;
	}

}
