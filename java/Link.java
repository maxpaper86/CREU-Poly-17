//Link object that holds the string link and it's priority over other links.
public class Link implements Comparable{
	public String link;
	public int priority;
	
	public Link (String l, int p) {
		link = l;
		priority = p;
	}

	@Override
	public int compareTo(Object o) {
		if(priority == ((Link)o).priority) return 0;
		else if(priority < ((Link)o).priority) return -1;
		else return 1;
	}

}
