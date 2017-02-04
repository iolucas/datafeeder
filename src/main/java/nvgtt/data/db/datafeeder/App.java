package nvgtt.data.db.datafeeder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;


/**
 * Hello world!
 *
 */
public class App 
{
	static Transaction tx;
	
    public static void main( String[] args ) throws HttpRequestException, Exception
    {
    	print("Server starting...");
    	
    	GraphDatabaseService graphDb = new GraphDatabaseFactory()
    			.newEmbeddedDatabase(new File("C:/neo4j-enterprise-3.1.0/data/databases/test3.db"));
    	
    	//Thread pool to download wikipedia data
    	ExecutorService downloadPool = Executors.newFixedThreadPool(200);
    	
    	//List to store pageData 
    	Vector<WikipediaPageData> pageDataList = new Vector<WikipediaPageData>();
    	
		//Thread safe integer
		AtomicInteger downloadCount = new AtomicInteger();
    	
		//must use some thing to store download data to speedup 
		
    	print("Getting empty links");    	
    	ArrayList<String> emptyLinks = getEmptyLinks(graphDb, 1000);
		final int size = emptyLinks.size();
			
		//Iterate thru empty links
		for(String emptyLink : emptyLinks) {
			
			downloadPool.execute(() -> {
				try {
					pageDataList.add(WikipediaApi.getAbstractLinks(emptyLink, "en"));
				} catch(Exception e) {
					print("Error " + e.getMessage());
				}
				
				int currentCount = downloadCount.incrementAndGet();
				print("Downloaded " + currentCount + "/" + emptyLinks.size());
				
				//Once everything has been done
				if(currentCount == size) {
					downloadPool.shutdown(); //Shutdown download pool
					
					while(!downloadPool.isShutdown());
					
					//Insert data into database
			    	int count = 0;
			    	
			    	try (Transaction tx = graphDb.beginTx()) {
			    		
				    	for(WikipediaPageData pageData : pageDataList) {
				    		feedDatabase(graphDb, pageData);
				    		count++;
				    		print("Inserted " + count + "/" + pageDataList.size());
				    	}	

				    	tx.success();
			    	} catch(Exception e) {
			    		print("ERROR WHILE INSERTING");
			    		print(e);
			    		print(e.getMessage());
			    		print(e.getLocalizedMessage());
			    	}
				}
			});
		}
    	
    	//--------------------------
    	
    
    	


    	//---------------------------
    	
    	
    	
    	
    	//feedEmptyNodes(graphDb);
    	
    	//feedDatabase(graphDb, WikipediaApi.getAbstractLinks("MQTT", "en"));
    	
		//Register a Shutdown Hook
		registerShutdownHook(graphDb);		
    }

    
    static void feedEmptyNodes(GraphDatabaseService graphDb) {
    	
    	//Thread pool to execute tasks
    	print("Creating thread pools...");
    	ExecutorService dbInsertPool = Executors.newFixedThreadPool(1);
    	ExecutorService downloadPool = Executors.newFixedThreadPool(5);
    	
		//Thread safe integers
		AtomicInteger downloadCount = new AtomicInteger();
		AtomicInteger dbInsertCount = new AtomicInteger();
    	
    	//Get empty links
    	print("Getting empty links");    	
    	ArrayList<String> emptyLinks = getEmptyLinks(graphDb, 100);
		final int size = emptyLinks.size();
			
		//Iterate thru empty links
		for(String emptyLink : emptyLinks) {
			
			//Execute action on the thread
			downloadPool.execute(() -> {

				try {
					WikipediaPageData pageData = WikipediaApi.getAbstractLinks(emptyLink, "en");
					
					dbInsertPool.execute(() -> {
						feedDatabase(graphDb, pageData);
						int currentDbInsertCount = dbInsertCount.incrementAndGet();
						
						print("Inserted: " + currentDbInsertCount + "/" + size);
						
						//If everything has been finished, shutdown the pool
						if(currentDbInsertCount == size)
							dbInsertPool.shutdown(); // Disable new tasks from being submitted
					});
					
				} catch(Exception e) {
					print("ERROR " + e.getMessage());
					print(e.getLocalizedMessage());
				} finally {
					int currentDownloadCount = downloadCount.incrementAndGet();
					
					print("Downloaded: " + currentDownloadCount + "/" + size);
					
					//If everything has been finished, shutdown the pool
					if(currentDownloadCount == size)
						downloadPool.shutdown(); // Disable new tasks from being submitted
				}		
			});
		}
    }
    
    static ArrayList<String> getEmptyLinks(GraphDatabaseService graphDb, int limit) {
    	
    	//Array to keep empty links
    	ArrayList<String> emptyLinks = new ArrayList<String>();
    	
    	try (Transaction tx = graphDb.beginTx()) {
    	
	    	//Object to iterate thru nodes
	    	ResourceIterator<Node> nodeIterator = graphDb.findNodes(NvgttLabels.Hyperlink);
	    	
	    	//Iterate thru nodes
	    	while(emptyLinks.size() < limit && nodeIterator.hasNext()) {
	    		Node node = nodeIterator.next();
	    		
	    		//If the node has no wikiPageId property, means it is empty
	    		if(!node.hasProperty("wikiPageId"))
	    			emptyLinks.add((String) node.getProperty("url"));		
	    	}
	    	
	    	//Finish transaction
	    	tx.success();	    	
    	}
    	
    	return emptyLinks;    	
    }
   
    
    static void feedDatabase(GraphDatabaseService graphDb, WikipediaPageData pageData) {
    	//!!!!!----- The transaction must already be started and will be close out of here -----!!!!!
		
    	//print("Adding data from " + pageData.Url);
    	
    	//Flags to sinalize whether the object has been created or just got
    	boolean artCreated = false;
    	boolean urlCreated = false;
    	
    	Node targetArticle = null;
    	
    	//try (Transaction tx = graphDb.beginTx()) {
    		
	    	//Check if the target article exists, if not, create it
    		try {
    			targetArticle = graphDb.findNode(NvgttLabels.Article, "wikiPageId", pageData.PageId);
    		} catch(Exception e) {
    			print(e);
    			print("ERROR ON PAGEID: " + pageData.PageId);
    		}
    		
	    	if(targetArticle == null) {
	    		targetArticle = graphDb.createNode(NvgttLabels.Article);
	    		targetArticle.setProperty("name", pageData.Title);
	    		targetArticle.setProperty("wikiPageId", pageData.PageId);
	    		
	    		artCreated = true; //set created flag true
	    	}
	    	
	    	//Check if the target url exists, if not create it
			Node targetUrl = null;
    		
			try {
    			targetUrl = graphDb.findNode(NvgttLabels.Hyperlink, "url", pageData.Url);
    		} catch(Exception e) {
    			print(e);
    			print("ERROR ON PAGEURL: " + pageData.Url);
    		}
			
			
			if(targetUrl == null) {
				targetUrl = graphDb.createNode(NvgttLabels.Hyperlink);
				targetUrl.setProperty("url", pageData.Url);
				
				urlCreated = true; //set created flag true
			}
			//Since even the existing do not have the wikiPageId, add it
			targetUrl.setProperty("wikiPageId", pageData.PageId); //Add wikipage id reference
			
			//Verify if the relation exists, if not, create it
			//If some of them has been created now, is not necessary verify relation
			if(artCreated || urlCreated || !checkRelationBetween(targetUrl, targetArticle, NvgttRelations.RedirectsTo)) {
				targetUrl.createRelationshipTo(targetArticle, NvgttRelations.RedirectsTo);
			}
		
			//If the article node were just created, create its links relations
			if(artCreated) {
				for(String link : pageData.Links) {
					
					//Check if the hyperlink exists, if not create it
					Node linkUrl = null;
					
					try {
						linkUrl = graphDb.findNode(NvgttLabels.Hyperlink, "url", link);
		    		} catch(Exception e) {
		    			print(e);
		    			print("ERROR ON LINK: " + link);
		    		}
					
					if(linkUrl == null) {
						linkUrl = graphDb.createNode(NvgttLabels.Hyperlink);
						linkUrl.setProperty("url", link);
					}
					
					//Create relation
					targetArticle.createRelationshipTo(linkUrl, NvgttRelations.LinksTo);
					
				}
			}
				
			//finish transaction
			/*tx.success();
			
		} catch(Exception e) {
    		print("Error while adding page data.");
    		print(e.getMessage());
    		print(e.getLocalizedMessage());
    		print(e);
    	}*/
    }
    
    
    
    static boolean checkRelationBetween(Node n1, Node n2, RelationshipType type) {
		
    	for(Relationship rel : n1.getRelationships(type, Direction.OUTGOING)) {
    		if(rel.getEndNode().getId() == n2.getId())
    			return true;
    	}
    	
    	return false; 	
    }
    
	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j.
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				//Shutdown the Database
				System.out.println("Server is shutting down");

				graphDb.shutdown();
			}
		});
	}
    
	static void print(Object x) {
		System.out.println(x);
	}
}
