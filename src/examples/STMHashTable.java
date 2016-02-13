package examples;

import carlstm.CarlSTM;
import carlstm.Transaction;
import carlstm.TxInfo;
import carlstm.TxObject;
import carlstm.TransactionSTM.MyTransaction;

import java.util.Objects;
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
            int size = Integer.parseInt(args[1]);
            int numThreads = Integer.parseInt(args[2]);

            int NUM_ITEMS = 10000;
            // Make STMHashSet
            if(args[0].equals("transaction")) {
                STMHashSet<TxObject> set = new STMHashSet<>(size);
                HashThread[] threadList = new HashThread[numThreads];
                long startTime = System.nanoTime();

                for(int i = 0;i<threadList.length;i++) {
                    threadList[i] = new HashThread(set,(NUM_ITEMS/numThreads));
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
            else if(args[0].equals("coarse")) {
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

        public static STMHashSet<TxObject> hashSet;

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
        private STMHashSet<TxObject> set;
        private int numItems;

        public HashThread(STMHashSet<TxObject> set, int numItems) {
            this.set = set;
            this.numItems = numItems;
        }
        // MAIN RUN METHOD
        @Override
        public void run() {
            for(int i = 0;i<numItems;i++) {
                int num = i;
                CarlSTM.execute(new MyTransaction() {
                    @Override
                    public Integer run() {
                        TxObject<Integer> five = new TxObject<Integer>(num);
                        boolean added = set.add(five);
                        return 0;
                    }
                });
            }
        }
    }

//    static class AddTransaction implements Transaction<Boolean> {
//
//        private TxObject<Object> item;
//        private STMHashSet<TxObject> set;
//
//
//        public AddTransaction(TxObject<Object> t,STMHashSet<TxObject> set) {
//            this.item = t;
//            this.set = set;
//        }
//
//        @Override
//        public Boolean run() {
//            // Java returns a negative number for the hash; this is just converting
//            // the negative number to a location in the array.
//            int hash = (item.hashCode() % CAPACITY + CAPACITY) % CAPACITY;
//            Bucket bucket = set.table[hash];
//            if (CarlSTM.execute(new ContainsThread(bucket, item) ){
//                return false;
//            }
//            if(set.table[hash]==null){
//                size++;
//            }
//            set.table[hash] = new Bucket(item, bucket);
//            return true;
//        }
//    }

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
        public Bucket[] table;

        private int size;

        /**
         * Capacity of the array. Since we do not support resizing, this is a
         * constant.
         */
        private static int capacity;

        /**
         * Create a new HashSet.
         */
        public STMHashSet(int size) {
            this.table = new Bucket[capacity];
            this.capacity = size;
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
            try{
                int hash = (item.hashCode() % capacity + capacity) % capacity;
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
            catch(ArrayIndexOutOfBoundsException e) {
                // Don't add
                return false;
            }
        }

        /*
         * (non-Javadoc)
         * @see examples.Set#contains(java.lang.Object)
         */
        @Override
        public boolean contains(TxObject item) {
            int hash = (item.hashCode() % capacity + capacity) % capacity;
            Bucket bucket = table[hash];
            return contains(bucket, item);
        }

        public int size() {
            return size;
        }
    }
}
