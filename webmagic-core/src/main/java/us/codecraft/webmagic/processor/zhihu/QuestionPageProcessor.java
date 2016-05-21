package us.codecraft.webmagic.processor.zhihu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

/**
 * @author lenovo 处理问题页面，提取问题的标题、描述以及每个答案的url
 */
public class QuestionPageProcessor implements PageProcessor {
	// 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
	private Site site = Site.me().setRetryTimes(3).setSleepTime(0);
	private Map<String, String> articleMap = new HashMap<String, String>();
	private Page currentPage;

	@Override
	// process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
	public void process(Page page) {
		processByXPath(page);
	}

	public void processByXPath(Page page) {
		Html html = page.getHtml();
		// System.out.printf("html is %s%n", html);
		Selectable s = html.links();
		// 匹配标题
		String titleXPathString = "//head/title/text()";
		String title = html.xpath(titleXPathString).toString();
		System.out.printf("title is %s%n", title);
		// 问题详情
		String questionDetailXPathExp = "//div[@id='zh-question-detail']/div[@class='zh-summary summary clearfix']/text()";
		String questionDetail = html.xpath(questionDetailXPathExp).get();
		System.out.printf("questionDesc is : %s%n", questionDetail);
		// 抽取所有答案的url、回答者姓名、回答者ID、答案点赞数
		// 抽取所有回答者姓名
		String answerXPathExp = "//div[@class='zm-item-answer  zm-item-expanded']";
		List<String> answerInfoList = html.xpath(answerXPathExp).all();
		System.out.printf("answerinfo[0] is : %s%n", answerInfoList.get(0));

		// extractInfo(html);

		// 保存到文件
		// saveToPipeline(page);
	}

	public static void main(String[] args) {
		/*
		 * Page page = new Page(); String content =
		 * "<div id=\"zh-question-detail\" class=\"zm-item-rich-text\" data-resourceid=\"710427\" data-action=\"/question/detail\"><textarea class=\"content hidden\">对这个问题很感兴趣，附上原文，不大相信，加起来用了8小时左右。</textarea><div class=\"zh-summary summary clearfix\">对这个问题很感兴趣，附上原文，不大相信<br><br>有一个咨询公司的创始人招聘员工时，布置了这样一份任务——一周之内给我一份某一个行业的报告。来投简历并写报告书的不乏国内外名校的学生，可是他失望的发现没有一个应聘者达标了，他收到的那些报告书都没有一个亮点。<br><br>一个星期之内如何摸清一个行业的情况呢？让他来和我们说说吧。<br><br>一默是恒嘉智略咨询有限公司的创始人，《销售无处不在》的作者。<br>他说：<br>“了解一个行业”这件事本身不太可能快速完成。不过，如果我们只是想摸清楚最基本的情况，我们可以通过问对几个关键问题着手。这些关键问题围绕着一个根本问题：这个行业的链条是如何运转起来的？<br><br>1  这个行业的存在是因为它提供了什么价值？<br>2  这个行业从源头到终点都有哪些环节？<br>3  这个行业的终端产品售价都由谁分享？<br>4  每个环节凭借什么关键因素，创造了什么价值获得他所应得的利益？<br>5  谁掌握产业链的定价权？<br>6  这个行业的市场集中度如何？<br><br>而信息获取的渠道，则包括：<br>1  金融投资机构的行业报告；…<a href=\"javascript:;\" class=\"toggle-expand\">显示全部</a></div>"
		 * ; Html html = new Html(content); page.setHtml(html); new
		 * QuestionPageProcessor().process(page);
		 */
		// String url1 = "https://www.zhihu.com/question/21324385";
		String url1 = "http://192.168.1.104:8080/question.html";
		Spider.create(new QuestionPageProcessor()).addUrl(url1).run();
	}

	public void extractInfo(List<String> articleInfoList) {
		for (String info : articleInfoList) {
			// title
			String articletitle = getMatchedString(">(.+?)</a>", info);
			if (articletitle == null)
				continue;
			// url
			String articleurl = getMatchedString("href=\"(.+?)/answer", info);
			// System.out.printf("find a title : %s%n", articletitle);
			// System.out.printf("find a url : %s%n", articleurl);
			articleMap.put(articletitle, articleurl);
		}
	}

	public void saveToPipeline(Page page) {
		int i = 1;
		for (String title : articleMap.keySet()) {
			page.putField("title" + i, title);
			page.putField("url" + i, articleMap.get(title));
			i++;
		}
	}

	public String getMatchedString(String rex, String content) {
		String matchedString = null;
		Pattern pattern = Pattern.compile(rex);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			matchedString = matcher.group(1);
		}
		return matchedString;
	}

	@Override
	public Site getSite() {
		return site;
	}
}