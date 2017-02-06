package nvgtt.data.db.datafeeder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StorageQueue<T> extends ConcurrentLinkedQueue<T> {


	/**
	 * 
	 */
	private static final long serialVersionUID = 9538475983L;
	final String fileName;
	
	public StorageQueue(String fileName) {
		this.fileName = fileName;
		
		load();
	}
	
	public boolean save() {
		
		T[] queueArray = (T[]) this.toArray();
		
		try(
				FileOutputStream fos = new FileOutputStream(this.fileName);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
			) {
				oos.writeObject(queueArray);
				return true;
			} catch(Exception e) {
				return false;
			}	
	}
	
	private boolean load() {
		try(
			FileInputStream fis = new FileInputStream(this.fileName);
			ObjectInputStream ois = new ObjectInputStream(fis);
		) {
			T[] queueArray = (T[]) ois.readObject();
			
			for(int i = 0; i < queueArray.length; i++)
				this.offer(queueArray[i]);

			return true;
		} catch(Exception e) {
			return false;
		}		
	}
}
