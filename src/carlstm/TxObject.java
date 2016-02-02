package carlstm;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * A TxObject is a special kind of object that can be read and written as part
 * of a transaction.
 * 
 * @param <T> type of the value stored in this TxObject
 */

public final class TxObject<T> {
	T value;
	Lock lock = new ReentrantLock();

	// Constructor for creating a new TxObject that adds it to the TxInfo array
	public TxObject(T value) throws NoActiveTransactionException, TransactionAbortedException {
		this.value = value;
		Pair<T, T> pair = new Pair<T, T>(this, this);
		TxInfo.addPair(pair);
	}

	// Constructor for creating a TxObject to be passed in functions without adding to the TxInfo array
	public TxObject(T value, boolean flag) {
		this.value = value;
	}

	public T read() throws NoActiveTransactionException,
			TransactionAbortedException {
		// TODO implement me
		lock.lock();
		lock.unlock();

		return value;

	}

	// Writes the new object value into the TxInfo array
	public void write(T newValue) throws NoActiveTransactionException,
			TransactionAbortedException {
		// TODO implement me a little more
		lock.lock();

		TxObject updated = new TxObject(newValue,true);
		TxInfo.updatePair(this,updated);

		lock.unlock();

	}

}
