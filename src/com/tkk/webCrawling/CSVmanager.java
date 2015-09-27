package com.tkk.webCrawling;
import java.io.*;
import java.lang.String;
import java.util.List;
import java.util.Iterator;

import org.apache.commons.csv.*;

public class CSVmanager extends FileManager {

	public static String OUTPUT_SEPARATOR = ",";
	CSVFormat csvFileFormat;
	CSVParser csvFileParser;
	List<CSVRecord> csvRecords;

	public CSVmanager (String filepath){
		super(filepath);
	}

	public List<CSVRecord> CreateParseInRecord (String[] csvHeader) throws FileNotFoundException, IOException {
		FileInputStream in = new FileInputStream(path);
		fileReader = new BufferedReader(new InputStreamReader(in));
		csvFileFormat = CSVFormat.DEFAULT.withHeader(csvHeader);
		csvFileParser = new CSVParser(fileReader,csvFileFormat);
		csvRecords = csvFileParser.getRecords();//after this line, csvFileParser.getRecords().size() return 0
		return csvRecords;
	}

	public Iterator<CSVRecord> GetRecordIterator () {
		try {
			System.out.println("[CSVmanager] the record get records size: " + csvRecords.size());
			return csvRecords.iterator();
		} catch (Exception e) {
			System.err.println("get record iterator error");
		}
		return null;
	}

	public void Close() throws IOException {
		super.Close();
		csvRecords = null;
	}
}
