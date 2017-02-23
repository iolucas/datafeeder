package nvgtt.data.db.datafeeder;

import java.util.ArrayList;

public class FilterPendentLinks {

	public static void main(String[] args) {
		App.print("Loading files...");
		
		ObjectStorage<String, Long> linksIds = new ObjectStorage<String, Long>("linksIds.ser");
		StorageQueue<String> pendentLinks = new StorageQueue<String>("pendent-links.ser");
		
		App.print("Getting sizes...");
		int pendSize = pendentLinks.size();
		int linksIdsSize = linksIds.size();
		
		App.print("Pendent links size: " + pendSize);
		App.print("linksIds size: " + linksIdsSize);
		
		StorageQueue<String> filteredPendentLinks = new StorageQueue<String>("filtered-pendent-links.ser");

		for(int i = 0; i < pendSize; i++) {
			
			if(pendentLinks.isEmpty())
				break;
			
			App.print("Working on " + i + "/" + pendSize);
			
			String link = pendentLinks.poll();
			
			//If the link is not in the links ids, push to the filtered queue
			if(!linksIds.containsKey(link))
				filteredPendentLinks.offer(link);
		}
		
		App.print("Done all. Saving...");
		
		filteredPendentLinks.save();
		
		App.print(filteredPendentLinks.size());
		
		App.print("Saved");
	}

}
