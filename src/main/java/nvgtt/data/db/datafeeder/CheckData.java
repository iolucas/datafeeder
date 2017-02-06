package nvgtt.data.db.datafeeder;

import java.util.ArrayList;

public class CheckData {
	
	static ObjectStorage<String, Long> linksIds;
	static ObjectStorage<Long, ArrayList<String>> pageLinks;
	static ObjectStorage<Long, String> pageIds;
	static StorageQueue<String> pendentLinks;

	public static void main(String[] args) {
		pendentLinks = new StorageQueue<String>("pendent-links.ser");
		linksIds = new ObjectStorage<String, Long>("linksIds.ser");
		pageLinks = new ObjectStorage<Long, ArrayList<String>>("pageLinks.ser");
		pageIds = new ObjectStorage<Long, String>("pageIds.ser");

		
		print("Pendent links size: " + pendentLinks.size());
		print("linksIds size: " + linksIds.size());
		print("pageLinks size: " + pageLinks.size());
		print("pageIds size: " + pageIds.size());
		
	}
	
	static void print(Object x) {
		System.out.println(x);
	}

}
