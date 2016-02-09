In order to run our program, run Test.java in the src/CarlSTM folder. We have not yet implemented the execute function so the Test.java acts as a transaction.

You should see a series of print statements that shows how our program commits a transaction. When we have a thread that notices different values in a pair it aborts, hence the abort output. When the thread doesn't have a pair that has different values in its TxInfo it commits. At the end we print the final values of the two objects that we set indicating that has aborted since the two values were different.

We put into place locks so that we can use multiple threads.


