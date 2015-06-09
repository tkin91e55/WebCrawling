import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JsoupTest {

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
heading_args = args[1];
content_args = args[2];
Document doc = Jsoup.connect(url).data("query","Java").userAgent("Mozilla").cookie("auth","token").timeout(3000).post();

//String title = doc.title();
//System.out.println(title);
//String result = doc.text();
//System.out.println(result);

//System.out.println("The searched result: ");
Elements heading = doc.select(heading_args);
Elements content = doc.select(content_args);
System.out.println(heading.text());
System.out.println(content.text());
}
}

}
