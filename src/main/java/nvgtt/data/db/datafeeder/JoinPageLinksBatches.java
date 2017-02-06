package nvgtt.data.db.datafeeder;

import java.io.File;
import java.util.ArrayList;

public class JoinPageLinksBatches {

	public static void main(String[] args) {
		
		ObjectStorage<Long, ArrayList<String>> completePageLinks = 
				new ObjectStorage<Long, ArrayList<String>>("completePageLinks.ser"); 
		
		String[] batchesList = new File("pageLinksBatches").list();
		
		//Iterate thru the batches on the folder
		for(int i = 0; i < batchesList.length; i++) {
			App.print("Working on " + (i + 1) + "/" + batchesList.length);
			
			String fileName = batchesList[i];
			
			completePageLinks.putAll(new ObjectStorage<Long, ArrayList<String>>("pageLinksBatches/" + fileName));
		}
		
		App.print("Done with batches. Verifying...");
		
		//Open page ids and verify if the lengths are equal
		ObjectStorage<Long, String> pageIds = new ObjectStorage<Long, String>("pageIds.ser");
		
		App.print("pageIds size: " + pageIds.size());
		App.print("completePageLinks size: " + completePageLinks.size());
		
		App.print("Saving...");
		completePageLinks.save();
		
		App.print("Done");		

	}

}
