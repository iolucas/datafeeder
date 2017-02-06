package nvgtt.data.db.datafeeder;

import java.util.ArrayList;

public class CheckData {
	
	public static void main(String[] args) {
		StorageQueue<String> pendentLinks = new StorageQueue<String>("pendent-links.ser");
		ObjectStorage<String, Long> linksIds = new ObjectStorage<String, Long>("linksIds.ser");
		ObjectStorage<Long, ArrayList<String>> pageLinks = new ObjectStorage<Long, ArrayList<String>>("pageLinks.ser");
		ObjectStorage<Long, String> pageIds = new ObjectStorage<Long, String>("pageIds.ser");

		App.print("Pendent links size: " + pendentLinks.size());
		App.print("linksIds size: " + linksIds.size());
		App.print("pageLinks size: " + pageLinks.size());
		App.print("pageIds size: " + pageIds.size());
	}

}
