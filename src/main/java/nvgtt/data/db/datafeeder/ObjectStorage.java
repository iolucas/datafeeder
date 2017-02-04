package nvgtt.data.db.datafeeder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;

public class ObjectStorage<K, V> extends Hashtable<K,V> {


	private static final long serialVersionUID = 1L;

	final String fileName;
	
	public ObjectStorage(String fileName) {
		this.fileName = fileName;
		
		load();
	}
	
	public boolean save() {
		try(
				FileOutputStream fos = new FileOutputStream(this.fileName);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
			) {
				oos.writeObject(this);
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
			this.putAll((ObjectStorage<K,V>) ois.readObject());
			return true;
		} catch(Exception e) {
			return false;
		}		
	}
	
}
