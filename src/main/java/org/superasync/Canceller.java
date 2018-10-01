package org.superasync;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

class Canceller {

    private final AtomicBoolean isCancelled = new AtomicBoolean(false);

    private final CopyOnWriteArrayList<CancellableTask> cancellableTasks = new CopyOnWriteArrayList<CancellableTask>();

    void add(CancellableTask cancellableTask) {
        if (isCancelled.get()) {
            cancellableTask.cancel();
            return;
        }

        for (CancellableTask c : cancellableTasks) {
            if (c.isDone()) {
                cancellableTasks.remove(c);
            }
        }

        if (cancellableTask.isDone()) {
            return;
        }

        cancellableTasks.add(cancellableTask);
        if (isCancelled.get()) {
            cancellableTask.cancel();
        }
    }

    void cancel() {
        if (isCancelled.compareAndSet(false, true)) {
            for (CancellableTask c : cancellableTasks) {
                c.cancel();
            }
        }
    }
}
