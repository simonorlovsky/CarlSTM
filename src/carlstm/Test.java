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
        public TxInfo info = new TxInfo();
    }

    // Thread local variable containing each thread's ID
    private static final MyThreadLocal<Integer> threadId =
            new MyThreadLocal<Integer>();

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread() {
            public void run() {
                threadId.set(1);
                Pair<String, String> pairs[] = threadId.info.getPairs();
                TxObject<String> oldObject = new TxObject<String>("Hello");
            }
        };
        Thread t2 = new Thread() {
            public void run() {
                threadId.set(2);
                Pair<String, String> pairs[] = threadId.info.getPairs();
                TxObject<String> oldObject = new TxObject<String>("Hello");
            }
        };
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(threadId + " " + threadId.get()
                + " " + threadId.info.getPairs());
    }

}
