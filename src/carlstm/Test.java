//package carlstm;
//
///**
// * Created by simonorlovsky on 1/28/16.
// */
//
//public class Test {
//
//    private static class MyThreadLocal<T> extends ThreadLocal<T> {
//        public TxInfo info;
//        public TxInfo info2;
//    }
//
//    // Thread local variable containing each thread's ID
//    private static final MyThreadLocal<Integer> threadId =
//            new MyThreadLocal<Integer>();
//
//    public static void main(String[] args) throws InterruptedException,
//            TransactionAbortedException, NoActiveTransactionException {
//        threadId.info = new TxInfo();
//        Thread t1 = new Thread() {
//            @Override
//            public void run() {
//                try {
//                    threadId.set(1);
//                    Pair<String, String> pairs[] = threadId.info.getPairs();
//                    TxObject<String> object = new TxObject<String>("Hello");
//                    threadId.info.start(); // Should print commit
//                    object.write("GOOD");
//                    threadId.info.start(); // Should print abort because of change
//                }
//                catch(NoActiveTransactionException e) {
//                    System.out.println("No Active Transaction... aborting.");
//                }catch(TransactionAbortedException e) {
//                    System.out.println("Transaction aborted... aborting.");
//                }
//
//            }
//        };
//        threadId.info2 = new TxInfo();
//        Thread t2 = new Thread() {
//            @Override
//            public void run() {
//                try {
//                    //TxInfo info = new TxInfo();
//                    threadId.set(2);
//                    Pair<Object, Object> pairs[] = threadId.info2.getPairs();
//                    TxObject<Object> object = new TxObject<Object>("World");
//                    //threadId.info.start();
//                    object.write(10);
//                    threadId.info2.start(); // Should print abort because of change
//                }
//                catch(NoActiveTransactionException e) {
//                    System.out.println("No Active Transaction... aborting.");
//                }catch(TransactionAbortedException e) {
//                    System.out.println("Transaction aborted... aborting.");
//                }
//            }
//        };
//        t1.start();
//        t2.start();
//        t1.join();
//        t2.join();
//
//        Pair[] pairs = threadId.info.getPairs();
//
//        // Check for correctness by printing the object pairs
//        for(int i = 0;i<pairs.length;i++) {
//            if (pairs[i] != null) {
//                System.out.println(pairs[i].getOldObject().value+","+pairs[i].getNewObject().value);
//            } else {
//                break;
//            }
//
//        }
//    }
//
//}
