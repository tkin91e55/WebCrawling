package com.tkk.webCrawling.webCrawler;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.tkk.webCrawling.Crawlee;

public class TutorGroupCrawler extends BaseCrawler {

	public static String URL_KEY = "WC_URL";

	static final String threadName = "TutorGroup-thread";
	private static TutorGroupCrawler instance = null;

	protected TutorGroupCrawler() {
		super(BaseCrawler.CrawlerKeyBinding.TutorGroup, threadName);
	}

	public static TutorGroupCrawler GetInstance() {
		if (instance == null) {
			instance = new TutorGroupCrawler();
		}
		return instance;
	}

	// Start to run function, shall not be called from external
	public void run() {
		try {
			ProcessUrlsAction();
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	protected void ProcessUrlsAction() {

		@SuppressWarnings("unchecked")
		Collection<String> urls = (Collection<String>) config.get(URL_KEY);
		List<Document> tmpDocs = new ArrayList<Document>();
		List<Crawlee> tmpCrles = new ArrayList<Crawlee>();

		for (String url : urls) {
			System.out.println("The url: " + url);
			try {
				Document aDoc = Jsoup.connect(url).data("query", "Java").userAgent("Mozilla").cookie("auth", "token")
						.timeout(6000).post();
				tmpDocs.add(aDoc);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Do search part:
		AnalyzeContentAction(tmpDocs,tmpCrles);

		synchronized (crawlees) {
			crawlees.addAll(tmpCrles);
			System.out.println("[TutorGroup] crawlees size: " + crawlees.size());
			crawlees.notify();
		}

	}

	public void AnalyzeContentAction(Crawlee crawlee) {
	}

	void AnalyzeContentAction(List<Document> Docs, List<Crawlee> Crles) {
		String JsoupSearchNode_HEAD = "span[id$=cs%d]";
		String JsoupSearchNode_CONTENT = "div[id$=cdiv%d]";

		Pattern dayPattern = Pattern.compile(" [0-9]{1,2} ");
		Date today = new Date();
		DateFormat df = new SimpleDateFormat("dd");
		String todayDay = df.format(today);
		Pattern TodayPattern = Pattern.compile(todayDay);

		for (Document aDoc : Docs) {
			for (int i = 0; i < 30; i++) {
				String header = String.format(JsoupSearchNode_HEAD, i);
				String text = String.format(JsoupSearchNode_CONTENT, i);

				Elements heading = aDoc.select(header);
				Elements content = aDoc.select(text);
				String headingStr = heading.text();
				String contentStr = content.text();

				// Filter out not today's post
				Matcher dayMatcher = dayPattern.matcher(headingStr);
				if (dayMatcher.find()) {
					// System.out.println(dayMatcher.group(0));

					// dayMatcher.group(0) is header_text and 1 is context
					// text
					Matcher TodayMatcher = TodayPattern.matcher(dayMatcher.group(0));
					if (!TodayMatcher.find()) {
						// System.out.println("NONONONO!!!!");
						continue;
					}
					// System.out.println("Today's day: " + todayDay);
				}

				String[] phaseToBeEmpty = { "自我介紹: ", "時間: ", "我同意所有有關導師條款" };
				for (String outPhase : phaseToBeEmpty) {
					headingStr = headingStr.replace(outPhase, "");
					contentStr = contentStr.replace(outPhase, "");
				}
				System.out.println(headingStr);
				System.out.println(contentStr);

				Crawlee tmpCrle = new Crawlee(0, "", this);
				tmpCrle.Put("Website", "Website: " + this.toString());
				tmpCrle.Put("Fee", "Fee: " + GetFee(headingStr));
				tmpCrle.Put("Info", "Info :" + headingStr);
				tmpCrle.Put("Other", "Other: " + contentStr);

				Crles.add(tmpCrle);
			}
		}
	}
	
	String GetFee(String aText) {
		Pattern price = Pattern.compile("\\$[0-9]{2,4}");
		Matcher matcher = price.matcher(aText);
		if(matcher.find()){
			System.out.println("[SearchCritP] the price is : " + matcher.group(0) + " and the text: " + aText);
			return matcher.group(0) + "/hr";
			}
		return "$98734/hr"; 
	}
	
	public void FilterByCritAction() {
		super.FilterByCritAction();
		
		for (Iterator<Crawlee> crawlee_ite = crawlees.iterator(); crawlee_ite.hasNext();) {
			Crawlee crawlee = crawlee_ite.next();
			Boolean beDeleted = true;

			if (FilterInBySubject(crawlee.Context())) {
				if (!FilterByFee(crawlee)) {
					if (FilterOutByLocation(crawlee.Context())) {
						beDeleted = false;
					}
				}
			}

			if (beDeleted) {
				System.out.println("[SearchCrit] TutorGroup Going to delete crawlee: " + crawlee.GetValueByKey("Info"));
				crawlee_ite.remove();
			}
		}
	}
	
	@Override
	public String toString() {
		return "TutorGroupCrawler";
	}
}
