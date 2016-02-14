package carlstm;

/**
 * This class coordinates transaction execution. You can execute a transaction
 * using {@link #execute}. For example:
 *
 * <pre>
 * class MyTransaction implements Transaction&lt;Integer&gt; {
 * 	TxObject&lt;Integer&gt; x;
 *
 * 	MyTransaction(TxObject&lt;Integer&gt; x) {
 * 		this.x = x;
 * 	}
 *
 * 	public Integer run() throws NoActiveTransactionException,
 * 			TransactionAbortedException {
 * 		int value = x.read();
 * 		x.write(value + 1);
 * 		return value;
 * 	}
 *
 * 	public static void main(String[] args) {
 * 		TxObject&lt;Integer&gt; x = new TxObject&lt;Integer&gt;(0);
 * 		int result = CarlSTM.execute(new MyTransaction(x));
 * 		System.out.println(result);
 * 	}
 * }
 * </pre>
 */
public class CarlSTM {
	public static long curWaitTime = (long) .0001;
	/**
	 * Execute a transaction and return its result. This method needs to
	 * repeatedly start, execute, and commit the transaction until it
	 * successfully commits.
	 *
	 * @param <T> return type of the transaction
	 * @param tx transaction to be executed
	 * @return result of the transaction
	 */
	public static <T> T execute(Transaction<T> tx) {
		TxInfo info = TransactionSTM.MyThreadLocal.getInfo();
		try {
			info.start();
			T result = tx.run();
			boolean committed = info.commit();
			if (committed){
				return result;
			}
			else {
				try{
//					System.out.println("SLEEPING");
					Thread.currentThread().sleep(curWaitTime);
				}
				catch(InterruptedException e) {
					// Thread interrupted
				}
				finally {
					curWaitTime *= 5;
					return execute(tx);
				}

			}

		} catch (NoActiveTransactionException e) {
			return null;
		} catch (TransactionAbortedException e) {
			if(TxInfo.aborted > 0) {
				TxInfo.aborted--;
				info.abort();
				return execute(tx);
			}
			else {
				TxInfo.aborted = 20;
			}
			return null;

		} catch (TransactionAlreadyActiveException e) {
			if(TxInfo.aborted > 0) {
				TxInfo.aborted--;
//				info.abort();
//				return execute(tx);
			}
			else {
				TxInfo.aborted = 20;
			}
			return null;

		}

	}
}
