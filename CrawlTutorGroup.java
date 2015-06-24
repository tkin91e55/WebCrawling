import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.FileReader;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.Formatter;

import java.util.Set;
import java.util.Collection;
import org.apache.commons.collections4.*;
import org.apache.commons.collections4.map.MultiValueMap;

public class CrawlTutorGroup {

	/**
	 * @param args the command line arguments
	 */

	public static String URL_KEY = "WC_URL";

	public static void main(String[] args) throws IOException {

		MultiMap<String,String> config = new MultiValueMap<String,String>();
		ParseInConfig(config);
		//////////////////////////////////////
		Collection<String> urls = (Collection<String>) config.get(URL_KEY);
		for(String url: urls){
			System.out.println("The url: " + url);
			ProcessUrl(url);
		}
	}

	static void ParseInConfig (MultiMap<String,String> mapConfig) throws IOException {
		FileReader fileReader = new FileReader("config.csv");
		System.out.println("The encoding is: " + fileReader.getEncoding());
		CSVReader reader = new CSVReader(fileReader);
		String [] nextLine;
		//MultiMap config = new MultiValueMap<String,String>();
		while ((nextLine = reader.readNext()) != null) {
			// nextLine[] is an array of values from the line
			System.out.println(nextLine[0] + " " + nextLine[1] );
			mapConfig.put(nextLine[0],nextLine[1]);	
		}

		Set<String> keys = mapConfig.keySet();
		System.out.println("For testing: ");			
		for(String key: keys){
			System.out.println("Key: " + key + " value: " + mapConfig.get(key));
			Collection<String> values = (Collection) mapConfig.get(key);
			for(String i: values)
				System.out.println("value i: " + i);
		}
	}

	static void ProcessUrl (String urlStr) throws IOException {
		//suppose you understanding the incoming args
		Document aDoc = Jsoup.connect(urlStr).data("query","Java").userAgent("Mozilla").cookie("auth","token").timeout(6000).post();

		//String title = doc.title();
		//System.out.println(title);
		//String result = doc.text();
		//System.out.println(result);

		DoSearchOnContent (aDoc);
	}

	static void DoSearchOnContent (Document doc) throws IOException {
		for (int i=0; i<30; i++) {
			String header = String.format("span[id$=cs%d]",i);
			String text = String.format("div[id$=cdiv%d]",i);
			System.out.println("The header: " + header + " and the text: " + text);

			Elements heading = doc.select(header);
			Elements content = doc.select(text);
			System.out.println(heading.text());
			System.out.println(content.text());
		}
	}
}
