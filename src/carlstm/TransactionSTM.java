package carlstm;

import carlstm.CarlSTM;
import carlstm.NoActiveTransactionException;
import carlstm.Transaction;
import carlstm.TransactionAbortedException;
import carlstm.TxObject;

/**
 * Created by simonorlovsky on 2/8/16.
 */
public class TransactionSTM {

    private static TxObject<Integer> x = new TxObject<Integer>(0);


    public static class MyThreadLocal<T> extends ThreadLocal<T> {
        public static TxInfo info = new TxInfo();

        public static void setInfo(TxInfo info) {
            MyThreadLocal.info = info;
        }

        public static TxInfo getInfo() {
            return info;
        }
    }

    private static final MyThreadLocal<TxInfo> threadId =
            new MyThreadLocal<TxInfo>();

    /**
     * A transaction that repeatedly increments the integer value stored in a
     * TxObject.
     */
    static class MyTransaction implements Transaction<Integer>{
        /*
         * (non-Javadoc)
         *
         * @see carlstm.Transaction#run()
         */

        @Override
        public Integer run() throws NoActiveTransactionException,
                TransactionAbortedException {
            try {

                // This print may happen more than once if the transaction aborts
                // and restarts.

                // This loop repeatedly reads and writes a TxObject. The read and
                // write operations should all behave as if the entire transaction
                // happened exactly once, and as if there were no
                // intervening reads or writes from other threads.
                TxInfo info = threadId.getInfo();
                for (int i = 0; i < 5; i++) {
                    //Read the new value
                    Integer val = x.read();
                    for(Pair<Object,Object> pair: info.getPairs()) {
                        if(pair.getOldObject().value.equals(x.read())){
                            val = (Integer) pair.getNewObject().value;
                        }
                    }

                    x.write(val + 1);
                    Thread.yield();
                }
                for(Pair<Object,Object> pair: info.getPairs()) {
                    if(pair.getOldObject().value.equals(x.read())){
                        return (Integer) pair.getNewObject().value;
                    }
                }
                return x.read();
            }
            catch (NoActiveTransactionException e){
//                MyThreadLocal.getInfo().abort();
                return run();
            }
            catch (TransactionAbortedException e){
                return run();
            }
        }
    }

    /**
     * A Java Thread that executes a transaction and prints its result.
     */
    static class MyThread extends Thread {
        private static final MyThreadLocal<TxInfo> threadId =
                new MyThreadLocal<TxInfo>();

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            int result = CarlSTM.execute(new MyTransaction());
            System.out.println(Thread.currentThread().getName() + ": " + result);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Create two threads
        Thread thread0 = new MyThread();
        Thread thread1 = new MyThread();

        // Start the threads (executes MyThread.run)
        thread0.start();
        thread1.start();

        // Wait for the threads to finish.
        thread0.join();
        thread1.join();
    }
}
