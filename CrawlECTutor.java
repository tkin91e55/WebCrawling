import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.util.Formatter;
import java.util.Collection;
import java.util.Collections;
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
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

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

		/*
		   Current keys of HashMap are:
		   Location,LastUpdateAt,Time,Gender,Info,Subject,Fee,Other
		 */

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

		public int GetFee () {
			if(map.containsKey("Fee")){
				System.out.println("[SearchCrit] fee: " + map.get("Fee"));
				Pattern price = Pattern.compile("\\$[0-9]{2,4}");
				Matcher matcher = price.matcher(map.get("Fee"));
				if(matcher.find()){
					String casePriceStr = matcher.group(0).substring(1);
					int casePrice = 99999;
					casePrice = Integer.parseInt(casePriceStr);
					if (casePrice != 99999)
						return casePrice;
				}
			}
			return 0;	
		}

		public String GetValueByKey (String key) {

			if(map.containsKey(key)){
				return map.get(key);
			}

			return "";
		}

	}

	static class Crawlee_DB {

		class DateCrawlee {
			public Date day;
			public Date time;
			public Crawlee crawlee;

			public DateCrawlee (Date aDay, Date aTime, Crawlee crle){
				day = aDay;
				time = aTime;
				crawlee = crle;
			}
		}

		static String DB_HISTORY = "case_library.csv";
		static String[] library_header_mapping = {"DISCOVERD DATE","AND TIME","INDEX","TUTOR TIME","GENDER","INFO","SUBJECT","FEE"};

		static public SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
		static public SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
		static public Date today;

		static Calendar oldestDayInRecord = Calendar.getInstance();

		List<DateCrawlee> records = new ArrayList<DateCrawlee>();

		public Crawlee_DB() {
			today = new Date();
			oldestDayInRecord.add(Calendar.DATE, -5);
			System.out.println("[Crawlee_DB, dayFormat] dayFormat : " + dayFormat.format(today));

			if(!CheckDBexist()){
				CreateDBFile();
			}
			WriteStreamToDB();
			flushOldHistory();
		}

		boolean CheckDBexist () {
			//DB File checking
			File DBfile = new File(DB_HISTORY);

			if(DBfile.exists())
				System.out.println("[File] db file exists");
			else
				System.out.println("[File] db file not exists");

			if(DBfile.isDirectory())
				System.out.println("[File] db file is directory");
			else
				System.out.println("[File] db file is not directory");

			if(!DBfile.exists() && !DBfile.isDirectory()){
				return true;	
			}
			return false;
		}

		void CreateDBfile () {

			//Create filewriter for header
			FileWriter writer = new FileWriter(DB_HISTORY,true);

			System.out.println("[DB] writing headers");
			int size = library_header_mapping.length;
			for(int i = 0; i < size-1; i++){
				writer.append(library_header_mapping[i]+",");
			}
			writer.append(library_header_mapping[size-1]);
			writer.append("\n");

			filewriter.close();

		}

		void ReadFromDB () {

			//Create CSV reader
			//{"DISCOVERD DATE","AND TIME","INDEX","TUTOR TIME","GENDER","INFO","SUBJECT","FEE"};
			CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(library_header_mapping);
			FileReader fileReader = new FileReader(DB_HISTORY);
			CSVParser csvFileParser = new CSVParser(fileReader, csvFileFormat);

			//TODO:actually DB is organize data from file, need to be translated to well defined Crawlee_DB , so DB role should be encapsulated in Crawlee_DB, but not exposed here
			List DB = csvFileParser.getRecords(); 
			System.out.println("[DB] DB read lines: " + DB.size());

			//This loop is parsing raw to Crawlee_DB
			for(int i = 1; i < DB.size(); i++){

				CSVRecord record = (CSVRecord) DB.get(i);
				System.out.println("[DB] sampling: " + record.get(library_header_mapping[0]) + " , " + record.get(library_header_mapping[1]) + " , " + record.get(library_header_mapping[2]));
				Crawlee sample = new Crawlee(Integer.parseInt(record.get(library_header_mapping[2])));
				sample.Put("Time",record.get(library_header_mapping[3]));
				sample.Put("Gender",record.get(library_header_mapping[4]));
				sample.Put("Info",record.get(library_header_mapping[5]));
				sample.Put("Subject",record.get(library_header_mapping[6]));
				sample.Put("Fee",record.get(library_header_mapping[7]));
				
				Date recordDay = new Date();
				Date recordTime = new Date();
				try{
					recordDay = dayFormat.parse(record.get(library_header_mapping[0]));
					recordTime = timeFormat.parse(record.get(library_header_mapping[1]));
					System.out.println("[DB, record day] recordDay: " + recordDay.getTime()+ " ,and time: " + recordTime.getTime());

				} catch (ParseException ex){
					System.err.println("Parse Exception");
				}

				StreamToRecords(recordDay,recordTime,sample);
			}

			fileReader.close();

		}

		void WriteToDB () {

		}

		//match if the input aCrle be added to DB, aCrle, newly grasped from remote
		public boolean MatchBeforeWriteDB (Crawlee aCrle){

			for(DateCrawlee record: records){
				//if the index happened in previous already, just skip
				if( record.crawlee.case_index == aCrle.case_index){
					System.out.println("[DB matching] remote crawlee of index: " + aCrle.case_index + " rejected.");
					return false;
				}

				System.out.println("[DB matching] record.crawlee.subject: " + record.crawlee.GetValueByKey("Subject")
						+ " and aCrle.subject: " + aCrle.GetValueByKey("Subject"));
				System.out.println("[DB matching] record.crawlee.info: " + record.crawlee.GetValueByKey("Info")
						+ " and aCrle.info: " + aCrle.GetValueByKey("Info"));
				if(record.crawlee.GetValueByKey("Subject") == aCrle.GetValueByKey("Subject")){
					if(record.crawlee.GetFee() == aCrle.GetFee()){
						AppendNewEntryOnDB(aCrle);						
						System.out.println("[DB matching] remote crawlee of index: " + aCrle.case_index + " accepted.");
						return true;
					}
				}
			}

			return false;
		}

		void AppendNewEntryOnDB (Crawlee newEntry) {
			//TODO: remember to replace comma to \comma

			//Create filewriter for header
			//{"DISCOVERD DATE","AND TIME","INDEX","TUTOR TIME","GENDER","INFO","SUBJECT","FEE"};
			FileWriter writer = new FileWriter(DB_HISTORY,true);

			System.out.println("[DB] writing new entry");
			int size = library_header_mapping.length;
			for(int i = 0; i < size-1; i++){
				writer.append(library_header_mapping[i]+",");
			}
			writer.append(library_header_mapping[size-1]);
			writer.append("\n");

			filewriter.close();

	

		}

		String CommaToSharp (String withComma){
			return "";
		}

		String SharpToComma (String withSharp){
		
			return "";
		}

		public int Size (){
			return records.size();
		}

		void flushOldHistory () {

			//TODO: need to archive last day record, maybe just to keep several days record to by reading date, Crawlee_DB.flushOldHistory, static dayOfHisotry	
		}

		void StreamToRecords (Date day, Date time,Crawlee crle){
			//records.crawlee.add(crle);
			System.out.println("[DB, read entry] today: " + dayFormat.format(day) + " and crle: " + crle.Context()); 
			records.add(new DateCrawlee(day,time,crle));
		}

	}

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

	public static void main(String[] args) throws IOException {

		//	if(args[0] != null){

		/*try {
		  startIndex  = Integer.parseInt(args[0]);
		  } catch (NumberFormatException e) {
		  System.err.println("Argument " + args[0] + " must be an integer.");
		  System.exit(1);
		  }*/

		MultiMap<String,String> config = new MultiValueMap<String,String>();
		ParseInConfig(config);

		ProcessUrl(config);

		//FilterByCriteria(config);

		//Result:
		//	for (Crawlee cr: crawlees){
		//		System.out.println("[SearchCrit] Remaining crawlee: " + cr.case_index);
		//	}

		//	ParseInResult();

		//	}else {
		//		System.err.println("Need to ASSIGN starting pop up case number");
		//	}

	}

	static void ParseInConfig (MultiMap<String,String> mapConfig) throws IOException {

		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(config_header_mapping);
		FileReader fileReader = new FileReader("config.csv");
		System.out.println("The encoding is: " + fileReader.getEncoding());
		CSVParser csvFileParser = new CSVParser(fileReader, csvFileFormat);
		List csvRecords = csvFileParser.getRecords();
		System.out.println("[Apache] csvRecords.getRecords() size: " + csvRecords.size());

		for(int i = 1; i < csvRecords.size(); i++) {
			CSVRecord record = (CSVRecord) csvRecords.get(i);
			// 97077 System.out.println("[Apache] apache commons csv here, The TYPE: " + record.get(config_header_mapping[0]) + " and the VALUE: " + record.get(config_header_mapping[1]));
			mapConfig.put(record.get(config_header_mapping[0]),record.get(config_header_mapping[1]));
		}
	}

	static void ProcessUrl (MultiMap<String,String> config) throws IOException {

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
				Collection<String> urls = (Collection<String>) config.get(URL_KEY);
				for(String url: urls){
					String URL = url + index;
					Document caseDoc = Jsoup.connect(URL).data("query","Java").userAgent("Mozilla").cookie("auth","token").timeout(6000).post();
					if (!caseDoc.text().contains("Server Error")) {
						//      String title = caseDoc.title();
						//      System.out.println("[Doc] Title: " + title);
						//      String result = caseDoc.text();
						//      System.out.println("[Doc] Result: " + result);
						DoSearchOnContent(caseDoc,Integer.parseInt(index)); //crawlees got filled

						//Add qualified curled case to csv, Crawlee_DB.WriteToDBFile()
						for(Crawlee crawlee: crawlees){
							DBagent.MatchBeforeWriteDB(crawlee);
						}
					}
				}
			}

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

		//97077 System.out.println("[Jsoup] location: " + location.text() + " and lastUpdate: " + lastUpdate.text());

		for (int i = 0; i < eles.size(); i++){
			Element ele = eles.get(i);
			//97077	System.out.println("[Jsoup] ele text: " + ele.text());
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

		crawlees.add(crawlee);
		// 97077 System.out.println("[Crawlee] crawlees size: " + crawlees.size() + " and the cralwee content: \n" + crawlee.Context());
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
				//	System.out.println("[SearchCrit] Going to delete crawlee: " + crawlee.case_index + " , " + crawlee.context_text);
				System.out.println("[SearchCrit] Going to delete crawlee: " + crawlee.case_index);
				crawlee_ite.remove();
			}
		}
	}

	static Boolean FilterByFee (Crawlee crawlee, MultiMap<String,String> config) {
		int price_above = -1;
		Collection<String> price_str = (Collection<String>) config.get(CRIT_PRICE_KEY);
		price_above = Integer.parseInt((String) price_str.toArray()[0]);
		if (price_above != -1) {
			if( crawlee.GetFee() > price_above)
				return false;
		}
		return true;
	}

	static Boolean FilterOutByLocation(Crawlee crawlee, MultiMap<String,String> config) {

		Collection<String> location_Strs = (Collection<String>) config.get(CRIT_LOCATION_KEY);

		for (String aCrit: location_Strs){
			Pattern crit = Pattern.compile(aCrit);
			Matcher matcher = crit.matcher(crawlee.GetValueByKey("Location"));
			if(matcher.find())
				return true;
		}
		return false;
	}

	static Boolean FilterInBySubject(Crawlee crawlee, MultiMap<String,String> config) {

		Collection<String> subject_Strs = (Collection<String>) config.get(CRIT_SUBJECT_KEY);

		for (String aCrit: subject_Strs){
			Pattern crit = Pattern.compile(aCrit);
			Matcher matcher = crit.matcher(crawlee.GetValueByKey("Subject"));
			if(matcher.find())
				return false;
		}
		return true;
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
