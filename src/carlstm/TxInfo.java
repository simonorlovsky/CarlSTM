package carlstm;

/**
 * This class holds transactional state for a single thread. You should use
 * {@link java.lang.ThreadLocal} to allocate a TxInfo to each Java thread. This
 * class is only used within the STM implementation, so it and its members are
 * set to package (default) visibility.
 */
class TxInfo {
	private static final int CAPACITY = 1024;
//	static TxObject<String> oldObject = new TxObject<String>("Hello");
//	static TxObject<String> newObject = new TxObject<String>("Hellos");
//	static Pair<String, String> pair = new Pair<String, String>(oldObject, newObject);

//	private static Pair[] pairs = {pair};
	private boolean abort = false;
	private static Pair[] pairs;

	public TxInfo() {
		pairs = new Pair[CAPACITY];
	}

	/**
	 * Start a transaction by initializing any necessary state. This method
	 * should throw {@link TransactionAlreadyActiveException} if a transaction
	 * is already being executed.
	 */
	void start() {
		// TODO implement me
		for (Pair<String, String> p: pairs){
			if (!p.getNewObject().value.equals(p.getOldObject().value)){
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
			if (pairs[i]==null){
				pairs[i]=pair;
				break;
			}
		}
	}


}
