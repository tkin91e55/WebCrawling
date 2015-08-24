import java.lang.String;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Formatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.Iterator;

public class Crawlee {

		public int case_index;
		public HashMap<String,String> map = new HashMap<String,String>();

		/*
		   Current keys of HashMap are:
		   Location,LastUpdateAt,Time,Gender,Info,Subject,Fee,Other
		 */

		public Crawlee (int idx){
			case_index = idx;
		}

		public void Put (String Key, String Value) {
			map.put(Key,Value);
		}

		public String Context () {
			String content = "";
			Collection<String> strings = map.values();
			for (String str: strings){
				content = content + str + "\n";
			}
			//System.out.println("[Crawlee] content: " + content);
			return content;
		}

		public int GetFee () {
			if(map.containsKey("Fee")){
				//System.out.println("[SearchCrit] fee: " + map.get("Fee"));
				Pattern price = Pattern.compile("\\$[0-9]{2,4}");
				Matcher matcher = price.matcher(map.get("Fee"));
				if(matcher.find()){
					String casePriceStr = matcher.group(0).substring(1);
					int casePrice = 55699;
					casePrice = Integer.parseInt(casePriceStr);
					if (casePrice != 55699)
						return casePrice;
				}
			}
			return 689831;	
		}

		public String GetValueByKey (String key) {

			if(map.containsKey(key)){
				return map.get(key);
			}

			return "";
		}

}


