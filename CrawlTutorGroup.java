import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.FileReader;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.Formatter;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

//for csv parser:
//import com.opencsv.CSVReader;
//import com.opencsv.CSVReaderBuilder;

public class CrawlTutorGroup {

	/**
	 * @param args the command line arguments
	 */

	static String url="";
	static String heading_args="";
	static String content_args="";

	public static void main(String[] args) throws IOException {


		if(args[0] != null){
			url = args[0];
			//suppose you understanding the incoming args
			//Document doc = Jsoup.connect(url).data("query","Java").userAgent("Mozilla").cookie("auth","token").timeout(6000).post();
			System.out.println("The url: " + url);

			//String title = doc.title();
			//System.out.println(title);
			//String result = doc.text();
			//System.out.println(result);

			//System.out.println("The searched result: ");

			//Elements heading = doc.select(heading_args);
			//Elements content = doc.select(content_args);
			//System.out.println(heading.text());
			//System.out.println(content.text());

			for (int i=0; i<30; i++) {
				//String header = String.format("span[id$=cs%d]",i);
				//String content = String.format("div[id$=cdiv%d]",i);
				//System.out.println("The header: " + header + " and the content: " + content);
			}

			FileReader fileReader = new FileReader("config.csv");
			System.out.println("The encoding is: " + fileReader.getEncoding());
			CSVReader reader = new CSVReader(fileReader);
			String [] nextLine;
			Map config = new Hashtable<String,String>();
			while ((nextLine = reader.readNext()) != null) {
				// nextLine[] is an array of values from the line
				System.out.println(nextLine[0] + " " + nextLine[1] );
				config.put(nextLine[0],nextLine[1]);
			}
			
			Set<String> keys = config.keySet();
			for(String key: keys)
				System.out.println("Key: " + key + " value: " + config.get(key));
		}
	}

}
