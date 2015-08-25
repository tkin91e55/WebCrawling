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

	static String DB_HISTORY = "case_DB.csv";
	static String[] library_header_mapping = {"DISCOVERED DATE","AND TIME","INDEX","LOCATION","TUTOR TIME","GENDER","INFO","SUBJECT","FEE","OTHER"};

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
		//{"DISCOVERD DATE","AND TIME","INDEX","LOCATION","TUTOR TIME","GENDER","INFO","SUBJECT","FEE","OTHER"}
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(library_header_mapping);
		FileReader fileReader = new FileReader(DB_HISTORY);
		CSVParser csvFileParser = new CSVParser(fileReader, csvFileFormat);

		List<CSVRecord> DB = csvFileParser.getRecords(); 
		System.out.println("[DB] DB read lines: " + DB.size());

		//This loop is parsing raw to Crawlee_DB
		for(int i = 1; i < DB.size(); i++){
			CSVRecord record = DB.get(i);

			Crawlee sample = new Crawlee(Integer.parseInt(record.get(library_header_mapping[2])));

			sample.Put("Location",record.get(library_header_mapping[3]));
			sample.Put("Time",record.get(library_header_mapping[4]));
			sample.Put("Gender",record.get(library_header_mapping[5]));
			sample.Put("Info",record.get(library_header_mapping[6]));
			sample.Put("Subject",record.get(library_header_mapping[7]));
			sample.Put("Fee",record.get(library_header_mapping[8]));
			sample.Put("Other",record.get(library_header_mapping[9]));

			Date recordDay = new Date();
			Date recordTime = new Date();
			try{
				recordDay = dayFormat.parse(record.get(library_header_mapping[0]));
				recordTime = timeFormat.parse(record.get(library_header_mapping[1]));
				//	System.out.println("[DB, record day] recordDay: " + dayFormat.format(recordDay) + " ,and time: " + timeFormat.format(recordTime) );

			} catch (ParseException ex){
				System.err.println("Parse Exception");
			}
			StreamToRecords(recordDay,recordTime,sample);
		}

		fileReader.close();

	}

	void StreamToRecords (Date day, Date time,Crawlee crle) {
		//records.crawlee.add(crle);
		//System.out.println("[DB, read entry] today: " + dayFormat.format(day) + " and crle: " + crle.Context()); 
		records.add(new DateCrawlee(day,time,crle));
	}

	static public int WriteToDBcount = 0;
	static public int WriteToDBLoopCnt = 0;
	public boolean LookUpFromDB (Crawlee aCrle,Date time) throws IOException {
		boolean isNewDBentry = (!MatchBeforeWriteDB(aCrle) | records.size() == 0);
		//boolean isNewDBentry = !MatchBeforeWriteDB(aCrle);
		//		System.out.println("[DB,matching] isNewDBentry: " + isNewDBentry + " , records.size(): " + records.size());
		WriteToDBcount ++;
		if(isNewDBentry){
			WriteToDBLoopCnt ++;
			//System.out.println("[DB, matching] records,size(): " + records.size());
			AppendNewEntryOnDB(time,aCrle);
			// remember also to add to record, there is problem here added records is not in # format, well, suppose new entries should not have same index
		}
		return !isNewDBentry;
	}

	static public int MatchBeforeWriteDBcount = 0;
	static public int MatchBeforeWriteDBLoopCnt = 0;
	//match if the input aCrle be added to DB, aCrle, newly grasped from remote, if add more condition checking, actaully 
	//increasing the possibility of aCrle to be passed , more similar to be considered same, adding burden to DB as a subtle difference still considered new case
	//just thinking the conditions are what the system will respond if the cond changed
	boolean MatchBeforeWriteDB (Crawlee aCrle) throws IOException {

		MatchBeforeWriteDBcount ++;
		boolean hasSameMatch = false;
		//	boolean hasSameMatch = true;
		for(DateCrawlee record: records){
			MatchBeforeWriteDBLoopCnt ++;
			if( record.crawlee.case_index == aCrle.case_index){
				//should check info, since the student info should strongly bind to the case index
				String infoValue = CommaTransform(aCrle.GetValueByKey("Info"));
				String subjectValue = CommaTransform(aCrle.GetValueByKey("Subject"));
				String locationValue = CommaTransform(aCrle.GetValueByKey("Location"));
				boolean infoBool = infoValue.equals(record.crawlee.GetValueByKey("Info"));
				boolean subjectBool = subjectValue.equals(record.crawlee.GetValueByKey("Subject"));
				boolean locationBool = locationValue.equals(record.crawlee.GetValueByKey("Location"));
				boolean feeBool = (record.crawlee.GetFee() == aCrle.GetFee());

				//				System.out.println("[DB matching] the four, infoBool: " + infoBool + " subjectBool: " + subjectBool + " locationBool: " + locationBool + ", feeBool: " + feeBool);
				//				System.out.println("[DB matching] locationValue: " + locationValue + ",record location: " + record.crawlee.GetValueByKey("Location"));
				if(infoBool && subjectBool && feeBool && locationBool){
					hasSameMatch = true;
					break;
				}
			}

		}
		if(!hasSameMatch)
			System.out.println("[DB matching] DB matching return false as no same matching, and remote crawlee id: " + aCrle.case_index);
		return hasSameMatch;
	}

	//Write on DBFile
	void AppendNewEntryOnDB (Date discoverTime, Crawlee newEntry) throws IOException {

		//{"DISCOVERD DATE","AND TIME","INDEX","LOCATION","TUTOR TIME","GENDER","INFO","SUBJECT","FEE","OTHER"}
		FileWriter writer = new FileWriter(DB_HISTORY,true);

		//System.out.println("[DB] writing new entry");

		writer.append("\"" + dayFormat.format(today) + "\",");
		writer.append("\"" + timeFormat.format(discoverTime) + "\",");
		writer.append("\"" + newEntry.case_index + "\",");
		String location = newEntry.GetValueByKey("Location"); location = CommaTransform(location);
		writer.append("\"" + location + "\",");
		String tutorTime = newEntry.GetValueByKey("Time"); tutorTime = CommaTransform(tutorTime);
		writer.append("\"" + tutorTime + "\",");
		String gender  = newEntry.GetValueByKey("Gender"); gender = CommaTransform(gender);
		writer.append("\"" + gender + "\",");
		String info  = newEntry.GetValueByKey("Info"); info = CommaTransform(info);
		writer.append("\"" + info + "\",");
		String subject  = newEntry.GetValueByKey("Subject"); subject = CommaTransform(subject);
		writer.append("\"" + subject + "\",");
		String fee  = newEntry.GetValueByKey("Fee"); //fee should not have comma
		writer.append("\"" + fee + "\",");
		String other  = newEntry.GetValueByKey("Other"); other = CommaTransform(other);
		writer.append("\"" + other + "\"");

		writer.append("\n");
		writer.close();

	}

	String CommaTransform (String withComma){

		withComma = withComma.replace(',','，');
		return withComma;
	}

	public int Size (){
		return records.size();
	}

	public void FlushOldHistory () throws IOException,FileNotFoundException,ParseException {

		boolean needArchive = false;
		Date archiveTime = new Date();

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

		//now case_DB.csv to be moved as temp file, for passed cases they are copid to another tmp file, this tmp file
		//renamed to case_DB.csv
		//====================================================
		FileInputStream in = new FileInputStream(DB_HISTORY);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
		bufferedReader.readLine();
		CSVFormat DBfileFormat = CSVFormat.DEFAULT.withHeader(library_header_mapping);
		CSVParser DBfileParser = new CSVParser(bufferedReader, DBfileFormat);
		Iterator<CSVRecord> recordItr = DBfileParser.getRecords().iterator();//now recordItr should not have CSV header
		//====================================================

		bufferedReader.close();
		in = new FileInputStream(DB_HISTORY);
		bufferedReader = new BufferedReader(new InputStreamReader(in));
		output.write(bufferedReader.readLine());//write first line which is the headers
		output.write(newline);

		int count = 0;

		while(recordItr.hasNext()){
		//System.out.println("[Flushing] recordItr has iterated");
			CSVRecord record = recordItr.next();

			String dayParsed = record.get(library_header_mapping[0]);
			//System.out.println("[Flushing] dayParsed: " + dayParsed);
			Date readDay = dayFormat.parse(dayParsed);

			String strLine;
			if((strLine = bufferedReader.readLine()) == null){
				System.err.println("[Error] csvRecord and bufferReader number seems not matching");
			}

			count++ ;

			try { 
				if(TimeUnit.DAYS.convert( readDay.getTime() - oldestDayInRecord.getTime().getTime(), TimeUnit.MILLISECONDS) < 0 ){
					//record entry too old, not writing to the case_DB.csv
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
