package nvgtt.data.db.datafeeder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

public class PopulateDb {
	
	private static BatchInserter inserter;
	private static Map<Long, Long> inMemoryIndex;

	public static void main(String[] args) throws IOException {
    	
		App.print("Server starting...");
		
		inserter = BatchInserters.inserter(new File("C:/neo4j-enterprise-3.1.0/data/databases/test4.db"));
        inserter.createDeferredSchemaIndex(NvgttLabels.Article).on("wikiPageId").create();
        inMemoryIndex = new HashMap<Long, Long>();
    	
    	App.print("Initing data files...");
    	
    	ObjectStorage<String, Long> linksIds = new ObjectStorage<String, Long>("linksIds.ser");
    	ObjectStorage<Long, ArrayList<String>> pageLinks = new ObjectStorage<Long, ArrayList<String>>("pageLinks.ser");
    	ObjectStorage<Long, String> pageIds = new ObjectStorage<Long, String>("pageIds.ser");
  	
    	App.print("Creating nodes...");

    		
		int count = 0;
		int size = pageIds.size();
    		
		for(Entry<Long, String> page : pageIds.entrySet()) {
			
			String pageName = page.getValue();
			Long pageId = page.getKey();
			
			App.print("Creating " + pageName);
			
			HashMap<String, Object> properties = new HashMap<String, Object>();
			properties.put("name", pageName);
			properties.put("wikiPageId", pageId);
			
			long nodeId = inserter.createNode(properties, NvgttLabels.Article);
			inMemoryIndex.put(pageId, nodeId);
			
			count++;
			App.print("Done node " + count + "/" + size);
		}

    	App.print("Done with nodes.");	
    	
    	

		
    	App.print("Creating relations...");

		count = 0;
		size = pageLinks.size();
    		
		for(Entry<Long, ArrayList<String>> page : pageLinks.entrySet()) {
			
			Long pageId = page.getKey();
			ArrayList<String> pLinks = page.getValue();
			
			for(String pLink : pLinks) {
				try {
					long sourceNodeId = inMemoryIndex.get(pageId);
					long targetNodeId = inMemoryIndex.get(linksIds.get(pLink));
					
					inserter.createRelationship(sourceNodeId, targetNodeId, NvgttRelations.RefersTo, null);
					
					count++;
					App.print("Done relation " + count + "/" + size);
					
				} catch(Exception e) {
					App.print("Fail on creating relationship.");
					e.printStackTrace();
				}
			}
		}

    	App.print("Done with relations.");	
    	
    	inserter.shutdown();
    	App.print("Done");

	}

}
