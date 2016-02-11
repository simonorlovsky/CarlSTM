package examples;

import carlstm.CarlSTM;
import carlstm.Transaction;
import carlstm.TxInfo;
import carlstm.TxObject;

/**
 * Created by Cody on 2/11/16.
 */
public class STMHashTable {
    public static void main(String[] args) {
        STMHashSet<TxObject> set = new STMHashSet<TxObject>();
        TxObject<Integer> five = new TxObject<Integer>(5);
        set.add(five);
        set.add(new TxObject<Integer>(100));
        set.add(new TxObject<String>("Hello!"));

        for(int i = 0;i<1024;i++) {
            TxObject<Integer> num = new TxObject<Integer>(i);
            System.out.println(num.hashCode());
            set.add(num);
        }

        System.out.println(set.size());
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
    }

    static class HashThread extends Thread {
        private static final MyHashThreadLocal<TxInfo> threadId =
                new MyHashThreadLocal<TxInfo>();

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
            /**
             * The item stored at this entry. This is morally of type T, but Java
             * generics do not play well with arrays, so we have to use Object
             * instead.
             */
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
