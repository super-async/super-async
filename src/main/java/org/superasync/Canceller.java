package org.superasync;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

class Canceller {

    private final AtomicBoolean isCancelled = new AtomicBoolean(false);

    private final CopyOnWriteArrayList<CompletableCancellable> list = new CopyOnWriteArrayList<CompletableCancellable>() ;

    void add(CompletableCancellable cancellable) {
        if (isCancelled.get()) {
            cancellable.cancel();
            return;
        }

        for (CompletableCancellable c : list) {
            if (c.isDone()) {
                list.remove(c);
            }
        }

        if (cancellable.isDone()) {
            return;
        }

        list.add(cancellable);
        if (isCancelled.get()) {
            cancellable.cancel();
        }
    }

    void cancel() {
        if (isCancelled.compareAndSet(false, true)) {
            for (Cancellable c : list) {
                c.cancel();
            }
        }
    }
}
