package pub.willow.a.spider.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.log4j.Logger;

import pub.willow.a.baseservice.beans.SpiderParamsBean;
import pub.willow.a.baseservice.beans.TaskBean;

/**
 * 基础抓取服务
 * 
 * @author albert.zhang on 2014-7-16
 * 
 */
public class SpiderService {
	// Cookie类型
	public static final int COOKIE_TYPE = 1;
	// Header类型
	public static final int HEADER_TYPE = 3;
	// Header多个参数之间的分隔符
	public static final String HEADER_PARAM_SEPARATE = "\\{;\\}";
	// Header的key,value之间的分隔符
	public static final String HEADER_KEY_VALUE_SEPARATE = "\\{-\\}";

	private Logger log = Logger.getLogger("SpiderHtmlService");

	public TaskBean spiderHtml(TaskBean taskBean) throws Exception {
		String url = taskBean.getUrl();
		String charset = taskBean.getCharset();
		SpiderParamsBean spiderParamConfig = taskBean.getSpiderParamsBean();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("Referer", url);
		httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:28.0) Gecko/20100101 Firefox/28.0");
		httpGet.setHeader("Connection", "keep-alive");
		httpGet.setHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		httpGet.setHeader("Accept-Encoding", "gzip, deflate");

		// 抓取参数设置
		if (spiderParamConfig != null) {
			List<String> headers = spiderParamConfig.getHeaders();
			for (String header : headers) {
				String[] keyValueArray = header.split(HEADER_KEY_VALUE_SEPARATE);
				if (keyValueArray != null && keyValueArray.length == 2) {
					httpGet.setHeader(keyValueArray[0], keyValueArray[1]);
				}
			}
		}

		client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);// 连接时间20s
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
		
		HttpResponse httpResponse = client.execute(httpGet);  
		int statusCode = httpResponse.getStatusLine().getStatusCode();  
		if (statusCode == HttpStatus.SC_OK) {  
		    InputStream is = httpResponse.getEntity().getContent();  
		    String source = getResponseBodyAsString(is,charset);
		    taskBean.setSource(source);
		}  
		
		return taskBean;
	}

	/**
	 * 获取网页编码格式
	 * 
	 * @param html
	 * @return
	 * @throws IOException
	 */
	public String getSourceByCharset(InputStream is, String charset) throws IOException {
		byte[] by = new byte[40000];
		List<byte[]> arr = new ArrayList<byte[]>();
		int actlength = 0;
		while ((actlength = is.read(by)) != -1) {
			byte[] b = Arrays.copyOf(by, actlength);
			if (charset == null || "".equals(charset)) {
				BytesEncodingDetect s = new BytesEncodingDetect();
				charset = BytesEncodingDetect.javaname[s.detectEncoding(b)];
			}
			arr.add(b);
		}
		actlength = 0;
		for (byte[] b : arr) {
			actlength += b.length;
		}
		byte[] by1 = new byte[actlength];
		actlength = 0;
		for (byte[] b : arr) {
			System.arraycopy(b, 0, by1, actlength, b.length);
			actlength += b.length;
		}
		return new String(by1, charset).replaceAll("\u0000", "");
	}

	/**
	 * 处理gzip压缩形式的数据
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public String getResponseBodyAsString(InputStream is, String charset) throws IOException {
		InputStream gzin = new GZIPInputStream(is);

		return getSourceByCharset(gzin, charset);
	}

	public static void saveFile(String text, String url) {
		File f = new File("d:/" + url + ".txt");
		FileWriter fw;
		BufferedWriter bw;
		try {
			fw = new FileWriter(f);// 初始化输出流
			bw = new BufferedWriter(fw);// 初始化输出字符流
			bw.write(text);// 写文件
			bw.flush();
			bw.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 构造抓取结果json
	 * 
	 * @param response
	 * @param charset
	 * @param responseCode
	 * @param taskJson
	 * @param urlhashcode
	 */
	private TaskBean makeJsonForResult(String response, String charset, int responseCode, TaskBean taskBean,
			String urlhashcode, String realUrl) {
		if (taskBean.getSiteId() == 19352 && !taskBean.getUrl().contains("zhuanlan.zhihu.com")) {
			response = decodeUnicode(response);
		}
		taskBean.setCharset(charset);
		return taskBean;
	}

	/**
	 * 判断是否是搜狗微信关键字任务，如果是则返回true。
	 * 
	 * @param taskBean
	 * @return
	 */
	private boolean isSougouWeixinKeywordTask(TaskBean taskBean) {
		if (taskBean == null) {
			return false;
		}
		if (taskBean.getSiteId() != 11) {
			return false;
		}
		int listpageId = taskBean.getListpageId();
		int[] listpageIds = { 3, 57, 4389, 5007, 5008, 5009, 5010 };
		for (int listpageIdTmp : listpageIds) {
			if (listpageId == listpageIdTmp) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * unicode 转换成 中文
	 * 
	 * @param theString
	 * @return
	 */
	public static String decodeUnicode(String theString) {
		char aChar;
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len);
		for (int x = 0; x < len;) {
			aChar = theString.charAt(x++);
			if (aChar == '\\') {
				aChar = theString.charAt(x++);
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = theString.charAt(x++);
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException("Malformed   \\uxxxx   encoding.");
						}
					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					outBuffer.append(aChar);
				}
			} else
				outBuffer.append(aChar);
		}
		return outBuffer.toString();
	}

	public static void main(String[] args) throws Exception {
		System.out.println("HH");
	}
}
