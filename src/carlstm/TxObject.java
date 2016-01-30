package carlstm;

/**
 * A TxObject is a special kind of object that can be read and written as part
 * of a transaction.
 * 
 * @param <T> type of the value stored in this TxObject
 */
public final class TxObject<T> {
	T value;

	public TxObject(T value) throws NoActiveTransactionException, TransactionAbortedException {
		this.value = value;
		Pair<T, T> pair = new Pair<T, T>(this, this);
		TxInfo.addPair(pair);
	}

	public TxObject(T value, boolean flag) {
		this.value = value;
	}

	public T read() throws NoActiveTransactionException,
			TransactionAbortedException {
		// TODO implement me

		return value;
	}

	public void write(T newValue) throws NoActiveTransactionException,
			TransactionAbortedException {
		// TODO implement me

		TxObject updated = new TxObject(newValue,true);
		TxInfo.updatePair(this,updated);

//		this.value = value;
	}
//
//	public TxObject<T> updateObject(T value) {
//		this.value = value;
//		System.out.println(this.value+" is being updated");
//		return this;
//	}
}
