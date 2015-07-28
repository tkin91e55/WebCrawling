import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileWriter;
import java.io.LineNumberReader; //for DB class
import java.io.BufferedReader; //for flushing DB
import java.io.BufferedWriter; //for flushing DB
import java.io.FileInputStream; //for flushing DB
import java.io.InputStreamReader; //for flushing DB
import java.io.Writer; // for flushing DB
import java.lang.String;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar; //for DB class
import java.util.concurrent.TimeUnit; //for DB class
import java.util.Formatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.collections4.*;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.csv.*;

public class Crawlee_DB {

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

	public Crawlee_DB() throws IOException,FileNotFoundException,ParseException {
		today = new Date();
		oldestDayInRecord.add(Calendar.DATE, -5);
		System.out.println("[Crawlee_DB, dayFormat] dayFormat : " + dayFormat.format(today));

		if(!CheckDBexist()){
			CreateDBfile();
		}
		//if the DB has data import it and do flusing and tream to records var
		LineNumberReader lnr = new LineNumberReader(new FileReader(new File(DB_HISTORY)));
		lnr.skip(Long.MAX_VALUE);

		if(lnr.getLineNumber() >= 1){
			int lineNum = lnr.getLineNumber();
			System.out.println("[DB,line] Line number of DB: " + lineNum);
			FlushOldHistory();
			ReadFromDB();
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
		//boolean isNewDBentry = !MatchBeforeWriteDB(aCrle);
		System.out.println("[DB,matching] isNewDBentry: " + isNewDBentry + " , records.size(): " + records.size());
		WriteToDBcount ++;
		if(isNewDBentry){
			WriteToDBLoopCnt ++;
			System.out.println("[DB, matching] records,size(): " + records.size());
			AppendNewEntryOnDB(time,aCrle);
			// remember also to add to record, there is problem here added records is not in # format, well, suppose new entries should not have same index
		}
		return !isNewDBentry;
	}

	static public int MatchBeforeWriteDBcount = 0;
	static public int MatchBeforeWriteDBLoopCnt = 0;
	//match if the input aCrle be added to DB, aCrle, newly grasped from remote
	boolean MatchBeforeWriteDB (Crawlee aCrle) throws IOException {

		MatchBeforeWriteDBcount ++;
		boolean hasSameMatch = false;
	//	boolean hasSameMatch = true;
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

		}
		if(!hasSameMatch)
			System.out.println("[DB matching] DB matching return false as no same matching, and remote crawlee id: " + aCrle.case_index);
		return hasSameMatch;
	}

	//Write on DBFile
	void AppendNewEntryOnDB (Date discoverTime, Crawlee newEntry) throws IOException {

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

		withComma = withComma.replace(',','，');
		return withComma;
	}

	public int Size (){
		return records.size();
	}

	public void FlushOldHistory () throws IOException,FileNotFoundException,ParseException {

		//TODO: need to archive the record to be deleted, maybe just to keep several days record to by reading date, Crawlee_DB.flushOldHistory, static dayOfHisotry	
		boolean needArchive = false;
		Date archiveTime = new Date();

		//TODO: need to check has folder checking existence
		File theDir = new File("OLD_DB");
		String oldDB = String.format("%s/%s_tmp",theDir.getName(),DB_HISTORY);

		try{
			if(!theDir.exists()){
				boolean result = theDir.mkdir();
				if(result){
					System.out.println("OLD_DB folder created");
				}
			}
		}catch(Exception e){
			System.err.println("OLD_DB folder created");
		}

		// Creates file to write to
		Writer output = null;
		output = new BufferedWriter(new FileWriter(oldDB));
		String newline = System.getProperty("line.separator");

		// Read in a file & process line by line
		FileInputStream in = new FileInputStream(DB_HISTORY);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;

		FileInputStream in2 = new FileInputStream(DB_HISTORY);
		BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
		CSVFormat DBfileFormat = CSVFormat.DEFAULT.withHeader(library_header_mapping);
		CSVParser DBfileParser = new CSVParser(br2, DBfileFormat);
		Iterator<CSVRecord> recordItr = DBfileParser.getRecords().iterator();
		CSVRecord record = recordItr.next();

		//skip first line which is the headers
		strLine = br.readLine();
		output.write(strLine);
		output.write(newline);

		int count = 1;
		while ((strLine = br.readLine()) != null) {

			if(recordItr.hasNext()){
				System.out.println("[Flushing] recordItr has iterated");
				record = recordItr.next();
			}

			String dayParsed = record.get(library_header_mapping[0]);
			System.out.println("[Flushing] dayParsed: " + dayParsed);
			Date readDay = dayFormat.parse(dayParsed);

			count++ ;

			try { 
				if(TimeUnit.DAYS.convert( readDay.getTime() - oldestDayInRecord.getTime().getTime(), TimeUnit.MILLISECONDS) < 0 ){
					System.out.print("[Flusing] count: " + count + ", and [Sampling]: " + record.get(library_header_mapping[6]) + ", and readDay: " + dayFormat.format(readDay));
					System.out.println(" Line Deleted.");
					needArchive = true;
					System.out.println("");
				}else{
					// Write non deleted lines to file
					output.write(strLine);
					output.write(newline);
				}

			} catch (IOException ioe) { 
				System.out.println("IO error reading command line input");
				System.exit(1); 
			}

		}

		System.out.println("[Flushing] at last count: " + count);

		output.close();

		if(needArchive){
			//TODO: swap files and rename the archived file with date
			String archiveFile = String.format("%s/%s_%s%s",theDir.getName(),dayFormat.format(archiveTime),timeFormat.format(archiveTime),DB_HISTORY);
			File DB = new File(DB_HISTORY);
			File archive = new File(oldDB);//this is a temp file, exist if check nothing to flush
			File targetArchive = new File(archiveFile);

			if(DB.renameTo(targetArchive) && archive.renameTo(DB)){
				System.out.println("[Swapping file] swapping file right!!!");
			}else{
				System.out.println("[Swapping file] something wrong!!!");
			}


		}
	}
}
