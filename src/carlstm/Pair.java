package carlstm;

/**
 * Created by simonorlovsky on 1/28/16.
 *
 * This class represents a Pair of objects with potentially different object types
 */
public class Pair<T, Q> {
    private TxObject<T> oldObject;
    private TxObject<Q> newObject;

    // Constructor to instantiate a new pair
    public Pair(TxObject<T>oldObject, TxObject<Q>newObject) {
        this.oldObject = oldObject;
        this.newObject = newObject;
    }

    // Changes the newObject to the parameter
    public void setNewObject(TxObject<Q> newObject) {
        this.newObject = newObject;
    }

    // Returns the newObject
    public TxObject<Q> getNewObject() {
        return newObject;
    }

    // Returns the oldObject
    public TxObject<T> getOldObject() {
        return oldObject;
    }
}
