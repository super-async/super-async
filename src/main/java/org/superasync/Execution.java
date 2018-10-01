package org.superasync;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Execution<V> {

    private final CancellersHolder canceller;
    private final Future<V> future;

    Execution(CancellersHolder canceller, Future<V> future) {
        this.canceller = canceller;
        this.future = future;
    }

    public void cancel() {
        canceller.cancel();
    }

    V blockingGet() throws ExecutionException, InterruptedException {
        return future.get();
    }
}
