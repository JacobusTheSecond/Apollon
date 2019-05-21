package apollon.util;

import org.jetbrains.annotations.NotNull;

public abstract class Pointer<T extends Pointer<T>> implements Comparable<T> {
    private T next = null;

    private int index;

    protected Pointer(int index) {
        this.index = index;
    }

    @NotNull
    public T getTarget() {
        T pointer = getThis();
        while (pointer.hasNext()) {
            pointer = pointer.next();
        }
        return pointer;
    }

    public int getIndex() {
        return getTarget().index();
    }

    public void set(@NotNull T target) {
        target = target.getTarget();
        if (getIndex() == target.getIndex()) {
            return;
        }
        T pointer = getThis();
        T next;
        while (pointer.hasNext()) {
            next = pointer.next();
            pointer.setNext(target);
            pointer = next;
        }
        pointer.setNext(target);
    }

    public int index() {
        return index;
    }

    protected boolean hasNext() {
        return next != null;
    }

    @NotNull
    protected T next() {
        return next;
    }

    protected void setNext(@NotNull T next) {
        this.next = next;
    }

    @Override
    public int compareTo(@NotNull T o) {
        return Integer.compare(getIndex(), o.getIndex());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Pointer<?> && getIndex() == ((Pointer) obj).getIndex();
    }

    @Override
    public int hashCode() {
        return getIndex();
    }

    @Override
    public String toString() {
        return name() + ": " + index + (hasNext() ? " (" + getIndex() + ")" : "");
    }

    @NotNull
    protected abstract String name();

    @NotNull
    protected abstract T getThis();
}
