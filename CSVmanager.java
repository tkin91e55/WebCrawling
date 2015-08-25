import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileWriter;
import java.lang.String;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Formatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.collections4.*;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.csv.*;

public class CSVManager extends FileManager {

	public static String OUTPUT_SEPARATOR = ",";
	CSVFormat csvFileFormat;
	CSVParser csvFileParser;
	List<CSVRecord> csvRecords;

	public CSVManager (String filepath){
		path = filepath;	
	}

	public List<CSVRecord> CreateParseInRecord (String[] csvHeader){
		fileReader = new FileReader(path);
		csvFileFormat = CSVFormat.DEFAULT.withHeader(csvHeader);
		csvFileParser = new CSVParser(fileReader,csvFileFormat);
		csvRecords = csvFileParser.getRecords();
		return csvRecords;
	}

	public Iterator<CSVRecord> GetRecordIterator () {
		try {
			return csvFileParser.getRecords().iterator();
		} catch (Exception e) {
			System.err.println("get record iterator error");
		}
		return null;
	}

	public Close() {
		super.Close();
		csvRecords = null;
	}
}
