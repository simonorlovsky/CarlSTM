In order to run our program, run TransactionSTM.java in the src/CarlSTM folder. We create two threads that each execute a trasaction, defined in TransactionSTM.java as MyTransaction. 

You will be able to see the values change as the transaction executes. When the abort print statement it called this means the program rolls back changes to the TxInfo object and retries the tranaction. When the threads have completed you should be to see commit print statements followed by the respective threadID and the current value of the object at the time it was committed.

We implemented the exponential backoff that delays the transaction if the transaction fails to be committed.

See TxObject.java to see our implemention of read and write locks.
