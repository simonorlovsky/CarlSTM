package carlstm;


/**
 * This class holds transactional state for a single thread. You should use
 * {@link java.lang.ThreadLocal} to allocate a TxInfo to each Java thread. This
 * class is only used within the STM implementation, so it and its members are
 * set to package (default) visibility.
 */
class TxInfo {
	private static final int CAPACITY = 10;

	private boolean abort = false;
	public static Pair[] pairs;

	public TxInfo() throws TransactionAbortedException, NoActiveTransactionException {
		pairs = new Pair[CAPACITY];

		// Populate the pair array with pseudo-null pairs
		for(int i = 0;i<pairs.length;i++) {
			TxObject<String> oldObject = new TxObject<String>("HELLO",true);
			TxObject<String> newObject = new TxObject<String>("HELLO",true);
			Pair<String, String> pair = new Pair<String, String>(oldObject, newObject);
			addPair(pair);
		}
	}

	/**
	 * Start a transaction by initializing any necessary state. This method
	 * should throw {@link TransactionAlreadyActiveException} if a transaction
	 * is already being executed.
	 */
	void start() {
		// TODO implement me
		for (Pair<String, String> p: pairs){
			if(p == null) {

			}
			else if (!p.getNewObject().value.equals(p.getOldObject().value)){
				abort = true;
			}
		}
		if (abort){
			//throw error
			abort();
		}
		else {
			//commit
			for (Pair<String, String> p: pairs){
				if (p == null){
					break;
				}
				else {
					if (p.getNewObject().lock.tryLock() && p.getOldObject().lock.tryLock()){
						//Commit can occur
					}
					else {
						abort();
					}
				}
			}
			commit();
		}
	}

	/**
	 * Try to commit a completed transaction. This method should update any
	 * written TxObjects, acquiring locks on those objects as needed.
	 * 
	 * @return true if the commit succeeds, false if the transaction aborted
	 */
	boolean commit() {
		// TODO implement me
		System.out.println("Commit");
		return false;
	}

	/**
	 * This method cleans up any transactional state if a transaction aborts.
	 */
	void abort() {
		// TODO implement me
		System.out.println("Abort");
	}

	// Returns the Pair array object
	public Pair[] getPairs() {
		return pairs;
	}

	// Adds the pair in the parameter to the Pair array
	public static void addPair(Pair pair) {
		for (int i=0; i<pairs.length; i++){
			if(pairs[i] == null) {
				pairs[i] = pair;
				break;
			}
			else if (pairs[i].getOldObject().value.equals("HELLO")){ // HELLO indicates an unset pair
				pairs[i]=pair;
				break;
			}
		}
	}

	// Search the array for the old object, and update the new object of that pair with the 2nd parameter
	public static void updatePair(TxObject<?> oldObject, TxObject<?> newObject) {
		for (int i=0; i<pairs.length; i++){
			if (pairs[i].getOldObject().value.equals("HELLO")){ // HELLO indicates an unset pair
				break;
			}
			else if (pairs[i].getOldObject().value.equals(oldObject.value)) {
				pairs[i].setNewObject(newObject);
				break;
			}
		}
	}
}
