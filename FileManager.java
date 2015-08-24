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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.commons.collections4.*;
import org.apache.commons.collections4.map.MultiValueMap;

public class FileManager {

	//public static String OUTPUT_LINE_ENDING = "\n";
	public final static String OUTPUT_LINE_ENDING = System.getProperty("line.separator");

	String path;
	File file;
	BufferedReader fileReader;
	Writer fileWriter;

	public FileManager (String filePath) {
		path = filePath;
	}

	//By default not to overwrite existing file
	public void AppendOnNewLine (String newline) throws IOException {
		if (fileWriter == null) {
			fileWriter = new FileWriter(path,true);
		}

		fileWriter.append(newline);
		fileWriter.append(OUTPUT_LINE_ENDING):
	}

	public void AppendOnNewLine (String newline, boolean notToOverwrite) throws IOException {
		if (fileWriter == null) {
			fileWriter = new FileWriter(path,notToOverwrite);
		}

		fileWriter.append(newline);
		fileWriter.append(OUTPUT_LINE_ENDING):
	}

	public void AppendBufferedOnNewLine(String newline){
		if( fileWriter == null) {
			fileWriter = new BufferedWriter(new FileWriter(path));
		}
		fileWriter.append(newline);
		fileWriter.newline();
	}

	public String ReadLine () throws IOException {
		if(fileReader == null){
			FileInputStream in = new FileInputStream(path);
			fileReader = new BufferedReader(new InputStreamReader(in));
		}
		return fileReader.readline();
	}

	public void Close () {
		file = null;
		if(fileReader != null)
			fileReader.close();
		if(fileWriter != null)
			fileWriter.close();
	}

/*	public static void SwapFiles (String fileA, String fileB){
		File Afile = new File(fileA);
		File Bfile = new File(fileB);
	}*/

	public static boolean RenameFile (String filePath, String NewName) {
		boolean success = false;
		File file = new File(filePath);	
		File newfile = new File(NewName);

		if(file.renameTo(newfile)){
			success = true;
			}

		return success;
	}

	public static boolean CheckFileExist (String path) {
		boolean fileExist;
		File file = new File(path);

		if(!file.exists() && !file.isDirectory()){
			fileExist = false;
		}else{
			fileExist = true;
		}

		return fileExist;
	}

	public static boolean HasMoreLinesThan (String filename, int lnNu) {
		boolean hasMore = false;

		LineNumberReader lnr = new LineNumberReader(new FileReader(new File(filename)));
		lnr.skip(Long.MAX_VALUE);

		if(lnr.getLineNumber() >= lnNu) {
			System.out.println("[DB,line] Line number of DB: " + lnr.getLineNumber());
			hasMore = true;
		}
		lnr.close();
		return hasMore;
	}

	public static boolean CreateFolder (String folderName) {
		File theDir = new File(folderName);
		boolean createdFolder = false;

		try{
			if(!theDir.exists()){
				boolean result = theDir.mkdir();
				if(result){
					System.out.println(folderName+" folder created");
					createdFolder = result;
				}
			}
		}catch(Exception e){
			System.err.println("CreateFolder() error");
		}

		return createdFolder;
	}
}
