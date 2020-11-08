package zame.game.core.serializer;

import java.lang.reflect.Array;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DataList<T extends DataListItem> {
    private T firstPtr;
    private T lastPtr;
    private T freePtr;
    private int itemsCount;

    public T[] buffer;

    @SuppressWarnings("unchecked")
    public DataList(Class<T> theClass, int capacity) {
        buffer = (T[])Array.newInstance(theClass, capacity);

        for (int i = 0; i < capacity; i++) {
            try {
                buffer[i] = DataUtils.instantiate(theClass);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        buffer[0].prev = null;
        buffer[0].next = (capacity > 1 ? buffer[1] : null);
        int lastIdx = capacity - 1;

        for (int i = 1; i < lastIdx; i++) {
            buffer[i].prev = buffer[i - 1];
            buffer[i].next = buffer[i + 1];
        }

        if (lastIdx > 0) {
            buffer[lastIdx].prev = buffer[lastIdx - 1];
            buffer[lastIdx].next = null;
        }

        firstPtr = null;
        lastPtr = null;
        freePtr = buffer[0];
        itemsCount = 0;
    }

    public T first() {
        return firstPtr;
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public T last() {
        return lastPtr;
    }

    public int count() {
        return itemsCount;
    }

    public boolean canTake() {
        return (freePtr != null);
    }

    @SuppressWarnings("unchecked")
    public T take() {
        if (freePtr == null) {
            return null;
        }

        T item = freePtr;
        freePtr = (T)freePtr.next;

        if (freePtr != null) {
            freePtr.prev = null;
        }

        item.prev = lastPtr;
        item.next = null;

        if (lastPtr != null) {
            lastPtr.next = item;
        }

        lastPtr = item;

        if (firstPtr == null) {
            firstPtr = item;
        }

        itemsCount++;
        return item;
    }

    @SuppressWarnings("unchecked")
    public void release(T item) {
        if (item.prev != null) {
            item.prev.next = item.next;
        } else {
            firstPtr = (T)item.next;
        }

        if (item.next != null) {
            item.next.prev = item.prev;
        } else {
            lastPtr = (T)item.prev;
        }

        item.prev = null;
        item.next = freePtr;

        if (freePtr != null) {
            freePtr.prev = item;
        }

        freePtr = item;
        itemsCount--;
    }

    public void clear() {
        if (lastPtr == null) {
            return;
        }

        lastPtr.next = freePtr;

        if (freePtr != null) {
            freePtr.prev = lastPtr;
        }

        freePtr = firstPtr;
        firstPtr = null;
        lastPtr = null;
        itemsCount = 0;
    }
}
