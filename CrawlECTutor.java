import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileWriter;
import java.lang.String;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Formatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.commons.collections4.*;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.csv.*;

public class CrawlECTutor {
	//Params
	public static String URL_KEY = "WC_URL";
	public static String URL_INDEX_KEY = "WC_INDEX_URL";
	public static String CRIT_SUBJECT_KEY = "WC_SEARCH_CRIT";
	public static String CRIT_LOCATION_KEY = "WC_SEARCH_OUT_CRIT";
	public static String CRIT_PRICE_KEY = "WC_SEARCH_COND_PRICE_ABOVE";
	public static String[] config_header_mapping = {"TYPE","VALUE"};
	public static String OUTPUT_DELIMITER = ",";
	public static String OUTPUT_LINE_ENDING = "\n";
	public static String LAST_RECORD = "last_index.csv";
	public static int MAX_CONTU_ERR = 10;

	//Runtime global var
	static List<Crawlee> crawlees = new ArrayList<Crawlee>();
	static int startIndex = 0; //pop up case start index

	public static void main(String[] args) throws IOException,ParseException {

		MultiMap<String,String> config = new MultiValueMap<String,String>();
		ParseInConfig(config);

		ProcessUrl(config);

		//System.out.println("[Counting] MatchBeforeWriteDBcount: " + Crawlee_DB.MatchBeforeWriteDBcount); 
		//System.out.println("[Counting] MatchBeforeWriteDBLoopCnt: " + Crawlee_DB.MatchBeforeWriteDBLoopCnt); 
		//System.out.println("[Counting] WriteToDBcount: " + Crawlee_DB.WriteToDBcount); 
		//System.out.println("[Counting] WriteToDBLoopCnt: " + Crawlee_DB.WriteToDBLoopCnt); 

		FilterByCriteria(config);

		//Result:
			for (Crawlee cr: crawlees){
				System.out.println("[SearchCrit] Remaining crawlee: " + cr.case_index);
			}

			ParseInResult();

	}

	static void ParseInConfig (MultiMap<String,String> mapConfig) throws IOException {

		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(config_header_mapping);
		FileReader fileReader = new FileReader("config.csv");
		//System.out.println("The encoding is: " + fileReader.getEncoding());
		CSVParser csvFileParser = new CSVParser(fileReader, csvFileFormat);
		List<CSVRecord> csvRecords = csvFileParser.getRecords();
		//System.out.println("[Apache] csvRecords.getRecords() size: " + csvRecords.size());

		for(int i = 1; i < csvRecords.size(); i++) {
			CSVRecord record = csvRecords.get(i);
			System.out.println("[Apache] apache commons csv here, The TYPE: " + record.get(config_header_mapping[0]) + " and the VALUE: " + record.get(config_header_mapping[1]));
			mapConfig.put(record.get(config_header_mapping[0]),record.get(config_header_mapping[1]));
		}
	}

	static void ProcessUrl (MultiMap<String,String> config) throws IOException,ParseException {
		Date runTime = new Date();

		@SuppressWarnings({"unchecked"})
			Collection<String> idx_urls = (Collection<String>) config.get(URL_INDEX_KEY);

		//load inx board page to get on-board indices
		for(String idx_url: idx_urls){
			System.out.println("The idx url: " + idx_url);

			Document idxDoc = Jsoup.connect(idx_url).data("query","Java").userAgent("Mozilla").cookie("auth","token").timeout(6000).post();

			List<String> onboard_indices = new ArrayList<String>();
			Pattern atrbt = Pattern.compile("bk_case_[0-9]{6}");
			Matcher idxMatcher = atrbt.matcher(idxDoc.body().toString());

			while(idxMatcher.find()){
				String str = idxMatcher.group();
				str = str.substring(str.lastIndexOf('_') + 1);
				onboard_indices.add(str);
			}

			Collections.sort(onboard_indices);

			Crawlee_DB DBagent = new Crawlee_DB();

			System.out.println("[DB] DBagent size: " + DBagent.Size());

			//Do searches on remote website contents
			for(String index: onboard_indices){
				//System.out.println("[On-board] idx : " + str);
				@SuppressWarnings({"unchecked"})
					Collection<String> urls = (Collection<String>) config.get(URL_KEY);
				for(String url: urls){
					String URL = url + index;

					System.out.println("[ProcessUrl] url connected: " + URL);

					Document caseDoc = Jsoup.connect(URL).data("query","Java").userAgent("Mozilla").cookie("auth","token").timeout(6000).post();
					if (!caseDoc.text().contains("Server Error")) {
						//      String title = caseDoc.title();
						//      System.out.println("[Doc] Title: " + title);
						//      String result = caseDoc.text();
						//      System.out.println("[Doc] Result: " + result);
						Crawlee crawlee = DoSearchOnContent(caseDoc,Integer.parseInt(index)); //crawlees got filled

						//Add qualified curled case to csv, Crawlee_DB.WriteToDBFile()
						if(!DBagent.LookUpFromDB(crawlee,runTime)){
							crawlees.add(crawlee);
						}
					}
				}
			}

		}
	}

	static Crawlee DoSearchOnContent (Document doc, int indx) throws IOException {

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

		for (int i = 0; i < eles.size(); i++){
			Element ele = eles.get(i);
			//System.out.println("[Jsoup] ele text: " + ele.text());
		}

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

		System.out.println("[Crawlee] crawlees size: " + crawlees.size() + " and the cralwee content: \n" + crawlee.Context());
		return crawlee;
	}

	//Case filter descriptor
	static void FilterByCriteria (MultiMap<String,String> config) throws IOException {

		for (Iterator<Crawlee> crawlee_ite = crawlees.iterator(); crawlee_ite.hasNext();) {
			Crawlee crawlee = crawlee_ite.next();
			Boolean beDeleted = true;

			if(FilterInBySubject(crawlee,config)){
				if(!FilterByFee(crawlee,config)){
					if(FilterOutByLocation(crawlee, config)){
						beDeleted = false;
					}
				}
			}

			if(beDeleted) {
				System.out.println("[SearchCrit] Going to delete crawlee: " + crawlee.case_index);
				crawlee_ite.remove();
			}
		}
	}

	static Boolean FilterByFee (Crawlee crawlee, MultiMap<String,String> config) {
		int price_above = -1;
		@SuppressWarnings({"unchecked"})
			Collection<String> price_str = (Collection<String>) config.get(CRIT_PRICE_KEY);
		price_above = Integer.parseInt((String) price_str.toArray()[0]);
		if (price_above != -1) {
			if( crawlee.GetFee() > price_above)
				return false;
		}
		return true;
	}

	static Boolean FilterOutByLocation(Crawlee crawlee, MultiMap<String,String> config) {

		@SuppressWarnings({"unchecked"})
			Collection<String> location_Strs = (Collection<String>) config.get(CRIT_LOCATION_KEY);

		for (String aCrit: location_Strs){
			Pattern crit = Pattern.compile(aCrit);
			Matcher matcher = crit.matcher(crawlee.GetValueByKey("Location"));
			if(matcher.find())
				return false;
		}
		return true;
	}

	static Boolean FilterInBySubject(Crawlee crawlee, MultiMap<String,String> config) {

		@SuppressWarnings({"unchecked"})
			Collection<String> subject_Strs = (Collection<String>) config.get(CRIT_SUBJECT_KEY);

		for (String aCrit: subject_Strs){
			Pattern crit = Pattern.compile(aCrit);
			Matcher matcher = crit.matcher(crawlee.GetValueByKey("Subject"));
			if(matcher.find())
				return true;
		}
		return false;
	}

	static void ParseInResult () throws IOException {

		//Parsing
		FileWriter filewriter = new FileWriter("result.csv");
		filewriter.append(new SimpleDateFormat().format(new Date()) + " 's update:\n"); 
		for (Crawlee cr: crawlees){
			filewriter.append("The case index: " + cr.case_index + "\n");
			filewriter.append(cr.Context());
			filewriter.append(OUTPUT_LINE_ENDING);
		}
		filewriter.close();

	}
}
