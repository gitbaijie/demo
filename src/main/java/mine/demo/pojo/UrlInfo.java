package mine.demo.pojo;

public class UrlInfo {

	/** 序号 */
	private Integer num;

	/** url */
	private String url;

	/** 产品名称 */
	private String proName;

	/** 公司名称 */
	private String comName;

	/** 原产地 */
	private String oriplace;

	/** 商标 */
	private String brandName;

	/** 金额 */
	private String price;

	public UrlInfo(Integer num, String url, String proName, String comName, String oriplace, String price) {
		super();
		this.num = num;
		this.url = url;
		this.proName = proName;
		this.comName = comName;
		this.oriplace = oriplace;
		this.price = price;
	}

	public UrlInfo() {
	}

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getProName() {
		return proName;
	}

	public void setProName(String proName) {
		this.proName = proName;
	}

	public String getComName() {
		return comName;
	}

	public void setComName(String comName) {
		this.comName = comName;
	}

	public String getOriplace() {
		return oriplace;
	}

	public void setOriplace(String oriplace) {
		this.oriplace = oriplace;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

}
