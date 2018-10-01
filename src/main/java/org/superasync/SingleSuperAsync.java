package org.superasync;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

class SingleSuperAsync<V> extends SuperAsync<V> {

    private final Callable<V> task;

    SingleSuperAsync(Executor executor, Callable<V> task) {
        super(executor);
        this.task = task;
    }

    @Override
    public void execute(BaseObserver<V> baseObserver, Canceller canceller) {
        CancellableTask cancellable = submit(task, baseObserver);
        canceller.add(cancellable);
    }
}
