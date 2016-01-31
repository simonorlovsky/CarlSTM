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
		System.out.println("Constructor for TxInfo");
		pairs = new Pair[CAPACITY];

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
			else if (p.getNewObject().value.equals("HELLO") &&
					!p.getOldObject().value.equals(p.getNewObject().value)) {
				abort = true;
			}
		}
		if (abort){
			//throw error
			abort();
		}
		else {
			//commit
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

	public Pair[] getPairs() {
		return pairs;
	}

	public static void addPair(Pair pair) {
		for (int i=0; i<pairs.length; i++){
			if(pairs[i] == null) {
				//System.out.println("Added pair "+pair.getOldObject().value+","+pair.getNewObject().value);
				pairs[i] = pair;
				break;
			}
			else if (pairs[i].getOldObject().value.equals("HELLO")){
				pairs[i]=pair;
				//System.out.println("Added pair "+pair.getOldObject().value+","+pair.getNewObject().value);
				break;
			}
		}
	}

	public static void updatePair(TxObject<?> oldObject, TxObject<?> newObject) {
		System.out.println(newObject.value);
		for (int i=0; i<pairs.length; i++){
			if (pairs[i].getOldObject().value.equals("HELLO")){
				break;
			}
			else if (pairs[i].getOldObject().value.equals(oldObject.value)) {
				pairs[i].setNewObject(newObject);
				//System.out.println("New object set");
				break;
			}
			else {
				//System.out.println("passing.. "+i);
			}
		}
	}
}
