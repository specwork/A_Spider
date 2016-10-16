package pub.willow.a.spider.ws.impl;

import javax.annotation.Resource;

import pub.willow.a.baseservice.beans.TaskBean;
import pub.willow.a.spider.service.SpiderService;
import pub.willow.a.spider.ws.WSICrawlTaskService;

public class WSCrawlTaskServiceImpl implements WSICrawlTaskService{
	@Resource(name="spiderService")
	public SpiderService spiderService;
	@Override
	public TaskBean crawlTask(TaskBean taskBean) {
		try {
			taskBean = spiderService.spiderHtml(taskBean);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return taskBean;
	}
	
}
