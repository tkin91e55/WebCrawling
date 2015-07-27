import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileWriter;
import java.io.LineNumberReader; //for DB class
import java.lang.String;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar; //for DB class
import java.util.HashMap;
import java.util.concurrent.TimeUnit; //for DB class
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

public static class Crawlee {

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
				//System.out.println("[SearchCrit] fee: " + map.get("Fee"));
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

public static class Crawlee_DB {

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

		public Crawlee_DB() throws IOException,FileNotFoundException {
			today = new Date();
			oldestDayInRecord.add(Calendar.DATE, -5);
			System.out.println("[Crawlee_DB, dayFormat] dayFormat : " + dayFormat.format(today));

			if(!CheckDBexist()){
				CreateDBfile();
			}
			//if the DB has data import it and do flusing and tream to records var
			LineNumberReader lnr = new LineNumberReader(new FileReader(new File(DB_HISTORY)));
			lnr.skip(Long.MAX_VALUE);

			if(lnr.getLineNumber() + 1 >= 2){
				int lineNum = lnr.getLineNumber();
				System.out.println("[DB,line] Line number of DB: " + lineNum);
				ReadFromDB();
				FlushOldHistory();
			}
			lnr.close();
		}

		boolean CheckDBexist () throws IOException {
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
				return false;	
			}
			return true;
		}

		void CreateDBfile () throws IOException {

			//Create filewriter for header
			FileWriter writer = new FileWriter(DB_HISTORY,true);

			System.out.println("[DB] writing headers");
			int size = library_header_mapping.length;
			for(int i = 0; i < size-1; i++){
				writer.append(library_header_mapping[i]+",");
			}
			writer.append(library_header_mapping[size-1]);
			writer.append("\n");

			writer.close();

		}

		void ReadFromDB () throws FileNotFoundException,IOException {

			//Create CSV reader
			//{"DISCOVERD DATE","AND TIME","INDEX","TUTOR TIME","GENDER","INFO","SUBJECT","FEE"};
			CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(library_header_mapping);
			FileReader fileReader = new FileReader(DB_HISTORY);
			CSVParser csvFileParser = new CSVParser(fileReader, csvFileFormat);

			//TODO:actually DB is organize data from file, need to be translated to well defined Crawlee_DB , so DB role should be encapsulated in Crawlee_DB, but not exposed here
			List<CSVRecord> DB = csvFileParser.getRecords(); 
			System.out.println("[DB] DB read lines: " + DB.size());

			//This loop is parsing raw to Crawlee_DB
			for(int i = 1; i < DB.size(); i++){
				CSVRecord record = DB.get(i);
				System.out.println("[DB] sampling: " + record.get(library_header_mapping[0]) + " , " + record.get(library_header_mapping[1]) + " , " + record.get(library_header_mapping[2]) + ", and the Fee: " + record.get(library_header_mapping[7]));
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
					System.out.println("[DB, record day] recordDay: " + dayFormat.format(recordDay) + " ,and time: " + timeFormat.format(recordTime) );

				} catch (ParseException ex){
					System.err.println("Parse Exception");
				}
				StreamToRecords(recordDay,recordTime,sample);
			}

			fileReader.close();

		}

		void StreamToRecords (Date day, Date time,Crawlee crle) {
			//records.crawlee.add(crle);
			System.out.println("[DB, read entry] today: " + dayFormat.format(day) + " and crle: " + crle.Context()); 
			records.add(new DateCrawlee(day,time,crle));
		}
		
		static public int WriteToDBcount = 0;
		static public int WriteToDBLoopCnt = 0;
		public boolean LookUpFromDB (Crawlee aCrle,Date time) throws IOException {
			boolean isNewDBentry = (!MatchBeforeWriteDB(aCrle) | records.size() == 0);
			WriteToDBcount ++;
			if(isNewDBentry){
				WriteToDBLoopCnt ++;
				System.out.println("[DB, matching] records,size(): " + records.size());
				AppendNewEntryOnDB(time,aCrle);
				// remember also to add to record, there is problem here added records is not in # format, well, suppose new entries should not have same index
				//	records.add(new DateCrawlee(today,time,aCrle)); 
			}
			return isNewDBentry;
		}

		static public int MatchBeforeWriteDBcount = 0;
		static public int MatchBeforeWriteDBLoopCnt = 0;
		//match if the input aCrle be added to DB, aCrle, newly grasped from remote
		boolean MatchBeforeWriteDB (Crawlee aCrle) throws IOException {
			
			MatchBeforeWriteDBcount ++;
			boolean hasSameMatch = false;
			for(DateCrawlee record: records){
				MatchBeforeWriteDBLoopCnt ++;
				//if the index happened in previous already, just skip
				if( record.crawlee.case_index == aCrle.case_index){
				System.out.println("[DB matching] CommaToSharp(aCrle.GetValueByKey(Subject) : " + CommaToSharp(aCrle.GetValueByKey("Subject")) + " and record subject is: " + record.crawlee.GetValueByKey("Subject"));
					String subjectValue = CommaToSharp(aCrle.GetValueByKey("Subject"));
					if( subjectValue.equals(record.crawlee.GetValueByKey("Subject")) ){
						if(record.crawlee.GetFee() == aCrle.GetFee()){
//						System.out.println("[DB matching] remote crawlee of index: " + aCrle.GetFee() + "and the record crle fee:" + record.crawlee.GetFee());
						System.out.println("[DB matching] DB matching return true, record crawlee id: "+  record.crawlee.case_index + " , and remote crawlee id: " + aCrle.case_index);
							hasSameMatch = true;
							break;
						}
					}
				}

				//	System.out.println("[DB matching] record.crawlee.subject: " + record.crawlee.GetValueByKey("Subject")
				//			+ " and aCrle.subject: " + aCrle.GetValueByKey("Subject"));
				//	System.out.println("[DB matching] record.crawlee.info: " + record.crawlee.GetValueByKey("Info")
				//			+ " and aCrle.info: " + aCrle.GetValueByKey("Info"));
			}
			 if(!hasSameMatch)
				System.out.println("[DB matching] DB matching return false as no same matching, and remote crawlee id: " + aCrle.case_index);
			return hasSameMatch;
		}

		//Write on DBFile
		void AppendNewEntryOnDB (Date discoverTime, Crawlee newEntry) throws IOException {
			//TODO: remember to replace comma to sharp

			//Create filewriter for header
			//{"DISCOVERD DATE","AND TIME","INDEX","TUTOR TIME","GENDER","INFO","SUBJECT","FEE"};
			FileWriter writer = new FileWriter(DB_HISTORY,true);

			System.out.println("[DB] writing new entry");

			writer.append("\"" + dayFormat.format(today) + "\",");
			writer.append("\"" + timeFormat.format(discoverTime) + "\",");
			writer.append("\"" + newEntry.case_index + "\",");
			String tutorTime = newEntry.GetValueByKey("Time"); tutorTime = CommaToSharp(tutorTime);
			writer.append("\"" + tutorTime + "\",");
			String gender  = newEntry.GetValueByKey("Gender"); gender = CommaToSharp(gender);
			writer.append("\"" + gender + "\",");
			String info  = newEntry.GetValueByKey("Info"); info = CommaToSharp(info);
			writer.append("\"" + info + "\",");
			String subject  = newEntry.GetValueByKey("Subject"); subject = CommaToSharp(subject);
			writer.append("\"" + subject + "\",");
			String fee  = newEntry.GetValueByKey("Fee"); //fee should not have comma
			writer.append("\"" + fee + "\"");

			writer.append("\n");
			writer.close();

		}

		String CommaToSharp (String withComma){
			
			withComma = withComma.replace(',','#');
			withComma = withComma.replace('，','#');
			return withComma;
		}

		String SharpToComma (String withSharp){

			return "";
		}

		public int Size (){
			return records.size();
		}

		void FlushOldHistory () {

			//TODO: need to archive last day record, maybe just to keep several days record to by reading date, Crawlee_DB.flushOldHistory, static dayOfHisotry	
		}


}


