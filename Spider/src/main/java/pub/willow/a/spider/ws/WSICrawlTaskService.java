package pub.willow.a.spider.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import pub.willow.a.baseservice.beans.TaskBean;

/**
 * 抓取任务服务
 * @author albert.zhang
 * on 2014-6-9
 *
 */
@WebService
public interface WSICrawlTaskService {
	@WebMethod(operationName="crawlTask")
	public TaskBean crawlTask(@WebParam(name="taskBean") TaskBean taskBean);
}
