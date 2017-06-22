package nvgtt.data.db.datafeeder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

public class eb4 {
	
	private static BatchInserter inserter;

	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		File folder = new File("C:\\Users\\du0x\\Desktop\\embedtree2\\learn_data\\packetpub_data");
		File[] listOfFiles = folder.listFiles();

		inserter = BatchInserters.inserter(new File("C:/neo4j-enterprise-3.1.0/data/databases/eb4.db"));
        inserter.createDeferredSchemaIndex(Eb4Labels.Content).on("title").create();
		
        int doneData = 0;
        
		for (File f : listOfFiles) {
			JSONObject jsondata = readJSONFile(f);
			
	        Object[] nodes_and_links = get_nodes_and_links(jsondata);
	        ArrayList<JSONObject> nodes = (ArrayList<JSONObject>)nodes_and_links[0];
	        ArrayList<int[]> links = (ArrayList<int[]>)nodes_and_links[1];
	        ArrayList<int[]> prereq_links = get_prereq_links(jsondata);
	        
	        addNodesAndLinks(nodes, links, prereq_links);
	        
	        doneData++;
	        print("Done data: " + doneData);
		}
		
		//String filepath = "C:\\Users\\du0x\\Desktop\\embedtree2\\learn_data\\packetpub_data\\9781782160007.json";
        //String readText = readFile(filepath);
        //JSONObject jsondata = new JSONObject(readText);

        inserter.shutdown();
	}
	
	
	public static void print(Object text) {
		System.out.println(text);
	}
	
	public static void addNodesAndLinks(ArrayList<JSONObject> nodes, ArrayList<int[]> links, ArrayList<int[]> prereq_links) {
		
		HashMap<Integer, Long> object2dbMap = new HashMap<Integer, Long>();
		
		for(JSONObject n : nodes) {
		
			HashMap<String, Object> properties = new HashMap<String, Object>();
			properties.put("title", n.get("t"));
			if(n.has("i"))
				properties.put("content_id", n.get("i"));
			
			long nodeId = inserter.createNode(properties, Eb4Labels.Content);
			object2dbMap.put(n.getInt("idx"), nodeId);
		}
		//print("Done nodes");
		
		for(int[] link : links) {
			long sourceId = object2dbMap.get(link[0]);
			long targetId = object2dbMap.get(link[1]);
			
			inserter.createRelationship(sourceId, targetId, Eb4Relations.has_content, null);
		}
		//print("Done links");
		
		for(int[] link : prereq_links) {
			long sourceId = object2dbMap.get(link[0]);
			long targetId = object2dbMap.get(link[1]);
			
			HashMap<String, Object> link_properties = new HashMap<String, Object>();
			link_properties.put("factor", 1.0);
			
			inserter.createRelationship(sourceId, targetId, Eb4Relations.needs, link_properties);
		}
		//print("Done prereq links");
		
	}
	
	public static Object[] get_nodes_and_links(JSONObject jsondata) {
		ArrayList<JSONObject> nodes = new ArrayList<JSONObject>();
		
		nodes.add(jsondata);
				
	    //Get all nodes from deep 1 and deep 2
	    for(Object c1 : (JSONArray)jsondata.get("c")) {
	        nodes.add((JSONObject)c1);
	        
	        for(Object c2 : (JSONArray)((JSONObject)c1).get("c")) {
	        	nodes.add((JSONObject)c2);
	        }
	    }
	            
	    //Set indexes
	    for(int i = 0; i < nodes.size(); i++) {
	    	nodes.get(i).put("idx", i);
	    }
	    
	    ArrayList<int[]> links = new ArrayList<int[]>();    
	    for (JSONObject n : nodes) {
	    	if(n.has("c")) {
	    		for(Object c : n.getJSONArray("c")) {
	    			int source_idx = n.getInt("idx");
	    			int target_idx = ((JSONObject)c).getInt("idx");
	    			links.add(new int[]{source_idx, target_idx});
	    		}
	    	}
	    }
	        
	    return new Object[]{nodes, links};	
	}
	

	public static ArrayList<int[]> get_prereq_links(JSONObject jsondata) {
	    //"""Must call this after generate nodes 'idx'."""
	    
		ArrayList<int[]> prereq_links = new ArrayList<int[]>();
	    
		ArrayList<Integer> done_child1_indexes = new ArrayList<Integer>();
		ArrayList<Integer> done_child2_indexes = new ArrayList<Integer>();
	    
		for(Object child1_obj : jsondata.getJSONArray("c")) {
			JSONObject child1 = (JSONObject)child1_obj;
	        //We may option to not include excluded topics later (but best way is to apply probabilistic models to it too)
	        //Set prereq factor to 1. In the future this should be calculated maybe
	        
	        //For the current, append everything before
	        int source_idx = child1.getInt("idx");
	        for (int target_idx :done_child1_indexes)
	            prereq_links.add(new int[]{source_idx, target_idx});
	        
	        done_child1_indexes.add(source_idx);
	        
	        for(Object child2_obj : child1.getJSONArray("c")) {
	        	JSONObject child2 = (JSONObject)child2_obj;
	       
	            source_idx = child2.getInt("idx");
	            for (int target_idx : done_child2_indexes)
	                prereq_links.add(new int[]{source_idx, target_idx});
	            
	            done_child2_indexes.add(source_idx);
	        }
		}
	        
	    return prereq_links;
	}
	
	public static JSONObject readJSONFile(File file) throws JSONException, FileNotFoundException, IOException {
		return new JSONObject(readFile(file));
	}
	
	public static String readFile(File file) throws FileNotFoundException, IOException {
	    
		String content = null;
		try (FileReader reader = new FileReader(file)) {

	        char[] chars = new char[(int) file.length()];
	        reader.read(chars);
	        content = new String(chars);
	    } 
	    
	    return content;
	}
	
	public static String readFile(String path) throws FileNotFoundException, IOException {
	    
	    File file = new File(path); //for ex foo.txt
	    return readFile(file);

	}

}
