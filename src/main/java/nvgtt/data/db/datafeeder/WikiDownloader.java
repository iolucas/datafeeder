package nvgtt.data.db.datafeeder;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class WikiDownloader {
	
	static ObjectStorage<String, Long> linksIds;
	static ObjectStorage<Long, ArrayList<String>> pageLinks;
	static ObjectStorage<Long, String> pageIds;
	
	public static void main(String[] args) {
		
		linksIds = new ObjectStorage<String, Long>("linksIds.ser");
		pageLinks = new ObjectStorage<Long, ArrayList<String>>("pageLinks.ser");
		pageIds = new ObjectStorage<Long, String>("pageIds.ser");
		
		ExecutorService downloadPool = Executors.newFixedThreadPool(200);
		AtomicInteger downloadCount = new AtomicInteger();
		
		
		
		
		/*feedData("MQTT");
		
		linksIds.save();
		pageLinks.save();
		pageIds.save();*/
		
		print(linksIds.get("MQTT"));
		print(pageIds.get(linksIds.get("MQTT")));
		
		for(String link : pageLinks.get(linksIds.get("MQTT"))) {
			print(link);
		}
		
		/*WikipediaPageData pageData = pageDatas.get(linksIds.get("MQTT"));
		
		for(String link : pageData.Links) {
			print(link);
		}*/
		
		

		
	}
	
	static void feedData(String url) {
		try {
			WikipediaPageData pageData = WikipediaApi.getAbstractLinks(url, "en");
			
			linksIds.put(url, pageData.PageId);
			pageLinks.put(pageData.PageId, pageData.Links);
			pageIds.put(pageData.PageId, pageData.Title);

			
		} catch(Exception e) {}
		
	}
	
	static void print(Object x) {
		System.out.println(x);
	}

}
