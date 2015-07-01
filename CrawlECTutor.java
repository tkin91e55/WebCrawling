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
import java.util.HashMap;

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
		public HashMap<String,String> map = new HashMap<String,String>();

		public Crawlee (int idx){
			case_index = idx;
		}

		public void Put (String Key, String Value) {
			map.put(Key,Value);
		}

		public String Context () {
			String content = "";
			Collection<String> strings = map.values();
			for (String str: strings){
				content = content + str + "\n";
			}
			//System.out.println("[Crawlee] content: " + content);
			return content;
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
				//System.out.println("[SearchCrit] Remaining crawlee: " + cr.case_index + " , " + cr.context_text);
				System.out.println("[SearchCrit] Remaining crawlee: " + cr.case_index);
			}

			ParseInResult();

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

		HashMap<String,String> searchNodes = new HashMap<String,String>();
		searchNodes.put("Location","span[class$=title]");
		searchNodes.put("LastUpdateAt","span[class$=loginTime]");
		//searchNodes.put("Detail","div[class$=detail]:eq(1) > p:eq(2)");
		searchNodes.put("Details","div[class$=detail] > div[class$=item]");
		//String JsoupSearchNode_CONTENT = "div[class$=detail]:eq(1)";

		Elements location = doc.select(searchNodes.get("Location"));
		Elements lastUpdate = doc.select(searchNodes.get("LastUpdateAt"));
		Elements eles = doc.select(searchNodes.get("Details"));

		System.out.println("[Jsoup] location: " + location.text() + " and lastUpdate: " + lastUpdate.text());

		/*for (int i = 0; i < eles.size(); i++){
		  Element ele = eles.get(i);
		  System.out.println("[Jsoup] ele text: " + ele.text());
		  }*/

		Crawlee crawlee = new Crawlee(indx);
		//location
		crawlee.Put("Location","Location: " + location.text());
		//LastupdateAt
		crawlee.Put("LastUpdateAt","Last Update: " + lastUpdate.text());
		//Time
		crawlee.Put("Time", eles.get(0).text());
		//Gender
		crawlee.Put("Gender", eles.get(1).text());
		//Info
		crawlee.Put("Info", eles.get(2).text());
		//Subject
		crawlee.Put("Subject", eles.get(3).text());
		//Fee
		crawlee.Put("Fee", eles.get(4).text());
		//Other
		crawlee.Put("Other", eles.get(5).text());

		crawlees.add(crawlee);
		System.out.println("[Crawlee] crawlees size: " + crawlees.size() + " and the cralwee content: \n" + crawlee.Context());
	}

	static void FilterByCriteria (Collection<String> Crits) throws IOException {

		for (Iterator<Crawlee> crawlee_ite = crawlees.iterator(); crawlee_ite.hasNext();) {
			Crawlee crawlee = crawlee_ite.next();
			Boolean beDeleted = true;

			for (String aCrit: Crits){
				Pattern crit = Pattern.compile(aCrit);
				Matcher matcher = crit.matcher(crawlee.Context());

				if(matcher.find()){
					beDeleted = false;
				}
			}
			if(beDeleted) {
				//	System.out.println("[SearchCrit] Going to delete crawlee: " + crawlee.case_index + " , " + crawlee.context_text);
				System.out.println("[SearchCrit] Going to delete crawlee: " + crawlee.case_index);
				crawlee_ite.remove();
			}
		}
	}

	static void ParseInResult () throws IOException {

		//Parsing
		FileWriter filewriter = new FileWriter("result.csv");
		  filewriter.append(new SimpleDateFormat().format(new Date()) + " 's update:\n"); 
		  for (Crawlee cr: crawlees){
		  filewriter.append("The case index: " + cr.case_index);
		  filewriter.append(cr.Context());
		  filewriter.append(OUTPUT_LINE_ENDING);
		  }
		  filewriter.close();

	}
}
