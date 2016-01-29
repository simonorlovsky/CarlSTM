package carlstm;

/**
 * Created by simonorlovsky on 1/28/16.
 */
public class Pair<T, Q> {
    private TxObject<T> oldObject;
    private TxObject<Q> newObject;

    public Pair(TxObject<T>oldObject, TxObject<Q>newObject) {
        this.oldObject = oldObject;
        this.newObject = newObject;
    }

    public void setNewObject(TxObject<Q> newObject) {
        this.newObject = newObject;
    }

    public TxObject<Q> getNewObject() {
        return newObject;
    }

    public TxObject<T> getOldObject() {
        return oldObject;
    }
}
