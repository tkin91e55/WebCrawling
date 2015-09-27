package com.tkk.webCrawling;
import java.io.*;
import java.lang.String;

public class FileManager {

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
		fileWriter.append(OUTPUT_LINE_ENDING);
	}

	public void Append (String str) throws IOException {
		if (fileWriter == null) {
			fileWriter = new FileWriter(path,true);
		}

		fileWriter.append(str);
	}

	public void AppendOnNewLine (String newline, boolean notToOverwrite) throws IOException {
		if (fileWriter == null) {
			fileWriter = new FileWriter(path,notToOverwrite);
		}

		fileWriter.append(newline);
		fileWriter.append(OUTPUT_LINE_ENDING);
	}

	public void Append (String str, boolean notToOverwrite) throws IOException {
		if (fileWriter == null) {
			fileWriter = new FileWriter(path,notToOverwrite);
		}

		fileWriter.append(str);
	}

	//this will only overwrite the existing file
	public void AppendBufferedOnNewLine(String newline) throws IOException {
		if( fileWriter == null) {
			fileWriter = new BufferedWriter(new FileWriter(path));
		}
		fileWriter.append(newline);
		fileWriter.append(OUTPUT_LINE_ENDING);
	}

	public String ReadLine () throws IOException {
		if(fileReader == null){
			FileInputStream in = new FileInputStream(path);
			fileReader = new BufferedReader(new InputStreamReader(in));
		}
		return fileReader.readLine();
	}

	public void Close () throws IOException {
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

	public static boolean HasMoreLinesThan (String filename, int lnNu) throws IOException, FileNotFoundException {
		boolean hasMore = false;
		LineNumberReader lnr = new LineNumberReader(new FileReader(new File(filename)));
		lnr.skip(Long.MAX_VALUE);
		if(lnr.getLineNumber() > lnNu) {
			System.out.println("[FileManager,line] Line number of file: " + lnr.getLineNumber());
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
