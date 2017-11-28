import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {

	public static void main(String[] args) throws IOException {
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("please enter the link to start at, example: "+"https://www.asu.edu/");
		String link = console.readLine();
		System.out.println("please enter the website to stay on, example: asu.edu");
		String baseURL = console.readLine();
		System.out.println("please enter the word to search for");
		String target = console.readLine();
		
		BufferedWriter searchedWriter = new BufferedWriter(new FileWriter("linksSearched.txt"));
		BufferedWriter foundWriter = new BufferedWriter(new FileWriter("linksFound.txt"));
		searchWebsite(link, baseURL, searchedWriter, foundWriter, target);

	}
	
	public static boolean linkAlreadySearched(String link) throws IOException {
		BufferedReader reader = new BufferedReader( new FileReader("links.txt"));
		String line = reader.readLine();
		while(line != null) {
			if(link.equals(line)) {
				return true;
			}
			line = reader.readLine();
		}
		reader.close();
		return false;
	}
	
	public static void searchWebsite(String url, String baseURL, BufferedWriter searchedWriter, BufferedWriter foundWriter, String target) throws IOException {		
		searchedWriter.flush();
		foundWriter.flush();
		Document page = null;
		boolean worked = true;
		try {
			page = Jsoup.connect(url).get();
		}
		catch(Exception e) {
			worked = false;
		}
		if(worked) {
			Elements targetElements = page.select("*:contains("+target+")");
			boolean blah = false;
			if(!targetElements .isEmpty()) {
				foundWriter.write(url);
				foundWriter.newLine();
			}
			Elements linkElements = page.select("a[href*="+baseURL+"]").select("a:not(a[href^=mailto:])");
			for(Element linkElement : linkElements) {
				String link = (linkElement.attr("href")).toLowerCase();
				if(!(link.contains("www."))){
					link = "www." + link;
				}
				if(!(link.contains("http:")) && !(link.contains("https:"))) {
					link = "http:" + link;
				}
				if(!linkAlreadySearched(link) & !(link.contains("google.com") || link.contains("facebook.com"))) {
					searchedWriter.write(link, 0, link.length());
					searchedWriter.newLine();
					searchWebsite(link, baseURL, searchedWriter, foundWriter, target);
				}
			}
		}
	}
}
