package mine.demo.test;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import mine.demo.service.IAlibabaUrlService;

@RunWith(SpringJUnit4ClassRunner.class)		//表示继承了SpringJUnit4ClassRunner类
@ContextConfiguration(locations = {"classpath:spring-mybatis.xml"})
public class TestAlibaba {
	@Resource
	private IAlibabaUrlService service = null;

	@Test
	public void test() throws Exception {
		service.searchUrl();
	}
	
}
