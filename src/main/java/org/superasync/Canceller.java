package org.superasync;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

class Canceller implements Cancellable {

    @SuppressWarnings("FieldCanBeLocal")
    private final int INITIAL = 0, CANCELLED = 1, INTERRUPTED = 2;

    private final AtomicInteger state = new AtomicInteger(0);

    private final Collection<Completable.Cancellable> collection = new ConcurrentLinkedQueue<Completable.Cancellable>();

    void add(Completable.Cancellable cancellable) {
        int s = state.get();
        if (s != INITIAL) {
            cancellable.cancel(s == INTERRUPTED);
            return;
        }

        Iterator<Completable.Cancellable> it = collection.iterator();
        while (it.hasNext()) {
            Completable.Cancellable c = it.next();
            if (c.isDone()) {
                it.remove();
            }
        }

        if (cancellable.isDone()) {
            return;
        }

        collection.add(cancellable);

        s = state.get();
        if (s != INITIAL) {
            cancellable.cancel(s == INTERRUPTED);
        }
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        if (state.compareAndSet(INITIAL, mayInterruptIfRunning ? INTERRUPTED : CANCELLED)) {
            for (Completable.Cancellable c : collection) {
                c.cancel(mayInterruptIfRunning);
            }
            return true;
        }
        return false;
    }
}
