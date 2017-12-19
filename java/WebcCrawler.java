import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.xml.ws.Response;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {
	
	public static int num = 0; //number of pages the target is found on
	public static int num1 = 0;
	//Writes the items we have already searched
	public static BufferedWriter searchedWriter;
	//writes the links with the target on them
	public static BufferedWriter foundWriter;

	//main function
	public static void main(String[] args) throws IOException, ParseException {
		//console reader for user input
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		//get user inuput
		System.out.println("please enter the link to start at, example: "+"https://www.asu.edu/");
		String link = console.readLine(); //home page to begin the search
		System.out.println("please enter the website to stay on, example: asu.edu");
		String baseURL = console.readLine(); //domain we have to stay on
		System.out.println("please enter the word to search for");
		String target = console.readLine(); //target word searched for on every page
		
		searchedWriter = new BufferedWriter(new FileWriter("linksSearched.txt"));
		foundWriter = new BufferedWriter(new FileWriter("linksFound.txt"));
		foundWriter.write(link);
		//begin searching websites
		searchWebsite(link, baseURL, target);

	}
	
	public static void searchWebsite(String url, String baseURL, String target) throws IOException, ParseException {		
		//num1++;
		//System.out.println(num1);
		//Try block exists because sometimes we find pages we do not have access to (needs a log in, etc) and there is no way to predict this.
		try {
			Document page = getPage(url); // get the page, using some filters I made
			if(page != null) {
				//function determines if those elements contain the target and returns 1 if they do
				int pageContains = parsePage(target, page, url);
				//selecting all link elements that are not emails.
				Elements linkElements = page.select("a[href*="+baseURL+"]").select("a:not(a[href^=mailto:])");
				//This for-loop searches all of the links recursively
				for(Element linkElement : linkElements) {
					//creates a link object:Clean link function returns a string link that is absolute and valid. Page contains is part of the link's priority
					Link link = new Link(cleanLink((linkElement.attr("href")).toLowerCase(), url, baseURL), pageContains); //if the page contains the target, the links found here get higher priority
					if(link.link != null) {// if the clean link function actually return a valid link
						if(!linkAlreadySearched(link.link)) {//if the link has not already been searched, search it.
							if(link.link.contains(target)) link.priority ++;//if the URL contains the target, the link gets higher priority.
							searchedWriter.write(link.link, 0, link.link.length());//labeling the link as searched
							searchedWriter.newLine();
							searchedWriter.flush();
							searchWebsite(link.link, baseURL, target);//searching the new link.
						}
					}
				}
			}
		}
		catch(Exception e) {
		}
	}
	
	//Function gets the page, attempting to filter out old pages. Other filters will be applied later.
	public static Document getPage(String url) throws IOException {
		Connection connect = Jsoup.connect(url); //connect to the URL
		String lastModified = connect.execute().header("Last-Modified"); //get the last modified header. Last modified header will 
																		 //return the access date if the page is dynamically created
		//If the date is null I accept it and I accept it if it was made in 2017 or later
		if(lastModified == null || Integer.parseInt(lastModified.substring(12, 16)) >=2017) {
			return connect.get();
		}
		//null means the page is unacceptable.
		return null;
	}
	
	//Searches the file containing all links previously search. Soon to be obsolete
	public static boolean linkAlreadySearched(String link) throws IOException {
		BufferedReader reader = new BufferedReader( new FileReader("linksSearched.txt"));
		String line = reader.readLine();
		while(line != null) {
			boolean matches;
			try {
				matches = new URL(line).sameFile(new URL(link));
			}
			catch(Exception e) {
				matches = true;
			}
			if(matches) {
				return true;
			}
			line = reader.readLine();
		}
		reader.close();
		return false;
	}
	
	//Cleans the link so it is a valid, absolute path.
	public static String cleanLink(String link, String parent, String baseURL) throws MalformedURLException {
		//adding https to make the link valid
		if(!(link.contains("http:")) && !(link.contains("https:"))) {
			link = "https:" + link;
		}
		//Creating a Java URL object to clean the link with
		URL url = new URL(link);
		//getting the hst/domain
		String host= url.getHost();
		if(host.isEmpty()){//if the host is empty, this must be a relative path
			if(link.charAt(0)=='\\'){//Java URL class requires that relative links do not begin with a '\'
				link = link.substring(1);
			}
			URL urlp = new URL(parent);//Creating a URL object from the parent page's link
			url = new URL(urlp, link);//Creating a absolute link with the parent URL and relative path
			return url.toString(); //we do not need to check if the link is in the right domain because we can assme the parent was in the right domain
		}
		else if(host.contains(baseURL)) {//if the link was already an absolute link, we have to check the domain.
			return url.toString();
		}
		return null;
	}
	
	//function to parse the page and look for elements
	public static int parsePage(String target, Document page, String url) throws IOException {
		Elements els = page.select("*:containsOwn("+target+")"); // getting all elements in an HTML doc that contain the target
		if(!els.isEmpty()) {// if the elements are not empty, we have found some elements with the target
			num++; //number of pages the target is found on increases
			BufferedWriter writer = new BufferedWriter(new FileWriter("page" + num + ".txt"));// the file we write the information to is numbered
			//System.out.println(num + url);
			//write the new link to the found file
			foundWriter.write(url);
			foundWriter.newLine();
			//write the link to the file with the information
			writer.write(url);
			writer.newLine();
			//writing the contents of each element with the data
			for(Element el : els) {
				writer.write(el.ownText());
				writer.newLine();
			}
			writer.flush();
			foundWriter.flush();
			//this means the target was found
			return 1;
		}
		//this means the target was not found
		return 0;
	}
}
