package pub.willow.a.spider;

import javax.annotation.Resource;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import pub.willow.a.baseservice.beans.TaskBean;
import pub.willow.a.spider.service.SpiderService;

public class TestSpiderHtmlService extends AbstractDependencyInjectionSpringContextTests {
	
	@Override
	protected String[] getConfigLocations() {
		return new String[] { 
				"applicationContext-init.xml","applicationContext-service.xml"
//				"applicationContext-*.xml"
	  			  };
	}
	
	@Resource(name="spiderService")
	public SpiderService spiderService;
	
	
	public void testSpiderHtml(){
		TaskBean taskBean = new TaskBean();
		taskBean.setUrl("http://bbs.csdn.net/topics/390615511");
		try {
			System.out.println(spiderService);
			spiderService.spiderHtml(taskBean );
			String source = taskBean.getSource();
			System.out.println(source);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
