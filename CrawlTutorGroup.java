import java.io.FileReader;
import java.io.IOException;
import java.util.Formatter;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Date;
import java.util.regex.Matcher;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.commons.collections4.*;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.csv.*;

public class CrawlTutorGroup {

	/**
	 * @param args the command line arguments
	 */

	public static String URL_KEY = "WC_URL";
	public static String[] file_header_mapping = {"TYPE","VALUE"};
	public static String[] phaseToBeEmpty = {"自我介紹: ","時間: ","我同意所有有關導師條款"};

	public static void main(String[] args) throws IOException {

		MultiMap<String,String> config = new MultiValueMap<String,String>();
		ParseInConfig(config);

		Collection<String> urls = (Collection<String>) config.get(URL_KEY);
		for(String url: urls){
			System.out.println("The url: " + url);
			ProcessUrl(url);
		}
	}

	static void ParseInConfig (MultiMap<String,String> mapConfig) throws IOException {

		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(file_header_mapping);
		FileReader fileReader = new FileReader("config.csv");
		System.out.println("The encoding is: " + fileReader.getEncoding());
		CSVParser csvFileParser = new CSVParser(fileReader, csvFileFormat);
		List csvRecords = csvFileParser.getRecords();
		System.out.println("[Apache] csvRecords.getRecords() size: " + csvRecords.size());

		for(int i = 1; i < csvRecords.size(); i++) {
			CSVRecord record = (CSVRecord) csvRecords.get(i);
			System.out.println("Testing apache commons csv here, The TYPE: " + record.get(file_header_mapping[0]) + " and the VALUE: " + record.get(file_header_mapping[1]));
			mapConfig.put(record.get(file_header_mapping[0]),record.get(file_header_mapping[1]));
		}
		//Test
		/*Set<String> keys = mapConfig.keySet();
		System.out.println("For testing: ");			
		for(String key: keys){
			System.out.println("Key: " + key + " value: " + mapConfig.get(key));
			Collection<String> values = (Collection) mapConfig.get(key);
			for(String i: values)
				System.out.println("value i: " + i);
		}*/
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
			String headingStr = heading.text();
			String contentStr = content.text();

			//Filter out not today's post
			Pattern dayPattern = Pattern.compile(" [0-9]{1,2} ");
			Matcher dayMatcher = dayPattern.matcher(headingStr);
			if (dayMatcher.find()){
				System.out.println(dayMatcher.group(0));
				String todayDay;
				Date today = new Date();
				DateFormat df = new SimpleDateFormat("dd");
				todayDay = df.format(today);
				Pattern TodayPattern = Pattern.compile(todayDay);
				Matcher TodayMatcher = TodayPattern.matcher(dayMatcher.group(0));
				if (!TodayMatcher.find()){
					System.out.println("NONONONO!!!!");
					continue;
					}
			//	System.out.println("Today's day: " + todayDay);
			}

			for (String outPhase: phaseToBeEmpty){
				headingStr = headingStr.replace(outPhase,"");
				contentStr = contentStr.replace(outPhase,"");
			}
			System.out.println(headingStr);
			System.out.println(contentStr);
		}
	}
}
