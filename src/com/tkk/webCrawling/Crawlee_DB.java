package com.tkk.webCrawling;

import java.io.*;
import java.lang.String;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.*;

/*
 * This class is supposed to be a global singleton,
 * otherwise need structural change.
 */
public class Crawlee_DB {

	class DateCrawlee {
		public Date day;
		public Date time;
		public Crawlee crawlee;

		public DateCrawlee(Date aDay, Date aTime, Crawlee crle) {
			day = aDay;
			time = aTime;
			crawlee = crle;
		}
	}

	static String DB_HISTORY = "case_DB.csv";
	static String[] library_header_mapping = { "DISCOVERED DATE", "AND TIME", "WEBSITE", "INDEX", "LOCATION",
			"TUTOR TIME", "GENDER", "INFO", "SUBJECT", "FEE", "OTHER" };

	static public SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
	static public SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
	static public Date today;

	static Calendar oldestDayInRecord = Calendar.getInstance();

	List<DateCrawlee> records = new ArrayList<DateCrawlee>();

	private static Crawlee_DB instance;

	public static Crawlee_DB GetInstance() {
		if (instance == null) {
			try {
				instance = new Crawlee_DB();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	public Crawlee_DB() throws IOException, ParseException {
		today = new Date();
		oldestDayInRecord.add(Calendar.DATE, -5);
		System.out.println("[Crawlee_DB, dayFormat] dayFormat : " + dayFormat.format(today));

		if (!CheckDBexist()) {
			CreateDBfile();
		}
		// if the DB has data import it and do flushing and stream to records
		// var
		if (FileManager.HasMoreLinesThan(DB_HISTORY, 0)) {
			FlushOldHistory();
			ReadFromDB();
		}
	}

	boolean CheckDBexist() {
		// DB File checking
		return FileManager.CheckFileExist(DB_HISTORY);
	}

	void CreateDBfile() throws IOException {
		// Create filewriter for header
		FileManager writer = new FileManager(DB_HISTORY);

		System.out.println("[DB] writing headers");
		int size = library_header_mapping.length;
		String stringBuilder = "";
		for (int i = 0; i < size - 1; i++) {
			stringBuilder += library_header_mapping[i];
			stringBuilder += ",";
		}
		stringBuilder += library_header_mapping[size - 1];
		writer.AppendOnNewLine(stringBuilder);

		writer.Close();
	}

	void ReadFromDB() throws FileNotFoundException, IOException {

		// Create CSV reader
		// { "DISCOVERED DATE", "AND TIME","WEBSITE", "INDEX", "LOCATION",
		// "TUTOR TIME",
		// "GENDER", "INFO", "SUBJECT", "FEE", "OTHER" }
		CSVmanager csvParser = new CSVmanager(DB_HISTORY);

		List<CSVRecord> DB = csvParser.CreateParseInRecord(library_header_mapping);
		System.out.println("[DB] DB read lines: " + DB.size());

		// This loop is parsing raw to Crawlee_DB
		for (int i = 1; i < DB.size(); i++) {
			CSVRecord record = DB.get(i);

			Crawlee sample = new Crawlee(Integer.parseInt(record.get(library_header_mapping[3])));

			sample.Put("Website", record.get(library_header_mapping[2]));
			sample.Put("Location", record.get(library_header_mapping[4]));
			sample.Put("Time", record.get(library_header_mapping[5]));
			sample.Put("Gender", record.get(library_header_mapping[6]));
			sample.Put("Info", record.get(library_header_mapping[7]));
			sample.Put("Subject", record.get(library_header_mapping[8]));
			sample.Put("Fee", record.get(library_header_mapping[9]));
			sample.Put("Other", record.get(library_header_mapping[10]));

			Date recordDay = new Date();
			Date recordTime = new Date();

			try {
				recordDay = dayFormat.parse(record.get(library_header_mapping[0]));
				recordTime = timeFormat.parse(record.get(library_header_mapping[1]));
				// System.out.println("[DB, record day] recordDay: " +
				// dayFormat.format(recordDay) + " ,and time: " +
				// timeFormat.format(recordTime) );

			} catch (ParseException ex) {
				System.err.println("Parse Exception");
			}
			StreamToRecords(recordDay, recordTime, sample);
		}

		csvParser.Close();

	}

	void StreamToRecords(Date day, Date time, Crawlee crle) {
		// records.crawlee.add(crle);
		// System.out.println("[DB, read entry] today: " + dayFormat.format(day)
		// + " and crle: " + crle.Context());
		records.add(new DateCrawlee(day, time, crle));
	}

	static public int WriteToDBcount = 0;
	static public int WriteToDBLoopCnt = 0;

	public synchronized boolean LookUpFromDB(Crawlee aCrle, Date time) throws IOException {
		boolean isNewDBentry = (!MatchBeforeWriteDB(aCrle) | records.size() == 0);
		// boolean isNewDBentry = !MatchBeforeWriteDB(aCrle);
		// System.out.println("[DB,matching] isNewDBentry: " + isNewDBentry + "
		// , records.size(): " + records.size());
		WriteToDBcount++;
		if (isNewDBentry) {
			WriteToDBLoopCnt++;
			// System.out.println("[DB, matching] records,size(): " +
			// records.size());
			AppendNewEntryOnDB(time, aCrle);
			// remember also to add to record, there is problem here added
			// records is not in # format, well, suppose new entries should not
			// have same index
		}
		return !isNewDBentry;
	}

	static public int MatchBeforeWriteDBcount = 0;
	static public int MatchBeforeWriteDBLoopCnt = 0;

	/*
	 * match if the input aCrle be added to DB, aCrle, newly grasped from
	 * remote, if add more condition checking, actaully increasing the
	 * possibility of aCrle to be passed , more similar to be considered same,
	 * adding burden to DB as a subtle difference still considered new case just
	 * thinking the conditions are what the system will respond if the cond
	 * changed
	 */
	boolean MatchBeforeWriteDB(Crawlee aCrle) {

		MatchBeforeWriteDBcount++;
		boolean hasSameMatch = false;
		// boolean hasSameMatch = true;
		for (DateCrawlee record : records) {

			if (!record.crawlee.GetValueByKey("Website").equals(aCrle.GetValueByKey("Website")))
				continue;

			MatchBeforeWriteDBLoopCnt++;
			if (record.crawlee.getCase_index() == aCrle.getCase_index()) {
				// should check info, since the student info should strongly
				// bind to the case index
				String infoValue = CommaTransform(aCrle.GetValueByKey("Info"));
				String subjectValue = CommaTransform(aCrle.GetValueByKey("Subject"));
				String locationValue = CommaTransform(aCrle.GetValueByKey("Location"));
				boolean infoBool = infoValue.equals(record.crawlee.GetValueByKey("Info"));
				boolean subjectBool = subjectValue.equals(record.crawlee.GetValueByKey("Subject"));
				boolean locationBool = locationValue.equals(record.crawlee.GetValueByKey("Location"));
				boolean feeBool = (record.crawlee.GetFee() == aCrle.GetFee());

				// System.out.println("[DB matching] the four, infoBool: " +
				// infoBool + " subjectBool: " + subjectBool + " locationBool: "
				// + locationBool + ", feeBool: " + feeBool);
				// System.out.println("[DB matching] locationValue: " +
				// locationValue + ",record location: " +
				// record.crawlee.GetValueByKey("Location"));
				if (infoBool && subjectBool && feeBool && locationBool) {
					hasSameMatch = true;
					break;
				}
			}

		}
		if (!hasSameMatch)
			System.out.println("[DB matching] DB matching return false as no same matching, and remote crawlee id: "
					+ aCrle.getCase_index());
		return hasSameMatch;
	}

	// Write on DBFile
	void AppendNewEntryOnDB(Date discoverTime, Crawlee newEntry) throws IOException {

		// {"DISCOVERD DATE","AND TIME","INDEX","LOCATION","TUTOR
		// TIME","GENDER","INFO","SUBJECT","FEE","OTHER"}
		FileManager writer = new FileManager(DB_HISTORY);

		// System.out.println("[DB] writing new entry");
		writer.Append("\"" + dayFormat.format(today) + "\",");

		writer.Append("\"" + timeFormat.format(discoverTime) + "\",");

		writer.Append("\"" + newEntry.GetValueByKey("Website") + "\",");

		writer.Append("\"" + newEntry.getCase_index() + "\",");

		String location = newEntry.GetValueByKey("Location");
		location = CommaTransform(location);
		writer.Append("\"" + location + "\",");

		String tutorTime = newEntry.GetValueByKey("Time");
		tutorTime = CommaTransform(tutorTime);
		writer.Append("\"" + tutorTime + "\",");

		String gender = newEntry.GetValueByKey("Gender");
		gender = CommaTransform(gender);
		writer.Append("\"" + gender + "\",");

		String info = newEntry.GetValueByKey("Info");
		info = CommaTransform(info);
		writer.Append("\"" + info + "\",");

		String subject = newEntry.GetValueByKey("Subject");
		subject = CommaTransform(subject);
		writer.Append("\"" + subject + "\",");

		String fee = newEntry.GetValueByKey("Fee"); // fee should not have comma
		writer.Append("\"" + fee + "\",");

		String other = newEntry.GetValueByKey("Other");
		other = CommaTransform(other);
		writer.Append("\"" + other + "\"");

		writer.AppendOnNewLine("");
		writer.Close();

	}

	String CommaTransform(String withComma) {
		withComma = withComma.replace(',', '，');
		return withComma;
	}

	public int Size() {
		return records.size();
	}

	public void FlushOldHistory() throws FileNotFoundException, IOException, ParseException {

		boolean needArchive = false;
		Date archiveTime = new Date();

		String oldDBfolder = "OLD_DB";
		FileManager.CreateFolder(oldDBfolder);
		String oldDB = String.format("%s/%s_tmp", oldDBfolder, DB_HISTORY);

		// Creates file to write to
		FileManager output = new FileManager(oldDB);

		// now case_DB.csv to be moved as temp file, for passed cases they are
		// copid to another tmp file, this tmp file
		// renamed to case_DB.csv
		// ====================================================
		FileManager bufferedCSVReader = new CSVmanager(DB_HISTORY);
		bufferedCSVReader.ReadLine();
		((CSVmanager) bufferedCSVReader).CreateParseInRecord(library_header_mapping);
		Iterator<CSVRecord> recordItr = ((CSVmanager) bufferedCSVReader).GetRecordIterator();
		recordItr.next();// now recordItr should not have CSV header
		// ====================================================

		bufferedCSVReader.Close();
		bufferedCSVReader = new FileManager(DB_HISTORY);
		// write the first line which is the headers
		output.AppendBufferedOnNewLine(bufferedCSVReader.ReadLine());

		int count = 0;

		while (recordItr.hasNext()) {
			// System.out.println("[Flushing] recordItr has iterated");
			CSVRecord record = recordItr.next();

			String dayParsed = record.get(library_header_mapping[0]);
			// System.out.println("[Flushing] dayParsed: " + dayParsed);
			Date readDay = dayFormat.parse(dayParsed);

			String strLine;
			if ((strLine = bufferedCSVReader.ReadLine()) == null) {
				System.err.println("[Error] csvRecord and bufferReader number seems not matching");
			}

			count++;

			if (TimeUnit.DAYS.convert(readDay.getTime() - oldestDayInRecord.getTime().getTime(),
					TimeUnit.MILLISECONDS) < 0) {
				// record entry too old, not writing to the case_DB.csv
				System.out.print("[Flusing] count: " + count + ", and [Sampling]: "
						+ record.get(library_header_mapping[7]) + ", and readDay: " + dayFormat.format(readDay));
				System.out.println(" Line Deleted.");
				needArchive = true;
				System.out.println("");
			} else {
				// Write non deleted lines to file
				output.AppendBufferedOnNewLine(strLine);
			}

		}

		System.out.println("[Flushing] at last count: " + count);

		output.Close();

		if (needArchive) {
			String archiveFile = String.format("%s/%s_%s%s", oldDBfolder, dayFormat.format(archiveTime),
					timeFormat.format(archiveTime), DB_HISTORY);

			if (FileManager.RenameFile(DB_HISTORY, archiveFile) && FileManager.RenameFile(oldDB, DB_HISTORY)) {
				System.out.println("[Swapping file] swapping file right!!!");
			} else {
				System.out.println("[Swapping file] something wrong!!!");
			}
		}
	}
}
