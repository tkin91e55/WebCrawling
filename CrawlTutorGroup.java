import java.io.FileReader;
import java.io.IOException;
import java.util.Formatter;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Date;
import java.util.regex.Matcher;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.commons.collections4.*;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.csv.*;

public class CrawlTutorGroup {

	/**
	 * @param args the command line arguments
	 */

	static class Crawlee {

		public String header_text;
		public String context_text;
		public Crawlee (String h, String c){
			header_text = h;
			context_text = c;
		}
	}

	//Params
	public static String URL_KEY = "WC_URL";
	public static String CRIT_KEY = "WC_SEARCH_CRIT";
	public static String[] file_header_mapping = {"TYPE","VALUE"};
	public static String[] phaseToBeEmpty = {"自我介紹: ","時間: ","我同意所有有關導師條款"};
	public static String JsoupSearchNode_HEAD = "span[id$=cs%d]";
	public static String JsoupSearchNode_CONTENT = "div[id$=cdiv%d]";

	//Runtime global var
	static List<Crawlee> crawlees = new ArrayList<Crawlee>();

	public static void main(String[] args) throws IOException {

		MultiMap<String,String> config = new MultiValueMap<String,String>();
		ParseInConfig(config);

		Collection<String> urls = (Collection<String>) config.get(URL_KEY);
		for(String url: urls){
			System.out.println("The url: " + url);
			ProcessUrl(url);
		}
		Collection<String> crits = (Collection<String>) config.get(CRIT_KEY);
		/*for(String crit: crits){
		  System.out.println("The crit: " + crit);
		  FilterByCriteria(crit);
		  }*/

		FilterByCriteria(crits);

		//Result:
		for (Crawlee cr: crawlees){
			System.out.println("[SearchCrit] Remaining crawlee: " + cr.header_text + " , " + cr.context_text);
		}
	}

	static void ParseInConfig (MultiMap<String,String> mapConfig) throws IOException {

		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(file_header_mapping);
		FileReader fileReader = new FileReader("config.csv");
		System.out.println("The encoding is: " + fileReader.getEncoding());
		CSVParser csvFileParser = new CSVParser(fileReader, csvFileFormat);
		List csvRecords = csvFileParser.getRecords();
		System.out.println("[Apache] csvRecords.getRecords() size: " + csvRecords.size());

		for(int i = 1; i < csvRecords.size(); i++) {
			CSVRecord record = (CSVRecord) csvRecords.get(i);
			System.out.println("Testing apache commons csv here, The TYPE: " + record.get(file_header_mapping[0]) + " and the VALUE: " + record.get(file_header_mapping[1]));
			mapConfig.put(record.get(file_header_mapping[0]),record.get(file_header_mapping[1]));
		}
	}

	static void ProcessUrl (String urlStr) throws IOException {
		//suppose you understanding the incoming args
		Document aDoc = Jsoup.connect(urlStr).data("query","Java").userAgent("Mozilla").cookie("auth","token").timeout(6000).post();

		//String title = doc.title();
		//System.out.println(title);
		//String result = doc.text();
		//System.out.println(result);

		DoSearchOnContent (aDoc);
	}

	static void DoSearchOnContent (Document doc) throws IOException {
		for (int i=0; i<30; i++) {
			String header = String.format(JsoupSearchNode_HEAD,i);
			String text = String.format(JsoupSearchNode_CONTENT,i);
			//System.out.println("The header: " + header + " and the text: " + text);

			Elements heading = doc.select(header);
			Elements content = doc.select(text);
			String headingStr = heading.text();
			String contentStr = content.text();

			//Filter out not today's post
			Pattern dayPattern = Pattern.compile(" [0-9]{1,2} ");
			Matcher dayMatcher = dayPattern.matcher(headingStr);
			if (dayMatcher.find()){
				System.out.println(dayMatcher.group(0));
				String todayDay;
				Date today = new Date();
				DateFormat df = new SimpleDateFormat("dd");

				//TEMP: get yesterday's date
				Calendar cal = Calendar.getInstance(); //TEMP: get yesterday's date
				cal.add(Calendar.DATE, -1); //TEMP: get yesterday's date
				todayDay = df.format(cal.getTime()); //TEMP: get yesterday's date
				//todayDay = df.format(today); //TEMP: get yesterday's date

				Pattern TodayPattern = Pattern.compile(todayDay);
				Matcher TodayMatcher = TodayPattern.matcher(dayMatcher.group(0)); //dayMatcher.group(0) is header_text, and 1 is content_text
				if (!TodayMatcher.find()){
					System.out.println("NONONONO!!!!");
					continue;
				}
				//	System.out.println("Today's day: " + todayDay);
			}

			for (String outPhase: phaseToBeEmpty){
				headingStr = headingStr.replace(outPhase,"");
				contentStr = contentStr.replace(outPhase,"");
			}
			System.out.println(headingStr);
			System.out.println(contentStr);

			crawlees.add(new Crawlee(headingStr,contentStr));
			//	System.out.println("crawlees size: " + crawlees.size());
		}
	}
	static void FilterByCriteria (Collection<String> Crits) throws IOException {

		for (Iterator<Crawlee> crawlee_ite = crawlees.iterator(); crawlee_ite.hasNext();) {
			Crawlee crawlee = crawlee_ite.next();
			Boolean beDeleted = true;

			for (String aCrit: Crits){
				Pattern crit = Pattern.compile(aCrit);
				Matcher matcher = crit.matcher(crawlee.header_text);
				Matcher matcher2 = crit.matcher(crawlee.context_text);

				if(matcher.find() || matcher2.find()){
					beDeleted = false;
				}
			}
			if(beDeleted) {
				System.out.println("[SearchCrit] Going to delete crawlee: " + crawlee.header_text + " , " + crawlee.context_text);
				crawlee_ite.remove();
			}
		}
	}
}
