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
    public void execute(Observer<V> observer, Canceller canceller) {
        Task cancellable = submit(task, observer);
        canceller.add(cancellable);
    }
}
