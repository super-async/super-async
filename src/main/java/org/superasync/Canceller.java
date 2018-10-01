package org.superasync;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

class Canceller {

    private final AtomicBoolean isCancelled = new AtomicBoolean(false);

    private final Collection<CompletableCancellable> collection = new ConcurrentLinkedQueue<CompletableCancellable>();

    void add(CompletableCancellable cancellable) {
        if (isCancelled.get()) {
            cancellable.cancel();
            return;
        }

        Iterator<CompletableCancellable> it = collection.iterator();
        while (it.hasNext()){
            CompletableCancellable c = it.next();
            if (c.isDone()) {
                it.remove();
            }
        }

        if (cancellable.isDone()) {
            return;
        }

        collection.add(cancellable);
        if (isCancelled.get()) {
            cancellable.cancel();
        }
    }

    void cancel() {
        if (isCancelled.compareAndSet(false, true)) {
            for (Cancellable c : collection) {
                c.cancel();
            }
        }
    }
}
