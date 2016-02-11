package carlstm;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * A TxObject is a special kind of object that can be read and written as part
 * of a transaction.
 *
 * @param <T> type of the value stored in this TxObject
 */

import javax.naming.Context;

public final class TxObject<T> {
	T value;
	ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	// Constructor for creating a new TxObject that adds it to the TxInfo array
	public TxObject(T value) {
		this.value = value;
		Pair<T, T> pair = new Pair<T, T>(this, this);
		TransactionSTM.MyThreadLocal.info.addPair(pair);
	}

	// Constructor for creating a TxObject to be passed in functions without adding to the TxInfo array
	public TxObject(T value,  boolean flag) {
		this.value = value;
	}

	public T read() throws NoActiveTransactionException,
			TransactionAbortedException {
		try{
			if(lock.readLock().tryLock()) {
				lock.readLock().unlock();
				return value;
			}
		}
		finally {
			return value;
		}
	}

	// Writes the new object value into the TxInfo array
	public void write(Object newValue) throws NoActiveTransactionException,
			TransactionAbortedException {
		try{
			lock.writeLock().lock();
			TxInfo info = TransactionSTM.MyThreadLocal.info;

			TxObject updated = new TxObject(newValue, true);
			System.out.println("Updated value = "+updated.value);
			info.updatePair(this,updated);
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	public void setValue(T val) {
		this.value = val;
	}

}
