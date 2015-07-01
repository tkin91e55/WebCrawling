import java.io.FileReader;
import java.io.IOException;
import java.util.Formatter;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.FileWriter;
import java.lang.String;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.commons.collections4.*;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.csv.*;

public class CrawlECTutor {

	/**
	 * @param args the command line arguments
	 */

	static class Crawlee {

		public int case_index;
		public String context_text;
		public Crawlee (int idx, String c){
			case_index = idx;
			context_text = c;
		}
	}

	//Params
	public static String URL_KEY = "WC_URL";
	public static String CRIT_KEY = "WC_SEARCH_CRIT";
	public static String[] file_header_mapping = {"TYPE","VALUE"};
	public static String[] phaseToBeEmpty = {" ","",""};
	public static String OUTPUT_DELIMITER = ",";
	public static String OUTPUT_LINE_ENDING = "\n";
	public static String LAST_RECORD = "last_index.csv";
	public static int MAX_CONTU_ERR = 10;

	//For debug use
	public static Boolean SEARCH_LAST_DAY=false;

	//Runtime global var
	static List<Crawlee> crawlees = new ArrayList<Crawlee>();
	static int startIndex = 0; //pop up case start index

	public static void main(String[] args) throws IOException {

		if(args[0] != null){

			try {
				startIndex  = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.err.println("Argument " + args[0] + " must be an integer.");
				System.exit(1);
			}

			MultiMap<String,String> config = new MultiValueMap<String,String>();
			ParseInConfig(config);

			Collection<String> urls = (Collection<String>) config.get(URL_KEY);
			for(String url: urls){
				System.out.println("The url: " + url);
				ProcessUrl(url);
			}

			Collection<String> crits = (Collection<String>) config.get(CRIT_KEY);
			FilterByCriteria(crits);

			//Result:
			for (Crawlee cr: crawlees){
				System.out.println("[SearchCrit] Remaining crawlee: " + cr.case_index + " , " + cr.context_text);
			}

		}else {
			System.err.println("Need to ASSIGN starting pop up case number");
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
			System.out.println("[Apache] apache commons csv here, The TYPE: " + record.get(file_header_mapping[0]) + " and the VALUE: " + record.get(file_header_mapping[1]));
			mapConfig.put(record.get(file_header_mapping[0]),record.get(file_header_mapping[1]));
		}
	}

	static void ProcessUrl (String urlStr) throws IOException {

		//	DoSearchOnContent (aDoc);
		boolean loop = true;
		int _case = 0;
		int continuous_error_count = 0;

		_case = startIndex;

		while (loop) {
			String URL = urlStr + Integer.toString(_case);
			System.out.println("URL : "+ URL);
			Document aDoc = Jsoup.connect(URL).data("query","Java").userAgent("Mozilla").cookie("auth","token").timeout(6000).post();

			if (!aDoc.text().contains("Server Error")) {
				//	String title = aDoc.title();
				//	System.out.println("[Doc] Title: " + title);
				//	String result = aDoc.text();
				//	System.out.println("[Doc] Result: " + result);

				DoSearchOnContent(aDoc,_case);
				continuous_error_count = 0;
			}
			else {
				continuous_error_count++;
				if(continuous_error_count >= MAX_CONTU_ERR){
					loop = false;
				}
			}

			if (!loop){

				Date today = new Date();
				DateFormat df = new SimpleDateFormat();
				FileWriter filewriter = new FileWriter(LAST_RECORD,true);
				filewriter.append(df.format(today));
				filewriter.append(",");
				filewriter.append(Integer.toString(_case-continuous_error_count));
				filewriter.append("\n");
				filewriter.close();

				break;
			}
			_case++;
		}
	}

	static void DoSearchOnContent (Document doc, int indx) throws IOException {

		//String JsoupSearchNode_CONTENT = "div[class$=detail]:eq(1)";
		//String text = String.format(JsoupSearchNode_CONTENT);
		//System.out.println("the text: " + text);

	MultiMap<String,String> searchNodes = new MultiValueMap<String,String>();
	searchNodes.put("Location","span[class$=title]");
	searchNodes.put("LastUpdateAt","span[class$=loginTime]");
	searchNodes.put("Detail","div[class$=detail]:eq(1) > p:eq(2)");
	searchNodes.put("Time","div[class$=detail]:eq(1) > p:eq(2)");

		//Elements heading = doc.select(header);
		Elements content = doc.select(text);
		//String headingStr = heading.text();
		String contentStr = content.text();

		//Filter out not today's post
		//Pattern dayPattern = Pattern.compile(" [0-9]{1,2} ");
		//Matcher dayMatcher = dayPattern.matcher(headingStr);
		//if (dayMatcher.find()){
		//System.out.println(dayMatcher.group(0)); //print the day of the month
		//String todayDay;
		//Date today = new Date();
		//DateFormat df = new SimpleDateFormat("dd");

		//if(!SEARCH_LAST_DAY)
		//	todayDay = df.format(today); 
		//else {
		//	Calendar cal = Calendar.getInstance(); //TEMP: get yesterday's date
		//	cal.add(Calendar.DATE, -1); //TEMP: get yesterday's date
		//	todayDay = df.format(cal.getTime()); //TEMP: get yesterday's date

		//}

		//Pattern TodayPattern = Pattern.compile(todayDay);
		//Matcher TodayMatcher = TodayPattern.matcher(dayMatcher.group(0)); //dayMatcher.group(0) is header_text, and 1 is content_text
		//if (!TodayMatcher.find()){
		//System.out.println("NONONONO!!!!");
		//continue;
		//}
		//	System.out.println("Today's day: " + todayDay);
		//}

		for (String outPhase: phaseToBeEmpty){
			//headingStr = headingStr.replace(outPhase,"");
			contentStr = contentStr.replace(outPhase,"");
		}
		//			System.out.println(headingStr);
		System.out.println(contentStr);

		crawlees.add(new Crawlee(indx,contentStr));
		//	System.out.println("crawlees size: " + crawlees.size());
	}

	static void FilterByCriteria (Collection<String> Crits) throws IOException {

		for (Iterator<Crawlee> crawlee_ite = crawlees.iterator(); crawlee_ite.hasNext();) {
			Crawlee crawlee = crawlee_ite.next();
			Boolean beDeleted = true;

			for (String aCrit: Crits){
				Pattern crit = Pattern.compile(aCrit);
				//Matcher matcher = crit.matcher(crawlee.header_text);
				Matcher matcher2 = crit.matcher(crawlee.context_text);

				//if(matcher.find() || matcher2.find()){
				if(matcher2.find()){
					beDeleted = false;
				}
			}
			if(beDeleted) {
				System.out.println("[SearchCrit] Going to delete crawlee: " + crawlee.case_index + " , " + crawlee.context_text);
				crawlee_ite.remove();
			}
			}
		}

		static void ParseInResult () throws IOException {

			//Parsing
			/*FileWriter filewriter = new FileWriter("result.csv");
			  filewriter.append("HEAD,CONTENT\n"); 
			  for (Crawlee cr: crawlees){
			  filewriter.append("\""+cr.header_text+"\"");
			  filewriter.append(OUTPUT_DELIMITER);
			  filewriter.append("\""+cr.context_text+"\"");
			  filewriter.append(OUTPUT_LINE_ENDING);
			  }
			  filewriter.close();*/

		}
	}
