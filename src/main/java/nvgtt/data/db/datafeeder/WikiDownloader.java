package nvgtt.data.db.datafeeder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class WikiDownloader {
	
	static ObjectStorage<String, Long> linksIds;
	static ObjectStorage<Long, ArrayList<String>> pageLinks;
	static ObjectStorage<Long, String> pageIds;
	static StorageQueue<String> pendentLinks;
	
	public static void main(String[] args) {
		
		//Init all collections
		print("Initing collections...");
		pendentLinks = new StorageQueue<String>("pendent-links.ser");
		linksIds = new ObjectStorage<String, Long>("linksIds.ser");
		pageLinks = new ObjectStorage<Long, ArrayList<String>>("pageLinks.ser");
		pageIds = new ObjectStorage<Long, String>("pageIds.ser");
		
		//pendentLinks.offer("MQTT");
		
		recursiveDownload();		

	}
	
	static void recursiveDownload() {
		
		downloadData(()->{
			recursiveDownload();
		});		
	}
	
	static void downloadData(Runnable callback) {
		
		//Init thread pool
		print("Initing thread pool...");
		ExecutorService downloadPool = Executors.newFixedThreadPool(100);
				
		//thread safe integer for download count
		AtomicInteger downloadCount = new AtomicInteger();
				
		final int maxLinksDownload = Math.min(pendentLinks.size(), 2000);
				
		//If there is nothing to download, shutdown download pool
		if(maxLinksDownload == 0)
			downloadPool.shutdown();
				
		//Download stuff while the max links has not been achieved or the queue is not empty
		print("Initing download of " + maxLinksDownload + " links...");
		for(int i = 0; i < maxLinksDownload; i++) {
					
			String currentLink = pendentLinks.poll();
					
			//if the queue is empty, break
			if(currentLink == null)
				break;
					
			//If the current link has already been downloaded,
			//Sum one unit to the track value and skip it
			if(linksIds.contains(currentLink)) {
				downloadCount.incrementAndGet();
				continue;
			}
					
			//Create a thread for download
			downloadPool.execute(() -> {
						
				try {
					print("Downloading " + currentLink + "...");
					WikipediaPageData pageData = WikipediaApi.getAbstractLinks(currentLink, "en");
							
					//Ensure downloaded url is attached to the pageId
					linksIds.put(pageData.Url, pageData.PageId);
							
					//Check if the page has not been downloaded yet, if not
					if(!pageIds.contains(pageData.PageId)) {
								
						//Update page ids
						pageIds.put(pageData.PageId, pageData.Title);
							
						//Update page links
						pageLinks.put(pageData.PageId, pageData.Links);
								
						//Update pendent links
						for(String link : pageData.Links) {
							//If the link is not in the page ids (has not been downloaded) push it
							if(!linksIds.containsKey(link))
								pendentLinks.offer(link);
						}
					}

				} catch (Exception e) {
					//If something fail, put link back to queue
					pendentLinks.offer(currentLink);
					e.printStackTrace();
				} finally {
							
					int currentDownloadCount = downloadCount.incrementAndGet();
							
					print("Downloaded: " + currentDownloadCount + "/" + maxLinksDownload);
							
					//If everything has been finished, shutdown the pool
					if(currentDownloadCount == maxLinksDownload) {
						print("Shutting down queue...");
						downloadPool.shutdown(); // Disable new tasks from being submitted
								
						while(!downloadPool.isShutdown());
								
						print("Saving stuff...");
								
						while(true) {
							
							if(!linksIds.save()) {
								print("Error while saving linksIds.");
								break;
							}
							print("linksIds saved.");
										
							if(!pageIds.save()) {
								print("Error while saving pageIds.");
								break;
							}
							print("pageIds saved.");
									
							if(!pageLinks.save()) {
								print("Error while saving pageLinks.");
								break;
							}
							print("pageLinks saved.");
									
							if(!pendentLinks.save()) {
								print("Error while saving pendentLinks.");
								break;
							}
							print("pendentLinks saved.");
									
							print("Done successfully.");
							break;
						}
								
						//Execute finish callback
						callback.run();
					}
				}
			});		
		}
				
		print("Everything has been initiated.");		

	}
	
	static void print(Object x) {
		System.out.println(x);
	}

}
