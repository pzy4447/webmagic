package us.codecraft.webmagic.processor.example;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

public class ZhihuProcessor implements PageProcessor {
	// 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
	private Site site = Site.me().setRetryTimes(3).setSleepTime(0);

	@Override
	// process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
	public void process(Page page) {
		// 部分二：定义如何抽取页面信息，并保存下来
		Html html = page.getHtml();
		System.out.printf("html is %s%n", html);
		Selectable s = html.links();
		// 匹配文章标题
		String titleXPathString = "//head/title/text()";
		String title = html.xpath(titleXPathString).toString();
		System.out.printf("title is %s%n", title);
		String articleInfoXPathExp = "//h2/a[@class='question_link']";// /text()
		List<String> articleInfoList = html.xpath(articleInfoXPathExp).all();
		for (String info : articleInfoList) {
			// title
			String titleReg = ">(.+?)</a>";
			Pattern titlePattern = Pattern.compile(titleReg);
			Matcher titleMatcher = titlePattern.matcher(info);
			if (titleMatcher.find()) {
				String articletitle = titleMatcher.group(1);
				System.out.printf("find a title : %s%n", articletitle);
			}
			// url
			String urlReg = "href=\"(.+?)\"";
			Pattern urlPattern = Pattern.compile(urlReg);
			Matcher urlMatcher = urlPattern.matcher(info);
			if (urlMatcher.find()) {
				String articleurl = urlMatcher.group(1);
				System.out.printf("find a url : %s%n", articleurl);
			}
		}
		// page.addTargetRequests(articleTitleRequest);
		// System.out.printf("articleTitleRequest is %s%n",
		// articleTitleRequest);
	}

	@Override
	public Site getSite() {
		return site;
	}

	public static void main(String[] args) {
		// 从"https://github.com/code4craft"开始抓
		String url1 = "http://www.zhihu.com/explore/recommendations";
		Spider.create(new ZhihuProcessor()).addUrl(url1).run();
	}

}
