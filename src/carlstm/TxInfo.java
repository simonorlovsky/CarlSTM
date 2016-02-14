package carlstm;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

/**
 * This class holds transactional state for a single thread. You should use
 * {@link java.lang.ThreadLocal} to allocate a TxInfo to each Java thread. This
 * class is only used within the STM implementation, so it and its members are
 * set to package (default) visibility.
 */
public class TxInfo {

	private boolean active = false;
	private ArrayList<Pair> pairs;
	private ArrayList<TxObject<Object>> objects;
	public static int aborted = 20;

	public TxInfo() {
		pairs = new ArrayList<Pair>();
		objects = new ArrayList<TxObject<Object>>();
	}

	/**
	 * Start a transaction by initializing any necessary state. This method
	 * should throw {@link TransactionAlreadyActiveException} if a transaction
	 * is already being executed.
	 */
	void start() {
		if(active) {
			abort();
			throw new TransactionAlreadyActiveException();
		}
		else {
			active = true;
		}
	}

	/**
	 * Try to commit a completed transaction. This method should update any
	 * written TxObjects, acquiring locks on those objects as needed.
	 *
	 * @return true if the commit succeeds, false if the transaction aborted
	 */
	boolean commit() {
		//System.out.println("Commit");
		for(int i = 0;i<pairs.size()-1;i++) {
			if(pairs.get(i) != null) {
				if(!pairs.get(i).getOldObject().value.equals(objects.get(i).value)){
					return false;
				}
				pairs.get(i).getOldObject().setValue(pairs.get(i).getNewObject().value);
			}
//			if(pairs.get(i) == null) {
//				System.out.println(pairs.get(i).getOldObject());
//			}
		}
		return true;
	}

	/**
	 * This method cleans up any transactional state if a transaction aborts.
	 */
	void abort() {
		//System.out.println("Abort");
		try {
			if(aborted > 0) {
				for(Pair<Object,Object> pair: pairs) {
					if(pair != null) {
						pair.getNewObject().setValue(pair.getOldObject().value);
					}
				}
				active = false;
				aborted = 20;
			}
			else {
				aborted = 20;
			}

		}
		catch(ConcurrentModificationException e) {
			abort();
			aborted--;
		}
	}

	// Returns the Pair array object
	public ArrayList<Pair> getPairs() {
		return pairs;
	}

	// Search the array for the old object, and update the new object of that pair with the 2nd parameter
	public void updatePair(TxObject<?> oldObject, TxObject<?> newObject) {
		for (int i=0; i<pairs.size(); i++){
			if (pairs.get(i).getOldObject().value.equals(oldObject.value)) {
				pairs.get(i).setNewObject(newObject);
				break;
			}
		}
	}

	// Adds the pair in the parameter to the Pair array
	public void addPair(Pair pair){
		try{
			pairs.add(pair);
			objects.add(pair.getOldObject());
		}
		catch (ArrayIndexOutOfBoundsException e) {
			addPair(pair);
		}
	}
}
