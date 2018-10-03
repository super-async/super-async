package org.superasync;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Execution<V> {

    private final Canceller canceller;
    private final Future<V> future;

    Execution(Canceller canceller, Future<V> future) {
        this.canceller = canceller;
        this.future = future;
    }

    public boolean cancel() {
        return canceller.cancel(false);
    }

    V blockingGet() throws ExecutionException, InterruptedException {
        return future.get();
    }
}
