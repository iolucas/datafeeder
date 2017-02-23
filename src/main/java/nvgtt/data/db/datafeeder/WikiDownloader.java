package nvgtt.data.db.datafeeder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import java.lang.Thread;
import java.sql.Time;

public class WikiDownloader {
	
	ObjectStorage<String, Long> linksIds;
	ObjectStorage<Long, String> pageIds;
	StorageQueue<String> pendentLinks;
	
	public static void main(String[] args) {
		
		//Init all collections
		print("Initing collections...");
		
		//pendentLinks.offer("MQTT");
		
		WikiDownloader downloader = new WikiDownloader();
		
		while(true) {
			downloader.downloadDataAndSave(10000, 100);
			
			App.print("Done download.");
		}

	}
	
	public WikiDownloader() {
		pendentLinks = new StorageQueue<String>("pendent-links.ser");
		linksIds = new ObjectStorage<String, Long>("linksIds.ser");
		pageIds = new ObjectStorage<Long, String>("pageIds.ser");
	}
	
	public void downloadDataAndSave(int batchSize, int threadPoolSize) {
		
		//Init pageLinks batch storage
		App.print("Initing pagelinksbatch...");
		String pageLinksBatchFileName = "pageLinksBatch" + System.currentTimeMillis() + ".ser";
		ObjectStorage<Long, ArrayList<String>> pageLinks = 
				new ObjectStorage<Long, ArrayList<String>>("pageLinksBatches/" + pageLinksBatchFileName);
		
		//List to get pages that had its request failed to be put back to the pendent links queue
		//ArrayList<String> failRequestList = new ArrayList<String>();
		//this list must be syncronized
		
		//instead failrequest list, must use a list to store pendent links
		
		//check which solution is faster
		
		//Init thread pool
		print("Initing thread pool...");
		ExecutorService downloadPool = Executors.newFixedThreadPool(threadPoolSize);

		//thread safe integer for download count
		AtomicInteger downloadCount = new AtomicInteger();
				
		final int maxLinksDownload = Math.min(pendentLinks.size(), batchSize);
				
				
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
			
			//failRequestList.add(currentLink);
					
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
					
					//If successful, remove current link from fail list
					//failRequestList.remove(currentLink);				

				} catch (Exception e) {
					//If something fail, put link back to queue
					print("Error. Putting " + currentLink + " back to queue...");
					pendentLinks.offer(currentLink);
					print("Done putting " + currentLink + " back to queue.");
					e.printStackTrace();
					
				} finally {
							
					int currentDownloadCount = downloadCount.incrementAndGet();
					
					
					print(getTimeStamp() + " Downloaded: " + currentDownloadCount + "/" + maxLinksDownload);
				}
			});		
		}
				
		print("Everything has been initiated.");
		
		//Init shutdown
		downloadPool.shutdown();
		
		try {
			//Blocks until everything is done
			if(downloadPool.awaitTermination(1, TimeUnit.HOURS))
				print("Everything done successfully!");
			else
				print("Something has failed. Timeout fired.");
		
			//Put back links to the queue that doesn't succeeded
			/*for(int i = 0; i < failRequestList.size(); i++) {
				String failLink = failRequestList.get(i);
				print("Putting back " + failLink + "...");
				pendentLinks.offer(failLink);
			}*/
			
			
			print("Saving stuff...");
			
			if(!linksIds.save())
				throw new Exception("Error while saving linksIds.");

			print("linksIds saved.");
						
			if(!pageIds.save())
				throw new Exception("Error while saving pageIds.");

			print("pageIds saved.");
					
			if(!pageLinks.save())
				throw new Exception("Error while saving pageLinks.");

			print("pageLinks saved.");
					
			if(!pendentLinks.save())
				throw new Exception("Error while saving pendentLinks.");

			print("pendentLinks saved.");
					
			print("Save successfully.");	
		
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	static void print(Object x) {
		System.out.println(x);
	}
	
	static String getTimeStamp() {
		Time cTime = new Time(System.currentTimeMillis());
		return cTime.toString();
	}

}
