package org.superasync;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

class CancellersHolder {

    private final AtomicBoolean isCancelled = new AtomicBoolean(false);

    private final CopyOnWriteArrayList<Future> cancellers = new CopyOnWriteArrayList<Future>();

    void add(Future canceller) {
        if (isCancelled.get()) {
            canceller.cancel(false);
            return;
        }

        for (Future c : cancellers) {
            if (c.isDone()) {
                cancellers.remove(c);
            }
        }

        if (canceller.isDone()) {
            return;
        }

        cancellers.add(canceller);
        if (isCancelled.get()) {
            canceller.cancel(false);
        }
    }

    void cancel() {
        if (isCancelled.compareAndSet(false, true)) {
            for (Future c : cancellers) {
                c.cancel(false);
            }
        }
    }
}
