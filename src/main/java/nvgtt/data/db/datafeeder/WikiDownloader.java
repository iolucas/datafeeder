package nvgtt.data.db.datafeeder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;

public class WikiDownloader {

	public static void main(String[] args) {
		
		ObjectStorage<String, Long> storage = new ObjectStorage<String, Long>("lucas.ser");
		
		print(storage.get("ae"));
		
		storage.put("ae", (long) 123);
		
		
		
		print(storage.save());
		
		print("DOne");
		
		/*Hashtable<String, Long> test = new Hashtable<String, Long>();
		
		test.put("Lucas", (long) 12324324);
		
		try {
			FileOutputStream fos = new FileOutputStream("hashtable.ser");
	        ObjectOutputStream oos = new ObjectOutputStream(fos);
	             
	        oos.writeObject(test);
	        oos.close();
	        fos.close();
		} catch(Exception e) {
			print(e);
		}
		
		
		try {
			FileInputStream fis = new FileInputStream("hashtable.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			
			Hashtable<String, Long> test2 = (Hashtable<String, Long>) ois.readObject();
			
			print(test2.get("Lucas"));
			
		} catch(Exception e) {
			print(e);
		}
		
		
		print("Done");
*/
	}
	
	static void print(Object x) {
		System.out.println(x);
	}

}
