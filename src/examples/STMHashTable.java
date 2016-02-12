package examples;

import carlstm.CarlSTM;
import carlstm.Transaction;
import carlstm.TxInfo;
import carlstm.TxObject;
import java.util.Random;

/**
 * Created by Cody on 2/11/16.
 */
public class STMHashTable {

    private static final MyHashThreadLocal<TxInfo> threadId =
            new MyHashThreadLocal<TxInfo>();

    public static void main(String[] args) {

        if(args.length != 3) {
            System.out.println("Usage: java STMHashTable <transaction|coarse|fine> <20|1000> <number of threads>");
        }
        else {
            int NUM_ITEMS = 10000;
            // Make STMHashSet
            if(args[0].equals("transaction")) {
                int size = Integer.parseInt(args[1]);
                int numThreads = Integer.parseInt(args[2]);
                STMHashSet<TxObject> set = new STMHashSet<TxObject>();


                TxObject<Integer> five = new TxObject<Integer>(5);
                set.add(five);
                set.add(new TxObject<Integer>(100));
                set.add(new TxObject<String>("Hello!"));


                for(int i = 0;i<size;i++) {
                    TxObject<Integer> num = new TxObject<Integer>(i);
                    System.out.println(num.hashCode());
                    set.add(num);
                }
            }
            else if(args[0].equals("coarse")) {
                int size = Integer.parseInt(args[1]);
                int numThreads = Integer.parseInt(args[2]);

                CoarseHashSet<TxObject> set = new CoarseHashSet<TxObject>(size);
                CoarseThread[] threadList = new CoarseThread[numThreads];
                long startTime = System.nanoTime();

                for(int i = 0;i<threadList.length;i++) {
                    threadList[i] = new CoarseThread(set,(NUM_ITEMS/numThreads));
                    threadList[i].start();
                }
                for(int i = 0;i<threadList.length;i++) {
                    try {
                        threadList[i].join();
                    }
                    catch (InterruptedException e) {
                        System.out.println("Thread interrupted..");
                    }
                }
                long endTime = System.nanoTime();
                System.out.println("Took "+(endTime-startTime));
            }
            else if(args[0].equals("fine")) {
                int size = Integer.parseInt(args[1]);
                int numThreads = Integer.parseInt(args[2]);

                FineHashSet<TxObject> set = new FineHashSet<TxObject>(size);

                FineThread[] threadList = new FineThread[numThreads];
                long startTime = System.nanoTime();
                for(int i = 0;i<threadList.length;i++) {
                    threadList[i] = new FineThread(set,(NUM_ITEMS/numThreads));
                    threadList[i].start();
                }
                for(int i = 0;i<threadList.length;i++) {
                    try {
                        threadList[i].join();
                    }
                    catch (InterruptedException e) {
                        System.out.println("Thread interrupted..");
                    }
                }
                long endTime = System.nanoTime();
                System.out.println("Took "+(endTime-startTime));
            }
        }
    }

    public static class CoarseThread extends Thread {

        private CoarseHashSet<TxObject> coarseHashSet;
        private int numItems;

        public CoarseThread(CoarseHashSet coarseHashSet, int numItems) {
            this.coarseHashSet = coarseHashSet;
            this.numItems = numItems;
        }

        @Override
        public void run() {
            Random r = new Random();
            for(int i = 0;i<this.numItems;i++) {
                coarseHashSet.add(new TxObject(r.nextInt(10000)));
            }
        }
    }

    public static class FineThread extends Thread {

        private FineHashSet<TxObject> fineHashSet;
        private int numItems;

        public FineThread(FineHashSet fineHashSet, int numItems) {
            this.fineHashSet = fineHashSet;
            this.numItems = numItems;
        }

        @Override
        public void run() {
            Random r = new Random();
            for(int i = 0;i<numItems;i++) {
                fineHashSet.add(new TxObject(r.nextInt(10000)));
            }

        }
    }

    public static class MyHashThreadLocal<T> extends ThreadLocal<T> {
        public static TxInfo info = new TxInfo();

        public static String funcType;

        public static STMHashSet<TxObject> hashSet;

        public static void setFuncType(String s) {
            funcType = s;
        }

        public static void runFunction() {

        }

        public static void setInfo(TxInfo info) {
            MyHashThreadLocal.info = info;
        }

        public static TxInfo getInfo() {
            return info;
        }

        public static void setHashSet(STMHashSet set) {
            hashSet = set;
        }

        public static STMHashSet getHashSet() {
            return hashSet;
        }
    }

    static class HashThread extends Thread {
        private static final MyHashThreadLocal<TxInfo> threadId =
                new MyHashThreadLocal<TxInfo>();

        // MAIN RUN METHOD
        @Override
        public void run() {
            boolean result = CarlSTM.execute(new AddTransaction());
        }
    }

    static class AddTransaction implements Transaction<Boolean> {
        @Override
        public Boolean run() {
            return true;
        }
    }

    static class STMHashSet<TxObject> implements Set<TxObject> {
        /**
         * Helper class - basically is a linked list of items that happen to map to
         * the same hash code.
         */
        private static class Bucket {

            Object item;

            /**
             * Next item in the list.
             */
            Bucket next;

            /**
             * Create a new bucket.
             *
             * @param item item to be stored
             * @param next next item in the list
             */
            public Bucket(Object item, Bucket next) {
                synchronized (this) {
                    this.item = item;
                    this.next = next;
                }
            }
        }

        /**
         * Our array of items. Each location in the array stores a linked list items
         * that hash to that locations.
         */
        private Bucket[] table;

        private int size;

        /**
         * Capacity of the array. Since we do not support resizing, this is a
         * constant.
         */
        private static final int CAPACITY = 1024;

        /**
         * Create a new HashSet.
         */
        public STMHashSet() {
            this.table = new Bucket[CAPACITY];
        }

        /**
         * A helper method to see if an item is stored at a given bucket.
         *
         * @param bucket bucket to be searched
         * @param item   item to be searched for
         * @return true if the item is in the bucket
         */
        private boolean contains(Bucket bucket, TxObject item) {
            while (bucket != null) {
                if (item.equals(bucket.item)) {
                    return true;
                }
                bucket = bucket.next;
            }
            return false;
        }

        /*
         * (non-Javadoc)
         * @see examples.Set#add(java.lang.Object)
         */
        @Override
        public boolean add(TxObject item) {
            // Java returns a negative number for the hash; this is just converting
            // the negative number to a location in the array.
            int hash = (item.hashCode() % CAPACITY + CAPACITY) % CAPACITY;
            Bucket bucket = table[hash];
            if (contains(bucket, item)) {
                return false;
            }
            if(table[hash]==null){
                size++;
            }
            table[hash] = new Bucket(item, bucket);
            return true;
        }

        /*
         * (non-Javadoc)
         * @see examples.Set#contains(java.lang.Object)
         */
        @Override
        public boolean contains(TxObject item) {
            int hash = (item.hashCode() % CAPACITY + CAPACITY) % CAPACITY;
            Bucket bucket = table[hash];
            return contains(bucket, item);
        }

        public int size() {
            return size;
        }
    }
}
