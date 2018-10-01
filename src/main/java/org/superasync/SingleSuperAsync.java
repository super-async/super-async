package org.superasync;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

class SingleSuperAsync<V> extends SuperAsync<V> {

    private final Callable<V> task;

    SingleSuperAsync(Executor executor, Callable<V> task) {
        super(executor);
        this.task = task;
    }

    @Override
    public void execute(BaseObserver<V> baseObserver, CancellersHolder cancellersHolder) {
        Future<V> future = submit(task, baseObserver);
        cancellersHolder.add(future);
    }
}
