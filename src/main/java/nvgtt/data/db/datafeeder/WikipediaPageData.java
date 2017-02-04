package nvgtt.data.db.datafeeder;

import java.util.ArrayList;

public class WikipediaPageData {

	public final String Title;
	public final String Url;
	public final long PageId;
	public ArrayList<String> Links;
	
	public WikipediaPageData(String title, String url, long pageId, ArrayList<String> links) {
		Title = title;
		Url = url;
		PageId = pageId;
		Links = links;
	}
}
