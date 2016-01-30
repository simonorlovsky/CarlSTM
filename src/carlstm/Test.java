package carlstm;

/**
 * Created by simonorlovsky on 1/28/16.
 */
//public class Test {
//    public static void main(String[] arg){
////        TxObject<String> oldObject = new TxObject<String>("Hello");
////        TxObject<String> newObject = new TxObject<String>("World");
////        Pair<String, String> pair = new Pair<String, String>(oldObject, newObject);
////        System.out.println(pair.getNewObject().value);
//
//        TxInfo info = new TxInfo();
//        info.start();
//    }
//}

public class Test {

    private static class MyThreadLocal<T> extends ThreadLocal<T> {
        public TxInfo info;
    }

    // Thread local variable containing each thread's ID
    private static final MyThreadLocal<Integer> threadId =
            new MyThreadLocal<Integer>();

    public static void main(String[] args) throws InterruptedException,
            TransactionAbortedException, NoActiveTransactionException {
        threadId.info = new TxInfo();
        Thread t1 = new Thread() {
            @Override
            public void run() {
                try {
                    //TxInfo info = new TxInfo();
                    threadId.set(1);
                    Pair<String, String> pairs[] = threadId.info.getPairs();
                    TxObject<String> object = new TxObject<String>("Hello");
                    object.write("GOOD");
                    threadId.info.start();
                }
                catch(NoActiveTransactionException e) {
                    System.out.println("No Active Transaction... aborting.");
                }catch(TransactionAbortedException e) {
                    System.out.println("Transaction aborted... aborting.");
                }

            }
        };
        Thread t2 = new Thread() {
            @Override
            public void run() {
                try {
                    //TxInfo info = new TxInfo();
                    threadId.set(2);
                    Pair<String, String> pairs[] = threadId.info.getPairs();
                    TxObject<String> object = new TxObject<String>("World");
                    threadId.info.start();
                    object.write("BAD");
                    threadId.info.start();
                }
                catch(NoActiveTransactionException e) {
                    System.out.println("No Active Transaction... aborting.");
                }catch(TransactionAbortedException e) {
                    System.out.println("Transaction aborted... aborting.");
                }
            }
        };
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        Pair[] pairs = threadId.info.getPairs();

        for(int i = 0;i<pairs.length;i++) {
            if (pairs[i] != null) {
                System.out.println(pairs[i].getOldObject().value);
            } else {
                break;
            }

        }
    }

}
